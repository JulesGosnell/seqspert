(ns seqspert.hash-set-test
  (:require  [clojure.core [reducers :as r]])
  (:use
   [clojure set test]
   [seqspert test-utils hash-set]))

(set! *warn-on-reflection* true)

(def factories
  [even?
   identity
   str
   (fn [v] (keyword (str v)))
   (fn [v] (symbol (str v)))
   list
   vector
   sorted-set
   hash-set
   (fn [v] (array-map v v))
   (fn [v] (sorted-map v v))
   (fn [v] (hash-map v v))])

(defn data [r] (map (fn [f v] (f v)) (apply concat (repeat factories)) r))

(deftest splice-test
  (testing "can we splice together two hash-sets"

    (def a (into #{} (data (range 100000))))
    (def b (into #{} (data (range 50000 150000))))
    (def c (into a b))
    (def d (splice-hash-sets a b))

    (is
     (and
      (= c d)
      (not (= c (conj d -1)))
      (not (= (conj c -1) d))))

    )

  (testing "check that mutating spliced has no effect on splicees"
    (def e (conj! (transient d) -1))
    (is
     (and
      (= a (into #{} (data (range 100000))))
      (= b (into #{} (data (range 50000 150000))))))

    (def f (persistent! e))
    (is (= f (conj c -1)))
    )

  (testing "check that mutating splicees has no effect on spliced"
    (let [d (splice-hash-sets a b)
          a2 (conj! (transient a) -1)
          b2 (conj! (transient b) -2)]
      (is
       (and 
        (= d c)
        (let [a3 (persistent! a2)
              b3 (persistent! b2)]
          (= d c)))))
    )

  (testing "what about if we splice something that was transient and the transient it again"
    (def a (into #{} (data (range 100000))))
    (def b (into #{} (data (range 50000 150000))))
    (def c (into a b))

    (is
     (and
      (let [a2 (transient a)
            b2 (transient b)
            d (splice-hash-sets a b)
            r1 (= c d)
            a3 (persistent! (conj! a2 -1))
            b3 (persistent! (conj! b2 -2))
            r2 (= c d)
            a4 (transient a3)
            b4 (transient b3)
            r3 (= c d)
            a5 (persistent! (conj! a4 -2))
            b5 (persistent! (conj! b4 -1))
            r4 (= c d)]
        (and r1 r2 r3 r4))

      (let [c (splice-hash-sets a b)
            a2 (into a (range -10000 0))
            b2 (into b (range -20000 -10000))]
        (= c d)
        )))
    ))

(deftest times
  (def m1 (apply hash-map (range 10000)))
  (def m2 (apply hash-map (range 5000 150000)))
  
  (def s1 (apply hash-set (range 10000)))
  (def s2 (apply hash-set (range 5000 150000)))
  (println)
  (println "two intersecting hash sets of 10000 items to be merged")  
  (println "union vs splice :" (millis 100 #(union s1 s2)) "ms" "vs" (millis 100 #(splice-hash-sets s1 s2)) "ms")

  (is (=
       (union s1 s2)
       (splice-hash-sets s1 s2)))

  (println)
  (println "a vector of 10000 items to be read into a hash set")
  
  (def r 10000)
  (def v (vec (range r)))
  (def p (/ r (.availableProcessors (Runtime/getRuntime))))
  
  (println "sequential 'into' vs parallel 'into' vs parallel spliced 'into-hash-set' into hash-set :"
           (millis 100 #(into #{} v)) "ms" "vs"
           (millis 100 #(r/fold p (r/monoid into hash-set) conj v)) "ms" "vs"
           (millis 100 #(into-hash-set p v)) "ms")
  
  (is (=
       (into #{} v)
       (r/fold p (r/monoid into hash-set) conj v)
       (into-hash-set p v)))
  )
