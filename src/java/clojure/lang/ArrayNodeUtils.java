package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeUtils {

	public static INode[] promoteAndSet(int shift, int bitmap, int hash, Object[] bitIndexedArray, int index, INode newNode) {
	    final INode[] newArray = new INode[32];
	    final int newShift = shift + 5;
	    int j = 0;
	    for (int i = 0; i < 32 ; i++) {
	        if ((bitmap & (1 << i)) != 0) {
	            newArray[i] = promote3(ArrayNodeUtils.mask(hash, newShift), bitIndexedArray[j++], bitIndexedArray[j++]);
	        }
	    }
	    newArray[index] = newNode;
	    return newArray;
	}
	
	public static INode[] cloneAndSetNode(INode[] oldArray, int index, INode node) {
	    final INode[] newArray = oldArray.clone();
	    newArray[index] = node;
	    return newArray;
	}

	public  static INode promote3(int partition, Object key, Object value) {
	    return (key == null) ? (INode) value : BitmapIndexedNodeUtils.create3(partition, key, value);
	}
	
	public static INode makeArrayNode(int count, INode[] nodes) {
	    return new ArrayNode(null, count, nodes);
	}

	public static int mask(int hash, int shift) {
	    return PersistentHashMap.mask(hash, shift);
	}


}
