package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeUtils {

    public static int getArrayNodePartition(int shift, ArrayNode node) {
        final int length = node.count * 2;
        final Object[] array = node.array;
        for (int i = 0; i < length; i += 2) {
            final Object key = array[i + 0];
            final Object value = array[i + 1];
            if (key != null || value != null)
                return getPartition(shift, key, value);
        }
        throw new UnsupportedOperationException("should never get to here!");
    }

    public static int getBitmapIndexedNodePartition(int shift, BitmapIndexedNode node) {
        return getPartition(shift, node.array[0], node.array[1]);
    }


    public static int getHashCollisionNodePartition(int shift, HashCollisionNode node) {
        return partition(node.hash, shift);
    }

    public static INode[] promoteAndSet(int shift, int bitmap, int hash, Object[] bitIndexedArray, int index, INode newNode) {
        final INode[] newArray = new INode[32];
        final int newShift = shift + 5;
        int j = 0;
        for (int i = 0; i < 32 ; i++) {
            if ((bitmap & (1 << i)) != 0) {
                final Object key = bitIndexedArray[j++];
                final Object value = bitIndexedArray[j++];                
                newArray[i] = promote3(getPartition(newShift, key, value), key, value);
            }
        }
        newArray[index] = newNode;
        return newArray;
    }

    public static int getPartition(int shift, Object key, Object value) {
        return key != null ?
            partition(BitmapIndexedNodeUtils.hash(key), shift) :
            value instanceof BitmapIndexedNode ?
            getBitmapIndexedNodePartition(shift, (BitmapIndexedNode) value) :
            value instanceof ArrayNode ?
            getArrayNodePartition(shift, (ArrayNode) value) :
            getHashCollisionNodePartition(shift, (HashCollisionNode) value);
    }
	
    public static INode[] cloneAndSetNode(INode[] oldArray, int index, INode node) {
        final INode[] newArray = oldArray.clone();
        newArray[index] = node;
        return newArray;
    }
    
    public  static INode promote3(int partition, Object key, Object value) {
        return (key == null) ? (INode) value : BitmapIndexedNodeUtils.create(partition, key, value);
    }
	
    public static INode makeArrayNode(int count, INode[] nodes) {
        return new ArrayNode(null, count, nodes);
    }

    public static int partition(int hash, int shift) {
        return PersistentHashMap.mask(hash, shift);
    }

}
