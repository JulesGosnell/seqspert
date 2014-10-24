package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndKeyValuePairSplicer implements Splicer {

    public INode splice(int shift, Counts counts, 
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {
        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;

        final int rightHash = NodeUtils.hash(rightKey);
        final int bit = BitmapIndexedNodeUtils.bitpos(rightHash, shift);
        final int index = leftNode.index(bit);
        final int keyIndex = index * 2;
        final int valueIndex = keyIndex + 1;
        final int leftBitmap = leftNode.bitmap;
        final Object[] leftArray = leftNode.array;
        if ((leftBitmap & bit) == 0) {
            // left hand side unoccupied
            final int leftBitCount = Integer.bitCount(leftBitmap);
            if (leftBitCount == 16)
                return new ArrayNode(null,
                                     17,
                                     NodeUtils.promoteAndSet(shift,
                                                             leftBitmap,
                                                             leftArray,
                                                             PersistentHashMap.mask(rightHash, shift),
                                                             NodeUtils.promote(shift + 5, rightKey, rightValue)));
            else
                return new BitmapIndexedNode(null,
                                             leftBitmap | bit,
                                             NodeUtils.cloneAndInsert(leftArray,
                                                                      leftBitCount * 2,
                                                                      keyIndex,
                                                                      rightKey,
                                                                      rightValue));
            
        } else {
            // left hand side already occupied...
            final Object subKey = leftArray[keyIndex];
            final Object subVal = leftArray[valueIndex];
            final INode newSubNode =
                NodeUtils.splice(shift + 5, counts, subKey, subVal, rightKey, rightValue);

            if (newSubNode == null) {
            	final Object resolved = counts.resolveFunction.invoke(subKey, subVal, rightValue);
                return (subVal == resolved) ?
                    leftNode :
                    new BitmapIndexedNode(null,
                                          leftBitmap,
                                          NodeUtils.cloneAndSetValue(leftArray, valueIndex, resolved));
            } else {
                return new BitmapIndexedNode(null,
                                             leftBitmap,
                                             NodeUtils.cloneAndSetNode(leftArray, valueIndex, newSubNode));
            }
        }
    }

}
