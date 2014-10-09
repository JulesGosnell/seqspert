package clojure.lang;

import static clojure.lang.NodeUtils.nodeHash;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndBitmapIndexedNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        int _, Object rightKey, Object rightValue) {

        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

        final int leftBitmap = leftNode.bitmap;
        final Object[] leftArray = leftNode.array;
        final int rightBitmap = rightNode.bitmap;
        final Object[] rightArray = rightNode.array;

        final int newBitmap = leftBitmap | rightBitmap;
        final int newBitCount = Integer.bitCount(newBitmap); // nasty - but we need to know...
        final Object[] newArray = new Object[newBitCount * 2];

        // if (newBitCount > 16) {
        //     // output will be an ArrayNode...
        //     // TODO: this is breaking Clojure tests...
        //     throw new RuntimeException("NYI");
        // } else 
        {
            // output will be a BitmapIndexedNode or a HashCollisionNode
            int lPosition = 0;
            int rPosition = 0;
            int oPosition = 0;
            int differences = 0;
            for (int i = 0; i < 32; i++) {
                int mask = 1 << i;
                boolean lb = ((leftBitmap & mask) != 0);
                boolean rb = ((rightBitmap & mask) != 0);

                // maybe we should make this check in splice() ?
                if (lb) {
                    if (rb) {
                        Object lk = leftArray[lPosition++];
                        Object lv = leftArray[lPosition++];
                        Object rk = rightArray[rPosition++];
                        Object rv = rightArray[rPosition++];

                        // TODO: ouch
                        final INode newNode = NodeUtils.splice(shift + 5, counts, lk, lv, nodeHash(rk), rk, rv);
                        if (newNode == null) {
                            // we must have spliced two leaves giving a result of a single leaf...
                            // the key must be unchanged
                            newArray[oPosition++] = lk;
                            // what is the value ? TODO: ouch - expensive and duplicate computation
                            final boolean same = Util.equiv(lv, rv);
                            newArray[oPosition++] = same ? lv : rv;
                            differences += same ? 0 : 1;
                        } else {
                            // result was a Node...
                            newArray[oPosition++] = null;
                            newArray[oPosition++] = newNode;
                            final boolean same = lv == newNode;
                            differences += same ? 0 : 1;
                        }
                    } else {
                        newArray[oPosition++] = leftArray[lPosition++];
                        newArray[oPosition++] = leftArray[lPosition++];
                    }
                } else {
                    if (rb) {
                        newArray[oPosition++] = rightArray[rPosition++];
                        newArray[oPosition++] = rightArray[rPosition++];
                        differences++;
                    } 
                }
            }

            return differences == 0 ?
                leftNode :
                new PersistentHashMap.BitmapIndexedNode(new AtomicReference<Thread>(), newBitmap, newArray);
        }
    }

}
