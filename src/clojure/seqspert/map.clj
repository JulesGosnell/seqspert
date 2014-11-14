(ns seqspert.map
  (:import [clojure.lang
            PersistentArrayMap
            PersistentHashMap
            PersistentStructMap
            PersistentTreeMap])
  (:require [seqspert.core :refer :all])
  (:require [seqspert.hash-map :refer :all]))

;;--------------------------------------------------------------------------------

(defmulti sequential-splice-maps (fn [l r] [(type l)(type r)]))

(defmethod sequential-splice-maps [PersistentArrayMap PersistentArrayMap]   [l r] (merge l r)) ;not worth it
(defmethod sequential-splice-maps [PersistentArrayMap PersistentHashMap]    [l r] (merge l r)) ;not worth it
(defmethod sequential-splice-maps [PersistentArrayMap PersistentStructMap]  [l r] (merge l r)) ;not worth it
(defmethod sequential-splice-maps [PersistentArrayMap PersistentTreeMap]    [l r] (merge l r)) ;not worth it

(defmethod sequential-splice-maps [PersistentHashMap PersistentArrayMap]    [l r] (merge l r)) ;not worth it
(defmethod sequential-splice-maps [PersistentHashMap PersistentHashMap]     [l r] (sequential-splice-hash-maps l r))
(defmethod sequential-splice-maps [PersistentHashMap PersistentTreeMap]     [l r] (merge l r)); NYI
(defmethod sequential-splice-maps [PersistentHashMap PersistentStructMap]   [l r] (merge l r)); NYI

(defmethod sequential-splice-maps [PersistentStructMap PersistentArrayMap]  [l r] (merge l r)); NYI
(defmethod sequential-splice-maps [PersistentStructMap PersistentHashMap]   [l r] (merge l r)); NYI
(defmethod sequential-splice-maps [PersistentStructMap PersistentStructMap] [l r] (merge l r)); NYI
(defmethod sequential-splice-maps [PersistentStructMap PersistentTreeMap]   [l r] (merge l r)); NYI

(defmethod sequential-splice-maps [PersistentTreeMap PersistentArrayMap]    [l r] (merge l r)); NYI
(defmethod sequential-splice-maps [PersistentTreeMap PersistentHashMap]     [l r] (merge l r)); NYI
(defmethod sequential-splice-maps [PersistentTreeMap PersistentStructMap]   [l r] (merge l r)); NYI
(defmethod sequential-splice-maps [PersistentTreeMap PersistentTreeMap]     [l r] (merge l r)); NYI

;;------------------------------------------------------------------------------

(defmulti parallel-splice-maps (fn [l r] [(type l)(type r)]))

(defmethod parallel-splice-maps [PersistentArrayMap PersistentArrayMap]   [l r] (merge l r)) ;not worth it
(defmethod parallel-splice-maps [PersistentArrayMap PersistentHashMap]    [l r] (merge l r)) ;not worth it
(defmethod parallel-splice-maps [PersistentArrayMap PersistentStructMap]  [l r] (merge l r)) ;not worth it
(defmethod parallel-splice-maps [PersistentArrayMap PersistentTreeMap]    [l r] (merge l r)) ;not worth it

(defmethod parallel-splice-maps [PersistentHashMap PersistentArrayMap]    [l r] (merge l r)) ;not worth it
(defmethod parallel-splice-maps [PersistentHashMap PersistentHashMap]     [l r] (parallel-splice-hash-maps l r))
(defmethod parallel-splice-maps [PersistentHashMap PersistentStructMap]   [l r] (merge l r)); NYI
(defmethod parallel-splice-maps [PersistentHashMap PersistentTreeMap]     [l r] (merge l r)); NYI

(defmethod parallel-splice-maps [PersistentStructMap PersistentArrayMap]  [l r] (merge l r)); NYI
(defmethod parallel-splice-maps [PersistentStructMap PersistentHashMap]   [l r] (merge l r)); NYI
(defmethod parallel-splice-maps [PersistentStructMap PersistentStructMap] [l r] (merge l r)); NYI
(defmethod parallel-splice-maps [PersistentStructMap PersistentTreeMap]   [l r] (merge l r)); NYI

(defmethod parallel-splice-maps [PersistentTreeMap PersistentArrayMap]    [l r] (merge l r)); NYI
(defmethod parallel-splice-maps [PersistentTreeMap PersistentHashMap]     [l r] (merge l r)); NYI
(defmethod parallel-splice-maps [PersistentTreeMap PersistentStructMap]   [l r] (merge l r)); NYI
(defmethod parallel-splice-maps [PersistentTreeMap PersistentTreeMap]     [l r] (merge l r)); NYI

;;------------------------------------------------------------------------------
