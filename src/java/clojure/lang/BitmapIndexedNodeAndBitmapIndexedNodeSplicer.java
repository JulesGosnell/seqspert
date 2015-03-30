package clojure.lang;

import static clojure.lang.ArrayNodeUtils.partition;
import static clojure.lang.ArrayNodeUtils.promote;
import static clojure.lang.BitmapIndexedNodeUtils.create;
import static clojure.lang.BitmapIndexedNodeUtils.hash;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndBitmapIndexedNodeSplicer implements Splicer {

    @Override
    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHash, Object leftKey, Object leftValue, 
                        boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

        final int leftBitmap = leftNode.bitmap;
        final Object[] leftArray = leftNode.array;
        final int rightBitmap = rightNode.bitmap;
        final Object[] rightArray = rightNode.array;

        final int newBitmap = leftBitmap | rightBitmap;
        final int newBitCount = Integer.bitCount(newBitmap); // possibly expensive - but we need to know...
        final int newShift = shift + 5;

        int leftIndex = 0;
        int rightIndex = 0;
        // N.B. these two alternates were a single body with a number of tests - cut-n-paste into two for performance reasons...
        if (newBitCount > 16) {
            final INode[] newAnArray = new INode[32];
            int count = Integer.bitCount(leftBitmap);
            for (int i = 0; i < 32; i++) {
                final int mask = 1 << i;
                final boolean hasLeft = ((leftBitmap & mask) != 0);
                final boolean hasRight = ((rightBitmap & mask) != 0);

                // maybe we should make this check in splice() ?
                if (hasLeft) {
                    final Object leftSubKey = leftArray[leftIndex++];
                    final Object leftSubValue = leftArray[leftIndex++];
                    if (hasRight) {
                        final Object rightSubKey = rightArray[rightIndex++];
                        final Object rightSubValue = rightArray[rightIndex++];
                        final INode newSubNode = Seqspert.splice(newShift, counts, false, 0, leftSubKey, leftSubValue, false, 0, rightSubKey, rightSubValue);
                        if (newSubNode == null) {
                            // we must have spliced two leaves giving a result of another leaf / KVP...
                            // the key must be unchanged
                            // the value could be either from the left or right -
                            // delegate decision to resolveFunction...
                            newAnArray[i] = create(partition(hash(leftSubKey), newShift),
                                                   leftSubKey,
                                                   counts.resolver.getResolver().invoke(leftSubKey, leftSubValue, rightSubValue));
                        } else {    // haveLeft and haveRight
                            // result was a Node...
                            newAnArray[i] = newSubNode;
                        }
                    } else {
                        // haveLeft and !haveRight
                        newAnArray[i] = promote(newShift, leftSubKey, leftSubValue);
                    }
                } else {
                    if (hasRight) { // and !haveLeft
                        final Object rightSubKey = rightArray[rightIndex++];
                        final Object rightSubValue = rightArray[rightIndex++];
                        // emulate the difference between an HCN occurring before and after BIN promotion
                        newAnArray[i] = (count >= 16) &&  (rightSubValue instanceof HashCollisionNode) ?
                            promote(newShift, ((HashCollisionNode) rightSubValue).hash, rightSubKey, rightSubValue) : // wrap anything
                            promote(newShift, rightSubKey, rightSubValue); // wrap KVPs, not INodes
                        count++;
                    }
                }
            }
        
            return new PersistentHashMap.ArrayNode(null,newBitCount, newAnArray);
        } else {
            final Object[] newBinArray = new Object[newBitCount * 2];

            int newBinIndex = 0;
            int leftDifferences = 0;
            int rightDifferences = 0;
            for (int i = 0; i < 32; i++) {
                final int mask = 1 << i;
                final boolean hasLeft = ((leftBitmap & mask) != 0);
                final boolean hasRight = ((rightBitmap & mask) != 0);

                // maybe we should make this check in splice() ?
                if (hasLeft) {
                    final Object leftSubKey = leftArray[leftIndex++];
                    final Object leftSubValue = leftArray[leftIndex++];
                    if (hasRight) {
                        final Object rightSubKey = rightArray[rightIndex++];
                        final Object rightSubValue = rightArray[rightIndex++];
                        final INode newSubNode = Seqspert.splice(newShift, counts, false, 0, leftSubKey, leftSubValue, false, 0, rightSubKey, rightSubValue);
                        if (newSubNode == null) {
                            // we must have spliced two leaves giving a result of another leaf / KVP...
                            // the key must be unchanged
                            // the value could be either from the left or right -
                            // delegate decision to resolveFunction...
                            final Object newSubValue = counts.resolver.getResolver().invoke(leftSubKey,
                                                                                     leftSubValue,
                                                                                     rightSubValue);
                            if (newSubValue != leftSubValue) leftDifferences++;
                            if (newSubValue != rightSubValue) rightDifferences++;
                            newBinArray[newBinIndex++] = leftSubKey;
                            newBinArray[newBinIndex++] = newSubValue;
                        } else {    // haveLeft and haveRight
                            // result was a Node...
                            newBinArray[newBinIndex++] = null;
                            newBinArray[newBinIndex++] = newSubNode;
                            if (leftSubValue != newSubNode) leftDifferences++;
                            if (rightSubValue != newSubNode) rightDifferences++;
                        }
                    } else {
                        // haveLeft and !haveRight
                        newBinArray[newBinIndex++] = leftSubKey;
                        newBinArray[newBinIndex++] = leftSubValue;
                        rightDifferences++;
                    }
                } else {
                    if (hasRight) { // and !haveLeft
                        final Object rightSubKey = rightArray[rightIndex++];
                        final Object rightSubValue = rightArray[rightIndex++];
                        newBinArray[newBinIndex++] = rightSubKey;
                        newBinArray[newBinIndex++] = rightSubValue;
                        leftDifferences++;
                    }
                }
            }
        
            final INode node = counts.resolver.resolveNodes(leftDifferences, leftNode, rightDifferences, rightNode);
			return node == null ?
                new PersistentHashMap.BitmapIndexedNode(null, newBitmap, newBinArray) :
                	node;
        }
    }

}
