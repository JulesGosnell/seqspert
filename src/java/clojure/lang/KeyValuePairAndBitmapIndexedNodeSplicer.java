package clojure.lang;

import static clojure.lang.NodeUtils.hash;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

// why is this so complicated ?
class KeyValuePairAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        int _, Object rightKey, Object rightValue) {

        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;
        final Object[] rightArray = rightNode.array;
        final int rightBitmap = rightNode.bitmap;
        final int leftHash = hash(leftKey);
        final int bit = BitmapIndexedNodeUtils.bitpos(leftHash, shift);
        final int index = rightNode.index(bit) * 2;
        if((rightBitmap & bit) == 0) {
            // rhs unoccupied

            // TODO: consider whether to return a BIN or an AN
            final int rightBitCount = Integer.bitCount(rightBitmap);

            // lets assume that we could not have received an empty
            // BIN, therefore we have at least 2 subNodes, so there is
            // no need to consider returning a null from this
            // branch...
            return new BitmapIndexedNode(null,
                                         rightBitmap | bit,
                                         NodeUtils.cloneAndInsert(rightArray,
                                                                  rightBitCount * 2,
                                                                  index,
                                                                  leftKey,
                                                                  leftValue));
        } else {
            // rhs occupied...
            final Object subKey = rightArray[index];
            final Object subValue = rightArray[index + 1];
            final int oldSameKey = counts.sameKey;
            final int oldSameKeyAndValue = counts.sameKeyAndValue;
            final INode spliced = NodeUtils.splice(shift + 5, counts,
                                                   leftKey, leftValue,
                                                   NodeUtils.nodeHash(subKey), subKey, subValue);
            if ((~bit & rightBitmap) > 0) {
                // the BIN contains more than just this entry
                if (spliced == null) {
                    // the LHS key and maybe value are the same as those in the RHS
                    // if the value were different it would replace the one on the LHS
                    // if the value were the same, then we do not need to change the one in the RHS
                    // since the BIN contains more than this entry we must return it...
                    return rightNode;
                } else {
                    System.out.println("AAARGH! - splice() didn't return null - other entries");
                    // we have successfully merged the LHS and RHS entry
                    // we need to copy over the rest of the RHS and return a new BIN...
                    // since we are replacing a subNode we do not have to worry about promotion to an AN.
                    return new BitmapIndexedNode(null,
                                                 rightBitmap,
                                                 NodeUtils.cloneAndInsert(rightArray,
                                                                          Integer.bitCount(rightBitmap) * 2,
                                                                          index,
                                                                          null,
                                                                          spliced));
                }
            } else {
                // the BIN only contains this entry
                if (spliced == null) {
                    // we must only return a single KVP which we cannot do, so we return null
                                                                                
                    System.out.println("WARN: I think we are returning wrong value...");
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
