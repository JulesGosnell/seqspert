(ns seqspert.vector-test
  (:import
   [clojure.lang Seqspert])
  (:use
   [clojure test]
   [seqspert vector test-utils]))

(set! *warn-on-reflection* true)

(deftest vector-to-array-test
  (let [r (vec (range 10000))]
    (println)
    (println "a vector of 10000 items to be flattened into an object array")
    (println "into-array vs vector-to-array :"
             (millis 100 #(into-array r)) "ms" "vs"
             (millis 100 #(vector-to-array r)) "ms")
    (is (=
         (seq (into-array r))
         (seq (vector-to-array r))))))

(deftest array-to-vector-test
  (let [r (object-array (range 1000000))]
    (println)
    (println "an array of 10000 items to be inflated into a vector")
    (println "into vs array-to-vector:" 
             (millis 100 #(into [] r)) "ms" "vs"
             (millis 100 #(array-to-vector r)) "ms")
    (is (=
         (into [] r)
         (array-to-vector r)))))

(deftest vmap-test
  (let [r (vec (range 1000000))]
    (println)
    (println "identity fn to be mapped over 1000000 items from one vector into another")
    (println "mapv vs vmap vs fjvmap:"
             (millis 100 #(mapv identity r)) "ms" "vs"
             (millis 100 #(vmap identity r)) "ms" "vs"
             (millis 100 #(fjvmap identity r)) "ms")
    (is (=
         (mapv identity r)
         (vmap identity r)
         (fjvmap identity r)))))
