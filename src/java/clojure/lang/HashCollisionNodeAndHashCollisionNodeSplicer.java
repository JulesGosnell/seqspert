package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

// TODO: support left and right same-ness

class HashCollisionNodeAndHashCollisionNodeSplicer implements Splicer {

    public INode recurse(int shift,
                     int leftHash, HashCollisionNode leftNode,
                     int rightHash, HashCollisionNode rightNode) {
        
        final int leftBits = PersistentHashMap.mask(leftHash, shift);
        final int rightBits = PersistentHashMap.mask(rightHash, shift);
        return
            (leftBits == rightBits) ?
            // keep recursing down...
            BitmapIndexedNodeUtils.create(leftBits, null,
                                          recurse(shift + 5, leftHash, leftNode, rightHash, rightNode)) :
            // end recursion
            BitmapIndexedNodeUtils.create(leftBits, null,
                                          leftNode, rightBits, null, rightNode);
    }
    
    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {
        final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
        final HashCollisionNode rightNode = (HashCollisionNode) rightValue;
        final int leftHash = leftNode.hash;
        final int rightHash = rightNode.hash;

        if (leftHash == rightHash) {
                
            final int leftLength = leftNode.count * 2;
            final int rightLength = rightNode.count* 2;
            final Object[] leftArray = leftNode.array;
            final Object[] rightArray = rightNode.array;
            final int oldSameKey = counts.sameKey;

            // TODO: consider tipping smallest into largest ?
            // strictly speaking, right should come after left...

            // if result of copying is same as left array, return original
            // if result of ...

            final Object[] newArray =
                HashCollisionNodeUtils.maybeAddAll(leftArray, leftLength, rightArray, rightLength, counts);

            final int newSameKey = counts.sameKey - oldSameKey;

            return
                newArray == leftArray ?
                leftNode :
                new HashCollisionNode(null,
                                      leftHash,
                                      ((leftLength + rightLength) / 2) - newSameKey,
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
