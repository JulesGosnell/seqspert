package clojure.lang;

import static clojure.lang.PersistentHashMap.hash;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class NodeUtils {

	public static INode create(int shift, Object key, Object value) {
	    return new BitmapIndexedNode(null, BitmapIndexedNodeUtils.bitpos(hash(key), shift), new Object[]{key, value});
	}

	// TODO: consider that BINs may morph into ANs as they increase in size...
	
	static INode assoc(INode left, int shift, int hash, Object rightKey, Object rightValue, Duplications duplications) {
	    // TODO: should be able to inline this and avoid Box allocation...
	    final Box addedLeaf = new Box(null);
	    final INode node = left.assoc(shift, hash, rightKey, rightValue, addedLeaf);
	    duplications.duplications += (addedLeaf.val == null ? 1 : 0);
	    return node;
	}

	// HashMap
	
	// N.B. - this does NOT handle with duplicate keys !!
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

	static INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
		return splicers[(4 * typeInt(leftKey, leftValue)) + typeInt(rightKey, rightValue)].
	            splice(shift, duplications, leftKey, leftValue, rightHash, rightKey, rightValue);
	}

	// HashMap

	// this could be prettier and maybe faster if PersistentHashMap
	// was refactored but it is not part of seqspert :-(
	
	static int typeInt(Object key, Object value) {
		return (key != null) ? 0 : (value instanceof BitmapIndexedNode) ? 1 : (value instanceof ArrayNode) ? 3 : 2;
	}

}
