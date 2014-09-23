package clojure.lang;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;

public class BitmapIndexedNodeUtils {
	
    static int bitpos(int hash, int shift){
	return 1 << PersistentHashMap.mask(hash, shift);
    }

    public static BitmapIndexedNode create(int index, INode node) {
	return new BitmapIndexedNode(null,
				     1 << index,
				     new Object[]{null, node});
    }

    public static  BitmapIndexedNode create(int leftIndex, INode leftNode, int rightIndex, Object rightKey, Object rightValue) {
	return new BitmapIndexedNode(null,
				     1 << leftIndex | 1 << rightIndex,
				     (leftIndex <= rightIndex) ?
				     new Object[]{null, leftNode, rightKey, rightValue} :
				     new Object[]{rightKey, rightValue, null, leftNode});
    }

    public static  BitmapIndexedNode create(int leftIndex, INode leftNode, int rightIndex, INode rightNode) {
	return new BitmapIndexedNode(null,
				     1 << leftIndex | 1 << rightIndex,
				     (leftIndex <= rightIndex) ?
				     new Object[]{null, leftNode, null, rightNode} :
				     new Object[]{null, rightNode, null, leftNode});
    }
    
}

