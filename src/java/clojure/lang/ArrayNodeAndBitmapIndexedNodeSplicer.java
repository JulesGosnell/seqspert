package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndBitmapIndexedNodeSplicer implements Splicer {

	public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int _, Object rightKey, Object rightValue) {

		final ArrayNode leftNode = (ArrayNode) leftValue;
		final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

		// TODO: we may not have to make a new Node here !!

		// optimistically, make a new array and keep track of whether
		// it differs from original lhs...
		final INode[] array = new INode[32];
		int differences = 0;

		// walk through existing l and r nodes, splicing them into array...
		final INode[] leftArray = leftNode.array;
		final int rightBitmap = rightNode.bitmap;
		final Object[] rightArray = rightNode.array;
		int count = 0;
		int rPosition = 0;
		for (int i = 0; i < 32; i++) {
			final INode lv = leftArray[i];
			final boolean lb = lv != null;
			final int mask = 1 << i;
			final boolean rb = ((rightBitmap & mask) != 0);

			if (lb) {
				count++;
				if (rb) {
					// both sides present - merge them...
					final Object rk = rightArray[rPosition++];;
					final Object rv = rightArray[rPosition++];;
					final INode newNode = NodeUtils.splice(shift + 5, counts, null, lv, NodeUtils.nodeHash(rk), rk, rv);
					array[i] = newNode;
					if (lv != newNode) differences++;
				} else {
					// only lhs present
					array[i] = lv;
				}
			} else { // not lb
				if (rb) {
					count++;
					// TODO: may force clone...
					// only rhs present - copy over
					final Object rk = rightArray[rPosition++];;
					final Object rv = rightArray[rPosition++];;
					array[i] = rk == null ? (INode) rv : NodeUtils.create(shift + 5, rk, rv);
					differences++;
				} else {
					// do nothing...
				}
			}
		}

		return differences > 0 ?  new ArrayNode(null, count, array) : leftNode;
	}
}
