package clojure.lang;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndBitmapIndexedNodeSplicer implements Splicer {

	public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			Object rightKey, Object rightValue) {

		final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
		final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

		final int leftBitmap = leftNode.bitmap;
		final Object[] leftArray = leftNode.array;
		final int rightBitmap = rightNode.bitmap;
		final Object[] rightArray = rightNode.array;

		final int newBitmap = leftBitmap | rightBitmap;
		final int newBitCount = Integer.bitCount(newBitmap); // nasty - but we need to know...
		final Object[] newBinArray = new Object[newBitCount * 2];
		final INode[] newAnArray = new INode[32];

		final boolean promoted = (newBitCount > 16); // TODO: handle this more efficiently
		int leftIndex = 0;
		int rightIndex = 0;
		int newBinIndex = 0;
		int differences = 0;
		for (int i = 0; i < 32; i++) {
			int mask = 1 << i;
			boolean lb = ((leftBitmap & mask) != 0);
			boolean rb = ((rightBitmap & mask) != 0);

			// maybe we should make this check in splice() ?
			if (lb) {
				if (rb) {
					Object lk = leftArray[leftIndex++];
					Object lv = leftArray[leftIndex++];
					Object rk = rightArray[rightIndex++];
					Object rv = rightArray[rightIndex++];

					// TODO: ouch
					final INode newNode = NodeUtils.splice(shift + 5, counts, lk, lv, rk, rv);
					if (newNode == null) {
						// we must have spliced two leaves giving a result of a single leaf...
						// the key must be unchanged
						newBinArray[newBinIndex++] = lk;
						// what is the value ? TODO: ouch - expensive and duplicate computation
						final boolean same = Util.equiv(lv, rv);
						final Object newValue = same ? lv : rv;
						newBinArray[newBinIndex++] = newValue;
						newAnArray[i] = NodeUtils.create(shift + 5, lk, newValue);
						differences += same ? 0 : 1;
					} else {
						// result was a Node...
						newBinArray[newBinIndex++] = null;
						newBinArray[newBinIndex++] = newNode;
						newAnArray[i] = newNode;
						final boolean same = lv == newNode;
						differences += same ? 0 : 1;
					}
				} else {
					final Object newKey = leftArray[leftIndex++];
					final Object newValue = leftArray[leftIndex++];
					newBinArray[newBinIndex++] = newKey;
					newBinArray[newBinIndex++] = newValue;
					newAnArray[i] = (newKey == null) ? (INode) newValue : NodeUtils.create(shift + 5, newKey, newValue);
				}
			} else {
				if (rb) {
					final Object newKey = rightArray[rightIndex++];
					final Object newValue = rightArray[rightIndex++];
					newBinArray[newBinIndex++] = newKey;
					newBinArray[newBinIndex++] = newValue;
					newAnArray[i] = (newKey == null) ? (INode) newValue : NodeUtils.create(shift + 5, newKey, newValue);
					differences++;
				} 
			}
		}

		return promoted ?
				new PersistentHashMap.ArrayNode(null,newBitCount, newAnArray) :
					differences == 0 ?
							leftNode :
								new PersistentHashMap.BitmapIndexedNode(new AtomicReference<Thread>(), newBitmap, newBinArray);
	}

}
