package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

// TODO: untested
class KeyValuePairAndKeyValuePairSplicer extends AbstractSplicer {
    public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
        // TODO: inline bodies of both createNode() methods and refactor for efficiency - first test being equiv will be slow...
        if (Util.equiv(leftKey, rightKey)) {
            duplications.duplications++;
            // TODO: I think that this is a problem - we should be returning a kvpair NOT a new Node
            throw new UnsupportedOperationException("AARGH! need big code change...");
            //return createNode(shift, leftKey, leftValue);
        } else {
            // collision / no collision
            return NodeUtils.create(shift, leftKey, leftValue, rightHash, rightKey, rightValue);
        }
    }
}