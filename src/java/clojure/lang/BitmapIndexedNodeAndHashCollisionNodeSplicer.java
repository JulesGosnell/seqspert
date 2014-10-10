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
        final int rightHash = rightNode.hash;
        int bit = BitmapIndexedNodeUtils.bitpos(rightHash, shift);
        final int index = leftNode.index(bit);
        final int keyIndex = index * 2;
        final int valueIndex = keyIndex + 1;
        if((leftNode.bitmap & bit) == 0) {
            // TODO: BIN or AN ?
            System.out.println("[1]HERE!: " + rightHash);
            return new BitmapIndexedNode(null,
                                         leftNode.bitmap | bit,
                                         cloneAndInsert(leftNode.array,
                                                        Integer.bitCount(leftNode.bitmap) * 2,
                                                        keyIndex,
                                                        rightNode));

        } else {
            System.out.println("[2]HERE!: " + rightHash);
            // left hand side already occupied...
            final Object subKey = leftNode.array[keyIndex];
            final Object subVal = leftNode.array[valueIndex];
            return new BitmapIndexedNode(null,
                                         leftNode.bitmap,
                                         NodeUtils.cloneAndSetNode(leftNode.array,
                                                                   valueIndex,
                                                                   NodeUtils.splice(shift + 5, counts,
                                                                                    subKey, subVal,
                                                                                    rightHash, rightKey, rightValue)
                                                                   ));

        }
    }

}
