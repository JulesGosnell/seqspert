package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {

        final ArrayNode leftNode = (ArrayNode) leftValue;
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

        final INode[] leftArray = leftNode.array;
        final Object[] rightArray = rightNode.array;
        final int rightBitmap = rightNode.bitmap;

        final INode[] newArray = new INode[32]; // allocate optimistically...

        int leftDifferences = 0; // can we just return LHS
        int empty = 0;
        int rightIndex = 0;
        for (int i = 0; i < 32; i++) {
            final INode leftSubNode = leftArray[i];
            final boolean haveLeft = leftSubNode != null;
            final boolean haveRight = ((rightBitmap & (1 << i)) != 0);

            if (haveRight) {
                final Object rightSubKey = rightArray[rightIndex++];
                final Object rightSubValue = rightArray[rightIndex++];
                if (haveLeft) {
                    // both sides present - splice them...
                    final INode newSubNode = NodeUtils.splice(shift + 5, counts, null, leftSubNode, rightSubKey, rightSubValue);
                    newArray[i] = newSubNode;
                    if (leftSubNode != newSubNode) leftDifferences++;
                } else {
                    // only rhs present
                    newArray[i] = NodeUtils.promote(shift + 5, rightSubKey, rightSubValue);
                    leftDifferences++;
                }
            } else { // not haveRight
                if (haveLeft) {
                    newArray[i] = leftSubNode;
                } else {
                    // neither lhs or rhs present...
                    empty++;
                }
            }
        }

        return leftDifferences == 0 ? leftNode : new ArrayNode(null, 32 - empty, newArray);
    }
}
