package clojure.lang;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndBitmapIndexedNodeSplicer implements Splicer {

    // TODO: seperate promotion and non-promotion code...
    public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			Object rightKey, Object rightValue) {

        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;

        final int leftBitmap = leftNode.bitmap;
        final Object[] leftArray = leftNode.array;
        final int rightBitmap = rightNode.bitmap;
        final Object[] rightArray = rightNode.array;

        final int newBitmap = leftBitmap | rightBitmap;
        final int newBitCount = Integer.bitCount(newBitmap); // nasty - but we need to know...
        final boolean promoted = (newBitCount > 16); // TODO: handle this more efficiently
        final Object[] newBinArray = promoted ? null : new Object[newBitCount * 2];
        final INode[] newAnArray = promoted ? new INode[32] : null;

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
                    final INode newSubNode = NodeUtils.splice(shift + 5, counts, leftSubKey, leftSubValue, rightSubKey, rightSubValue);
                    if (newSubNode == null) {
                        // we must have spliced two leaves giving a result of another leaf / KVP...
                        // the key must be unchanged
                        final Object newSubKey = leftSubKey;
                        // the value could be either from the left or right - delgate decision to resolveFunction...
                        final Object newSubValue = counts.resolveFunction.invoke(newSubKey, leftSubValue, rightSubValue);
                        if (newSubValue != leftSubValue) leftDifferences++;
                        if (newSubValue != rightSubValue) rightDifferences++;

                        if (promoted) {
                            newAnArray[i] = NodeUtils.create(shift + 5, newSubKey, newSubValue);
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
                        newAnArray[i] = ArrayNodeUtils.promote(shift + 5, leftSubKey, leftSubValue);
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
                        newAnArray[i] = ArrayNodeUtils.promote(shift + 5, rightSubKey, rightSubValue);
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
            new PersistentHashMap.BitmapIndexedNode(new AtomicReference<Thread>(), newBitmap, newBinArray);
    }

}
