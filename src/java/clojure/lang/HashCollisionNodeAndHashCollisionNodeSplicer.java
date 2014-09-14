package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndHashCollisionNodeSplicer extends AbstractSplicer {
	public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
		final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
		final HashCollisionNode rightNode = (HashCollisionNode) rightValue;

		final int leftLength = leftNode.count * 2;
		final int rightLength = rightNode.count* 2;
		final Object[] leftArray = leftNode.array;
		final int oldDuplications = duplications.duplications;

		final Object[] newArray = HashCollisionNodeUtils.maybeAddAll(leftArray, leftLength,
				rightNode.array,
				rightLength,
				duplications);

		return newArray == leftArray ?
				leftNode :
					new HashCollisionNode(null,
							shift,
							(leftLength + rightLength - ((duplications.duplications - oldDuplications) * 2)) / 2,
							newArray);
	}	
}