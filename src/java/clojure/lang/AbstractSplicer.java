package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

abstract class AbstractSplicer implements Splicer {
	
    public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
        throw new UnsupportedOperationException("NYI:" + getClass().getSimpleName() + ".splice()");
    }
    
}