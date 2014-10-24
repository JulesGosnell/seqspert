package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndInsert;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndHashCollisionNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts, 
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {

        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
        final HashCollisionNode rightNode = (HashCollisionNode) rightValue;
        final int rightHash = rightNode.hash;
        final int bit = BitmapIndexedNodeUtils.bitpos(rightHash, shift);
        final int index = leftNode.index(bit);
        final int keyIndex = index * 2;
        final int valueIndex = keyIndex + 1;
        final int leftBitmap = leftNode.bitmap;
        final Object[] leftArray = leftNode.array;
        if((leftBitmap & bit) == 0) {
            // left hand side unoccupied...
            final int leftBitCount = Integer.bitCount(leftBitmap);
            if (leftBitCount == 16)
                return new ArrayNode(null,
                                     17,
                                     ArrayNodeUtils.promoteAndSet(shift,
                                                             leftBitmap,
                                                             leftArray,
                                                             PersistentHashMap.mask(rightHash, shift),
                                                             NodeUtils.create(shift + 5, null, rightNode)));
            else
                return new BitmapIndexedNode(null,
                                             leftBitmap | bit,
                                             cloneAndInsert(leftArray,
                                                            leftBitCount * 2,
                                                            keyIndex,
                                                            rightNode));
            
        } else {
            // left hand side already occupied...
            final Object subKey = leftArray[keyIndex];
            final Object subVal = leftArray[valueIndex];
            final INode spliced = NodeUtils.splice(shift + 5, counts, subKey, subVal, rightKey, rightValue);
            return (subVal == spliced) ?
            	leftNode :
            	new BitmapIndexedNode(null, leftBitmap,
                                      NodeUtils.cloneAndSetNode(leftArray, valueIndex, spliced));
        }
    }

}
