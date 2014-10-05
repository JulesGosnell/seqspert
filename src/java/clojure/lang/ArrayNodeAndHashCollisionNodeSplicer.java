package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndSet;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndHashCollisionNodeSplicer implements Splicer {

	public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int _, Object rightKey, Object rightValue) {

		final ArrayNode leftNode = (ArrayNode) leftValue;
		final HashCollisionNode rightNode  = (HashCollisionNode) rightValue;

		final INode[] leftArray = leftNode.array;
		final int index = PersistentHashMap.mask(rightNode.hash, shift);
		final INode subNode = leftArray[index];
		if (subNode == null) {
			return new ArrayNode(null, leftNode.count + 1, cloneAndSet(leftArray, index, BitmapIndexedNodeUtils.create(0, rightNode)));
		} else {
			final INode newNode = NodeUtils.splice(shift, counts, null, subNode, rightNode.hash, null, rightNode);
			return (subNode == newNode) ?
					leftNode:
						new ArrayNode(null, leftNode.count, cloneAndSet(leftArray, index, newNode));
		}
	}

}

