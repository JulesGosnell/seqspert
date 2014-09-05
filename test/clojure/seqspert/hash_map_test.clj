(ns seqspert.hash-map-test
  (:require  [clojure.core [reducers :as r]])
  (:use
   [clojure set test]
   [seqspert test-utils hash-map]))

(deftest times
  (def m1 (apply hash-map (mapcat (fn [v] [(keyword (str v)) v]) (range 100000))))
  (def m2 (apply hash-map (mapcat (fn [v] [(keyword (str v)) v]) (range 50000 150000))))
  
  (println)
  (println "two intersecting hash maps of 10000 entries to be merged")  
  (println "merge vs splice :"
           (millis 100 #(merge m1 m2)) "ms" "vs"
           (millis 100 #(splice-hash-maps m1 m2)) "ms")

  ;; (is (=
  ;;      (merge m1 m2)
  ;;      (splice-hash-maps m1 m2)))

  (println)
  (println "a vector of 100000 pairs to be read into a hash map")
  
  (def a (java.lang.Math/min (int 8) (.availableProcessors (Runtime/getRuntime))))
  (def p 10)
  (def r (* p a))
  (def v (vec (map (fn [v] [(keyword (str v)) v]) (range r))))
  
  (println "sequential 'into' vs parallel 'into' vs parallel spliced 'into-hash-map' into hash-map :"
           (millis 100 #(into {} v)) "ms" "vs"
           (millis 100 #(r/fold p (r/monoid into hash-map) conj v)) "ms" "vs"
           (millis 100 #(into-hash-map p v)) "ms"
           )
  
  (is (=
       (into {} v)
       (r/fold p (r/monoid into hash-map) conj v)
       (into-hash-map p v)
       ))
  )
