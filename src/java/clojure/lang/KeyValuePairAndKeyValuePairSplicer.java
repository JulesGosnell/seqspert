package clojure.lang;

import static clojure.lang.PersistentHashMap.hash;

import clojure.lang.PersistentHashMap.INode;

// TODO: untested
class KeyValuePairAndKeyValuePairSplicer extends AbstractSplicer {
    public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
    	final int leftHash = hash(leftKey);
    	// TODO: might be more efficient to check for reference equality first...
    	if (leftHash == rightHash) {
    		if (Util.equiv(leftKey, rightKey)) {
    			// duplication
                duplications.duplications++;
                return null;
    		} else {
    			// collision
    			return HashCollisionNodeUtils.create(leftHash, leftKey, leftValue, rightKey, rightValue);
    		}
    	} else{
            // no collision
            return NodeUtils.create(shift, leftKey, leftValue, rightHash, rightKey, rightValue);
        }
    }
}
