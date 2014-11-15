(ns seqspert.set
  (:import [clojure.lang
            PersistentHashSet
            PersistentTreeSet])
  (:require [clojure.set :refer :all])
  (:require [seqspert.core :refer :all])
  (:require [seqspert.hash-set :refer :all]))

;;--------------------------------------------------------------------------------

(defmulti sequential-splice-sets (fn [l r] [(type l)(type r)]))

(defmethod sequential-splice-sets [PersistentHashSet PersistentHashSet]     [l r] (sequential-splice-hash-sets l r))
(defmethod sequential-splice-sets [PersistentHashSet PersistentTreeSet]     [l r] (union l r)); NYI


(defmethod sequential-splice-sets [PersistentTreeSet PersistentHashSet]     [l r] (union l r)); NYI
(defmethod sequential-splice-sets [PersistentTreeSet PersistentTreeSet]     [l r] (union l r)); NYI

;;------------------------------------------------------------------------------

(defmulti parallel-splice-sets (fn [l r] [(type l)(type r)]))

(defmethod parallel-splice-sets [PersistentHashSet PersistentHashSet]     [l r] (parallel-splice-hash-sets l r))
(defmethod parallel-splice-sets [PersistentHashSet PersistentTreeSet]     [l r] (union l r)); NYI

(defmethod parallel-splice-sets [PersistentTreeSet PersistentHashSet]     [l r] (union l r)); NYI
(defmethod parallel-splice-sets [PersistentTreeSet PersistentTreeSet]     [l r] (union l r)); NYI

;;------------------------------------------------------------------------------
