(ns seqspert.hash-map
  (:import [java.lang.reflect Field]
           [clojure.lang
            PersistentHashMap
            PersistentHashMap$BitmapIndexedNode
            PersistentHashMap$HashCollisionNode
            PersistentHashMap$ArrayNode
            PersistentHashMap$INode]
           [clojure.lang
            ArrayNodeUtils
            BitmapIndexedNodeUtils
            Counts
            Seqspert]
           )
  (:require [clojure.core [reducers :as r]]
            [seqspert.core :refer :all]))

;;--------------------------------------------------------------------------------
;; hash-map internals

;; TODO - use java static fns defined in a Utils class
(let [^Field f (unlock-field PersistentHashMap "count")] (defn hash-map-count [v] (.get f v)))
(let [^Field f (unlock-field PersistentHashMap "root")]  (defn hash-map-root  [v] (.get f v)))

(defrecord HashMap [count root])
(defrecord BitmapIndexedNode [bitmap array])

(let [^Field f (unlock-field PersistentHashMap$BitmapIndexedNode "bitmap")]
  (defn- bitmap-indexed-node-bitmap [v] (.get f v)))

(let [^Field f (unlock-field PersistentHashMap$BitmapIndexedNode "array")]
  (defn- bitmap-indexed-node-array [v] (.get f v)))

(defmethod inspect PersistentHashMap$BitmapIndexedNode [^PersistentHashMap$BitmapIndexedNode n]
  (BitmapIndexedNode. (Integer/toBinaryString (bitmap-indexed-node-bitmap n)) (inspect (bitmap-indexed-node-array n))))

(defmethod inspect PersistentHashMap [^PersistentHashMap m]
  (HashMap. (hash-map-count m) (inspect (hash-map-root m))))

(defrecord ArrayNode [count array])

(let [^Field f (unlock-field PersistentHashMap$ArrayNode "count")]
  (defn- array-node-count [v] (.get f v)))

(let [^Field f (unlock-field PersistentHashMap$ArrayNode "array")]
  (defn- array-node-array [v] (.get f v)))

(defmethod inspect PersistentHashMap$ArrayNode [^PersistentHashMap$ArrayNode n]
  (ArrayNode. (array-node-count n) (inspect (array-node-array n))))

(defrecord HashCollisionNode [hash count array])

(let [^Field f (unlock-field PersistentHashMap$HashCollisionNode "hash")]
  (defn- hash-collision-node-hash [v] (.get f v)))

(let [^Field f (unlock-field PersistentHashMap$HashCollisionNode "count")]
  (defn- hash-collision-node-count [v] (.get f v)))

(let [^Field f (unlock-field PersistentHashMap$HashCollisionNode "array")]
  (defn hash-collision-node-array [v] (.get f v)))

(defmethod inspect PersistentHashMap$HashCollisionNode [^PersistentHashMap$HashCollisionNode n]
  (HashCollisionNode. (hash-collision-node-hash n)
                      (hash-collision-node-count n)
                      (inspect (hash-collision-node-array n))))

;;------------------------------------------------------------------------------
;; sequential splicing

(defn sequential-splice-hash-maps
  "merge two hash-maps resulting in a third equivalent to the first
  with every pair from the second conj-ed into it."
  [l r]
  (Seqspert/spliceHashMaps l r))

;; what do we want to do with this now ?
(defn into-hash-map
  "parallel fold a sequence of pairs into a hash-map"
  ([values]
     (r/fold (r/monoid sequential-splice-hash-maps hash-map) conj values))
  ([parallelism values]
     (r/fold parallelism (r/monoid sequential-splice-hash-maps hash-map) conj values)))

;;------------------------------------------------------------------------------
;; parallel splicing

(defmulti ^{:private true} get-children type)

(defmethod get-children PersistentHashMap$BitmapIndexedNode [^PersistentHashMap$BitmapIndexedNode node]
  (loop [result (transient [])
         i 32
         bitmap (bitmap-indexed-node-bitmap node)
         kvps (seq (bitmap-indexed-node-array node))]
    (if (= (bit-and bitmap 1) 1)
      (recur (conj! result (take 2 kvps)) (dec i) (bit-shift-right bitmap 1) (drop 2 kvps))
      (if (zero? i)
        (persistent! result)
        (recur (conj! result nil) (dec i) (bit-shift-right bitmap 1) kvps)))))

(defmethod get-children PersistentHashMap$ArrayNode [^PersistentHashMap$ArrayNode node]
  (map (fn [n] (if n [nil n])) (array-node-array node)))

(defn- into-bitmap [s]
  (reduce (fn [result e] (bit-or (bit-shift-left result 1) (if e 1 0))) 0 (reverse s)))

(defn- into-array-node [kvns]
  (ArrayNodeUtils/makeArrayNode2
   (- 32 (count (filter nil? kvns)))
   (into-array PersistentHashMap$INode (map second kvns))))

(defn- into-bitmap-indexed-node [kvns]
  (BitmapIndexedNodeUtils/makeBitmapIndexedNode
   (into-bitmap kvns)
   (into-array Object (mapcat (fn [s] (take 2 s)) (filter some? kvns)))))

(defn- third [s] (nth s 2))

;; TODO - consider use of transducers in this fn - can we save on intermedite reps ?
(defn- parallel-splice-branch-nodes
  [^PersistentHashMap$INode left ^PersistentHashMap$INode right ^Counts counts]
  (let [left-children (get-children left)
        right-children (get-children right)
        promote-p (> (count (filter true? (map (fn [l r] (or l r)) left-children right-children))) 16)
        promote (if promote-p (fn [k v same-key] [nil (ArrayNodeUtils/promote 0 k v) same-key]) (fn [& args] args)) ;assume shift of 0
        maybe-promote (fn [k v] (if k (promote k v 0) [k v 0]))
        children (pmap
                  (fn [[lk lv :as l] [rk rv :as r]]
                    (if l
                      (if r
                        (let [^Counts c (Counts.)
                              spliced (Seqspert/splice 0 c false 0 lk lv false 0 rk rv)
                              same-key (.sameKey c)] ;does let preserve ordering ?
                          (if spliced
                            [nil spliced same-key]
                            (promote lk rv same-key)))
                        (maybe-promote lk lv))
                      (if r
                        (maybe-promote rk rv)
                        nil)))
                  left-children
                  right-children)]
    (set! (.sameKey counts) (reduce + 0 (map third (filter some? children))))
    ((if promote-p into-array-node into-bitmap-indexed-node) children)))

(defmulti ^{:private true} parallel-splice-nodes (fn [l r c] [(type l)(type r)]))

(defmethod parallel-splice-nodes [PersistentHashMap$ArrayNode PersistentHashMap$ArrayNode]                 [l r c] (parallel-splice-branch-nodes l r c))
(defmethod parallel-splice-nodes [PersistentHashMap$ArrayNode PersistentHashMap$BitmapIndexedNode]         [l r c] (parallel-splice-branch-nodes l r c))
(defmethod parallel-splice-nodes [PersistentHashMap$ArrayNode PersistentHashMap$HashCollisionNode]         [l r c] (Seqspert/splice 0 c false 0 nil l false 0 nil r))

(defmethod parallel-splice-nodes [PersistentHashMap$BitmapIndexedNode PersistentHashMap$ArrayNode]         [l r c] (parallel-splice-branch-nodes l r c))
(defmethod parallel-splice-nodes [PersistentHashMap$BitmapIndexedNode PersistentHashMap$BitmapIndexedNode] [l r c] (parallel-splice-branch-nodes l r c))
(defmethod parallel-splice-nodes [PersistentHashMap$BitmapIndexedNode PersistentHashMap$HashCollisionNode] [l r c] (Seqspert/splice 0 c false 0 nil l false 0 nil r))

(defmethod parallel-splice-nodes [PersistentHashMap$HashCollisionNode PersistentHashMap$ArrayNode]         [l r c] (Seqspert/splice 0 c false 0 nil l false 0 nil r))
(defmethod parallel-splice-nodes [PersistentHashMap$HashCollisionNode PersistentHashMap$BitmapIndexedNode] [l r c] (Seqspert/splice 0 c false 0 nil l false 0 nil r))
(defmethod parallel-splice-nodes [PersistentHashMap$HashCollisionNode PersistentHashMap$HashCollisionNode] [l r c] (Seqspert/splice 0 c false 0 nil l false 0 nil r))

;;------------------------------------------------------------------------------

(defn parallel-splice-hash-maps [^PersistentHashMap left ^PersistentHashMap right]
  (let [counts (Counts.)                ;consider left/right resolvers
        root (parallel-splice-nodes (hash-map-root left)(hash-map-root right) counts)] ;counts changed as side-effect
    (Seqspert/makeHashMap2 (- (+ (hash-map-count left) (hash-map-count right)) (.sameKey counts)) root)))

;;------------------------------------------------------------------------------
