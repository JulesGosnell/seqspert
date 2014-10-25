package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts, 
			boolean leftHaveHash, int leftHash,
			Object leftKey, Object leftValue, boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        final HashCollisionNode leftNode = (HashCollisionNode) leftValue;
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

        final int bit = BitmapIndexedNodeUtils.bitpos(leftNode.hash, shift);
        final int index = rightNode.index(bit);
        final int keyIndex = index * 2;
        final int rightBitmap = rightNode.bitmap;
        final Object[] rightArray = rightNode.array;
        if((rightBitmap & bit) == 0) {
            // left hand side unoccupied...
            final int rightBitCount = Integer.bitCount(rightBitmap);
            if (rightBitCount == 16)
                return new ArrayNode(null,
                                     17,
                                     ArrayNodeUtils.promoteAndSet(shift,
                                                             rightBitmap,
                                                             rightArray,
                                                             // TODO: already called bitpos - repetition ?
							     PersistentHashMap.mask(leftNode.hash, shift),
                                                             leftNode
                                                             ));
            else
                return new BitmapIndexedNode(null,
                                             rightBitmap | bit,
                                             BitmapIndexedNodeUtils.cloneAndInsert(rightArray,
                                                            rightBitCount * 2,
                                                            keyIndex,
                                                            leftNode));
        } else {
            // same hash partitions
            final Object rightSubKey = rightArray[keyIndex];
            final Object rightSubValue = rightArray[keyIndex + 1];
            final INode newSubNode = Seqspert.splice(shift + 5, 
                                                      counts,
                                                      false,
                                                      0,
                                                      null,
                                                      leftNode, false, 0, rightSubKey, rightSubValue);
            return
            	(~bit & rightBitmap) == 0 ?
                // BIN only had one subNode, now spliced into newSubNode
                newSubNode :
                rightSubValue == newSubNode ?
                rightNode :
                // TODO: is it possible to be leftSame here ?
                // BiN had other subNodes, return union of old and new...
                new BitmapIndexedNode(null,
                                      rightBitmap,
                                      BitmapIndexedNodeUtils.cloneAndSet(rightArray, keyIndex, null, newSubNode));
                }
    }

}
