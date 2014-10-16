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
        final INode rightSubNode = rightArray[index];
        
        int newCount;
        INode newSubNode;
        int rightDifferences = 0;
        if (rightSubNode == null) {
        	newCount = rightNode.count + 1;
        	newSubNode = leftNode;
                rightDifferences++;
        } else {
        	newCount = rightNode.count;
        	newSubNode = NodeUtils.splice(shift + 5, counts, null, leftNode, null, rightSubNode);
                if (rightSubNode != newSubNode) rightDifferences++;
        }

        return rightDifferences == 0 ?
            rightNode :
            new ArrayNode(null, newCount, cloneAndSet(rightArray, index, newSubNode));
            
    }

}
