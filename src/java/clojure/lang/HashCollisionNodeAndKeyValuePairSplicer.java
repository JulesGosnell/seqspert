package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndKeyValuePairSplicer extends AbstractSplicer {
public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
    final HashCollisionNode leftNode = (HashCollisionNode) leftValue;
    final int leftHash = leftNode.hash;
    if (rightKey.hashCode() == leftHash) {
	if (rightKey.equals(leftKey)) {
	    //if (rightValue
	    // TODO: overwrite kvp
	    throw new UnsupportedOperationException("NYI");
	    // TODO: provide tests for this Splicer
	    // TODO: clean up existing splicers, sharing more code...
	    // TODO: random test in clojure - runs for hours trying all sorts of stuff
	    // TODO: provide insert as well as append
	    // TODO: provide default splicer that just uses iteration and assoc() ?
	} else {
	    // add new kvp to existing collisions...
	    final int leftCount = leftNode.count;
	    final int newLeftCount = leftCount + 2;
	    final Object[] leftArray = leftNode.array;
	    final Object[] newLeftArray =
		HashCollisionNodeUtils.append(leftArray, leftCount, newLeftCount, rightKey, rightValue);
	    return new HashCollisionNode(null, leftHash, newLeftCount, newLeftArray);
	}
    } else {
	// TODO: I think that we need a new BitmapIndexedNode here...
	throw new UnsupportedOperationException("NYI");
    }
}
}
