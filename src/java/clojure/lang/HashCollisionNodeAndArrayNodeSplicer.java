package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndSet;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndArrayNodeSplicer extends AbstractSplicer {

    public INode splice(int shift, Duplications duplications,
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {
	final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
	final ArrayNode rightNode = (ArrayNode) rightValue;
	
	final INode[] rightArray = rightNode.array;
	final int index = PersistentHashMap.mask(rightHash, shift);
	final INode subNode = rightArray[index];
	return (subNode == null) ?
	    new ArrayNode(null, rightNode.count + 1, cloneAndSet(rightArray, index, leftNode)) :
	    NodeUtils.splice(shift, duplications, null, leftNode, 0, null, subNode);
	// TODO: we are passing 0 as rightHash - need to make sure that it is never used...
    }

}
