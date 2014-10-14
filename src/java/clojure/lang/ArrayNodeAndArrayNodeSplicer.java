package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;


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
        int differences = 0;
        for (int i = 0; i < 32; i++) {
            final INode leftSubNode = leftArray[i];
            final INode rightSubNode = rightArray[i];
            final boolean hasLeft = leftSubNode != null;
            final boolean hasRight = rightSubNode != null;
            if (hasLeft) {
                if (hasRight) {
                    final INode newSubNode = NodeUtils.splice(shift + 5, counts, null, leftSubNode, null, rightSubNode);
                    if (leftSubNode != newSubNode) differences++;
                    newArray[i] = newSubNode;
                } else {
                    newArray[i] = leftSubNode;
                }
            } else {
                if (hasRight) {
                    differences++;
                    newArray[i] = rightSubNode;
                } else {
                    empty++;
                }
            }
        }

        return differences == 0 ? leftNode : new ArrayNode(null, 32 - empty, newArray);
    }

}
