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

(deftype MyType [value hashcode] Object (hashCode [_] hashcode))

(deftest collision-test
  (let [k0 (MyType. :k0 0) v0 "v0"
        k1 (MyType. :k1 0) v1 "v1"
        k2 (MyType. :k2 0) v2 "v2"
        k3 (MyType. :k3 0) v3 "v3"]
    (testing "one : one"
      (is (= (splice-hash-maps (hash-map k0 v0) (hash-map k1 v1))
             (hash-map k0 v0 k1 v1))))
    (testing "one : two"
      (is (= (splice-hash-maps (hash-map k0 v0 k1 v1) (hash-map k2 v2))
             (hash-map k0 v0 k1 v1 k2 v2))))
    (testing "two : one"
      (is (= (splice-hash-maps (hash-map k0 v0) (hash-map k1 v1 k2 v2))
             (hash-map k0 v0 k1 v1 k2 v2))))
    (testing "two : two"
      (is (= (splice-hash-maps (hash-map k0 v0 k1 v1) (hash-map k2 v2 k3 v3))
             (hash-map k0 v0 k1 v1 k2 v2 k3 v3))))
    ))
