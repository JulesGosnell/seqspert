package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndHashCollisionNodeSplicer implements Splicer {

	public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int _, Object rightKey, Object rightValue) {

		final HashCollisionNode rightNode = (HashCollisionNode) rightValue;

		final int leftHash = NodeUtils.hash(leftKey);
		final int rightHash = rightNode.hash;


		// TODO - what if kvp is already contained within HCN ?
		if (leftHash == rightHash) {
			final int length = (rightNode.count + 1) * 2;
			final Object[] rarray = rightNode.array;
			final Object[] array = new Object[length];
			array[0] = leftKey;
			array[1] = leftValue;
			int r = 0;
			int j = 2;
			for (int i = 0; i < rightNode.count; i++) {
				final Object rKey = rarray[r++];
				final Object rVal = rarray[r++];
				if (Util.equiv(leftKey, rKey)) {
					// duplication - overwrite lhs k:v pair
					array[1] = rVal;
					counts.sameKey++;
				} else {
					// simple collision
					array[j++] = rKey;
					array[j++] = rVal;
				}
			}
			return BitmapIndexedNodeUtils.create(
					PersistentHashMap.mask(rightNode.hash, shift), null,
					new HashCollisionNode(null, rightNode.hash, j / 2, HashCollisionNodeUtils.trim(array, j)));
		} else {
			return BitmapIndexedNodeUtils.create(
					PersistentHashMap.mask(leftHash, shift), leftKey, leftValue,
					PersistentHashMap.mask(rightHash, shift), null, rightNode);
		}

	}

}
