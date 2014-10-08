package clojure.lang;

import static clojure.lang.NodeUtils.hash;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

// TODO: untested
class KeyValuePairAndBitmapIndexedNodeSplicer implements Splicer {

	public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int _, Object rightKey, Object rightValue) {

		final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;
		final int leftHash = hash(leftKey);
		// TODO
		// the next two lines do not work - splicing two keys with
		// same hashcode end up in two different partitions...
		final int bit = BitmapIndexedNodeUtils.bitpos(leftHash, shift);
		if((rightNode.bitmap & bit) == 0) {
			// no collision - we should just be able to add lhs to rhs
			// TODO - do not use assoc here - reference similar code
			// TODO: need a BIN insert fn.... how is that different from assoc ?
			// TODO: consider whether to return a BIN or an AN
			// TODO: inline logic and lose Box churn ...
			final Box addedLeaf = new Box(null);
			final INode n = rightNode.assoc(shift, leftHash, leftKey, leftValue, addedLeaf);
			counts.sameKey += (addedLeaf.val == null ? 1 : 0);
			return n;
		} else {
			// collision maybe duplication...
			final int idx = rightNode.index(bit);
			final Object k = rightNode.array[idx * 2];
			if (Util.equiv(leftKey, k)) { // TODO - expensive
				// duplication - no change
				counts.sameKey++;
				return rightNode;
			} else {
				// collision...
				final Object v = rightNode.array[(idx * 2) + 1];
				if (leftHash == hash(k))
					return
					    //NodeUtils.create(shift + 5, leftHash, null, new HashCollisionNode(null, leftHash, 2, leftKey, leftValue, k, v));
					    //NodeUtils.create(shift, leftHash, null, new HashCollisionNode(null, leftHash, 2, leftKey, leftValue, k, v));
					    //new HashCollisionNode(null, leftHash, 2, leftKey, leftValue, k, v);
					    new HashCollisionNode(null, leftHash, 2, new Object[]{leftKey, leftValue, k, v});
				else
					return NodeUtils.splice(shift + 5, counts, leftKey, leftValue, NodeUtils.nodeHash(k), k, v);
			}
		}
	}
}
