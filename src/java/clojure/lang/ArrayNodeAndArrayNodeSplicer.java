package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

// TODO: support rightDifferences...

class ArrayNodeAndArrayNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {

        final ArrayNode leftNode = (ArrayNode) leftValue;
        final ArrayNode rightNode = (ArrayNode) rightValue;

        final INode[] leftArray = leftNode.array;
        final INode[] rightArray = rightNode.array;
        final INode[] newArray = new INode[32]; // allocate optimistically...

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
                    final INode newSubNode = NodeUtils.splice(shift + 5, counts, null, leftSubNode, null, rightSubNode);
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
                    newArray[i] = rightSubNode;
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
