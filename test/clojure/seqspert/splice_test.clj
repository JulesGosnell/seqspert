(ns seqspert.splice-test
  (:import [clojure.lang Seqspert])
  (:require [clojure.test :refer :all]))

(set! *warn-on-reflection* true)

(defn splice-set [l r] (Seqspert/spliceHashSets l r))
(defn splice-map [l r] (Seqspert/spliceHashMaps l r))

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
    (def d (splice-set a b))

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
    (let [d (splice-set a b)
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
            d (splice-set a b)
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

      (let [c (splice-set a b)
            a2 (into a (range -10000 0))
            b2 (into b (range -20000 -10000))]
        (= c d)
        )))
    ))

(def m1 (apply hash-map (range 10000)))
(def m2 (apply hash-map (range 5000 150000)))

(println "merge map x 10000")
(dotimes [_ 10] (time (merge m1 m2)))
(println "splice-map x 10000")
(dotimes [_ 10] (time (splice-map m1 m2)))

(def s1 (apply hash-set (range 10000)))
(def s2 (apply hash-set (range 5000 150000)))

(println "merge set x 10000")
(dotimes [_ 10] (time (merge s1 s2)))
(println "splice-set x 10000")
(dotimes [_ 10] (time (splice-set s1 s2)))
