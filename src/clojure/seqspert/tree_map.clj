(ns seqspert.tree-map
  (:import [java.lang.reflect Field]
           [clojure.lang
            PersistentTreeMap
            PersistentTreeMap$Node
            APersistentVector
            PersistentTreeMap$BlackVal
            PersistentTreeMap$BlackBranch
            PersistentTreeMap$BlackBranchVal
            PersistentTreeMap$RedVal
            PersistentTreeMap$RedBranch
            PersistentTreeMap$RedBranchVal
            ])
  (:require [seqspert.core :refer :all]))

;;--------------------------------------------------------------------------------

;; array-map internals

(defrecord TreeMap [tree _count])

(let [^Field f (unlock-field PersistentTreeMap "tree")]
  (defn tree-map-tree [v] (.get f v)))

(let [^Field f (unlock-field PersistentTreeMap "_count")]
  (defn tree-map-count [v] (.get f v)))

(defmethod inspect PersistentTreeMap [^PersistentTreeMap m]
  (TreeMap. (inspect (tree-map-tree m)) (tree-map-count m)))

(let [^Field f (unlock-field PersistentTreeMap$Node "key")]
  (defn tree-map-node-key [v] (.get f v)))

(let [^Field f (unlock-field PersistentTreeMap$BlackVal "val")]
  (defn tree-map-blackval-val [v] (.get f v)))

(defrecord TreeMapBlackVal [key val])

(defmethod inspect PersistentTreeMap$BlackVal [^PersistentTreeMap$BlackVal n]
  (TreeMapBlackVal. (tree-map-node-key n) (tree-map-blackval-val n)))

(let [^Field f (unlock-field PersistentTreeMap$BlackBranch "left")]
  (defn tree-map-blackbranch-left [v] (.get f v)))

(let [^Field f (unlock-field PersistentTreeMap$BlackBranch "right")]
  (defn tree-map-blackbranch-right [v] (.get f v)))

(defrecord TreeMapBlackBranch [key left right])

(defmethod inspect PersistentTreeMap$BlackBranch [^PersistentTreeMap$BlackBranch n]
  (TreeMapBlackBranch.
   (tree-map-node-key n)
   (inspect (tree-map-blackbranch-left n))
   (inspect (tree-map-blackbranch-right n))))

(let [^Field f (unlock-field PersistentTreeMap$BlackBranchVal "val")]
  (defn tree-map-blackbranchval-val [v] (.get f v)))

(defrecord TreeMapBlackBranchVal [key val left right])

(defmethod inspect PersistentTreeMap$BlackBranchVal [^PersistentTreeMap$BlackBranchVal n]
  (TreeMapBlackBranchVal.
   (tree-map-node-key n)
   (tree-map-blackbranchval-val n)
   (inspect (tree-map-blackbranch-left n))
   (inspect (tree-map-blackbranch-right n))
   ))

;;------------------------------------------------------------------------------
;; Red

(let [^Field f (unlock-field PersistentTreeMap$RedVal "val")]
  (defn tree-map-redval-val [v] (.get f v)))

(defrecord TreeMapRedVal [key val])

(defmethod inspect PersistentTreeMap$RedVal [^PersistentTreeMap$RedVal n]
  (TreeMapRedVal.
   (tree-map-node-key n)
   (tree-map-redval-val n)))

(let [^Field f (unlock-field PersistentTreeMap$RedBranch "left")]
  (defn tree-map-redbranch-left [v] (.get f v)))

(let [^Field f (unlock-field PersistentTreeMap$RedBranch "right")]
  (defn tree-map-redbranch-right [v] (.get f v)))

(defrecord TreeMapRedBranch [key left right])

(defmethod inspect PersistentTreeMap$RedBranch [^PersistentTreeMap$RedBranch n]
  (TreeMapRedBranch.
   (tree-map-node-key n)
   (inspect (tree-map-redbranch-left n))
   (inspect (tree-map-redbranch-right n))))

(let [^Field f (unlock-field PersistentTreeMap$RedBranchVal "val")]
  (defn tree-map-redbranchval-val [v] (.get f v)))

(defrecord TreeMapRedBranchVal [key val left right])

(defmethod inspect PersistentTreeMap$RedBranchVal [^PersistentTreeMap$RedBranchVal n]
  (TreeMapRedBranchVal.
   (tree-map-node-key n)
   (tree-map-redbranchval-val n)
   (inspect (tree-map-redbranch-left n))
   (inspect (tree-map-redbranch-right n))
   ))

;;------------------------------------------------------------------------------

(inspect (sorted-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6))
