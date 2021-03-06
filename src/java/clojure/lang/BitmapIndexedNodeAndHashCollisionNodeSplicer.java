package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndHashCollisionNodeSplicer implements Splicer {

    @Override
    public INode splice(int shift, Counts counts, 
                        boolean leftHaveHash, int leftHash, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHashCode, Object rightKey, Object rightValue) {

        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
        final HashCollisionNode rightNode = (HashCollisionNode) rightValue;
        final int rightHash = rightNode.hash;
        final int bit = BitmapIndexedNodeUtils.index(rightHash, shift);
        final int index = leftNode.index(bit);
        final int keyIndex = index * 2;
        final int valueIndex = keyIndex + 1;
        final int leftBitmap = leftNode.bitmap;
        final Object[] leftArray = leftNode.array;
        final int leftBitCount = Integer.bitCount(leftBitmap);
        if((leftBitmap & bit) == 0) {
            // left hand side unoccupied...
            if (leftBitCount == 16) // TODO: probably a bug here
                return new ArrayNode(null,
                                     17,
                                     ArrayNodeUtils.promoteAndSet(shift,
                                                                  leftBitmap,
                                                                  leftArray,
                                                                  ArrayNodeUtils.partition(rightHash, shift),
                                                                  BitmapIndexedNodeUtils.create(ArrayNodeUtils.partition(rightHash, shift + 5), null, rightNode)));
            else
                return new BitmapIndexedNode(null,
                                             leftBitmap | bit,
                                             BitmapIndexedNodeUtils.cloneAndInsertNode(leftArray,
                                                                                       leftBitCount * 2,
                                                                                       keyIndex,
                                                                                       rightNode));
            
        } else {
            // left hand side already occupied...
            final Object subKey = leftArray[keyIndex];
            final Object subVal = leftArray[valueIndex];
            final INode spliced = Seqspert.splice(shift + 5, counts, false, 0, subKey, subVal, false, 0, null, rightNode);
            return (subVal == spliced) ?
		leftNode:
                new BitmapIndexedNode(null,
				      leftBitmap,
                                      BitmapIndexedNodeUtils.
				      cloneAndSetNode(leftArray,
						      keyIndex,
						      (leftBitCount == 15 && spliced instanceof HashCollisionNode) ?
						      BitmapIndexedNodeUtils.create(keyIndex, null, spliced) :
						      spliced
						      )
				      );
        }
    }

}
