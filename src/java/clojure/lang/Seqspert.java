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

    public static INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHash,
                        Object leftKey, Object leftValue, boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {
        return splicers[(4 * typeInt(leftKey, leftValue)) + typeInt(rightKey, rightValue)].
            splice(shift, counts, leftHaveHash, leftHash, leftKey, leftValue, rightHaveHash, rightHash, rightKey, rightValue);
    }

}
