package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndHashCollisionNodeSplicer implements Splicer {
    
    public INode recurse(int shift,
                         int leftHash, HashCollisionNode leftNode,
                         int rightHash, HashCollisionNode rightNode) {
        
        final int leftBits = PersistentHashMap.mask(leftHash, shift);
        final int rightBits = PersistentHashMap.mask(rightHash, shift);
        return
            (leftBits == rightBits) ?
            // keep recursing down...
            BitmapIndexedNodeUtils.create2(leftBits, null,
                                          recurse(shift + 5, leftHash, leftNode, rightHash, rightNode)) :
            // end recursion
            BitmapIndexedNodeUtils.create(leftBits, null,
                                          leftNode, rightBits, null, rightNode);
    }
    
    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHashCode,
                        Object leftKey, Object leftValue, boolean rightHaveHash, int rightHashCode, Object rightKey, Object rightValue) {
        final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
        final HashCollisionNode rightNode = (HashCollisionNode) rightValue;
        final int leftHash = leftHaveHash ? leftHashCode : leftNode.hash;
        final int rightHash = rightHaveHash ? rightHashCode : rightNode.hash;

        if (leftHash == rightHash) {
                
            final int leftLength = leftNode.count * 2;
            final int rightLength = rightNode.count* 2;
            final Object[] leftArray = leftNode.array;
            final Object[] rightArray = rightNode.array;
            final int oldSameKey = counts.sameKey;

            final Object[] newArray =
                HashCollisionNodeUtils.maybeAddAll(leftArray, leftLength, rightArray, rightLength, counts);
            
            return
                newArray == leftArray ?
                leftNode :
                newArray == rightArray ?
                rightNode :
                new HashCollisionNode(null,
                                      leftHash,
                                      ((leftLength + rightLength) / 2) - (counts.sameKey - oldSameKey),
                                      newArray);
        } else {
            
            // recursively build BINS and shift by 5 until hashes fall
            // into different partitions, then build a BIN with two
            // subNodes...

            // This should use splice, but I want to avoid the
            // overhead of further dynamic dispatch, casting, checking
            // hash equality etc...

            // since hashes are not =, keys cannot be =, so no need to
            // pass Counts...
            return recurse(shift, leftHash, leftNode, rightHash, rightNode);
        }
    }
        
}
