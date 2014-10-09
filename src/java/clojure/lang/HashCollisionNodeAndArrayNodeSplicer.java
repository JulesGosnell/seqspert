package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndSet;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndArrayNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        int _, Object rightKey, Object rightValue) {
        final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
        final ArrayNode rightNode = (ArrayNode) rightValue;
        
        final INode[] rightArray = rightNode.array;
        final int index = PersistentHashMap.mask(leftNode.hash, shift);
        final INode subNode = rightArray[index];
        return (subNode == null) ?
            new ArrayNode(null, rightNode.count + 1, cloneAndSet(rightArray, index + 1, leftNode)) :
            NodeUtils.splice(shift, counts, null, leftNode, 0, null, subNode);
    }

}
