(ns seqspert.all
  (:require [clojure [pprint :as p]])
  (:use [seqspert core vector array-map hash-map tree-map hash-set tree-set]))

;; (p/pprint (inspect (sorted-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 8 :h 9))) ; tree-map
;; (p/pprint (inspect {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 8 :h 9}))            ; array-map
;; (p/pprint (inspect {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 8 :h 9 :i 10}))      ; hash-map
;; (p/pprint (inspect #{:a :b :c :d :e :f :g :h}))                           ; hash-set
;; (p/pprint (inspect (sorted-set :a :b :c :d :e :f :g :h)))                 ; tree-set
;; (p/pprint (inspect [:a :b :c :d :e :f :g :h]))                            ; vector
;; (p/pprint (inspect (subvec [0 1 2 3 4 5 6 7 8 9] 2 8)))                   ; subvector

;; need to do sorted-set / tree-set
