package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        int _, Object rightKey, Object rightValue) {

        final ArrayNode leftNode = (ArrayNode) leftValue;
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

        final INode[] newArray = new INode[32]; // allocate optimistically...
        final INode[] leftArray = leftNode.array;
        final Object[] rightArray = rightNode.array;
        final int rightBitmap = rightNode.bitmap;

        int differences = 0;
        int count = 0;
        for (int i = 0, j = 0; i < 32; i++) {
            final INode lv = leftArray[i];
            final boolean haveLeft = lv != null;
            final boolean haveRight = ((rightBitmap & (1 << i)) != 0);

            if (haveRight) {
                count++;
                final Object rk = rightArray[j++];
                final Object rv = rightArray[j++];
                if (haveLeft) {
                    // both sides present - splice them...
                    if (lv !=
                        (newArray[i]
                         = NodeUtils.splice(shift + 5, counts, null, lv, NodeUtils.nodeHash(rk), rk, rv)))
                        differences++;
                } else {
                    // only rhs present
                    newArray[i] = (rk == null) ?
                        (INode) rv :
                        NodeUtils.create(shift + 5, rk, rv);
                    differences++;
                }
            } else { // not haveRight
                if (haveLeft) {
                    count++;
                    newArray[i] = lv;
                } else {
                    // neither lhs or rhs present...
                }
            }
        }

        return differences > 0 ?  new ArrayNode(null, count, newArray) : leftNode;
    }
}
