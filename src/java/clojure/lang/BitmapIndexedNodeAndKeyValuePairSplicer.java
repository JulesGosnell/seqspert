package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndKeyValuePairSplicer implements Splicer {

    @Override
    public INode splice(int shift, Counts counts, 
                        boolean leftHaveHash, int leftHash, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHashCode, Object rightKey, Object rightValue) {
        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;

        final int rightHash = BitmapIndexedNodeUtils.hash(rightHaveHash, rightHashCode, rightKey);
        final int bit = BitmapIndexedNodeUtils.index(rightHash, shift);
        final int index = leftNode.index(bit);
        final int keyIndex = index * 2;
        final int valueIndex = keyIndex + 1;
        final int leftBitmap = leftNode.bitmap;
        final Object[] leftArray = leftNode.array;
        if ((leftBitmap & bit) == 0) {
            final Object resolved = counts.resolver.getResolver().invoke(rightKey, null, rightValue);
            // left hand side unoccupied
            final int leftBitCount = Integer.bitCount(leftBitmap);
            if (leftBitCount == 16)
                return new ArrayNode(null,
                                     17,
                                     ArrayNodeUtils.promoteAndSet(shift, leftBitmap, leftArray,
                                                                  rightHash,
                                                                  rightKey,
                                                                  resolved));
            else
                return new BitmapIndexedNode(null,
                                             leftBitmap | bit,
                                             BitmapIndexedNodeUtils.cloneAndInsertKeyValuePair(leftArray,
                                                                                               leftBitCount * 2,
                                                                                               keyIndex,
                                                                                               rightKey,
                                                                                               resolved));
            
        } else {
            // left hand side already occupied...
            final Object subKey = leftArray[keyIndex];
            final Object subVal = leftArray[valueIndex];

            final INode newSubNode =
                Seqspert.splice(shift + 5, counts, false, 0, subKey, subVal, true, rightHash, rightKey, rightValue);

            if (newSubNode == null) {
                final Object resolved = counts.resolver.getResolver().invoke(subKey, subVal, rightValue);
                return (subVal == resolved) ?
                    leftNode :
                    new BitmapIndexedNode(null,
                                          leftBitmap,
                                          BitmapIndexedNodeUtils.cloneAndSetValue(leftArray, valueIndex, resolved));
            } else {
                return new BitmapIndexedNode(null,
                                             leftBitmap,
                                             BitmapIndexedNodeUtils.cloneAndSetNode(leftArray, keyIndex, newSubNode));
            }
        }
    }

}
