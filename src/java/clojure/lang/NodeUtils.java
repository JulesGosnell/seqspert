package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class NodeUtils {


    //------------------------------------------------------------------------------
    // for ArrayNodes...
	
    

    //------------------------------------------------------------------------------
    // for BitmapIndexedNodes...
    
    

    //------------------------------------------------------------------------------
    // TODO: should we move these to BIN Utils ?

    public static INode create(int shift, Object key, Object value) {
        return new BitmapIndexedNode(null,
                                     BitmapIndexedNodeUtils.bitpos(BitmapIndexedNodeUtils.hash(key), shift),
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

    public static PersistentHashMap makeHashMap(int count, INode root) {
        return new PersistentHashMap(count, root, false, null);
    }

}
