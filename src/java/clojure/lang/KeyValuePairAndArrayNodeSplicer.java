package clojure.lang;

import static clojure.lang.NodeUtils.create;
import static clojure.lang.NodeUtils.hash;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndArrayNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {

        final ArrayNode rightNode = (ArrayNode) rightValue;
        final int leftHash = hash(leftKey);
        final int index = PersistentHashMap.mask(leftHash, shift);
        
        final INode subNode = rightNode.array[index];

        if (subNode == null) {
            return new ArrayNode(null,
                                 rightNode.count + 1,
                                 NodeUtils.cloneAndSetNode(rightNode.array, index,
                                                           create(shift + 5, leftHash, leftKey, leftValue)));
        } else {
            final INode newNode = NodeUtils.splice(shift + 5, counts, leftKey, leftValue, null, subNode);
            return newNode == subNode ? 
                rightNode :
                new ArrayNode(null,
                              rightNode.count,
                              NodeUtils.cloneAndSetNode(rightNode.array, index, newNode));
        }
    }

}
