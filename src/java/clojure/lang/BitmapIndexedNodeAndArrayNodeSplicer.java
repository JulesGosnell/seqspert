package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndArrayNodeSplicer implements Splicer {

    @Override
    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHash, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;
        final ArrayNode rightNode = (ArrayNode) rightValue;

        final Object[] leftArray = leftNode.array;
        final INode[] rightArray = rightNode.array;

        final INode[] newArray = new INode[32];
        final int newShift = shift + 5;

        int rightDifferences = 0;
        int empty = 0;
        int leftIndex = 0;
        int count = Integer.bitCount(leftNode.bitmap);
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
            		final INode newSubNode = Seqspert.splice(newShift, counts, false, 0, leftSubKey, leftSubValue, false, 0, null, rightSubNode);
            		newArray[i] = newSubNode;
            		rightDifferences += (newSubNode == rightSubNode) ? 0 : 1;
            	} else {
            		newArray[i] = ArrayNodeUtils.promote(newShift, leftSubKey, leftSubValue);
            		rightDifferences++;
            	}
            } else { // not lb
            	if (hasRight) {
            		// only rhs present - copy over
            		boolean test = false;
            		if (count > 15)
            			if (rightSubNode instanceof HashCollisionNode) {
            				rightDifferences++;
            				test= true;
            			}
            			else
            				test = false;
            		else
            			test = false;

            		newArray[i] = test  ? 
            				ArrayNodeUtils.promote(shift + 5, ((HashCollisionNode)rightSubNode).hash, null, rightSubNode) :
            					rightSubNode;
            				count++;
            	} else {
            		// do nothing...
            		empty++;
            	}
            }
        }

        return rightDifferences == 0 ? rightNode : new ArrayNode(null, 32 - empty, newArray);
    }
}
