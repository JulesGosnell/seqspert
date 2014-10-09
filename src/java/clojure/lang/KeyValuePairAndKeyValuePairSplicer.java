package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndKeyValuePairSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        int rightHash, Object rightKey, Object rightValue) {
        
        // TODO: expensive - can we pass this down ?
        final int leftHash = NodeUtils.hash(leftKey);
        // TODO: might be more efficient to check for reference equality first...
        if (leftHash == rightHash) {
            if (Util.equiv(leftKey, rightKey)) {
                // duplication
                counts.sameKey++;
                //counts.sameKeyAndValue += Util.equiv(leftValue, rightValue) ? 1 : 0;
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
