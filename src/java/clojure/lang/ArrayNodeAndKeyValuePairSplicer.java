package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndKeyValuePairSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {

        final ArrayNode leftNode = (ArrayNode) leftValue;

        final int rightHash = BitmapIndexedNodeUtils.hash(rightKey);
        final int index = PersistentHashMap.mask(rightHash, shift);

        final INode[] leftArray = leftNode.array;
        final INode subNode = leftArray[index];

        if (subNode == null) {
            return new ArrayNode(null,
                                 leftNode.count + 1,
                                 ArrayNodeUtils.cloneAndSetNode(leftArray, index,
                                                           BitmapIndexedNodeUtils.create(shift + 5, rightHash, rightKey, rightValue)));
        } else {
            final INode newNode =
                Seqspert.splice(shift + 5, counts, null, subNode, rightKey, rightValue);
            
            return newNode == subNode ? 
                leftNode :
                new ArrayNode(null,
                              leftNode.count,
                              ArrayNodeUtils.cloneAndSetNode(leftArray, index, newNode));
        }
    }

}
