# Seqspert

<i>["seekspert"]</i>

## Introduction

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

Seqspert is currently a work in progress. I hope to get a release out
in the near future.

Seqspert contains both Java and Clojure code which is tested on every
build. Results of these tests may be ound here:

http://ouroboros.dyndns-free.com/ci/job/seqspert/

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

<pre>
user=> (use '[seqspert core all])
nil
user=> (use '[clojure.pprint])
nil
</pre>

array-map:
- inspect
<pre>
user=> (pprint (inspect {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 8 :h 9}))
{:array [:e 5 :g 8 :c 3 :h 9 :b 2 :d 4 :f 6 :a 1]}
nil
user=>
</pre>

hash-map:
- inspect
<pre>
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
user=>
</pre>
- splice-hash-maps [WIP]

Traditionally the merging of two hash-maps is done via the Sequence
abstraction, reading every key-value-pair from the right hand side and
assoc-ing each one to the left hand side. Unfortunately, this means
that all the work done to reduce a set of keys and values into the
right hand side is thrown away and has to be redone on the left hand
side.

Seqspert's splice-hash-maps function creates a new hash-trie
(underlying representation of a Clojure hash-map) directly from the
overlaying of the right hand side on top of the left hand side in a
single operation, reusing as much of the structure of both maps as
possible and avoiding most associated churn and re-calling of hash()
on keys.

Future Work: splice-hash-maps is currently performed
sequentially. Since hash-tries are a form of tree, parallelising it
should be relatively straightforward and yield further substantial
speed gains.

<pre>
user=> (use '[seqspert hash-map])
nil
user=> (def m1 (apply hash-map (range 0 2000000)))
#'user/m1
user=> (def m2 (apply hash-map (range 1000000 3000000)))
#'user/m2
user=> (time (def m3 (merge m1 m2))) ;; traditional approach
"Elapsed time: 786.300978 msecs"
#'user/m3
user=> (time (def m4 (splice-hash-maps m1 m2))) ;; seqspert replacement
"Elapsed time: 180.571396 msecs"
#'user/m4
user=> (= m3 m4)
true
user=> 
</pre>

hash-set:

Clojure hash-sets are implemented as hash-maps with each set element
being both the key and the value in its key-value pair.

- inspect

<pre>
user=> (use '[seqspert core all])
nil
user=> (use '[clojure.pprint])
nil
user=> (pprint (inspect #{:a :b :c :d :e :f :g :h}))
{:impl
 {:count 8,
  :root
  {:bitmap "1000001010100100100000000000000",
   :array
   [:e
    :e
    nil
    {:bitmap "100000000010000000000000",
     :array [:g :g :c :c nil nil nil nil]}
    :h
    :h
    :b
    :b
    nil
    {:bitmap "10000000000",
     :array
     [nil
      {:bitmap "100000001", :array [:d :d :f :f nil nil nil nil]}
      nil
      nil
      nil
      nil
      nil
      nil]}
    :a
    :a
    nil
    nil
    nil
    nil]}}}
nil
user=>
</pre>

- splice-hash-sets [WIP]

Since a hash-sets underlying representation is just a hash-map,
Seqspert can also implement a very efficient splice-hash-sets:

<pre>
user=> (def s1 (apply hash-set (range 1000000)))
#'user/s1
user=> (def s2 (apply hash-set (range 500000 1500000)))
#'user/s2
user=> (use '[clojure.set])
nil
user=> (time (def s3 (union s1 s2))) ;; traditional approach
"Elapsed time: 544.154135 msecs"
#'user/s3
user=> (use '[seqspert.hash-set])
nil
user=> (time (def s4 (splice-hash-sets s1 s2))) ;; seqspert replacement
"Elapsed time: 171.302296 msecs"
#'user/s4
user=> (= s3 s4)
true
user=>
</pre>

tree-map:
- inspect

tree-set:
inspect

vector:
- inspect

- vector-to-array / array-to-vector

These two functions use multiple threads and a knowledge of vector
internal structure to copy to/from Object[] more efficiently than the
traditional approach.

If you are performing large vector/array/vector copies then I would
expect these functions to be useful to you.

<pre>
user=> (def v1 (vec (range 5000000)))
#'user/v1
user=> (time (def a1 (into-array Object v1))) ;; traditional approach
"Elapsed time: 603.765253 msecs"
#'user/a1
user=> (use '[seqspert.vector])
nil
user=> (time (def a2 (vector-to-array v1))) ;; seqspert replacement
"Elapsed time: 48.920468 msecs"
#'user/a2
user=> (= (seq a1)(seq a2))
true
user=> (time (def v2 (into [] a1))) ;; traditional approach
"Elapsed time: 83.325507 msecs"
#'user/v2
user=> (time (def v3 (array-to-vector a2))) ;; seqspert replacement
"Elapsed time: 33.902564 msecs"
#'user/v3
user=> (= v2 v3)
true
user=> 
</pre>

- vmap

Traditionally a sequence is vmap-ped into a vector by conj-ing the
result of the application of a function to each element of the
sequence onto the vector.

vmap both expects and produces a vector. It walks the input vector,
applying the function to each element and builds up a vector of
similar dimensions as it goes. If you are performing small
vector->vector operations, vmap should be faster.

<pre>
user=> (def v1 (vec (range 1000000)))
#'user/v1
user=> (time (def v2 (mapv identity v1))) ;; traditional approach
"Elapsed time: 100.226994 msecs"
#'user/v2
user=> (use '[seqspert.vector])
nil
user=> (time (def v3 (vmap identity v1))) ;; seqspert replacement
"Elapsed time: 40.608011 msecs"
#'user/v3
user=> (= v1 v2 v3)
true
user=> 
</pre>

- fjvmap

fjvmap takes advantage of the fact that the underlying representation
of a vector is a tree.

It works in the same way as vmap, except that each branch of the tree
is handed off to a forkjoin pool thus the function is applied in
parallel.

There is no directly comparable traditional clojure function, so I
will compare it to vmap, which we have just seen.

If you are mapping vector/vector using pure functions, then you might
want to try fjvmap.

<pre>
user=> (use '[seqspert.vector])
nil
user=> (def v1 (vec (range 1000000)))
#'user/v1
user=> (time (def v2 (vmap inc v1))) ;; seqspert sequential
"Elapsed time: 74.748379 msecs"
#'user/v2
user=> (time (def v3 (fjvmap inc v1))) ;; seqspert parallel [on a 4 core box]
"Elapsed time: 29.287218 msecs"
#'user/v3
user=> (= v2 v3)
true
user=> 
</pre>

## Disclaimer

Mileage may vary !

All example timings are relative to the box on which this README was
written (4x 4.0ghz) and are not indicative of anything else.

The performance of these functions has been tested on a reasonable
cross-section of machines/jvms, however it is unlikely that this is
exactly the same combination of h/w, s/w and data that constitute your
production platform. ALWAYS test and test again until you are
satisfied that, in your particular usecase, a seqspert function
provides you with a significant performance win before adopting it.

Map/Set splicing functions are still a work in progress - although
nearly complete.

## License

Copyright © 2014 Julian Gosnell

Distributed under the Eclipse Public License, the same as Clojure.
