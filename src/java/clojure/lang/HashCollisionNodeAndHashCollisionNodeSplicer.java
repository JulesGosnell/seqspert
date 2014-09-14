package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndHashCollisionNodeSplicer extends AbstractSplicer {
	public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
		final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
		final HashCollisionNode rightNode = (HashCollisionNode) rightValue;

	// TODO - inline a few of these
		final int leftLength = leftNode.count * 2;
	final int rightLength = rightNode.count* 2;
	final Object[] leftArray = leftNode.array;
	final Object[] rightArray = rightNode.array;
	final int oldDuplications = duplications.duplications;

	final Object[] newArray = HashCollisionNodeUtils.maybeAddAll(leftArray, leftLength, rightArray, rightLength, duplications);
	// TODO: inline these
	final int newDuplications = (duplications.duplications - oldDuplications);
	final int newLength = leftLength + rightLength - (newDuplications * 2);

		return newArray == leftArray ? leftNode : new HashCollisionNode(null, shift, newLength / 2, newArray);
	}	
}