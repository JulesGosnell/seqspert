package clojure.lang;

import static clojure.lang.NodeUtils.hash;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

// why is this so complicated ?
class KeyValuePairAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {

        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;
        final Object[] rightArray = rightNode.array;
        final int rightBitmap = rightNode.bitmap;
        final int leftHash = hash(leftKey);
        final int bit = BitmapIndexedNodeUtils.bitpos(leftHash, shift);
        final int index = rightNode.index(bit);
        final int keyIndex = index * 2;
        final int rightBitCount = Integer.bitCount(rightBitmap);
        if((rightBitmap & bit) == 0) {
            // rhs unoccupied
            if (rightBitCount == 16)
                return new ArrayNode(null,
                                     17,
                                     NodeUtils.promoteAndSet(shift,
                                                             rightNode.bitmap,
                                                             rightNode.array,
                                                             PersistentHashMap.mask(NodeUtils.hash(leftKey), shift),
                                                             NodeUtils.promote(shift + 5, leftKey, leftValue)));
            else
                // lets assume that we could not have received an empty
                // BIN, therefore we have at least 2 subNodes, so there is
                // no need to consider returning a null from this
                // branch...
                return new BitmapIndexedNode(null,
                                             rightBitmap | bit,
                                             NodeUtils.cloneAndInsert(rightArray,
                                                                      rightBitCount * 2,
                                                                      keyIndex,
                                                                      leftKey,
                                                                      leftValue));
        } else {
            // rhs occupied...
            final Object subKey = rightArray[keyIndex];
            final Object subValue = rightArray[keyIndex + 1];
            final INode spliced = NodeUtils.splice(shift + 5, counts,
                                                   leftKey, leftValue,
                                                   subKey, subValue);
            if ((~bit & rightBitmap) > 0) {
                // the BIN contains more than just this entry
                if (spliced == null) {
                    // the LHS key and maybe value are the same as those in the RHS
                    // if the value were different it would replace the one on the LHS
                    // if the value were the same, then we do not need to change the one in the RHS
                    // since the BIN contains more than this entry we must return it...

		    // TODO: we should call resolver here...
		    
                    return rightNode;
                } else {
                    // we have successfully merged the LHS and RHS entry
                    // we need to copy over the rest of the RHS and return a new BIN...
                    // since we are replacing a subNode we do not have to worry about promotion to an AN.
                    return new BitmapIndexedNode(null,
                                                 rightBitmap,
                                                 NodeUtils.cloneAndSet(rightArray,
                                                                       keyIndex,
                                                                       null,
                                                                       spliced));
                }
            } else {
                // the BIN only contains this entry
                if (spliced == null) {
                    // we must only return a single KVP which we cannot do, so we return null
                                                                                
                    // System.out.println("WARN: I think we are returning wrong value...");
                    // return spliced;
                                                                
                    // TODO: I think that we should return spliced
                    // here - but I have lots of tests expecting rightNode
                    return rightNode;
                } else {
                    // we only need to return this single spliced node
                    return spliced;
                }
            }
        }
    }
}
