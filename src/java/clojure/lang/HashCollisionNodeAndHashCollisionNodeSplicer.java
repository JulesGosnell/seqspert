package clojure.lang;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndHashCollisionNodeSplicer extends AbstractSplicer {

    // TODO - think of a good name...
    public INode foo(int shift,
		     int leftHash, HashCollisionNode leftNode,
		     int rightHash, HashCollisionNode rightNode) {
	final AtomicReference<Thread> edit = null;
	
	final int leftBits = PersistentHashMap.mask(leftHash, shift);
	final int rightBits = PersistentHashMap.mask(rightHash, shift);
	return
	    (leftBits == rightBits) ?
	    // keep recursing down...
	    BitmapIndexedNodeUtils.create(leftBits,
					  foo(shift + 5,
					      leftHash, leftNode,
					      rightHash, rightNode)) :
	    // end recursion
	    BitmapIndexedNodeUtils.create(leftBits, leftNode,
					  rightBits, rightNode);
    }
    
    public INode splice(int shift, Duplications duplications,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {
	final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
	final HashCollisionNode rightNode = (HashCollisionNode) rightValue;

	if (leftNode.hash == rightNode.hash) {
		
	    final int leftLength = leftNode.count * 2;
	    final int rightLength = rightNode.count* 2;
	    final Object[] leftArray = leftNode.array;
	    final int oldDuplications = duplications.duplications;

	    final Object[] newArray = HashCollisionNodeUtils.maybeAddAll(leftArray, leftLength,
									 rightNode.array, rightLength,
									 duplications);

	    final int newDuplications = duplications.duplications - oldDuplications;

	    return newArray == leftArray ?
		leftNode :
		new HashCollisionNode(null,
				      leftNode.hash,
				      ((leftLength + rightLength) / 2) - newDuplications,
				      newArray);
	} else {
	    
	    // recursively build BINS and shift by 5 until hashes fall
	    // into different partitions, then build a BIN with two
	    // HCN elements...

	    return foo(shift, leftNode.hash, leftNode, rightNode.hash, rightNode);
	}
    }
	
}
