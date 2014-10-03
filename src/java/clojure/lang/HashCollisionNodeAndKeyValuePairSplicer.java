package clojure.lang;

import static clojure.lang.BitmapIndexedNodeUtils.create;
import static clojure.lang.HashCollisionNodeUtils.maybeAdd;
import static clojure.lang.PersistentHashMap.mask;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndKeyValuePairSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {
	final HashCollisionNode leftNode = (HashCollisionNode) leftValue;
	final int leftHash = leftNode.hash;
	if (rightKey.hashCode() == leftHash) {
	    final int leftCount = leftNode.count;
	    final Object[] leftArray = leftNode.array;
	    final int oldCounts = counts.sameKey;
	    final Object[] newArray = maybeAdd(leftArray, leftCount * 2, rightKey, rightValue, counts);
	    final int newCounts = counts.sameKey - oldCounts;
	    return (leftArray == newArray) ?
		leftNode : new HashCollisionNode(null, leftHash, leftCount + 1 - newCounts, newArray);
	} else {
	    return create(mask(leftHash, shift), leftNode, mask(rightHash, shift), rightKey, rightValue);
	}
    }

}
