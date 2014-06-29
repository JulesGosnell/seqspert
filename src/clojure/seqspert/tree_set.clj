(ns seqspert.tree-set
  (:import [java.lang.reflect Field]
           [clojure.lang
            PersistentTreeSet
            APersistentSet])
  (:require [seqspert.core :refer :all]))

(let [^Field f (unlock-field APersistentSet "impl")]  (defn tree-set-impl  [n] (.get f n)))

(defrecord TreeSet [impl])

(defmethod inspect PersistentTreeSet [^PersistentTreeSet s]
  (TreeSet. (inspect (tree-set-impl s))))
