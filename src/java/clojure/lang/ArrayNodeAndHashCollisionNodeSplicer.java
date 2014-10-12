package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndHashCollisionNodeSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        int _, Object rightKey, Object rightValue) {

        final ArrayNode leftNode = (ArrayNode) leftValue;
        final HashCollisionNode rightNode  = (HashCollisionNode) rightValue;

        final INode[] leftArray = leftNode.array;
        final int rightHash = rightNode.hash;
        final int index = PersistentHashMap.mask(rightHash, shift);
        final INode subNode = leftArray[index];

        int newCount;
        INode newNode;
        if (subNode == null) {
            newCount = leftNode.count + 1;
            newNode = BitmapIndexedNodeUtils.create(0, null, rightNode); // TODO - this 0 wrong ?
        } else {
            newCount = leftNode.count;;
            newNode = NodeUtils.splice(shift + 5, counts, null, subNode, rightHash, null, rightNode);
        }
        return new ArrayNode(null, newCount, NodeUtils.cloneAndSetNode(leftArray, index, newNode));
    }

}

