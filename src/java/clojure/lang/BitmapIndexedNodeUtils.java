package clojure.lang;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;

public class BitmapIndexedNodeUtils {
	
    static int bitpos(int hash, int shift){
	return 1 << PersistentHashMap.mask(hash, shift);
    }

    public static BitmapIndexedNode create(int bits, INode node) {
	return new BitmapIndexedNode(null,
				     1 << bits,
				     new Object[]{null, node});
    }

    public static  BitmapIndexedNode create(int leftBits, INode leftNode, int rightBits, INode rightNode) {
	return new BitmapIndexedNode(null,
				     1 << leftBits | 1 << rightBits,
				     (leftBits <= rightBits) ?
				     new Object[]{null, leftNode, null, rightNode} :
				     new Object[]{null, rightNode, null, leftNode});
    }
    
}

