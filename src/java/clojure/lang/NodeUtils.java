package clojure.lang;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class NodeUtils {


    //------------------------------------------------------------------------------
    // for ArrayNodes...
	
    public static INode[] cloneAndSetNode(INode[] oldArray, int index, INode node) {
        final INode[] newArray = oldArray.clone();
        newArray[index] = node;
        return newArray;
    }

    public  static INode promote(int shift, Object key, Object value) {
        return (key == null) ? (INode) value : create(shift, key, value);
    }

    public static INode[] promoteAndSet(int shift, int bitmap, Object[] bitIndexedArray, int index, INode newNode) {
        final INode[] newArray = new INode[32];
        final int newShift = shift + 5;
        int j = 0;
        for (int i = 0; i < 32 ; i++) {
            if ((bitmap & (1 << i)) != 0) {
                newArray[i] = promote(newShift, bitIndexedArray[j++], bitIndexedArray[j++]);
            }
        }
        newArray[index] = newNode;
        return newArray;
    }

    //------------------------------------------------------------------------------
    // for BitmapIndexedNodes...
    
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
    
    // TODO: rename
    public static Object[] cloneAndSet(Object[] oldArray, int keyIndex, Object key, Object value) {
        final Object[] newArray = oldArray.clone();
        newArray[keyIndex + 0] = key;
        newArray[keyIndex + 1] = value;
        return newArray;
    }

    // TODO: move to BIN Utils
    public static Object[] cloneAndInsert(Object[] oldArray, int oldLength, int keyIndex, INode node) {
        final Object[] newArray = new Object[oldLength + 2];
        System.arraycopy(oldArray, 0, newArray, 0, keyIndex);
        int newKeyIndex = keyIndex;
        newArray[newKeyIndex++] = null;
        newArray[newKeyIndex++] = node;
        System.arraycopy(oldArray, keyIndex, newArray, newKeyIndex, oldLength - keyIndex);
        return newArray;
    }

    // TODO: move to BIN Utils
    public static Object[] cloneAndInsert(Object[] oldArray, int oldLength,
                                          int keyIndex, Object key, Object value) {
        final Object[] newArray = new Object[oldLength + 2];
        System.arraycopy(oldArray, 0, newArray, 0, keyIndex);
        int newKeyIndex = keyIndex;
        newArray[newKeyIndex++] = key;
        newArray[newKeyIndex++] = value;
        System.arraycopy(oldArray, keyIndex, newArray, newKeyIndex, oldLength - keyIndex);
        return newArray;
    }

    //------------------------------------------------------------------------------
    // TODO: where are we using these ?

    public static int hash(Object key) {
        return PersistentHashMap.hash(key);
    }

    public static INode create(int shift, Object key, Object value) {
        return new BitmapIndexedNode(null,
                                     BitmapIndexedNodeUtils.bitpos(hash(key), shift),
                                     new Object[]{key, value});
    }

    public static INode create(int shift, int hash, Object key, Object value) {
        return new BitmapIndexedNode(null,
                                     BitmapIndexedNodeUtils.bitpos(hash, shift) ,
                                     new Object[]{key, value});
    }
    
    //------------------------------------------------------------------------------
    // dynamic dispatch utils

    static Splicer[] splicers = new Splicer[] {
        new KeyValuePairAndKeyValuePairSplicer(),
        new KeyValuePairAndBitmapIndexedNodeSplicer(),
        new KeyValuePairAndHashCollisionNodeSplicer(),
        new KeyValuePairAndArrayNodeSplicer(),
        new BitmapIndexedNodeAndKeyValuePairSplicer(),
        new BitmapIndexedNodeAndBitmapIndexedNodeSplicer(),
        new BitmapIndexedNodeAndHashCollisionNodeSplicer(),
        new BitmapIndexedNodeAndArrayNodeSplicer(),
        new HashCollisionNodeAndKeyValuePairSplicer(),
        new HashCollisionNodeAndBitmapIndexedNodeSplicer(),
        new HashCollisionNodeAndHashCollisionNodeSplicer(),
        new HashCollisionNodeAndArrayNodeSplicer(),
        new ArrayNodeAndKeyValuePairSplicer(),
        new ArrayNodeAndBitmapIndexedNodeSplicer(),
        new ArrayNodeAndHashCollisionNodeSplicer(),
        new ArrayNodeAndArrayNodeSplicer(),
        null
    };

    static int typeInt(Object key, Object value) {
        return (key != null) ?
            0 :
            (value instanceof BitmapIndexedNode) ?
            1 :
            (value instanceof ArrayNode) ?
            3 :
            2;
    }

    static INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {
        return splicers[(4 * typeInt(leftKey, leftValue)) + typeInt(rightKey, rightValue)].
            splice(shift, counts, leftKey, leftValue, rightKey, rightValue);
    }

    //------------------------------------------------------------------------------
    // TODO: probably move to Counts ?

    public static IFn resolveLeft  = new AFn() {
            @Override public Object invoke(Object key, Object leftValue, Object rightValue) {
                return (Util.equiv(leftValue, rightValue)) ? leftValue : rightValue;
            }};
			
    public static IFn resolveRight = new AFn() {
            @Override public Object invoke(Object key, Object leftValue, Object rightValue) {
                return rightValue; 
            }};

    //------------------------------------------------------------------------------
    // TODO:
    // expose some stuff that is used elsewhere in seqspert - should probably move...
        
    public static INode EMPTY = BitmapIndexedNode.EMPTY;
    
    public static INode assoc(INode node, int shift, int hash, Object key, Object value, Box addedLeaf) {
        return node.assoc(shift, hash, key, value, addedLeaf);
    }
    
    public static int mask(int hash, int shift) {
        return PersistentHashMap.mask(hash, shift);
    }

    public static INode makeArrayNode(int count, INode[] nodes) {
        return new ArrayNode(null, count, nodes);
    }

    public static PersistentHashMap makeHashMap(int count, INode root) {
        return new PersistentHashMap(count, root, false, null);
    }

}
