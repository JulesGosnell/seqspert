(ns seqspert.hash-set
  (:import [java.lang.reflect Field]
           [clojure.lang
            PersistentHashSet
            APersistentSet])
  (:require [seqspert.core :refer :all]))

(let [^Field f (unlock-field APersistentSet "impl")]  (defn hash-set-impl  [n] (.get f n)))

(defrecord HashSet [impl])

(defmethod decloak PersistentHashSet [^PersistentHashSet s]
  (HashSet. (decloak (hash-set-impl s))))
