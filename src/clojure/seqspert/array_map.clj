(ns seqspert.array-map
  (:import [java.lang.reflect Field]
           [clojure.lang PersistentArrayMap])
  (:require [seqspert.core :refer :all]))

;;--------------------------------------------------------------------------------

;; array-map internals

(defrecord ArrayMap [array])

(let [^Field f (unlock-field PersistentArrayMap "array")]
  (defn array-map-array [v] (.get f v)))

(defmethod inspect PersistentArrayMap [^PersistentArrayMap m]
  (ArrayMap. (inspect (array-map-array m))))

(inspect {:a 1 :b 2 :c 3})
