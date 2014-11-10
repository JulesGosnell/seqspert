# Seqspert <i>["seekspert"]</i>

The Clojure collection library is built upon the abstraction of a
Sequence.

Whilst abstractions are a good thing in terms of getting useful work
done in simple terms, when it comes to raw performance, they may get
in the way.

Seqspert started life as a set of utils for examining the underlying
implementations and contents of various Clojure Sequence types but is
now growing into a library supporting a number of specific
high-performance, low-churn alternatives to common Sequence-based
operations.

## Build/Install

- git clone https://github.com/JulesGosnell/seqspert.git
- cd seqspert
- lein install

## Overview

Seqspert provides an "inspect" method for transforming the underlying
implementation of a number of Clojure Sequences into a corresponding
Clojure data structure which may then be printed.This aids
comprehension of exactly what is going on under the covers, hopefully
leading to the writing of tighter code.

Seqspert also provides a number of high-performace Sequence related
functions:

array-map:
- inspect

hash-map:
- splice-hash-maps
- into-hash-map

hash-set:
- splice-hash-sets
- into-hash-set

tree-map:
- inspect

tree-set:
inspect

vector:
- inspect
- vector-to-array
- array-to-vector
- vmap
- fjvmap

## Usage

<pre>
user=> (use '[seqspert core all])
nil
user=> (use '[clojure.pprint])
nil
user=> ;; an array-map

user=> (inspect {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 8 :h 9})
#seqspert.array_map.ArrayMap{:array [:e 5 :g 8 :c 3 :h 9 :b 2 :d 4 :f 6 :a 1]}
user=> 

user=> ;; a hash-map

user=> (inspect {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 8 :h 9 :i 10})
#seqspert.hash_map.HashMap{:count 9, :root #seqspert.hash_map.BitmapIndexedNode{:bitmap "1100001010100100100000000000000", :array [:e 5 nil #seqspert.hash_map.BitmapIndexedNode{:bitmap "100000000010000000000000", :array [:g 8 :c 3 nil nil nil nil]} :h 9 :b 2 nil #seqspert.hash_map.BitmapIndexedNode{:bitmap "10000000000", :array [nil #seqspert.hash_map.BitmapIndexedNode{:bitmap "100000001", :array [:d 4 :f 6 nil nil nil nil]} nil nil nil nil nil nil]} :i 10 :a 1 nil nil]}}
user=> 

user=> (pprint (inspect {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 8 :h 9 :i 10}))
{:count 9,
 :root
 {:bitmap "1100001010100100100000000000000",
  :array
  [:e
   5
   nil
   {:bitmap "100000000010000000000000",
    :array [:g 8 :c 3 nil nil nil nil]}
   :h
   9
   :b
   2
   nil
   {:bitmap "10000000000",
    :array
    [nil
     {:bitmap "100000001", :array [:d 4 :f 6 nil nil nil nil]}
     nil
     nil
     nil
     nil
     nil
     nil]}
   :i
   10
   :a
   1
   nil
   nil]}}
nil
user=> ;; a vector

user=> (inspect [:a :b :c :d :e :f :g :h])
#seqspert.vector.Vector{:cnt 8, :shift 5, :root #seqspert.vector.VectorNode{:array [nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil nil]}, :tail [:a :b :c :d :e :f :g :h]}
user=> 

user=> (pprint (inspect [:a :b :c :d :e :f :g :h]))
{:cnt 8,
 :shift 5,
 :root
 {:array
  [nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil
   nil]},
 :tail [:a :b :c :d :e :f :g :h]}
nil
user=> 

user=> 
</pre>

## License

Copyright © 2014 Julian Gosnell

Distributed under the Eclipse Public License, the same as Clojure.
