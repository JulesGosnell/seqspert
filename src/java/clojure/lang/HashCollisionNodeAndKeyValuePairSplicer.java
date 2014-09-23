package clojure.lang;

import static clojure.lang.PersistentHashMap.mask;
import static clojure.lang.HashCollisionNodeUtils.maybeAdd;
import static clojure.lang.BitmapIndexedNodeUtils.create;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndKeyValuePairSplicer extends AbstractSplicer {

    public INode splice(int shift, Duplications duplications,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {
	final HashCollisionNode leftNode = (HashCollisionNode) leftValue;
	final int leftHash = leftNode.hash;
	if (rightKey.hashCode() == leftHash) {
	    final int leftCount = leftNode.count;
	    final Object[] leftArray = leftNode.array;
	    final int oldDuplications = duplications.duplications;
	    final Object[] newArray = maybeAdd(leftArray, leftCount * 2, rightKey, rightValue, duplications);
	    final int newDuplications = duplications.duplications - oldDuplications;
	    return (leftArray == newArray) ?
		leftNode : new HashCollisionNode(null, leftHash, leftCount + 1 - newDuplications, newArray);
	} else {
	    return create(mask(leftHash, shift), leftNode, mask(rightHash, shift), rightKey, rightValue);
	}
    }

}
