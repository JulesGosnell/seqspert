package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

interface Splicer {
        
    INode splice(int shift, Counts counts,
                 boolean leftHaveHash, int leftHash,
                 Object leftKey, Object leftValue, boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue);
        
}
