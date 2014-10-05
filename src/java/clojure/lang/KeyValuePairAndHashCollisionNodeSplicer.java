package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndHashCollisionNodeSplicer implements Splicer {
	
    public INode splice(int shift, Counts counts,
			Object leftKey, Object leftValue,
			int _, Object rightKey, Object rightValue) {

    	// TODO - what if hashCodes do not collide ?
    	
        final HashCollisionNode rightNode = (HashCollisionNode) rightValue;

        final int length = (rightNode.count + 1) * 2;
        final Object[] rarray = rightNode.array;
        final Object[] array = new Object[length];
        array[0] = leftKey;
        array[1] = leftValue;
        int r = 0;
        int j = 2;
        for (int i = 0; i < rightNode.count; i++) {
            final Object rKey = rarray[r++];
            final Object rVal = rarray[r++];
            if (Util.equiv(leftKey, rKey)) {
                // duplication - overwrite lhs k:v pair
                array[0] = rKey;
                array[1] = rVal;
                counts.sameKey++;
            } else {
                // simple collision
                array[j++] = rKey;
                array[j++] = rVal;
            }
        }
        return new HashCollisionNode(null, rightNode.hash, j / 2, HashCollisionNodeUtils.trim(array, j));
    }
    
}
