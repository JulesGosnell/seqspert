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
  (let [r (vec (range 1000000))]
    (println "into-array x 1000000     :" (millis 100 #(into-array r)) "ms")
    (println "vector-to-array x 1000000:" (millis 100 #(vector-to-array r)) "ms")
    (is (= (seq (vector-to-array r)) (seq (object-array r))))))

(deftest array-to-vector-test
  (let [r (object-array (range 1000000))]
    (println "into [] x 1000000        :" (millis 100 #(into [] r)) "ms")
    (println "array-to-vector x 1000000:" (millis 100 #(array-to-vector r)) "ms")
    (is (= (array-to-vector r) (vec r)))))

(deftest vmap-test
  (let [r (vec (range 1000000))]
    (println "mapv x 1000000:" (millis 100 #(mapv identity r)) "ms")
    (println "vmap x 1000000:" (millis 100 #(vmap identity r)) "ms")
    (is (= (vmap inc r) (mapv inc r)))))

(deftest fjvmap-test
  (let [r (vec (range 1000000))]
    (println "mapv x 1000000  :" (millis 100 #(mapv identity r)) "ms")
    (println "fjvmap x 1000000:" (millis 100 #(fjvmap identity r)) "ms")
    (is (= (fjvmap inc r) (mapv inc r)))))
