;; investigate an idea for tipping large amounts of data into a
;; clojure hash-map in a short time...

(ns clojure.lang.quick-map
  (:import 
   [java.util.concurrent.atomic
    AtomicReference
    AtomicIntegerArray]
   [clojure.lang
    Box
    PersistentHashMap
    PersistentHashMap$BitmapIndexedNode
    PersistentHashMap$ArrayNode
    PersistentHashMap$INode

    ArrayNodeUtils
    BitmapIndexedNodeUtils
    Seqspert
    ])
  ;;(:require  [clojure.core [reducers :as r]])
  ;;(:require [clojure [pprint :as p]])
  (:use [seqspert vector])
  )

;;------------------------------------------------------------------------------
;; even though I am in the same package, I do not seem to be able to
;; access static fields and methods in the same package scope - why ?

;; (def empty-node PersistentHashMap$BitmapIndexedNode/EMPTY)

(def empty-node BitmapIndexedNodeUtils/EMPTY)

;; (defn hash-partition [hash shift]
;;   (PersistentHashMap/mask hash shift))

(defn hash-partition [hash shift]
  (ArrayNodeUtils/partition hash shift))

;;;(defn node-assoc [^PersistentHashMap$INode node shift hash ^Object key ^Object value ^Box box]
;;;  (.assoc node ^int shift ^int hash key value box))

(defn node-assoc [^PersistentHashMap$INode node shift hash ^Object key ^Object value ^Box box]
  (Seqspert/assoc node ^int shift ^int hash key value box))

(defn make-array-node [nodes]
  (ArrayNodeUtils/makeArrayNode 32 (into-array Object nodes)))

(defn make-hash-map [count nodes]
  (Seqspert/createPersistentHashMap count (make-array-node nodes)))

;;------------------------------------------------------------------------------

;; (def atoms (into-array (repeatedly 32 (fn [] (atom empty-node)))))

(defn ^"[Lclojure.lang.Atom;" create-atoms [n] (into-array (repeatedly n (fn [] (atom empty-node)))))

(defprotocol ParallelAssoc
  (passoc [_ ^Object key ^Object value])
  (persistent [_]))

(def range-32 (range 32))

(defrecord ParallelHashMap [^"[Lclojure.lang.Atom;" atoms ^AtomicIntegerArray int-array]
  ParallelAssoc
  (passoc [_ ^Object key ^Object value]
    (let [hc (hash key)
          box (Box. nil)
          hp (hash-partition hc 0)]
      (swap! (aget atoms hp) node-assoc 0 hc key value box)
      (if (not (identical? box (.val box))) (.incrementAndGet int-array hp))
      nil))
  (persistent [_]
    (make-hash-map
     (reduce + (for [n range-32] (.get int-array n)))
     (map deref atoms))))

;;(def pm (ParallelHashMap. (create-atoms 32) (AtomicIntegerArray. 32)))
;;(passoc pm :a "hello")
;;(passoc pm :b "goodbye")
;;(persistent pm 2)

;;(int (Math/ceil (/ 1000 32)))

;;(def data (vec (map vector (range 100000)(range 100000)))) ;100,000 pairs

(defn into-map [pairs]
  (let [pm (ParallelHashMap. (create-atoms 32) (AtomicIntegerArray. 32))]
    (fjvmap (fn [[k v]] (passoc pm k v)) pairs)
    (persistent pm)))
