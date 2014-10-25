package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndHashCollisionNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHashCode,
                        Object leftKey, Object leftValue, boolean rightHaveHash, int rightHashCode, Object rightKey, Object rightValue) {

        final HashCollisionNode rightNode = (HashCollisionNode) rightValue;

        final int leftHash = leftHaveHash ? leftHashCode : BitmapIndexedNodeUtils.hash(leftKey);
        final int rightHash = rightNode.hash;

        if (leftHash == rightHash) {
            final Object[] rightArray = rightNode.array;
            final int rightLength = rightNode.count * 2;
            final int keyIndex = HashCollisionNodeUtils.keyIndex(rightArray, rightLength, leftKey);
            if (keyIndex == -1) {
                final INode newNode =
                    new HashCollisionNode(null,
                                          rightHash,
                                          rightNode.count + 1,
                                          // since KVP is from LHS, insert at front of HCN
                                          BitmapIndexedNodeUtils.cloneAndInsert(rightArray, rightLength,
                                                                   0, leftKey, leftValue));
                return newNode;
            } else {
                counts.sameKey++;
                if (keyIndex == 0) {
                    return rightNode;
                } else {
                    // strictly speaking the left KVP should be first
                    // in the HCN - not efficient, but then I would
                    // imagine that this does not happen very often.
                    final Object[] newArray = new Object[rightLength];
                    newArray[0] = leftKey;
                    newArray[1] = rightArray[keyIndex + 1];
                    System.arraycopy(rightArray, 0, newArray, 2, keyIndex);
                    System.arraycopy(rightArray, keyIndex + 2, newArray, keyIndex + 2, rightLength - keyIndex - 2);
                    return new HashCollisionNode(null, rightHash, rightNode.count, newArray);
                }
            }
            
        } else {
            return BitmapIndexedNodeUtils
                .create(PersistentHashMap.mask(leftHash, shift), leftKey, leftValue,
                        PersistentHashMap.mask(rightHash, shift), null, rightNode);
        }

    }

}

