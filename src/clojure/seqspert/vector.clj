(ns seqspert.vector
  (:import
   [java.lang.reflect Field]
   [java.util Map TreeMap]
   [java.util.concurrent.atomic AtomicReference]
   [clojure.lang
    PersistentVector
    PersistentVector$Node
    APersistentVector$SubVector
    Seqspert
    ])
  (:require
   [seqspert.core :refer :all]))

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

;;------------------------------------------------------------------------------
;; fast vector -> array - working

;; (def v (vec (range (* 32 32 32 32))))
;; (dotimes [n 100](time (object-array v)))
;; ...
;; "Elapsed time: 144.589973 msecs"
;; (dotimes [n 100](time (vector-to-array v)))
;; ...
;; "Elapsed time: 33.427521 msecs"

;; 4x faster on a 2 core laptop

;; (= (seq (object-array v)) (seq (vector-to-array v))) -> true

(defn vector-node-to-array [offset shift ^PersistentVector$Node src ^objects tgt]
  (let [array (.array src)]
    (if (= shift 0)
      ;; copy leaf
      (System/arraycopy array 0 tgt offset 32)
      ;; copy child branches
      (let [m (clojure.lang.Numbers/shiftLeftInt 1 shift)
            new-shift (- shift 5)]
        (dotimes [n 32]
          (if-let [src (aget array n)]
            (vector-node-to-array (+ offset (* m n)) new-shift src tgt)))))))

(def thirty-two (vec (range 32)))

(defn vector-to-array [^PersistentVector src]
  (let [length (.count src)
        tgt (object-array length)
        tail (.tail src)
        tail-length (alength tail)
        shift (.shift src)]
    (if (> shift 5)
      ;; parallel copy 
      (let [m (clojure.lang.Numbers/shiftLeftInt 1 shift)
            new-shift (- shift 5)
            root-array (.array (.root src))
            branches (reduce
                      (fn [r i]
                        (if-let [node (aget root-array i)]
                          (conj r (future (vector-node-to-array (* i m) new-shift node tgt)))
                          r))
                      []
                      thirty-two)]
        ;; copy tail whilst giving branches some time to run...
        (System/arraycopy tail 0 tgt (- length tail-length) tail-length)          
        ;; wait for all branches to finish
        (doseq [branch branches] (deref branch)))
      ;; sequential copy
      (do
        (vector-node-to-array 0 shift (.root src) tgt)
        (System/arraycopy tail 0 tgt (- length tail-length) tail-length)))
    tgt))

;;------------------------------------------------------------------------------
;; fast array -> vector - replacement for (into [] array)

;; (def a (object-array (range (* 32 32 32 32))))
;; (dotimes [n 100](time (do (into [] a) nil)))
;; ...
;; "Elapsed time: 255.490017 msecs"
;; (dotimes [n 100](time (do (array-to-vector a) nil)))
;; ...
;; "Elapsed time: 20.570319 msecs"

;; 12x faster on a 2 core laptop

;; (= (into [] a) (array-to-vector a)) -> true

(let [^TreeMap powers-of-32
      (reduce
       (fn [^Map m [k v]] (.put m k v) m)
       (doto (TreeMap.) (.put 0 5))
       (map (fn [p] (let [b (* p 5)] [(inc (bit-shift-left 1 b)) b])) (range 2 10)))]
  
  (defn find-shift [n] (.getValue (.floorEntry powers-of-32 n)))
  )

(defn down-shift [n] (if (= n 5) 5 (- n 5)))
  
(defn round-up [n] (int (Math/ceil (double n))))

(defn ^PersistentVector$Node array-to-vector-node [^AtomicReference atom ^objects src src-start width shift]
  (let [tgt (object-array 32)]
    (if (= shift 5)
      (let [rem (- (count src) src-start)]
        (if (> rem 0)
          (System/arraycopy src src-start tgt 0 (min rem 32))))
      (let [new-shift (down-shift shift)
            new-width (bit-shift-left 1 new-shift)]
        (dotimes [n (round-up (/ width new-width))]
          (let [new-start (+ src-start (* n new-width))]
            (aset tgt n (array-to-vector-node atom src new-start new-width new-shift))))))
    (PersistentVector$Node. atom tgt)))

(defn array-to-vector [^objects src-array]
  (let [length (alength src-array)
        rem (mod length 32)
        tail-length (if (and (not (zero? length)) (= rem 0)) 32 rem)
        root-length (- length tail-length)
        shift (find-shift root-length)
        width (bit-shift-left 1 shift)
        atom (java.util.concurrent.atomic.AtomicReference. nil)
        root-array (object-array 32)
        nodes-needed (round-up (/ root-length (bit-shift-left 1 shift)))
        branches (object-array nodes-needed)]
    (dotimes [i nodes-needed]
      (aset branches i
            (future
              (let [start (* i width)
                    end (min (- root-length start) width)]
                (aset root-array i (array-to-vector-node atom src-array start end shift))))))
    (let [tail (object-array tail-length)
          v (Seqspert/createPersistentVector length shift (PersistentVector$Node. atom root-array) tail)]
      (System/arraycopy src-array (- length tail-length) tail 0 tail-length)
      (doseq [branch branches] (deref branch))
      v)))
