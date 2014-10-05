package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndSet;
import static clojure.lang.NodeUtils.create;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndKeyValuePairSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {

        final ArrayNode leftNode = (ArrayNode) leftValue;

	final int index = BitmapIndexedNodeUtils.bitpos(rightHash, shift);
	
	final INode subNode = leftNode.array[index];

	if (subNode == null) {
	    return new ArrayNode(null,
				 leftNode.count + 1,
				 cloneAndSet(leftNode.array, index,
					     create(shift, rightHash, rightKey, rightValue)));
	} else {
	    final INode newNode = splice(shift + 5, counts, null, subNode, rightHash, rightKey, rightValue);
	    return newNode == subNode ? 
		leftNode :
		new ArrayNode(null,
			      leftNode.count,
			      cloneAndSet(leftNode.array, index, newNode));
	}
    }

}
