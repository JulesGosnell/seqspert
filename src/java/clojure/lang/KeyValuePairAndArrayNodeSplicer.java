package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndArrayNodeSplicer implements Splicer {

    @Override
    public INode splice(int shift, Counts counts,
                        boolean leftHaveHash, int leftHashCode, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        final ArrayNode rightNode = (ArrayNode) rightValue;
        final int leftHash = BitmapIndexedNodeUtils.hash(leftHaveHash, leftHashCode, leftKey);
        final int index = ArrayNodeUtils.partition(leftHash, shift);
        
        final INode subNode = rightNode.array[index];

        if (subNode == null) {
            return new ArrayNode(null,
                                 rightNode.count + 1,
                                 ArrayNodeUtils.cloneAndSetNode(rightNode.array, index,
                                                                BitmapIndexedNodeUtils.create(ArrayNodeUtils.partition(leftHash, shift + 5), leftKey, leftValue)));
        } else {
            final INode newNode = Seqspert.splice(shift + 5, counts, true, leftHash, leftKey, leftValue, false, 0, null, subNode);
            return newNode == subNode ? 
                rightNode :
                new ArrayNode(null,
                              rightNode.count,
                              ArrayNodeUtils.cloneAndSetNode(rightNode.array, index, newNode));
        }
    }

}
