package clojure.lang;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.Seqspert.Duplications;

interface Splicer {
        INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, Object rightKey, Object rightValue);	
    }
