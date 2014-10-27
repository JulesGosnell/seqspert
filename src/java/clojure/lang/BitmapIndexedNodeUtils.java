package clojure.lang;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeUtils {
        
	public static INode create(int partition, Object key, Object value) {
	    return new BitmapIndexedNode(null, 1 << partition, new Object[]{key, value});
	}
    
    public static  BitmapIndexedNode create(int index0, Object key0, Object value0, int index1, Object key1, Object value1) {
        return new BitmapIndexedNode(null,
                                     1 << index0 | 1 << index1,
                                     (index0 <= index1) ?
                                     new Object[]{key0, value0, key1, value1} :
                                     new Object[]{key1, value1, key0, value0});
    }

	public static Object[] cloneAndSetNode(Object[] oldArray, int index, INode node) {
	    final Object[] newArray = oldArray.clone();
	    newArray[index - 1] = null; // yeugh - TODO - change to keyIndex
	    newArray[index] = node;
	    return newArray;
	}

	public static Object[] cloneAndSetValue(Object[] oldArray, int valueIndex, Object value) {
	    final Object[] newArray = oldArray.clone();
	    newArray[valueIndex] = value;
	    return newArray;
	}

	public static Object[] cloneAndSetKeyValuePair(Object[] oldArray, int keyIndex, Object key, Object value) {
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
            // TODO: assert that key is not null...
	    return PersistentHashMap.hash(key);
	}

    static int index(int hash, int shift){
        return 1 << ArrayNodeUtils.partition(hash, shift);
    }
        
	public static INode EMPTY = BitmapIndexedNode.EMPTY;
    
}

