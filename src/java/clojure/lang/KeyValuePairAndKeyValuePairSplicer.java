package clojure.lang;

import static clojure.lang.NodeUtils.hash;
import clojure.lang.PersistentHashMap.INode;

// TODO: untested
class KeyValuePairAndKeyValuePairSplicer implements Splicer {
    public INode splice(int shift, Counts counts, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
    	final int leftHash = hash(leftKey);
    	// TODO: might be more efficient to check for reference equality first...
    	if (leftHash == rightHash) {
    		if (Util.equiv(leftKey, rightKey)) {
    			// duplication
                counts.sameKey++;
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
