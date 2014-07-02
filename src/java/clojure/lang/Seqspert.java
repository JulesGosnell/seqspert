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

	private static Hack spliceNodes(int shift, INode lNode, INode rNode) {
		int numDuplicates = 0;
		int lBitmap, rBitmap;
		Object[] lArray, rArray;
		boolean lIsBIN = false;
		boolean rIsBIN = false;
		if (lNode instanceof BitmapIndexedNode) {
		    BitmapIndexedNode l = (BitmapIndexedNode)lNode;
		    lBitmap = l.bitmap;
		    lArray = l.array;
		    lIsBIN = true;
		} else if (lNode instanceof ArrayNode) {
		    ArrayNode l = (ArrayNode) lNode;
		    lBitmap = arrayBitmap(l);
		    lArray = l.array;
		} else {
		    HashCollisionNode l = (HashCollisionNode) lNode;
		    lBitmap = arrayCountToBits[l.count];
		    lArray = l.array;
		}

		if (rNode instanceof BitmapIndexedNode) {
		    BitmapIndexedNode r = (BitmapIndexedNode)rNode;
		    rBitmap = r.bitmap;
		    rArray = r.array;
		    rIsBIN = true;
		} else if (rNode instanceof ArrayNode) {
		    ArrayNode r = (ArrayNode) rNode;
		    rBitmap = arrayBitmap(r);
		    rArray = r.array;
		} else {
		    HashCollisionNode r = (HashCollisionNode) rNode;
		    rBitmap = arrayCountToBits[r.count];
		    rArray = r.array;
		}

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
