package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

/**
 * Seqspert needs access to a few Clojure internals. For the moment, this class seems like a sensible place to put them.
 * 
 * @author jules
 *
 */
public class Seqspert {

    // Vector

    static public PersistentVector createPersistentVector(int cnt, int shift, PersistentVector.Node root, Object[] tail) {
        return new PersistentVector(cnt, shift, root, tail);
    }

    // HashMap

    // this could be prettier and maybe faster if PersistentHashMap
    // was refactored but it is not part of seqspert :-(

    // TODO - create new AtomicRef or not ?
    
    public static PersistentHashMap spliceHashMaps(PersistentHashMap lMap, PersistentHashMap rMap) {
        // check null config the same
        final INode lRoot = lMap.root;
        final INode rRoot = rMap.root;
        if (lRoot == null)
            return rMap;
        else if (rRoot == null)
            return lMap;

        final Counts counts = new Counts(Counts.resolveLeft, 0, 0); // TODO: pass through correct resolveFn here
        final PersistentHashMap.INode root = Seqspert.splice(0, counts, false, 0, null, lRoot, false, 0, null, rRoot);
        final int count = lMap.count + rMap.count - counts.sameKey;
        return new PersistentHashMap(count, root, lMap.hasNull, lMap.nullValue);
    }
        
    // HashSet
        
    public static PersistentHashSet spliceHashSets(PersistentHashSet lSet, PersistentHashSet rSet) {
        final PersistentHashMap meta = PersistentHashMap.EMPTY; // TODO - consider merging METAs
        final IPersistentMap impl = spliceHashMaps((PersistentHashMap)lSet.impl, (PersistentHashMap)rSet.impl);
        return new PersistentHashSet(meta, impl);
    }

    public static PersistentHashMap createPersistentHashMap(int count, INode root) {
        return new PersistentHashMap(count, root, false, null);
    }

    public static PersistentHashMap makeHashMap2(int count, Object root) {
        return new PersistentHashMap(count, (INode)root, false, null);
    }

    public static INode assoc(INode node, int shift, int hash, Object key, Object value, Box addedLeaf) {
        return node.assoc(shift, hash, key, value, addedLeaf);
    }

    public static Splicer keyValuePairAndKeyValuePairSplicer		= new KeyValuePairAndKeyValuePairSplicer();
    public static Splicer keyValuePairAndBitmapIndexedNodeSplicer	= new KeyValuePairAndBitmapIndexedNodeSplicer();
    public static Splicer keyValuePairAndArrayNodeSplicer		= new KeyValuePairAndArrayNodeSplicer();
    public static Splicer keyValuePairAndHashCollisionNodeSplicer	= new KeyValuePairAndHashCollisionNodeSplicer();
    public static Splicer bitmapIndexedNodeAndKeyValuePairSplicer	= new BitmapIndexedNodeAndKeyValuePairSplicer();
    public static Splicer bitmapIndexedNodeAndBitmapIndexedNodeSplicer	= new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
    public static Splicer bitmapIndexedNodeAndArrayNodeSplicer		= new BitmapIndexedNodeAndArrayNodeSplicer();
    public static Splicer bitmapIndexedNodeAndHashCollisionNodeSplicer	= new BitmapIndexedNodeAndHashCollisionNodeSplicer();
    public static Splicer arrayNodeAndKeyValuePairSplicer		= new ArrayNodeAndKeyValuePairSplicer();
    public static Splicer arrayNodeAndBitmapIndexedNodeSplicer		= new ArrayNodeAndBitmapIndexedNodeSplicer();
    public static Splicer arrayNodeAndArrayNodeSplicer			= new ArrayNodeAndArrayNodeSplicer();
    public static Splicer arrayNodeAndHashCollisionNodeSplicer		= new ArrayNodeAndHashCollisionNodeSplicer();
    public static Splicer hashCollisionNodeAndKeyValuePairSplicer	= new HashCollisionNodeAndKeyValuePairSplicer();
    public static Splicer hashCollisionNodeAndBitmapIndexedNodeSplicer	= new HashCollisionNodeAndBitmapIndexedNodeSplicer();
    public static Splicer hashCollisionNodeAndArrayNodeSplicer		= new HashCollisionNodeAndArrayNodeSplicer();
    public static Splicer hashCollisionNodeAndHashCollisionNodeSplicer	= new HashCollisionNodeAndHashCollisionNodeSplicer();

    public static INode splice(int shift, Counts counts,
                               boolean leftHaveHash, int leftHash,
                               Object leftKey, Object leftValue, boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {
        return
	    (leftKey != null ?
	     (rightKey != null ?
	      keyValuePairAndKeyValuePairSplicer :
	      rightValue instanceof BitmapIndexedNode ?
	      keyValuePairAndBitmapIndexedNodeSplicer :
	      rightValue instanceof ArrayNode ?
	      keyValuePairAndArrayNodeSplicer :
	      keyValuePairAndHashCollisionNodeSplicer) :
	     leftValue instanceof BitmapIndexedNode ?
	     (rightKey != null ?
	      bitmapIndexedNodeAndKeyValuePairSplicer :
	      rightValue instanceof BitmapIndexedNode ?
	      bitmapIndexedNodeAndBitmapIndexedNodeSplicer :
	      rightValue instanceof ArrayNode ?
	      bitmapIndexedNodeAndArrayNodeSplicer :
	      bitmapIndexedNodeAndHashCollisionNodeSplicer) :
	     leftValue instanceof ArrayNode ?
	     (rightKey != null ?
	      arrayNodeAndKeyValuePairSplicer :
	      rightValue instanceof BitmapIndexedNode ?
	      arrayNodeAndBitmapIndexedNodeSplicer :
	      rightValue instanceof ArrayNode ?
	      arrayNodeAndArrayNodeSplicer :
	      arrayNodeAndHashCollisionNodeSplicer) :
	     (rightKey != null ?
	      hashCollisionNodeAndKeyValuePairSplicer :
	      rightValue instanceof BitmapIndexedNode ?
	      hashCollisionNodeAndBitmapIndexedNodeSplicer :
	      rightValue instanceof ArrayNode ?
	      hashCollisionNodeAndArrayNodeSplicer :
	      hashCollisionNodeAndHashCollisionNodeSplicer))
	    .splice(shift, counts, leftHaveHash, leftHash, leftKey, leftValue, rightHaveHash, rightHash, rightKey, rightValue);
    }

}
