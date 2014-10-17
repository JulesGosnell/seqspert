(ns clojure.lang.quick-map
  (:import [clojure.lang
            Box
            PersistentHashMap
            PersistentHashMap$BitmapIndexedNode
            PersistentHashMap$INode

            NodeUtils])
  ;;(:require  [clojure.core [reducers :as r]])
  ;;(:require [clojure [pprint :as p]])
  ;;(:use [clojure set])
  )

;;------------------------------------------------------------------------------
;; even though I am in the same package, I do not seem to be able to
;; access static fields and methods in the same package scope - why ?

;; (def empty-node PersistentHashMap$BitmapIndexedNode/EMPTY)

(def empty-node NodeUtils/EMPTY)

;; (defn mask [hash shift]
;;   (PersistentHashMap/mask hash shift))

(defn mask [hash shift]
  (NodeUtils/mask hash shift))

;;;(defn node-assoc [^PersistentHashMap$INode node shift hash ^Object key ^Object value ^Box box]
;;;  (.assoc node ^int shift ^int hash key value box))

(defn node-assoc [^PersistentHashMap$INode node shift hash ^Object key ^Object value ^Box box]
  (NodeUtils/assoc node ^int shift ^int hash key value box))

;;------------------------------------------------------------------------------

;; (def atoms (into-array (repeatedly 32 (fn [] (atom empty-node)))))

(defn ^"[Lclojure.lang.Atom;" create-atoms [n] (into-array (repeatedly n (fn [] (atom empty)))))

(defprotocol ParallelAssoc
  (passoc ^boolean [_ ^Object key ^Object value])
  (persistent [_]))

(defrecord ParallelHashMap [^"[Lclojure.lang.Atom;" atoms]
  ParallelAssoc
  (passoc ^boolean [_ ^Object key ^Object value]
    (let [hc (hash key)
          box (Box. nil)]
      (swap! (aget atoms (mask hc 0)) node-assoc 0 hc key value box)
      ;; (not (identical? box (.val box)))
      ))
  (persistent [_]
    nil))

(def pm (ParallelHashMap. (create-atoms 32)))


;;;(int (Math/ceil (/ 1000 32)))
