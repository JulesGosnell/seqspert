package clojure.lang;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class NodeUtils {


    //------------------------------------------------------------------------------
    // Array utils:

    // TODO: rationalise the way cloneAndSet and cloneAndInsert work...

    public static INode[] cloneAndSetNode(INode[] oldArray, int index, INode node) {
	final INode[] newArray = oldArray.clone();
	newArray[index - 1] = null; // yeugh
	newArray[index] = node;
	return newArray;
    }

    public static Object[] cloneAndSetNode(Object[] oldArray, int index, INode node) {
	final Object[] newArray = oldArray.clone();
	newArray[index - 1] = null; // yeugh
	newArray[index] = node;
	return newArray;
    }
    
    public static INode[] cloneAndSet(INode[] oldArray, int index, INode node) {
	final INode[] newArray = oldArray.clone();
	newArray[index] = node;
	return newArray;
    }

    public static Object[] cloneAndSet(Object[] oldArray, int index, Object value) {
	final Object[] newArray = oldArray.clone();
	newArray[index] = value;
	return newArray;
    }

    public static Object[] cloneAndInsert(Object[] oldArray, int oldLength, int index, INode node) {
	final Object[] newArray = new Object[oldLength + 2];
	System.arraycopy(oldArray, 0, newArray, 0, index);
	newArray[index + 1] = node;
	System.arraycopy(oldArray, index, newArray, index + 2, oldLength - index);
	return newArray;
    }

    public static Object[] cloneAndInsert(Object[] oldArray, int oldLength, int index, Object key, Object value) {
	final Object[] newArray = new Object[oldLength + 2];
	System.arraycopy(oldArray, 0, newArray, 0, index);
	newArray[index + 0] = key;
	newArray[index + 1] = value;
	System.arraycopy(oldArray, index, newArray, index + 2, oldLength - index);
	return newArray;
    }

    //------------------------------------------------------------------------------

    public static int hash(Object key) {
	return PersistentHashMap.hash(key);
    }

    public static INode create(int shift, Object key, Object value) {
	return new BitmapIndexedNode(null, BitmapIndexedNodeUtils.bitpos(hash(key), shift), new Object[]{key, value});
    }

    public static INode create(int shift, int hash, Object key, Object value) {
	return new BitmapIndexedNode(null,
				     //PersistentHashMap.mask(hash, shift)
				     BitmapIndexedNodeUtils.bitpos(hash, shift)
				     ,
				     new Object[]{key, value});
    }

    public static int nodeHash(Object rk) {
	return (rk == null ? 0 : hash(rk));
    }

    // HashMap
	
    // TODO: this should probably be somewhere else...
    // N.B. - this does NOT handle duplicate keys !!
    public static INode create(int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
	final AtomicReference<Thread> edit = new AtomicReference<Thread>();
	int key1hash = hash(key1);
	int p1 = PersistentHashMap.mask(key1hash, shift);
	int p2 = PersistentHashMap.mask(key2hash, shift);
	int bit1 = 1 << p1;
	int bit2 = 1 << p2;
	int bitmap = bit1 | bit2;
	return new BitmapIndexedNode(edit,
				     bitmap,
				     (bit1 == bit2) ?
				     new Object[]{null,
						  (key1hash == key2hash) ?
						  new HashCollisionNode(null, key1hash, 2, new Object[] {key1, val1, key2, val2}) :
						  create(shift + 5, key1, val1, key2hash, key2, val2), null, null, null, null, null, null} :
				     (p1 <= p2) ?
				     new Object[]{key1, val1, key2, val2, null, null, null, null} :
				     new Object[]{key2, val2, key1, val1, null, null, null, null});
    }
	

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

    static INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {
	return splicers[(4 * typeInt(leftKey, leftValue)) + typeInt(rightKey, rightValue)].
	    splice(shift, counts, leftKey, leftValue, rightHash, rightKey, rightValue);
    }

    // HashMap

    // this could be prettier and maybe faster if PersistentHashMap
    // was refactored but it is not part of seqspert :-(
	
    static int typeInt(Object key, Object value) {
	return (key != null) ? 0 : (value instanceof BitmapIndexedNode) ? 1 : (value instanceof ArrayNode) ? 3 : 2;
    }

    // TODO: move to TestUtils
    // integrate Box with Counts for simpler assoc calls...
    static INode assoc(INode left, int shift, int hash, Object rightKey, Object rightValue, Counts counts) {
	final Box addedLeaf = new Box(null);
	final INode node = left.assoc(shift, hash, rightKey, rightValue, addedLeaf);
	counts.sameKey += (addedLeaf.val == null ? 1 : 0);
	return node;
    }
    
}
