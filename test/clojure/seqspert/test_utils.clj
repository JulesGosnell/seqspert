(ns seqspert.test-utils
  (:import
   [clojure.lang Seqspert])
  (:use
   [clojure test]
   [seqspert vector]))

(println "\navailable processors:" (.availableProcessors (Runtime/getRuntime)))

(defn millis [n f]
  (let [t (System/nanoTime)]
    (dotimes [_ n] (f))
    (double (/ (- (System/nanoTime) t) n 1000000))))
