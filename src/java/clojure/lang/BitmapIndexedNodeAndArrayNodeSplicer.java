package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndArrayNodeSplicer extends AbstractSplicer {

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
                    array[i] = NodeUtils.splice(shift + 5, duplications, lk, lv, rightHash, null, rv);
                } else {
                    // only lhs present
                    array[i] = (lk == null) ? (INode) lv : NodeUtils.create(shift + 5, lk, lv);
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