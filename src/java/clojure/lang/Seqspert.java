package clojure.lang;

import static clojure.lang.PersistentHashMap.hash;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
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

    // N.B. - this does NOT handle with duplicate keys !!
    public static INode createNode(int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
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
                                        createNode(shift + 5, key1, val1, key2hash, key2, val2), null, null, null, null, null, null} :
                        (p1 <= p2) ?
                                new Object[]{key1, val1, key2, val2, null, null, null, null} :
                                new Object[]{key2, val2, key1, val1, null, null, null, null});
    }

   public static INode createNode(int shift, Object key, Object value) {
        return new BitmapIndexedNode(null, bitpos(hash(key), shift), new Object[]{key, value});
    }

    public static class Duplications {
        public int duplications;

        public Duplications(int duplications) {
            this.duplications = duplications;
        }
    }

    // this could be prettier and maybe faster if PersistentHashMap
    // was refactored but it is not part of seqspert :-(

    static int nodeTypeInt(Object key, Object value) {
    	return (key != null) ? 0 : (value instanceof BitmapIndexedNode) ? 1 : (value instanceof ArrayNode) ? 3 : 2;
    }
	
    // TODO: untested
    static class KeyValuePairAndKeyValuePairSplicer extends AbstractSplicer {
        public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
            // TODO: inline bodies of both createNode() methods and refactor for efficiency - first test being equiv will be slow...
            if (Util.equiv(leftKey, rightKey)) {
                duplications.duplications++;
                // TODO: I think that this is a problem - we should be returning a kvpair NOT a new Node
                throw new UnsupportedOperationException("AARGH! need big code change...");
                //return createNode(shift, leftKey, leftValue);
            } else {
                // collision / no collision
                return createNode(shift, leftKey, leftValue, rightHash, rightKey, rightValue);
            }
        }
    }

    // TODO: untested
    static class KeyValuePairAndBitmapIndexedNodeSplicer extends AbstractSplicer {
        public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
            final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;
            final int bit = bitpos(hash(leftKey), shift);
            if((rightNode.bitmap & bit) == 0) {
                // no collision - we should just be able to add lhs to rhs
                // TODO - do not use assoc here - reference similar code
                // TODO: need a BIN insert fn.... how is that different from assoc ?
                // TODO: consider whether to return a BIN or an AN
                // TODO: inline logic and lose Box churn ...
                return rightNode.assoc(shift + 5, 0, leftKey, leftValue, new Box(null));
            } else {
                // collision maybe duplication...
                final int idx = rightNode.index(bit);
                final Object k = rightNode.array[idx];
                if (Util.equiv(leftKey, k)) { // TODO - expensive
                    // duplication - no change
                    duplications.duplications++;
                    return rightNode;
                } else {
                    // collision...
                    final Object v = rightNode.array[idx + 1];
                    int lhash = hash(leftKey);
                    if (lhash == hash(k))
                        return new HashCollisionNode(null, lhash, 2, new Object[]{leftKey, leftValue, k, v});
                    else
                        return spliceNodes(shift + 5, duplications, leftKey, leftValue, rightHash, k, v);
                }
            }
        }
    }

    // TODO: untested
    static class KeyValuePairAndHashCollisionNodeSplicer extends AbstractSplicer {
        public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {

            final HashCollisionNode rightNode = (HashCollisionNode) rightValue;

            final int length = (rightNode.count + 1) * 2;
            final Object[] rarray = rightNode.array;
            final Object[] array = new Object[length];
            array[0] = leftKey;
            array[1] = leftValue;
            int r = 0;
            int j = 2;
            for (int i = 0; i < rightNode.count; i++) {
                final Object rKey = rarray[r++];
                final Object rVal = rarray[r++];
                if (Util.equiv(leftKey, rKey)) {
                    // duplication - overwrite lhs k:v pair
                    array[0] = rKey;
                    array[1] = rVal;
                    duplications.duplications++;
                } else {
                    // simple collision
                    array[j++] = rKey;
                    array[j++] = rVal;
                }
            }
            return new HashCollisionNode(null, rightNode.hash, j / 2, HashCollisionNodeUtils.trim(array, j));
        }
    }

    static class KeyValuePairAndArrayNodeSplicer extends AbstractSplicer {
    }

    // TODO: consider that BINs may morph into ANs as they increase in size...

    static class BitmapIndexedNodeAndKeyValuePairSplicer extends AbstractSplicer {
        // TODO: BIN or AN ?
    }

    static class BitmapIndexedNodeAndArrayNodeSplicer extends AbstractSplicer {

    	public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
    		final BitmapIndexedNode l = (BitmapIndexedNode) leftValue;
		    final ArrayNode r = (ArrayNode) rightValue;

            // make a new array
            final INode[] array = new INode[32];

            // walk through existing l and r nodes, splicing them into array...
            int count = 0;
            int lPosition = 0;
            for (int i = 0; i < 32; i++) {
                final int mask = 1 << i;
                final boolean lb = ((l.bitmap & mask) != 0);
                final INode rv = r.array[i];
                final boolean rb = rv != null;

                if (lb) {
                    count++;
                    final Object lk = l.array[lPosition++];
                    final Object lv = l.array[lPosition++];

                    if (rb) {
                        // both sides present - merge them...
                        array[i] = spliceNodes(shift + 5, duplications, lk, lv, rightHash, null, rv);
                    } else {
                        // only lhs present
                        array[i] = (lk == null) ? (INode) lv : createNode(shift + 5, lk, lv);
                    }
                } else { // not lb
                    if (rb) {
                        count++;
                        // only rhs present - copy over
                        array[i] = rv;

                    } else {
                        // do nothing...
                    }
                }
            }

            return new ArrayNode(null, count, array);
    	}
    }

    static INode assoc(INode left, int shift, int hash, Object rightKey, Object rightValue, Duplications duplications) {
        // TODO: should be able to inline this and avoid Box allocation...
        final Box addedLeaf = new Box(null);
        final INode node = left.assoc(shift, hash, rightKey, rightValue, addedLeaf);
        duplications.duplications += (addedLeaf.val == null ? 1 : 0);
        return node;
    }

    static class BitmapIndexedNodeAndBitmapIndexedNodeSplicer extends AbstractSplicer {
    	public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
            // TODO: BIN or AN ?
		    final BitmapIndexedNode l = (BitmapIndexedNode) leftValue;
		    final BitmapIndexedNode r = (BitmapIndexedNode) rightValue;

            int lBitmap = l.bitmap;
            int rBitmap = r.bitmap;
            Object[] lArray = l.array;
            Object[] rArray = r.array;

            int oBitmap = lBitmap | rBitmap;
            Object[] oArray = new Object[Integer.bitCount(oBitmap) * 2]; // nasty - but we need to know before we start the loop
            int lPosition = 0;
            int rPosition = 0;
            int oPosition = 0;
            for (int i = 0; i < 32; i++)
            {
                int mask = 1 << i;
                boolean lb = ((lBitmap & mask) != 0);
                boolean rb = ((rBitmap & mask) != 0);

                if (lb && !rb) {
                    oArray[oPosition++] = lArray[lPosition++];
                    oArray[oPosition++] = lArray[lPosition++];
                } else if (rb && !lb) {
                    oArray[oPosition++] = rArray[rPosition++];
                    oArray[oPosition++] = rArray[rPosition++];
                } else if  (lb && rb) {
                    Object lk = lArray[lPosition++];
                    Object lv = lArray[lPosition++];
                    Object rk = rArray[rPosition++];
                    Object rv = rArray[rPosition++];
                    Object ok= null;
                    Object ov = null;

                    // splice two nodes...
                    boolean lIsNode = lv instanceof INode;
                    boolean rIsNode = rv instanceof INode;
                    if (lIsNode) {
                        if (rIsNode) {
                            ov = spliceNodes(shift + 5, duplications, null, (INode) lv, rightHash, null, (INode) rv);
                        } else {
                            ov = assoc(((INode) lv), shift + 5, hash(rk), rk, rv, duplications);
                        }
                    } else {
                        if (rIsNode) {
                            // TODO: may cause unnecessary copying ? think...
                            ov = assoc(((INode) rv), shift + 5, hash(lk), lk, lv, duplications);
                        } else {
                            if (Util.equiv(lk, rk)) {
                                ok = lk;
                                ov = rv; // overwrite from right ?
                                duplications.duplications++;
                            } else {
                                // TODO - does not work anymore...
                                final int lkHash = hash(lk);
                                final int rkHash = hash(rk);
                                ov = (lkHash == rkHash) ?
                                        new HashCollisionNode(null, lkHash, 2, new Object[]{lk, lv, rk, rv}) :
                                        createNode(shift + 5, lk, lv, rkHash, rk, rv);
                            }
                        }
                    }

                    oArray[oPosition++] = ok;
                    oArray[oPosition++] = ov;
                }
            }

            return new PersistentHashMap.BitmapIndexedNode(new AtomicReference<Thread>(), oBitmap, oArray);
    	}
    }

    // TODO - create new AtomicRef or not ?

    private static int bitpos(int hash, int shift){
        return 1 << PersistentHashMap.mask(hash, shift);
    }

    static class BitmapIndexedNodeAndHashCollisionNodeSplicer extends AbstractSplicer {
        public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
            final BitmapIndexedNode l = (BitmapIndexedNode) leftValue;
            final HashCollisionNode r = (HashCollisionNode) rightValue;
            int bit = bitpos(r.hash, shift);
            int idx = l.index(bit);
            int kIdx = 2 * idx;
            int vIdx = kIdx + 1;
            if((l.bitmap & bit) == 0) {
                // TODO: BIN or AN ?
                // TODO: extract into BIN-insert()
                // nothing in same slot on left hand side - clone existing node and add right hand side...
                Object[] array = new Object[l.array.length + 2];
                System.arraycopy(l.array, 0, array, 0, kIdx);
                array[vIdx] = r;
                System.arraycopy(l.array, kIdx, array, kIdx + 2, l.array.length - kIdx);
                int bitmap = l.bitmap | bit;
                return new BitmapIndexedNode(null, bitmap, array);
            } else {
                // left hand side already occupied...
                final Object lk = l.array[kIdx];
                final Object lVal = l.array[vIdx];
                final int lHash = hash(lk);
                if (lVal instanceof INode) {
                    // TODO: UNTESTED
                    throw new UnsupportedOperationException("NYI");
                    // could try this:
//                    return assoc(((INode)lVal), shift + 5, r.hash, null, rNode, duplications);
                } else if (lHash == r.hash) {
                    // collision (maybe duplication)
                    // replace lhs with an HCN, and tip rhs into it, checking for duplication...
                    final HashCollisionNode node = (HashCollisionNode)spliceNodes(shift + 5, duplications, lk, lVal, rightHash, null, r);
                    final Object[] array = l.array.clone();
                    array[vIdx] = node;
                    return new BitmapIndexedNode(null, l.bitmap, array);
                } else {
                    // l & r share slot, but not hashcode - we need to drop down another level and merge the two nodes...
                    // we are only dealing with vals on the right, not nodes...
                    // REWRITE
                    final Object[] array = l.array.clone();
                    array[vIdx] = assoc(((INode) rightValue), shift + 5, lHash, l.array[kIdx], l.array[vIdx], duplications); // TODO - why the cast ?
                    return new BitmapIndexedNode(new AtomicReference<Thread>(), l.bitmap, array);
                }
            }
        }
    }

    static class ArrayNodeAndKeyValuePairSplicer extends AbstractSplicer {
    }

    static class ArrayNodeAndArrayNodeSplicer extends AbstractSplicer {
        public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
            final ArrayNode leftNode = (ArrayNode) leftValue;
            final ArrayNode rightNode = (ArrayNode) rightValue;

            final INode[] array = new INode[32];
            int empty = 0;
            for (int i = 0; i < 32; i++) {
                final INode l = leftNode.array[i];
                final INode r = rightNode.array[i];
                final boolean lb = l != null;
                final boolean rb = r != null;
                if (lb) {
                    array[i] = rb ? spliceNodes(shift + 5, duplications, null, l, rightHash, null, r) : l;
                } else {
                    if (rb)
                        array[i] = r;
                    else
                        empty--;
                }
            }

            return new ArrayNode(null, 32 - empty, array);
        }
    }

    static class ArrayNodeAndBitmapIndexedNodeSplicer extends AbstractSplicer {
    }

    static class ArrayNodeAndHashCollisionNodeSplicer extends AbstractSplicer {
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
    
	static INode spliceNodes(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
		return splicers[(4 * nodeTypeInt(leftKey, leftValue)) + nodeTypeInt(rightKey, rightValue)].
                splice(shift, duplications, leftKey, leftValue, rightHash, rightKey, rightValue);
	}

	public static PersistentHashMap spliceHashMaps(PersistentHashMap lMap, PersistentHashMap rMap) {
		// check null config the same
		final INode lRoot = lMap.root;
		final INode rRoot = rMap.root;
		if (lRoot == null)
			return rMap;
		else if (rRoot == null)
			return lMap;

        final Duplications duplications = new Duplications(0);
        final PersistentHashMap.INode root = spliceNodes(0, duplications, null, lRoot, 0, null, rRoot);
		final int count = lMap.count + rMap.count - duplications.duplications;
		return new PersistentHashMap(count, root, lMap.hasNull, lMap.nullValue);
	}
	
	// HashSet
	
	public static PersistentHashSet spliceHashSets(PersistentHashSet lSet, PersistentHashSet rSet) {
		final PersistentHashMap meta = PersistentHashMap.EMPTY; // TODO - consider merging METAs
		final IPersistentMap impl = spliceHashMaps((PersistentHashMap)lSet.impl, (PersistentHashMap)rSet.impl);
		return new PersistentHashSet(meta, impl);
	}


}
