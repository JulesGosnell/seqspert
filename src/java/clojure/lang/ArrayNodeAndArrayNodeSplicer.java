package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;


class ArrayNodeAndArrayNodeSplicer implements Splicer {

	public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int _, Object rightKey, Object rightValue) {

		final ArrayNode leftNode = (ArrayNode) leftValue;
		final ArrayNode rightNode = (ArrayNode) rightValue;

		final INode[] leftArray = leftNode.array;
		final INode[] rightArray = rightNode.array;
		final INode[] newArray = new INode[32]; // allocate optimistically...

		int empty = 0;
		int differences = 0;
		for (int i = 0; i < 32; i++) {
			final INode l = leftArray[i];
			final INode r = rightArray[i];
			final boolean lb = l != null;
			final boolean rb = r != null;
			if (lb) {
				if (rb) {
					final INode n = NodeUtils.splice(shift + 5, counts, null, l, 0, null, r);
					if (l != n) differences++;
					newArray[i] = n;
				} else {
					newArray[i] = l;
				}
			} else {
				if (rb) {
					differences++;
					newArray[i] = r;
				} else {
					empty++;
				}
			}
		}

		// now decide whether we need any of the work that we have
		// done - I expect that we do...
		return differences > 0 ? new ArrayNode(null, 32 - empty, newArray) : leftNode;
	}

}
