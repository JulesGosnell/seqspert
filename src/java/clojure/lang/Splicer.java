package clojure.lang;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.Seqspert.Duplications;

interface Splicer {
	
        INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue);
        
    }
