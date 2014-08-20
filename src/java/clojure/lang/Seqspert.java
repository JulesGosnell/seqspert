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
	
	// aargh - had to cut-n-past this fn because it is private
	static INode createNode(int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
		int key1hash = hash(key1);
		if(key1hash == key2hash)
			return new HashCollisionNode(null, key1hash, 2, new Object[] {key1, val1, key2, val2});
		Box addedLeaf = new Box(null);
		AtomicReference<Thread> edit = new AtomicReference<Thread>();
		return BitmapIndexedNode.EMPTY
			.assoc(edit, shift, key1hash, key1, val1, addedLeaf)
			.assoc(edit, shift, key2hash, key2, val2, addedLeaf);
	}

	public static class Hack {
		private final int numDuplicates;
		private final PersistentHashMap.BitmapIndexedNode node;

		public Hack(int numDuplicates, PersistentHashMap.BitmapIndexedNode node) {
			this.numDuplicates = numDuplicates;
			this.node = node;
		}
		public int getNumDuplicates() {
			return numDuplicates;
		}
		public PersistentHashMap.BitmapIndexedNode getNode() {
			return node;
		}
	}

	private static int arrayBitmap(ArrayNode node) {
		INode[] array = node.array;
		int bitmap = 0;
		for (int i = 0; i < 32; i++)
			bitmap |= (array[i] == null) ? 0 : (1 << i);
		return bitmap;
	}

    private static final int[] arrayCountToBits = {0, 1, 3, 7, 15, 31, 63, 127, 255, 511};

    // this could be prettier and maybe faster is PersistentHashMap
    // was refactored but it is not part of seqspert :-(

    static int nodeTypeInt(INode node) {
    	return (node instanceof ArrayNode) ? 0 : (node instanceof BitmapIndexedNode) ? 1 : 2;
    }
    

	
	static Hack spliceNodes(int lBitmap, Object[] lArray, boolean lIsBIN, int rBitmap, Object[] rArray, boolean rIsBIN, int numDuplicates, int shift) {
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
				oArray[oPosition++] = lIsBIN ? lArray[lPosition++] : null;
				oArray[oPosition++] = lIsBIN ? lArray[lPosition++] : lArray[i];
			} else if (rb && !lb) {
				oArray[oPosition++] = rIsBIN ? rArray[rPosition++] : null;
				oArray[oPosition++] = rIsBIN ? rArray[rPosition++] : rArray[i];
			} else if  (lb && rb) {
				Object lKey = lIsBIN ? lArray[lPosition++] : null;
				Object lVal = lIsBIN ? lArray[lPosition++] : lArray[i];
				Object rKey = rIsBIN ? rArray[rPosition++] : null;
				Object rVal = rIsBIN ? rArray[rPosition++] : rArray[i];
				Object oKey= null;
				Object oVal = null;
				// splice two nodes...
				boolean lIsNode = lVal instanceof INode;
				boolean rIsNode = rVal instanceof INode;
				if (lIsNode && rIsNode) {
					Hack hack = spliceNodes(shift + 5, (INode)lVal, (INode)rVal);
					numDuplicates += hack.numDuplicates;
					oVal = hack.getNode();
				} else if (lIsNode && !rIsNode) {
					// TODO: may cause unnecessary copying ? think... 
					Box addedLeaf = new Box(null);
					oVal = ((INode)lVal).assoc(shift + 5, hash(rKey), rKey, rVal, addedLeaf);
					numDuplicates += (addedLeaf.val == null ? 1 : 0);
				} else if (rIsNode && !lIsNode) {
					// TODO: may cause unnecessary copying ? think... 
					Box addedLeaf = new Box(null);
					oVal = ((INode)rVal).assoc(shift + 5, hash(lKey), lKey, lVal, addedLeaf);
					numDuplicates += (addedLeaf.val == null ? 1 : 0);
				} else {
					if (Util.equiv(lKey, rKey)) {
						oKey= lKey;
						oVal = rVal; // overwrite from right ?
						numDuplicates++;
					} else {
						oVal = createNode(shift + 5, lKey, lVal, hash(rKey), rKey, rVal);
					}
				}
				oArray[oPosition++] = oKey;
				oArray[oPosition++] = oVal;
			}
		}

		return new Hack(numDuplicates, new PersistentHashMap.BitmapIndexedNode(new AtomicReference<Thread>(), oBitmap, oArray));
	}
	
    static interface Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift);
    }
    
    static class ArrayNodeArrayNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
		    final ArrayNode l = (ArrayNode) lNode;
		    final ArrayNode r = (ArrayNode) rNode;
		    return spliceNodes(arrayBitmap(l), l.array, false, arrayBitmap(r), r.array, false, numDuplicates, shift);
    	}
    }
    
    static class ArrayNodeBitmapIndexedNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
    		final ArrayNode l = (ArrayNode) lNode;
		    final BitmapIndexedNode r = (BitmapIndexedNode)rNode;
		    return spliceNodes(arrayBitmap(l), l.array, false, r.bitmap, r.array, true, numDuplicates, shift);
    	}
    }
    
    static class ArrayNodeHashCollisionNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
		    final ArrayNode l = (ArrayNode) lNode;
		    final HashCollisionNode r = (HashCollisionNode) rNode;
		    return spliceNodes(arrayBitmap(l), l.array, false, arrayCountToBits[r.count], r.array, false, numDuplicates, shift);
    	}
    }
    
    static class BitmapIndexedNodeArrayNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
    		final BitmapIndexedNode l = (BitmapIndexedNode)lNode;
		    final ArrayNode r = (ArrayNode) rNode;
		    return spliceNodes(l.bitmap, l.array, true, arrayBitmap(r), r.array, false, numDuplicates, shift);
    	}
    }
    
    static class BitmapIndexedNodeBitmapIndexedNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
		    final BitmapIndexedNode l = (BitmapIndexedNode)lNode;
		    final BitmapIndexedNode r = (BitmapIndexedNode)rNode;
		    return spliceNodes(l.bitmap, l.array, true, r.bitmap, r.array, true, numDuplicates, shift);
    	}
    }

    static class BitmapIndexedNodeHashCollisionNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
		    final BitmapIndexedNode l = (BitmapIndexedNode)lNode;
		    final HashCollisionNode r = (HashCollisionNode) rNode;
		    return spliceNodes(l.bitmap, l.array, true, arrayCountToBits[r.count], r.array, false, numDuplicates, shift);
    	}
    }
    
    static class HashCollisionNodeArrayNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
    		final HashCollisionNode l = (HashCollisionNode) lNode;
		    final ArrayNode r = (ArrayNode) rNode;
		    return spliceNodes(arrayCountToBits[l.count], l.array, false, arrayBitmap(r), r.array, false, numDuplicates, shift);
    	}
    }
    
    static class HashCollisionNodeBitmapIndexedNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
    		final HashCollisionNode l = (HashCollisionNode) lNode;
		    final BitmapIndexedNode r = (BitmapIndexedNode)rNode;
		    return spliceNodes(arrayCountToBits[l.count], l.array, false, r.bitmap, r.array, true, numDuplicates, shift);
    	}
    }
    
    static class HashCollisionNodeHashCollisionNodeSplicer implements Splicer {
    	public Hack splice(INode lNode, INode rNode, int numDuplicates, int shift) {
		    HashCollisionNode l = (HashCollisionNode) lNode;
		    HashCollisionNode r = (HashCollisionNode) rNode;
		    throw new RuntimeException("NYI");
    	}
    }
    
    static Splicer[] splicers = new Splicer[] {
    	new ArrayNodeArrayNodeSplicer(),
    	new ArrayNodeBitmapIndexedNodeSplicer(),
    	new ArrayNodeArrayNodeSplicer(),
    	new BitmapIndexedNodeArrayNodeSplicer(),
    	new BitmapIndexedNodeBitmapIndexedNodeSplicer(),
    	new BitmapIndexedNodeArrayNodeSplicer(),
    	new HashCollisionNodeArrayNodeSplicer(),
    	new HashCollisionNodeBitmapIndexedNodeSplicer(),
    	new HashCollisionNodeHashCollisionNodeSplicer()
    };
    
	static Hack spliceNodes(int shift, INode lNode, INode rNode) {		
		return splicers[(3 * nodeTypeInt(lNode)) + nodeTypeInt(rNode)].splice(lNode, rNode, 0, shift);
	}

	public static PersistentHashMap spliceHashMaps(PersistentHashMap lMap, PersistentHashMap rMap) {
		// check null config the same
		INode lRoot = lMap.root;
		INode rRoot = rMap.root;
		if (lRoot == null)
			return rMap;
		else if (rRoot == null)
			return lMap;

		Hack hack = spliceNodes(0, lRoot, rRoot);
		PersistentHashMap.BitmapIndexedNode root = hack.getNode();
		int count = lMap.count + rMap.count - hack.numDuplicates; 
		return new PersistentHashMap(count, root, lMap.hasNull, lMap.nullValue);
	}
	
	// HashSet
	
	public static PersistentHashSet spliceHashSets(PersistentHashSet lSet, PersistentHashSet rSet) {
		PersistentHashMap meta = PersistentHashMap.EMPTY; // TODO - consider merging METAs
		IPersistentMap impl = spliceHashMaps((PersistentHashMap)lSet.impl, (PersistentHashMap)rSet.impl);
		return new PersistentHashSet(meta, impl);
	}


}
