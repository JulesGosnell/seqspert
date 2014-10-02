package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

interface Splicer {
	
        INode splice(int shift, Counts duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue);
        
    }
