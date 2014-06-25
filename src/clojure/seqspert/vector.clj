(ns seqspert.vector
  (:import [java.lang.reflect Field]
           [clojure.lang
            PersistentVector
            PersistentVector$Node
            APersistentVector$SubVector
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
;; looks like a vector builds a "tail" Object[32] and then fills
;; it. When it is full it moves it into a Node and hangs this from
;; root, then starts filling tail again.

;; when tail fills again, it takes the root.array and tail and hangs
;; them both from a new array which it puts in a new node which it
;; hangs from root. etc..

;; write some tests to verify this
;;------------------------------------------------------------------------------

