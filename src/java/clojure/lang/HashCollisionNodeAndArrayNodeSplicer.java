package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndSet;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndArrayNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {
        final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
        final ArrayNode rightNode = (ArrayNode) rightValue;
        
        final INode[] rightArray = rightNode.array;
        final int index = PersistentHashMap.mask(leftNode.hash, shift);
        final INode subNode = rightArray[index];
        
        int newCount;
        INode newSubNode;
        if (subNode == null) {
        	newCount = rightNode.count + 1;
        	newSubNode = leftNode;
        } else {
        	newCount = rightNode.count;
        	newSubNode = NodeUtils.splice(shift + 5, counts, null, leftNode, null, subNode);
        }

        return new ArrayNode(null, newCount, cloneAndSet(rightArray, index, newSubNode));
            
    }

}
