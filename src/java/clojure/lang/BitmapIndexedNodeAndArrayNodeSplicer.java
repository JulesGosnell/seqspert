package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndArrayNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHash,
                        Object leftKey, Object leftValue, boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
        final ArrayNode rightNode = (ArrayNode) rightValue;

        final Object[] leftArray = leftNode.array;
        final INode[] rightArray = rightNode.array;

        final INode[] newArray = new INode[32];

        int rightDifferences = 0;
        int empty = 0;
        int leftIndex = 0;
        for (int i = 0; i < 32; i++) {
            final int mask = 1 << i;
            final boolean hasLeft = ((leftNode.bitmap & mask) != 0);
            final INode rightSubNode = rightArray[i];
            final boolean hasRight = rightSubNode != null;

            if (hasLeft) {
                final Object leftSubKey = leftArray[leftIndex++];
                final Object leftSubValue = leftArray[leftIndex++];

                if (hasRight) {
                    // both sides present - merge them...
                    final INode newSubNode = Seqspert.splice(shift + 5, counts, false, 0, leftSubKey, leftSubValue, false, 0, null, rightSubNode);
                    newArray[i] = newSubNode;
                    rightDifferences += (newSubNode == rightSubNode) ? 0 : 1;
                } else {
                    newArray[i] = ArrayNodeUtils.promote(shift + 5, leftSubKey, leftSubValue);
                    rightDifferences++;
                }
            } else { // not lb
                if (hasRight) {
                    // only rhs present - copy over
                    newArray[i] = rightSubNode;
                } else {
                    // do nothing...
                    empty++;
                }
            }
        }

        return rightDifferences == 0 ? rightNode : new ArrayNode(null, 32 - empty, newArray);
    }
}
