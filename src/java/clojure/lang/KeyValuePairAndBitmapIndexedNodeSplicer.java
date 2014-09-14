package clojure.lang;

import static clojure.lang.PersistentHashMap.hash;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

// TODO: untested
class KeyValuePairAndBitmapIndexedNodeSplicer extends AbstractSplicer {
    public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;
        final int bit = BitmapIndexedNodeUtils.bitpos(hash(leftKey), shift);
        if((rightNode.bitmap & bit) == 0) {
            // no collision - we should just be able to add lhs to rhs
            // TODO - do not use assoc here - reference similar code
            // TODO: need a BIN insert fn.... how is that different from assoc ?
            // TODO: consider whether to return a BIN or an AN
            // TODO: inline logic and lose Box churn ...
            return rightNode.assoc(shift + 5, 0, leftKey, leftValue, new Box(null));
        } else {
            // collision maybe duplication...
            final int idx = rightNode.index(bit);
            final Object k = rightNode.array[idx];
            if (Util.equiv(leftKey, k)) { // TODO - expensive
                // duplication - no change
                duplications.duplications++;
                return rightNode;
            } else {
                // collision...
                final Object v = rightNode.array[idx + 1];
                int lhash = hash(leftKey);
                if (lhash == hash(k))
                    return new HashCollisionNode(null, lhash, 2, new Object[]{leftKey, leftValue, k, v});
                else
                    return NodeUtils.splice(shift + 5, duplications, leftKey, leftValue, rightHash, k, v);
            }
        }
    }
}