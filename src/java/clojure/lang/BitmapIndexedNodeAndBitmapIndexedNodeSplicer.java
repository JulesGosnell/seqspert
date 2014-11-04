package clojure.lang;

import static clojure.lang.ArrayNodeUtils.promote2;
import static clojure.lang.ArrayNodeUtils.getPartition;
import static clojure.lang.ArrayNodeUtils.partition;

import static clojure.lang.BitmapIndexedNodeUtils.create;
import static clojure.lang.BitmapIndexedNodeUtils.hash;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndBitmapIndexedNodeSplicer implements Splicer {

    // TODO: ?maybe? seperate promotion and non-promotion code...
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
        final int newBitCount = Integer.bitCount(newBitmap); // expensive - but we need to know...
        final boolean promoted = (newBitCount > 16);
        final Object[] newBinArray = promoted ? null : new Object[newBitCount * 2];
        final INode[] newAnArray = promoted ? new INode[32] : null;
        final int newShift = shift + 5;

        int leftIndex = 0;
        int rightIndex = 0;
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
                        final Object newSubKey = leftSubKey;
                        // the value could be either from the left or right -
                        // delegate decision to resolveFunction...
                        final Object newSubValue = counts.resolveFunction.invoke(newSubKey,
                                                                                 leftSubValue,
                                                                                 rightSubValue);
                        if (newSubValue != leftSubValue) leftDifferences++;
                        if (newSubValue != rightSubValue) rightDifferences++;

                        if (promoted) {
                            newAnArray[i] = create(partition(hash(newSubKey), newShift),
                                                   newSubKey, newSubValue);
                        } else {
                            newBinArray[newBinIndex++] = newSubKey;
                            newBinArray[newBinIndex++] = newSubValue;
                        }
                    } else {    // haveLeft and haveRight
                        // result was a Node...
                        if (promoted) {
                            newAnArray[i] = newSubNode;
                        } else {
                            newBinArray[newBinIndex++] = null;
                            newBinArray[newBinIndex++] = newSubNode;
                            if (leftSubValue != newSubNode) leftDifferences++;
                            if (rightSubValue != newSubNode) rightDifferences++;
                        }
                    }
                } else {
                    // haveLeft and !haveRight
                    if (promoted) {
                        newAnArray[i] = promote2(newShift, leftSubKey, leftSubValue);
                    } else {
                        newBinArray[newBinIndex++] = leftSubKey;
                        newBinArray[newBinIndex++] = leftSubValue;
                        rightDifferences++;
                    }
                }
            } else {
                if (hasRight) { // and !haveLeft
                    final Object rightSubKey = rightArray[rightIndex++];
                    final Object rightSubValue = rightArray[rightIndex++];
                    if (promoted) {
                        newAnArray[i] = promote2(newShift, rightSubKey, rightSubValue);
                    } else {
                        newBinArray[newBinIndex++] = rightSubKey;
                        newBinArray[newBinIndex++] = rightSubValue;
                        leftDifferences++;
                    }
                }
            }
        }
        
        return promoted ?
            new PersistentHashMap.ArrayNode(null,newBitCount, newAnArray) :
            leftDifferences == 0 ?
            leftNode :
            rightDifferences == 0 ?
            rightNode :
            new PersistentHashMap.BitmapIndexedNode(null, newBitmap, newBinArray);
    }

}
