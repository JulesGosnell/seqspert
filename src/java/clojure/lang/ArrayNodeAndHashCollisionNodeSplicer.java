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
        if (subNode == null) {
            final INode tmp = BitmapIndexedNodeUtils.create(
                                                            0, // TODO - this 0 wrong ?
                                                            //BitmapIndexedNodeUtils.bitpos(rightHash, shift + 5),
                                                            //BitmapIndexedNodeUtils.bitpos(rightHash, shift),
                                                            rightNode);

            return new ArrayNode(null, leftNode.count + 1, NodeUtils.cloneAndSetNode(leftArray, index, tmp));
        } else {
            final INode newNode = NodeUtils.splice(
                                                   //shift,
                                                   shift + 5,
                                                   counts,
                                                   null, subNode,
                                                   rightHash, null, rightNode);
            return (subNode == newNode) ?
                leftNode:
                new ArrayNode(null, leftNode.count, NodeUtils.cloneAndSetNode(leftArray, index, newNode));
        }
    }

}

