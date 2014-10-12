package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndHashCollisionNodeSplicer implements Splicer {

    // TODO - think of a good name...
    public INode foo(int shift,
                     int leftHash, HashCollisionNode leftNode,
                     int rightHash, HashCollisionNode rightNode) {
        
        final int leftBits = PersistentHashMap.mask(leftHash, shift);
        final int rightBits = PersistentHashMap.mask(rightHash, shift);
        return
            (leftBits == rightBits) ?
            // keep recursing down...
            BitmapIndexedNodeUtils.create(leftBits,
                                          null, foo(shift + 5,
                                              leftHash, leftNode,
                                              rightHash, rightNode)) :
            // end recursion
            BitmapIndexedNodeUtils.create(leftBits, null,
                                          leftNode, rightBits, null, rightNode);
    }
    
    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {
        final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
        final HashCollisionNode rightNode = (HashCollisionNode) rightValue;

        if (leftNode.hash == rightNode.hash) {
                
            final int leftLength = leftNode.count * 2;
            final int rightLength = rightNode.count* 2;
            final Object[] leftArray = leftNode.array;
            final int oldCounts = counts.sameKey;

            final Object[] newArray = HashCollisionNodeUtils.maybeAddAll(leftArray, leftLength,
                                                                         rightNode.array, rightLength,
                                                                         counts);

            final int newCounts = counts.sameKey - oldCounts;

            return newArray == leftArray ?
                leftNode :
                new HashCollisionNode(null,
                                      leftNode.hash,
                                      ((leftLength + rightLength) / 2) - newCounts,
                                      newArray);
        } else {
            
            // recursively build BINS and shift by 5 until hashes fall
            // into different partitions, then build a BIN with two
            // HCN elements...

            return foo(shift, leftNode.hash, leftNode, rightNode.hash, rightNode);
        }
    }
        
}
