package clojure.lang;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeUtils {
        
    static int bitpos(int hash, int shift){
        return 1 << PersistentHashMap.mask(hash, shift);
    }

    public static BitmapIndexedNode create(int index, INode node) {
        return new BitmapIndexedNode(null,
                                     1 << index,
                                     new Object[]{null, node});
    }
    
    // TODO: collapse these methods together...
    
    public static  BitmapIndexedNode create(int leftIndex, Object leftKey, Object leftValue) {
        return new BitmapIndexedNode(null,
                                     1 << leftIndex,
                                     new Object[]{leftKey, leftValue});
    }
    
    public static  BitmapIndexedNode create(int leftIndex, Object leftKey, Object leftValue, int rightIndex, Object rightKey, Object rightValue) {
        return new BitmapIndexedNode(null,
                                     1 << leftIndex | 1 << rightIndex,
                                     (leftIndex <= rightIndex) ?
                                     new Object[]{leftKey, leftValue, rightKey, rightValue} :
                                     new Object[]{rightKey, rightValue, leftKey, leftValue});
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

