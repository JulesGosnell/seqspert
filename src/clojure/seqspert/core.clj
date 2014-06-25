(ns seqspert.core
  (:import [java.lang.reflect Field]))

(set! *warn-on-reflection* true)

(defn unlock-field [^Class class name]
  (doto (.getDeclaredField class name) (.setAccessible true)))

(defmulti decloak type)

(defmethod decloak (type (into-array [])) [a]
  (mapv (fn [e] (if e (decloak e) e)) a))

(defmethod decloak :default [o]
  o)
