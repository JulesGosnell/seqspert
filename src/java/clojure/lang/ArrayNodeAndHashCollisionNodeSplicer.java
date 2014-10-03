package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndSet;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndHashCollisionNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
    			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {
    	final ArrayNode leftNode = (ArrayNode) leftValue;
    	final HashCollisionNode rightNode  = (HashCollisionNode) rightValue;
	
    	final INode[] leftArray = leftNode.array;
    	final int index = PersistentHashMap.mask(rightHash, shift);
    	final INode subNode = leftArray[index];
    	if (subNode == null) {
    	    return new ArrayNode(null, leftNode.count + 1, cloneAndSet(leftArray, index, rightNode));
	} else {
    	    final INode newNode = NodeUtils.splice(shift, counts, null, subNode, rightHash, null, rightNode);
	    if (subNode == newNode)
		return leftNode;
	    else
		return new ArrayNode(null, leftNode.count + 1, cloneAndSet(leftArray, index, newNode));
	}
    }

}
