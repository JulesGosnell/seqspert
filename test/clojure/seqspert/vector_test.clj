(ns seqspert.vector-test
  (:import
   [clojure.lang Seqspert])
  (:use
   [clojure test]
   [seqspert vector]))

(set! *warn-on-reflection* true)

(println "\navailable processors:" (.availableProcessors (Runtime/getRuntime)))

(defn millis [n f]
  (let [t (System/nanoTime)]
    (dotimes [_ n] (f))
    (double (/ (- (System/nanoTime) t) n 1000000))))

(deftest vector-to-array-test
  (let [r (vec (range 10000))]
    (println)
    (println "copy a vector of 10000 items into an object array")
    (println "into-array vs vector-to-array :"
             (millis 100 #(into-array r)) "ms" "vs"
             (millis 100 #(vector-to-array r)) "ms")
    (is (= (seq (into-array r)) (seq (vector-to-array r))))))

(deftest array-to-vector-test
  (let [r (object-array (range 1000000))]
    (println)
    (println "inflate an array of 10000 items into a vector")
    (println "into vs array-to-vector:" 
             (millis 100 #(into [] r)) "ms" "vs"
             (millis 100 #(array-to-vector r)) "ms")
    (is (= (into [] r) (array-to-vector r)))))

(deftest vmap-test
  (let [r (vec (range 1000000))]
    (println)
    (println "map identity fn over a vector of 1000000 items")
    (println "mapv vs vmap vs fjvmap:"
             (millis 100 #(mapv identity r)) "ms" "vs"
             (millis 100 #(vmap identity r)) "ms" "vs"
             (millis 100 #(fjvmap identity r)) "ms")
    (is (= (mapv identity r) (vmap identity r) (fjvmap identity r)))))
