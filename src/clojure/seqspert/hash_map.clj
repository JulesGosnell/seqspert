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
            Counts
            HashCodeKey
            Seqspert]
           )
  (:require [clojure.core [reducers :as r]]
            [seqspert.core :refer :all]))

;;--------------------------------------------------------------------------------

;; hash-map internals

(let [^Field f (unlock-field PersistentHashMap "count")] (defn hash-map-count [v] (.get f v)))
(let [^Field f (unlock-field PersistentHashMap "root")]  (defn hash-map-root  [v] (.get f v)))

(defrecord HashMap [count root])
(defrecord BitmapIndexedNode [bitmap array])

(let [^Field f (unlock-field PersistentHashMap$BitmapIndexedNode "bitmap")]
  (defn bitmap-indexed-node-bitmap [v] (.get f v)))

(let [^Field f (unlock-field PersistentHashMap$BitmapIndexedNode "array")]
  (defn bitmap-indexed-node-array [v] (.get f v)))

(defmethod inspect PersistentHashMap$BitmapIndexedNode [^PersistentHashMap$BitmapIndexedNode n]
  (BitmapIndexedNode. (Integer/toBinaryString (bitmap-indexed-node-bitmap n)) (inspect (bitmap-indexed-node-array n))))

(defmethod inspect PersistentHashMap [^PersistentHashMap m]
  (HashMap. (hash-map-count m) (inspect (hash-map-root m))))

(defrecord ArrayNode [count array])

(let [^Field f (unlock-field PersistentHashMap$ArrayNode "count")]
  (defn array-node-count [v] (.get f v)))

(let [^Field f (unlock-field PersistentHashMap$ArrayNode "array")]
  (defn array-node-array [v] (.get f v)))

(defmethod inspect PersistentHashMap$ArrayNode [^PersistentHashMap$ArrayNode n]
  (ArrayNode. (array-node-count n) (inspect (array-node-array n))))

(inspect (apply hash-map (range 100)))

(defrecord HashCollisionNode [hash count array])

(let [^Field f (unlock-field PersistentHashMap$HashCollisionNode "hash")]
  (defn hash-collision-node-hash [v] (.get f v)))

(let [^Field f (unlock-field PersistentHashMap$HashCollisionNode "count")]
  (defn hash-collision-node-count [v] (.get f v)))

(let [^Field f (unlock-field PersistentHashMap$HashCollisionNode "array")]
  (defn hash-collision-node-array [v] (.get f v)))

(defmethod inspect PersistentHashMap$HashCollisionNode [^PersistentHashMap$HashCollisionNode n]
  (HashCollisionNode. (hash-collision-node-hash n)
                      (hash-collision-node-count n)
                      (inspect (hash-collision-node-array n))))

;;------------------------------------------------------------------------------

(defn splice-hash-maps
  "merge two hash-maps resulting in a third equivalent to the first
  with ever pair from the second conj-ed into it."
  [l r]
  (Seqspert/spliceHashMaps l r))

(defn into-hash-map
  "parallel fold a sequence of pairs into a hash-map"
  ([values]
     (r/fold (r/monoid splice-hash-maps hash-map) conj values))
  ([parallelism values]
     (r/fold parallelism (r/monoid splice-hash-maps hash-map) conj values)))

;;------------------------------------------------------------------------------

(defmulti splice-nodes (fn [l r c] [(type l)(type r)]))

(defmethod splice-nodes [PersistentHashMap$BitmapIndexedNode PersistentHashMap$BitmapIndexedNode]
  [^PersistentHashMap$BitmapIndexedNode left ^PersistentHashMap$BitmapIndexedNode right count]
  (println "splicing: " left " : " right))

(def range32 (into [] (range 32)))

;; can I face writing 9 of these for all combs of AN, BIN and HCN or is there a better way ?  
;; maybe they should be written in java and share code with sequential splicers ?
;; lets assume that there is no point in parallelising HCN splicing - that leaves 4
;; the BIN ones need to consider promotion as well - lots of duplicate code :-(
;; bit and int manipulation would probably be faster in java
;; but how do we integrate java and clojure's thread pooling - investigate futures / fork-join pool
;; we could just use a multimethod to pick the top-level parallel splicer and then drop straight into java...

(defmethod splice-nodes [PersistentHashMap$ArrayNode PersistentHashMap$ArrayNode]
  [^PersistentHashMap$ArrayNode left ^PersistentHashMap$ArrayNode right ^Counts counts]
  (let [^"[Lclojure.lang.PersistentHashMap$INode;" left-array (array-node-array left)
        ^"[Lclojure.lang.PersistentHashMap$INode;" right-array (array-node-array right)
        ^"[Lclojure.lang.PersistentHashMap$INode;" new-array (make-array PersistentHashMap$INode 32)
        empty (atom 0)
        same-key
        (reduce
         (fn [sk f] (+ sk (.sameKey ^Counts @f)))
         0
         (reduce
          (fn [out i]
            (let [^PersistentHashMap$INode l (aget left-array i)
                  ^PersistentHashMap$INode r (aget right-array i)]
              (cond
               (and l r)
               (conj
                out
                (future
                  (let [^Counts c (Counts.)]
                    (aset
                     new-array
                     i
                     (Seqspert/splice 0 c false 0 nil l false 0 nil r))
                    c)))
               (not (nil? l))
               (do (aset new-array i l) out)
               (not (nil? r))
               (do (aset new-array i r) out)
               :else
               (do
                 (swap! empty inc)
                 out)
               )))
          []
          range32))]
    (set! (.sameKey counts) same-key)
    (ArrayNodeUtils/makeArrayNode2
     (- 32 @empty)
     new-array
     )
    ))

(defmulti splice-maps (fn [l r] [(type l)(type r)]))

(defmethod splice-maps [PersistentHashMap PersistentHashMap]
  [^PersistentHashMap left ^PersistentHashMap right]
  (let [left-count (hash-map-count left)
        right-count (hash-map-count right)
        counts (clojure.lang.Counts.)
        new-root (splice-nodes (hash-map-root left)(hash-map-root right) counts)]
    (Seqspert/makeHashMap2 (- (+ left-count right-count) (.sameKey counts)) new-root)))

;;------------------------------------------------------------------------------

(comment
  (def m1 (apply hash-map (mapcat (fn [i] [(HashCodeKey. (str i) i) i]) (range 10000000))))
  (def m2 (apply hash-map (mapcat (fn [i] [(HashCodeKey. (str i) i) i]) (range 5000000 15000000))))
  
  (def m3 (time (merge m1 m2)))
  (def m4 (time (splice-hash-maps m1 m2)))
  (def m5 (time (splice-maps m1 m2)))

  (= m3 m4 m5)
)
