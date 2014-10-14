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

        int differences = 0; // can we just return LHS
        int empty = 0;
        for (int i = 0, j = 0; i < 32; i++) {
            final INode leftSubNode = leftArray[i];
            final boolean haveLeft = leftSubNode != null;
            final boolean haveRight = ((rightBitmap & (1 << i)) != 0);

            if (haveRight) {
                final Object rightSubKey = rightArray[j++];
                final Object rightSubValue = rightArray[j++];
                if (haveLeft) {
                    // both sides present - splice them...
                    final INode newSubNode = NodeUtils.splice(shift + 5, counts, null, leftSubNode, rightSubKey, rightSubValue);
                    newArray[i] = newSubNode;
                    if (leftSubNode != newSubNode) differences++;
                } else {
                    // only rhs present
                    newArray[i] = (rightSubKey == null) ? (INode) rightSubValue : NodeUtils.create(shift + 5, rightSubKey, rightSubValue);
                    differences++;
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

        return differences == 0 ? leftNode : new ArrayNode(null, 32 - empty, newArray);
    }
}
