package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeUtils {

    public static int getArrayNodePartition(int shift, ArrayNode node) {
        final Object[] array = node.array;
        int i = 0;
        while (true) {
            final Object subNode = array[i++];
            if (subNode != null)
                return getPartition(shift, null, subNode);
        }
    }

    public static int getBitmapIndexedNodePartition(int shift, BitmapIndexedNode node) {
        return getPartition(shift, node.array[0], node.array[1]);
    }

    public static int getHashCollisionNodePartition(int shift, HashCollisionNode node) {
        return partition(node.hash, shift);
    }

    public static int getNodePartition(int shift, INode node) {
        return node instanceof BitmapIndexedNode ?
            getBitmapIndexedNodePartition(shift, (BitmapIndexedNode) node) :
            node instanceof ArrayNode ?
            getArrayNodePartition(shift, (ArrayNode) node) :
            getHashCollisionNodePartition(shift, (HashCollisionNode) node);
    }

    public static int getPartition(int shift, Object key, Object value) {
        return key != null ?
            partition(BitmapIndexedNodeUtils.hash(key), shift) :
            getNodePartition(shift, (INode) value);
    }

    // TODO: reorder/rename parameters
    public static INode[] promoteAndSet(int shift, int bitmap, int hash, Object[] bitIndexedArray, int index, INode newNode) {
        final INode[] newArray = new INode[32];
        final int newShift = shift + 5;
        int j = 0;
        for (int i = 0; i < 32 ; i++) {
            if ((bitmap & (1 << i)) != 0) {
                final Object key = bitIndexedArray[j++];
                final Object value = bitIndexedArray[j++];                
                newArray[i] = promote2(newShift, key, value);
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
    
    public  static INode promote2(int shift, Object key, Object value) {
        return (key == null) ?
            (INode) value :
            // unfortunately, we have to ask keys for their hashCodes here...
            BitmapIndexedNodeUtils.create(partition(BitmapIndexedNodeUtils.hash(key), shift), key, value);
    }
	
    public  static INode promote(int partition, Object key, Object value) {
        return (key == null) ? (INode) value : BitmapIndexedNodeUtils.create(partition, key, value);
    }

    public static int partition(int hash, int shift) {
        return PersistentHashMap.mask(hash, shift);
    }

    public static INode makeArrayNode(int count, INode[] nodes) {
        return new ArrayNode(null, count, nodes);
    }

}
