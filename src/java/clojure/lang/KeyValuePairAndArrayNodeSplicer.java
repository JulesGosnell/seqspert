package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndSet;
import static clojure.lang.NodeUtils.create;
import static clojure.lang.NodeUtils.hash;
import static clojure.lang.NodeUtils.nodeHash;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndArrayNodeSplicer extends AbstractSplicer {

    public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {

        final ArrayNode rightNode = (ArrayNode) rightValue;

	// TODO: can we pass down the hash to avoid work here ?
	final int leftHash = hash(leftKey);
	final int index = PersistentHashMap.mask(leftHash, shift);
	
	final INode subNode = rightNode.array[index];

	if (subNode == null) {
	    return new ArrayNode(null,
				 rightNode.count + 1,
				 cloneAndSet(rightNode.array, index,
					     create(shift, leftHash, leftKey, leftValue)));
	} else {
	    final INode newNode = NodeUtils.splice(shift + 5, counts, leftKey, leftValue, nodeHash(subNode), null, subNode);
	    return newNode == subNode ? 
		rightNode :
		new ArrayNode(null,
			      rightNode.count,
			      cloneAndSet(rightNode.array, index, newNode));
	}
    }

}
