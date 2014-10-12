package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

interface Splicer {
        
    INode splice(int shift, Counts counts,
                 Object leftKey, Object leftValue,
                 Object rightKey, Object rightValue);
        
}
