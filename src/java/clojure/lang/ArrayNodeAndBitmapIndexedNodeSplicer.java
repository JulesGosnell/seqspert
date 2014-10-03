package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {

	final ArrayNode leftNode = (ArrayNode) leftValue;
	final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

	throw new RuntimeException("NYI");

	// // TODO: we may not have to make a new Node here !!
	    
        // // make a new array
        // final INode[] array = new INode[32];

        // // walk through existing l and r nodes, splicing them into array...
        // int count = 0;
        // int lPosition = 0;
        // for (int i = 0; i < 32; i++) {
        //     final int mask = 1 << i;
        //     final boolean lb = ((l.bitmap & mask) != 0);
        //     final INode rv = r.array[i];
        //     final boolean rb = rv != null;

        //     if (lb) {
        //         count++;
        //         final Object lk = l.array[lPosition++];
        //         final Object lv = l.array[lPosition++];

        //         if (rb) {
        //             // both sides present - merge them...
        //             array[i] = NodeUtils.splice(shift + 5, counts, lk, lv, rightHash, null, rv);
        //         } else {
        //             // only lhs present
        //             array[i] = (lk == null) ? (INode) lv : NodeUtils.create(shift + 5, lk, lv);
        //         }
        //     } else { // not lb
        //         if (rb) {
        //             count++;
        //             // only rhs present - copy over
        //             array[i] = rv;

        //         } else {
        //             // do nothing...
        //         }
        //     }
        // }

        // return new ArrayNode(null, count, array);
    }
}
