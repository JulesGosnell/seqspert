(ns seqspert.vector
  (:import [java.lang.reflect Field]
           [clojure.lang PersistentVector PersistentVector$Node APersistentVector$SubVector
            ;;APersistentVector$SuperVector
            ])
  (:require [seqspert.core :refer :all]))

;; vector internals

(let [^Field f (unlock-field PersistentVector "cnt")]   (defn vector-cnt   [v] (.get f v)))
(let [^Field f (unlock-field PersistentVector "shift")] (defn vector-shift [v] (.get f v)))
(let [^Field f (unlock-field PersistentVector "root")]  (defn vector-root  [v] (.get f v)))
(let [^Field f (unlock-field PersistentVector "tail")]  (defn vector-tail  [v] (.get f v)))
(let [^Field f (unlock-field PersistentVector$Node "array")]  (defn vector-node-array  [n] (.get f n)))

(defrecord Vector [cnt shift root tail])
(defrecord VectorNode [array])

(defmethod decloak PersistentVector$Node [^PersistentVector$Node n]
  (VectorNode. (decloak (vector-node-array n))))

(defmethod decloak PersistentVector[^PersistentVector v]
  (Vector.
   (vector-cnt v)
   (vector-shift v)
   (decloak (vector-root v))
   (decloak (vector-tail v))))

(decloak (into [] (range (+ 1 (* 32 33)))))

;;------------------------------------------------------------------------------
;; subvector internals

(let [^Field f (unlock-field APersistentVector$SubVector "v")]     (defn subvector-v     [v] (.get f v)))
(let [^Field f (unlock-field APersistentVector$SubVector "start")] (defn subvector-start [v] (.get f v)))
(let [^Field f (unlock-field APersistentVector$SubVector "end")]   (defn subvector-end   [v] (.get f v)))

(defrecord SubVector [v start end])

(defmethod decloak APersistentVector$SubVector [^APersistentVector$SubVector s]
  (SubVector.
   (decloak (subvector-v s))
   (subvector-start s)
   (subvector-end s)))

(decloak (subvec [0 1 2 3 4 5 6 7 8 9] 2 8))

;;------------------------------------------------------------------------------
;; ;; supervector internals

;; (let [^Field f (unlock-field APersistentVector$SuperVector "left")]  (defn supervector-left   [v] (.get f v)))
;; (let [^Field f (unlock-field APersistentVector$SuperVector "right")] (defn supervector-right  [v] (.get f v)))
;; (let [^Field f (unlock-field APersistentVector$SuperVector "middle")](defn supervector-middle [v] (.get f v)))
;; (let [^Field f (unlock-field APersistentVector$SuperVector "count")] (defn supervector-count  [v] (.get f v)))

;; (defrecord SuperVector [left right middle count])

;; (defmethod decloak APersistentVector$SuperVector [^APersistentVector$SuperVector s]
;;   (SuperVector.
;;    (decloak (supervector-left s))
;;    (decloak (supervector-right s))
;;    (supervector-middle s)
;;    (supervector-count s)))

;; (defn supervec [l r] (APersistentVector$SuperVector. {} l r))

;; (decloak (supervec [0 1 2 3 4 5 6 7 8 9] [10 11 12 13 14 15 16 17 18 19]))

;; ;; supervec tests - should not live here:
;; (def a (into [] (range 10000000)))
;; (time (def b (into a a)))
;; (time (def c (supervec a a)))
;; (= b c)

;;;------------------------------------------------------------------------------
;; looks like a vector builds a "tail" Object[32] and then fills
;; it. When it is full it moves it into a Node and hangs this from
;; root, then starts filling tail again.

;; when tail fills again, it takes the root.array and tail and hangs
;; them both from a new array which it puts in a new node which it
;; hangs from root. etc..

;; write some tests to verify this
;;------------------------------------------------------------------------------

