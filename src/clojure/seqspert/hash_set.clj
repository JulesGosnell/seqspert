(ns seqspert.hash-set
  (:import [java.lang.reflect Field]
           [clojure.lang
            PersistentHashSet
            APersistentSet]
           [clojure.lang Seqspert])
  (:require [seqspert.core :refer :all]))

(let [^Field f (unlock-field APersistentSet "impl")]  (defn hash-set-impl  [n] (.get f n)))

(defrecord HashSet [impl])

(defmethod inspect PersistentHashSet [^PersistentHashSet s]
  (HashSet. (inspect (hash-set-impl s))))

(defn splice-hash-sets [l r] (Seqspert/spliceHashSets l r))
