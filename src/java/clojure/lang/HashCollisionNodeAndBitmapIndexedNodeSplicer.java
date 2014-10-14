package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndInsert;
import static clojure.lang.NodeUtils.cloneAndSet;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts, 
			Object leftKey, Object leftValue,
			Object rightKey, Object rightValue) {

        final HashCollisionNode leftNode = (HashCollisionNode) leftValue;
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

        final int bit = BitmapIndexedNodeUtils.bitpos(leftNode.hash, shift);
        final int index = rightNode.index(bit);
        final int keyIndex = index * 2;
        if((rightNode.bitmap & bit) == 0) {
            // left hand side unoccupied...
            final int rightBitCount = Integer.bitCount(rightNode.bitmap);
            if (rightBitCount == 16)
                return new ArrayNode(null,
                                     17,
                                     NodeUtils.promoteAndSet(shift,
                                                             rightNode.bitmap,
                                                             rightNode.array,
							     PersistentHashMap.mask(leftNode.hash, shift),
                                                             //NodeUtils.create(shift + 5, null, leftNode)
                                                             leftNode
                                                             ));
            else
                return new BitmapIndexedNode(null,
                                             rightNode.bitmap | bit,
                                             cloneAndInsert(rightNode.array,
                                                            rightBitCount * 2,
                                                            keyIndex,
                                                            leftNode));
        } else {
            // same hash partitions
            final Object[] rightArray = rightNode.array;
            final Object subKey = rightArray[keyIndex];
            final INode spliced = NodeUtils.splice(shift + 5, 
                                                   counts,
                                                   null,
                                                   leftNode,
                                                   subKey,
                                                   rightArray[keyIndex + 1]);

            if ((~bit & rightNode.bitmap) > 0) {
                // BIN contains other subNodes - return a new BIN containing them
                return new BitmapIndexedNode(null,
                                             rightNode.bitmap,
                                             cloneAndSet(rightNode.array,
                                                         keyIndex,
                                                         null,
                                                         spliced));
            } else {
                // this was the only subNode, now it is spliced into the LHC - return the result
                return spliced;
            }
        }

    }

}
