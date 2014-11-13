(ns seqspert.hash-map-test
  (:require  [clojure.core [reducers :as r]])
  (:require [clojure [pprint :as p]])
  (:import [clojure.lang
            TestUtils HashCodeKey])
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

  (is (=
       (merge m1 m2)
       (splice-hash-maps m1 m2)))
  
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

(deftest collision-test
  (let [k0 (HashCodeKey. :k0 0) v0 "v0"
        k1 (HashCodeKey. :k1 0) v1 "v1"
        k2 (HashCodeKey. :k2 0) v2 "v2"
        k3 (HashCodeKey. :k3 0) v3 "v3"]
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


(deftest collision-test
  (testing "merging of two HCN's with different hashCodes"
    (let [k0 (HashCodeKey. :k0 1) v0 "v0"
          k1 (HashCodeKey. :k1 1) v1 "v1"
          k2 (HashCodeKey. :k2 33) v2 "v2"
          k3 (HashCodeKey. :k3 33) v3 "v3"
          expected (hash-map k0 v0 k1 v1 k2 v2 k3 v3)
          actual (splice-hash-maps (hash-map k0 v0 k1 v1) (hash-map k2 v2 k3 v3))]
      (p/pprint (seqspert.core/inspect expected))
      (p/pprint (seqspert.core/inspect actual))
      (is (= actual expected))
      )
    ))

;;; random map test

(def shift 5)
(def depth 4)
(def breadth (bit-shift-left 1 shift))
(def n (int (Math/pow breadth depth)))

(defn rand-hash [breadth depth]
  (reduce
   (fn [hash partition] (bit-or (bit-shift-left hash shift) partition))
   0
   (repeatedly (inc (rand-int depth)) #(rand-int breadth))))

(defn rand-bool []
  (= (rand-int 2) 0))

(defn rand-assoc [m hash]
  (assoc m (HashCodeKey. (str (if (rand-bool) "black-" "white-") hash) hash) (rand-bool)))

(defn rand-map [n breadth depth]
  (reduce rand-assoc {} (repeatedly n #(rand-hash breadth depth))))

(defn check [n breadth depth]
  (let [l (rand-map n breadth depth)
        r (rand-map n breadth depth)]
    (TestUtils/assertHashMapEquals
     (merge l r)
     (splice-hash-maps l r))))

;(TestUtils/wrapSplicers)
;(check 512 breadth 4)
;(.printStackTrace *e)
;(repeatedly #(check 512 32 4))

;;(p/pprint (seqspert.core/inspect clojure.lang.TestSplicer/left))
;;(p/pprint (seqspert.core/inspect clojure.lang.TestSplicer/right))
;;(p/pprint (seqspert.core/inspect clojure.lang.TestSplicer/expected))
;;(p/pprint (seqspert.core/inspect clojure.lang.TestSplicer/actual))
