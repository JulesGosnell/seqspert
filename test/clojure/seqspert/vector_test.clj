(ns seqspert.vector-test
  (:import
   [clojure.lang Seqspert])
  (:use
   [clojure test]
   [seqspert vector]))

(set! *warn-on-reflection* true)

(deftest vector-to-array-test
  (let [r (vec (range 1000000))]
    (println "into-array x 1000000")
    (dotimes [_ 10] (time (into-array r)))
    (println "vector-to-array x 1000000")
    (dotimes [_ 10] (time (vector-to-array r)))
    (is (= (seq (vector-to-array r)) (seq (object-array r))))))

(deftest array-to-vector-test
  (let [r (object-array (range 1000000))]
    (println "into [] x 1000000")
    (dotimes [_ 10] (time (into [] r)))
    (println "array-to-vector x 1000000")
    (dotimes [_ 10] (time (array-to-vector r)))
    (is (= (array-to-vector r) (vec r)))))

(deftest vmap-test
  (let [r (vec (range 1000000))]
    (println "mapv x 1000000")
    (dotimes [_ 10] (time (mapv identity r)))
    (println "vmap x 1000000")
    (dotimes [_ 10] (time (vmap identity r)))
    (is (= (vmap inc r) (mapv inc r)))))

(deftest fjvmap-test
  (let [r (vec (range 1000000))]
    (println "mapv x 1000000")
    (dotimes [_ 10] (time (mapv identity r)))
    (println "fjvmap x 1000000")
    (dotimes [_ 10] (time (fjvmap identity r)))
    (is (= (fjvmap inc r) (mapv inc r)))))

  
