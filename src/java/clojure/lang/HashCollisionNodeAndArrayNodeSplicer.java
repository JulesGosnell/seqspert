package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndArrayNodeSplicer implements Splicer {

    @Override
    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHash,
                        Object leftKey, Object leftValue, boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {
        final HashCollisionNode leftNode  = (HashCollisionNode) leftValue;
        final ArrayNode rightNode = (ArrayNode) rightValue;
        
        final INode[] rightArray = rightNode.array;
        final int index = ArrayNodeUtils.partition(leftNode.hash, shift);
        final INode rightSubNode = rightArray[index];
        
        int newCount;
        INode newSubNode;
        if (rightSubNode == null) {
            newCount = rightNode.count + 1;
            newSubNode = leftNode;
        } else {
            newCount = rightNode.count;
            newSubNode = Seqspert.splice(shift + 5, counts, false, 0, null, leftNode, false, 0, null, rightSubNode);
        }

        return new ArrayNode(null, newCount, ArrayNodeUtils.cloneAndSetNode(rightArray, index, newSubNode));
            
    }

}
