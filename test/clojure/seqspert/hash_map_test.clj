(ns seqspert.hash-map-test
  (:require  [clojure.core [reducers :as r]])
  (:use
   [clojure set test]
   [seqspert test-utils hash-map]))

(deftest times
  (def m1 (apply hash-map (range 10000)))
  (def m2 (apply hash-map (range 5000 150000)))
  
  (println)
  (println "two intersecting hash maps of 5000 entries to be merged")  
  (println "merge vs splice :"
           (millis 100 #(merge m1 m2)) "ms" "vs"
           (millis 100 #(splice-hash-maps m1 m2)) "ms")

  (is (=
       (merge m1 m2)
       (splice-hash-maps m1 m2)))

  )
