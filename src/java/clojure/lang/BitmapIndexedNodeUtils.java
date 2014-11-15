package clojure.lang;

import static clojure.lang.BitmapIndexedNodeUtils.create;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeUtils {
        
    public static INode recurse(int shift,
                                int leftHash, Object leftKey, Object leftValue,
                                int rightHash, Object rightKey, Object rightValue) {
        final int leftPartition = ArrayNodeUtils.partition(leftHash, shift);
        final int rightPartition = ArrayNodeUtils.partition(rightHash, shift);
        return leftPartition == rightPartition ?
            create(leftPartition, null,
                   recurse(shift + 5, leftHash, leftKey, leftValue, rightHash, rightKey, rightValue)) :
            create(leftPartition, leftKey, leftValue, rightPartition, rightKey, rightValue);
    }
    
    public static INode create(int partition, Object key, Object value) {
        return new BitmapIndexedNode(null, 1 << partition, new Object[]{key, value});
    }
    
    public static BitmapIndexedNode create(int index0, Object key0, Object value0,
                                           int index1, Object key1, Object value1) {
        return new BitmapIndexedNode(null,
                                     1 << index0 | 1 << index1,
                                     (index0 <= index1) ?
                                     new Object[]{key0, value0, key1, value1} :
                                     new Object[]{key1, value1, key0, value0});
    }
    
    public static Object[] cloneAndSetNode(Object[] oldArray, int keyIndex, INode node) {
        final Object[] newArray = oldArray.clone();
        newArray[keyIndex + 0] = null;
        newArray[keyIndex + 1] = node;
        return newArray;
    }
    
    public static Object[] cloneAndSetValue(Object[] oldArray, int valueIndex, Object value) {
        final Object[] newArray = oldArray.clone();
        newArray[valueIndex] = value;
        return newArray;
    }
    
    public static Object[] cloneAndSetKeyValuePair(Object[] oldArray,
                                                   int keyIndex, Object key, Object value) {
        final Object[] newArray = oldArray.clone();
        newArray[keyIndex + 0] = key;
        newArray[keyIndex + 1] = value;
        return newArray;
    }

    public static Object[] cloneAndInsertNode(Object[] oldArray, int oldLength, int keyIndex, INode node) {
        final Object[] newArray = new Object[oldLength + 2];
        System.arraycopy(oldArray, 0, newArray, 0, keyIndex);
        int newKeyIndex = keyIndex;
        newArray[newKeyIndex++] = null;
        newArray[newKeyIndex++] = node;
        System.arraycopy(oldArray, keyIndex, newArray, newKeyIndex, oldLength - keyIndex);
        return newArray;
    }

    public static Object[] cloneAndInsertKeyValuePair(Object[] oldArray, int oldLength,
                                                      int keyIndex, Object key, Object value) {
        final Object[] newArray = new Object[oldLength + 2];
        System.arraycopy(oldArray, 0, newArray, 0, keyIndex);
        int newKeyIndex = keyIndex;
        newArray[newKeyIndex++] = key;
        newArray[newKeyIndex++] = value;
        System.arraycopy(oldArray, keyIndex, newArray, newKeyIndex, oldLength - keyIndex);
        return newArray;
    }

    public static int hash(Object key) {
        if (key == null)
            throw new IllegalArgumentException("hash() called on null"); // should be assertion
        
        return PersistentHashMap.hash(key);
    }

    public static int hash(boolean haveHash, int hash, Object key) {
        return haveHash ? hash : hash(key);
    }

    static int index(int hash, int shift){
        return 1 << ArrayNodeUtils.partition(hash, shift);
    }
        
    public static INode EMPTY = BitmapIndexedNode.EMPTY;

    // N.B. clojure will promote an unsigned 32-bit int to a long, so we accept a long
    // and cast it back to an int - if the top-bit is set, we just get a negative int...
    public static BitmapIndexedNode makeBitmapIndexedNode(long bitmap, Object[] array) {
        return new BitmapIndexedNode(null, (int)bitmap, array);
    }
}
