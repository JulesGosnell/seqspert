(ns seqspert.vector-test
  (:import
   [clojure.lang Seqspert])
  (:use
   [clojure test]
   [seqspert vector]))

(set! *warn-on-reflection* true)

(deftest vector-to-array-test
  (let [r (range 1000)]
    (is (= (seq (vector-to-array (vec r))) (seq (object-array r))))))

(deftest array-to-vector-test
  (let [r (range 1000)]
    (is (= (array-to-vector (object-array r)) (vec r)))))

  
