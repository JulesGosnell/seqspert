package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndHashCollisionNodeSplicer implements Splicer {

    @Override
    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHash, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHashCode, Object rightKey, Object rightValue) {

        final ArrayNode leftNode = (ArrayNode) leftValue;
        final HashCollisionNode rightNode  = (HashCollisionNode) rightValue;

        final INode[] leftArray = leftNode.array;
        final int rightHash = rightNode.hash;
        final int index = ArrayNodeUtils.partition(rightHash, shift);
        final INode leftSubNode = leftArray[index];

        int newCount;
        INode newSubNode;
        int leftDifferences = 0;
        if (leftSubNode == null) {
            newCount = leftNode.count + 1;
            newSubNode = BitmapIndexedNodeUtils.create(ArrayNodeUtils.partition(rightHash, shift + 5), null, rightNode);
            leftDifferences++;
        } else {
            newCount = leftNode.count;
            newSubNode = Seqspert.splice(shift + 5, counts, false, 0, null, leftSubNode, true, rightHash, null, rightNode);
            if (leftSubNode != newSubNode) leftDifferences++;
            
        }

        return leftDifferences == 0 ?
            leftNode :
            new ArrayNode(null, newCount, ArrayNodeUtils.cloneAndSetNode(leftArray, index, newSubNode));
    }

}
