package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

// why is this so complicated ?
class KeyValuePairAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHashCode, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;
        final Object[] rightArray = rightNode.array;
        final int rightBitmap = rightNode.bitmap;
        final int leftHash = leftHaveHash ? leftHashCode : BitmapIndexedNodeUtils.hash(leftKey);
        final int partition = ArrayNodeUtils.partition(leftHash, shift);
        final int bit = 1 << partition;
        final int index = rightNode.index(bit);
        final int keyIndex = index * 2;
        final int rightBitCount = Integer.bitCount(rightBitmap);
        if ((rightBitmap & bit) == 0) {
            // rhs unoccupied
            if (rightBitCount == 16) {
                return new ArrayNode(null,
                                     17,
                                     ArrayNodeUtils.promoteAndSet(shift, rightNode.bitmap, rightNode.array,
                                                                  leftHash, leftKey, leftValue));
	    }
            else
                // lets assume that we could not have received an empty
                // BIN, therefore we have at least 2 subNodes, so there is
                // no need to consider returning a null from this
                // branch...
                return new BitmapIndexedNode(null,
                                             rightBitmap | bit,
                                             BitmapIndexedNodeUtils.cloneAndInsertKeyValuePair(rightArray,
                                                                                               rightBitCount * 2,
                                                                                               keyIndex,
                                                                                               leftKey,
                                                                                               leftValue));
        } else {
            // rhs occupied...
            final Object subKey = rightArray[keyIndex];
            final Object subValue = rightArray[keyIndex + 1];
            final INode spliced = Seqspert.splice(shift + 5, counts,
						  true, leftHash, leftKey, leftValue,
                                                  false, 0, subKey, subValue);
            if (spliced == subValue || spliced == null) {
                // Either:

                // the splice had no effect, so we can just return the
                // rightNode unchanged.
                
                // Or:

                // leftKey matched a key in this BIN.
           
                // for now we will just assume that the value
                // found associated with this key would replace
                // leftValue - in which case no change needs
                // applying...
                return rightNode;
                                                                
                // TODO: at some point in the future we should
                // call a resolver to give the leftValue a chance
                // to override this assumption...
            } else {
                return ((~bit & rightBitmap) == 0 && spliced instanceof HashCollisionNode) ?
                    // If the BIN on the RHS has only one child and
                    // the result of splicing the LHS KVP into this is
                    // an HCN, then return this directly, taking the
                    // original RHS BIN out of the picture
                    // i.e. promoting it to an HCN...
                    spliced :
                    // we have successfully merged the LHS and RHS entry
                    // we need to copy over the rest of the RHS and return a new BIN...
                    // since we are replacing a subNode we do not have to worry about promotion to an AN.
                    new BitmapIndexedNode(null,
                                          rightBitmap,
                                          BitmapIndexedNodeUtils.cloneAndSetKeyValuePair(rightArray,
                                                                                         keyIndex,
                                                                                         null,
                                                                                         spliced));
            }
        }
    }
}
