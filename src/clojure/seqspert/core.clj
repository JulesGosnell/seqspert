(ns seqspert.core
  (:import [java.lang.reflect Field]))

(set! *warn-on-reflection* true)

(defn unlock-field [^Class class name]
  (doto (.getDeclaredField class name) (.setAccessible true)))

(defmulti inspect type)

(defmethod inspect (type (into-array [])) [a]
  (mapv (fn [e] (if e (inspect e) e)) a))

(defmethod inspect :default [o]
  o)
