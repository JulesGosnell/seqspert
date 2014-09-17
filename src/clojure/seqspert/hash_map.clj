(ns seqspert.hash-map
  (:import [java.lang.reflect Field]
           [clojure.lang
            PersistentHashMap
            PersistentHashMap$BitmapIndexedNode
            PersistentHashMap$HashCollisionNode
            PersistentHashMap$ArrayNode
            PersistentHashMap$INode]
           [clojure.lang Seqspert]
           )
  (:require [clojure.core [reducers :as r]]
            [seqspert.core :refer :all]))

;; (defn mask [hash shift]
;;   (clojure.lang.Numbers/and
;;    (clojure.lang.Numbers/unsignedShiftRightInt hash shift)
;;    0x01f))

;; (defn bitpos [hash shift]
;;   (clojure.lang.Numbers/shiftLeftInt 1 (mask hash shift)))

;; ;; from BitmapIndexedNode
;; (defn index [bit bitmap]
;;   (Integer/bitCount (clojure.lang.Numbers/and bitmap (- bit 1))))

;; (defn doit [o bitmap shift]
;;   (let [hash (clojure.lang.Util/hasheq o)
;;         bit (bitpos hash shift)]
;;     (index bit bitmap)))

;; (defn key-or-null [a i] (aget a (* 2 i)))
;; (defn val-or-node [a i] (aget a (inc (* 2 i))))

;; (defn b [i] (Integer/toBinaryString i))


;; ;; looks like I need to consider adding one hashset to another that is transient



;; (defn get-private [instance class name]
;;   (let [field (.getDeclaredField class name)]
;;     (.setAccessible field true)
;;     (.get field instance)))

;; (defn call-private [^Class class types & args]
;;   (let [ctor (.getDeclaredConstructor class types)]
;;     (.setAccessible ctor true)
;;     (eval (concat ('. ctor 'newInstance) args))))

;; (defn set-impl [s] (get-private s clojure.lang.APersistentSet "impl"))
;; (defn map-root [m] (get-private m clojure.lang.PersistentHashMap "root"))
;; (defn node-bitmap [n] (get-private n clojure.lang.PersistentHashMap$BitmapIndexedNode "bitmap"))
;; (defn node-array [n] (get-private n clojure.lang.PersistentHashMap$BitmapIndexedNode "array"))

;; (defn set-fields [s]
;;   [:bitmap (node-bitmap (map-root (set-impl s)))
;;    :array (node-array (map-root (set-impl s)))])

;; (defn abs [i] (if (< i 0) (* i -1) i))

;; (defn cmp-idx [o]
;;   (= (node-bitmap (map-root (set-impl #{o})))
;;      (bitpos (clojure.lang.Util/hasheq o) 0)))

;; ;;test
;; (mapcat (fn [n] (if (cmp-idx n) [] [n])) (range 1000))



;; (defn merge-bit [b lb rb]
;;   (bit-test lb b)
;;   (bit-test rb b)


;; (defn set-merge [l r]
;;   (bit-and (node-bitmap (map-root (set-impl l)))
;;            (node-bitmap (map-root (set-impl r)))))

;; ;;; starting   : 2r01001001
;; ;;; what I have: [f   t f   f   t f   f   t]
;; ;;; what I want: [nil 1 nil nil 2 nil nil 3]

;; ;; e.g. 
;; ;; (bits 2r10100100010000) ->
;; ;; [false false false false true false false false true false false true false true ...]
;; (defn bits [i]
;;   (mapv (fn [b] (bit-test i b)) (range 32)))

;; ;; e.g.
;; ;; (inspect (bits 2r10100100010000) 0 []) ->
;; ;; [false false false false 0 false false false 1 false false 2 false 3 ...]
;; (defn inspect [[head & tail] total output]
;;   (if (empty? tail)
;;     output
;;     (let [[v t] (if head [total (inc total)] [false total])]
;;       (inspect tail t (conj output v)))))

;; ;; try to use inspect bits to access arrays and pull keys/values for reforming into new set/map...

;; (let [types (into-array Class [java.util.concurrent.atomic.AtomicReference (java.lang.Integer/TYPE) (type (into-array []))])
;;       ^java.lang.reflect.Constructor ctor (doto ^java.lang.reflect.Constructor (.getDeclaredConstructor clojure.lang.PersistentHashMap$BitmapIndexedNode types)
;;                                                 (.setAccessible true))]

;;   (defn merge-nodes [l-node r-node]
;;     (let [^objects l-array (node-array l-node)
;;           l-bitmap (node-bitmap l-node)
;;           ^objects r-array (node-array r-node)
;;           r-bitmap (node-bitmap r-node)
;;           o-bitmap (bit-or l-bitmap r-bitmap)
;;           o-size (Integer/bitCount o-bitmap)
;;           o-array (object-array o-size)]
;;       ;; now pack this back into array...
;;       (map
;;        (fn [l r]
;;          [(if l (aget l-array (inc (* 2 l))) l)
;;           (if r (aget r-array (inc (* 2 r))) r)])
;;        (inspect (bits l-bitmap) 0 [])
;;        (inspect (bits r-bitmap) 0 []))
;;       (.newInstance
;;        ctor
;;        (into-array
;;         Object
;;         [(java.util.concurrent.atomic.AtomicReference.)
;;          (int o-bitmap)
;;          o-array])))))

;; ;; int Inode boolean Object
;; (let [types (into-array Class [(java.lang.Integer/TYPE)
;;                                clojure.lang.PersistentHashMap$INode
;;                                (java.lang.Boolean/TYPE)
;;                                java.lang.Object])
;;       ^java.lang.reflect.Constructor ctor (doto ^java.lang.reflect.Constructor (.getDeclaredConstructor clojure.lang.PersistentHashMap types)
;;                                                 (.setAccessible true))]

;;   (defn merge-maps [l r]
;;     (let [root (merge-nodes (map-root l) (map-root r))
;;           n (Integer/bitCount (node-bitmap root)) ;tmp
;;           has-null false
;;           null-value nil]
;;       (println "COUNT:" n)
;;       (.newInstance ctor (into-array Object [n root has-null null-value]))))) 

;; (defn merge-sets [l r]
;;   (merge-maps (set-impl l) (set-impl r)))

;; ;; e.g.

;; (p #{3,4,5} #{3,6,9})

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

(defn splice-hash-maps [l r] (Seqspert/spliceHashMaps l r))

(defn into-hash-map
  "parallel fold a sequence of pairs into a hash-map"
  ([values]
     (r/fold (r/monoid splice-hash-maps hash-map) conj values))
  ([parallelism values]
     (r/fold parallelism (r/monoid splice-hash-maps hash-map) conj values)))
