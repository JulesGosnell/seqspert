package clojure.lang;

import static clojure.lang.PersistentHashMap.hash;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndBitmapIndexedNodeSplicer extends AbstractSplicer {

    public INode splice(int shift, Duplications duplications,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {
    
	final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
	final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

        final int leftBitmap = leftNode.bitmap;
        final Object[] leftArray = leftNode.array;
        final int rightBitmap = rightNode.bitmap;
        final Object[] rightArray = rightNode.array;

	// if neither of our inputs are empty, then we must always return a fresh node...
	// not sure whether this is always the case...
	// what happens is we splice together two empty maps ? TODO
	assert(leftBitmap != 0);
	assert(rightBitmap != 0);

        final int newBitmap = leftBitmap | rightBitmap;
	final int newBitCount = Integer.bitCount(newBitmap);
        final Object[] newArray = new Object[newBitCount * 2]; // nasty - but we need to know...

	// if (newBitCount > 16) {
	//     // output will be an ArrayNode...
	//     // TODO: this is breaking Clojure tests...
	//     throw new RuntimeException("NYI");
	// } else 
	    {
	    // output will be a BitmapIndexedNode or a HashCollisionNode
	    int lPosition = 0;
	    int rPosition = 0;
	    int oPosition = 0;
	    for (int i = 0; i < 32; i++)
		{
		    int mask = 1 << i;
		    boolean lb = ((leftBitmap & mask) != 0);
		    boolean rb = ((rightBitmap & mask) != 0);

		    // maybe we should make this check in splice() ?
		    if (lb) {
			if (rb) {
			    Object lk = leftArray[lPosition++];
			    Object lv = leftArray[lPosition++];
			    Object rk = rightArray[rPosition++];
			    Object rv = rightArray[rPosition++];

			    // TODO: ouch
			    final int rh = (rk == null ? (rv instanceof HashCollisionNode ? ((HashCollisionNode)rv).hash : 0) : hash(rk));
			
			    final INode newNode = NodeUtils.splice(shift + 5, duplications, lk, lv, rh, rk, rv);
			    if (newNode == null) {
				// we must have spliced two leaves giving a result of a single leaf...
				// the key must be unchanged
				newArray[oPosition++] = lk;
				// what is the value ? TODO: ouch - expensive and duplicate computation
				newArray[oPosition++] = Util.equiv(lv, rv) ? lv : rv;
			    } else {
				// result was a Node...
				newArray[oPosition++] = null;
				newArray[oPosition++] = newNode;
			    }
			} else {
			    newArray[oPosition++] = leftArray[lPosition++];
			    newArray[oPosition++] = leftArray[lPosition++];
			}
		    } else {
			if (rb) {
			    newArray[oPosition++] = rightArray[rPosition++];
			    newArray[oPosition++] = rightArray[rPosition++];
			} 
		    }
		}
	    
	    return new PersistentHashMap.BitmapIndexedNode(new AtomicReference<Thread>(), newBitmap, newArray);
	}
    }
    
}
