package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

// TODO: support rightDifferences...

public class ArrayNodeAndArrayNodeSplicer implements Splicer {

    @Override
    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHash, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        final ArrayNode leftNode = (ArrayNode) leftValue;
        final ArrayNode rightNode = (ArrayNode) rightValue;

        final INode[] leftArray = leftNode.array;
        final INode[] rightArray = rightNode.array;
        final INode[] newArray = new INode[32]; // allocate optimistically...
        final int newShift = shift + 5;

        int empty = 0;
        int leftDifferences = 0;
        int rightDifferences = 0;
        for (int i = 0; i < 32; i++) {
            final INode leftSubNode = leftArray[i];
            final INode rightSubNode = rightArray[i];
            final boolean hasLeft = leftSubNode != null;
            final boolean hasRight = rightSubNode != null;
            if (hasLeft) {
                if (hasRight) {
                    final INode newSubNode = Seqspert.splice(newShift, counts, false, 0, null, leftSubNode, rightHaveHash, rightHash, null, rightSubNode);
                    if (leftSubNode != newSubNode) leftDifferences++;
                    if (rightSubNode != newSubNode) rightDifferences++;
                    newArray[i] = newSubNode;
                } else {
                    rightDifferences++;
                    newArray[i] = leftSubNode;
                }
            } else {
                if (hasRight) {
                    leftDifferences++;
                    newArray[i] = (rightSubNode instanceof HashCollisionNode) ?
                    		ArrayNodeUtils.promote(newShift, ((HashCollisionNode)rightSubNode).hash, null, rightSubNode) :
                    		ArrayNodeUtils.promote(newShift, null, rightSubNode);	;
                } else {
                    empty++;
                }
            }
        }

        return
            leftDifferences == 0 ? leftNode :
            rightDifferences == 0 ? rightNode :
            new ArrayNode(null, 32 - empty, newArray);
    }

}
