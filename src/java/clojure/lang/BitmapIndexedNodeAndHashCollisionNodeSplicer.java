package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndInsert;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndHashCollisionNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts, 
                        Object leftKey, Object leftValue,
                        int _, Object rightKey, Object rightValue) {

        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
        final HashCollisionNode rightNode = (HashCollisionNode) rightValue;
        int bit = BitmapIndexedNodeUtils.bitpos(rightNode.hash, shift);
        int index = leftNode.index(bit);
        int keyIndex = index * 2;
        int valueIndex = keyIndex + 1;
        if((leftNode.bitmap & bit) == 0) {
            // TODO: BIN or AN ?
            return new BitmapIndexedNode(null,
                                         leftNode.bitmap | bit,
                                         cloneAndInsert(leftNode.array,
                                                        Integer.bitCount(leftNode.bitmap) * 2,
                                                        keyIndex,
                                                        rightNode));

        } else {
            // left hand side already occupied...
            final Object subKey = leftNode.array[keyIndex];
            final Object subVal = leftNode.array[valueIndex];
            return new BitmapIndexedNode(null,
                                         leftNode.bitmap,
                                         NodeUtils.cloneAndSetNode(leftNode.array,
                                                                   valueIndex,
                                                                   NodeUtils.splice(shift + 5, counts,
                                                                                    subKey, subVal,
                                                                                    rightNode.hash, rightKey, rightValue)
                                                                   ));

        }
    }

}
