package clojure.lang;

import static clojure.lang.NodeUtils.hash;
import static clojure.lang.NodeUtils.nodeHash;
import static clojure.lang.NodeUtils.cloneAndSet;
import static clojure.lang.NodeUtils.create;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndArrayNodeSplicer extends AbstractSplicer {

    public INode splice(int shift, Duplications duplications,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {

        final ArrayNode rightNode = (ArrayNode) rightValue;

	// TODO: can we pass down the hash to avoid work here ?
	final int index = BitmapIndexedNodeUtils.bitpos(hash(leftKey), shift);
	
	final INode subNode = rightNode.array[index];

	if (subNode == null) {
	    return new ArrayNode(null,
				 rightNode.count + 1,
				 cloneAndSet(rightNode.array, index,
					     create(shift, hash(leftKey), leftKey, leftValue)));
	} else {
	    final INode newNode = splice(shift + 5, duplications, leftKey, leftValue, nodeHash(subNode), null, subNode);
	    return newNode == subNode ? 
		rightNode :
		new ArrayNode(null,
			      rightNode.count,
			      cloneAndSet(rightNode.array, index, newNode));
	}
    }

}
