package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

class ArrayNodeAndArrayNodeSplicer extends AbstractSplicer {
    public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
        final ArrayNode leftNode = (ArrayNode) leftValue;
        final ArrayNode rightNode = (ArrayNode) rightValue;

        final INode[] array = new INode[32];
        int empty = 0;
        for (int i = 0; i < 32; i++) {
            final INode l = leftNode.array[i];
            final INode r = rightNode.array[i];
            final boolean lb = l != null;
            final boolean rb = r != null;
            if (lb) {
                array[i] = rb ? NodeUtils.splice(shift + 5, duplications, null, l, rightHash, null, r) : l;
            } else {
                if (rb)
                    array[i] = r;
                else
                    empty--;
            }
        }

        return new ArrayNode(null, 32 - empty, array);
    }
}