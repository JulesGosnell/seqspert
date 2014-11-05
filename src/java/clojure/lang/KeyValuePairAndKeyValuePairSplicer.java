package clojure.lang;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndKeyValuePairSplicer implements Splicer {

	// c.f. recurse method in HCN/HCN Splicer...
	
    static INode recurse(int shift, int leftHash, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
    	final int leftPartition = ArrayNodeUtils.partition(leftHash, shift);
        final int rightPartition = ArrayNodeUtils.partition(rightHash, shift);
        final int leftBit = 1 << leftPartition;
        final int rightBit = 1 << rightPartition;
        return new BitmapIndexedNode(null,
                                     leftBit | rightBit,
                                     (leftBit == rightBit) ?
                                     new Object[]{null,recurse(shift + 5, leftHash, leftKey, leftValue, rightHash, rightKey, rightValue)} :
                                     (leftPartition <= rightPartition) ?
                                     new Object[]{leftKey, leftValue, rightKey, rightValue} :
                                     new Object[]{rightKey, rightValue, leftKey, leftValue});
    }
    
	public INode splice(int shift, Counts counts,
			boolean leftHaveHash, int leftHashCode,
			Object leftKey, Object leftValue, boolean rightHaveHash, int rightHashCode, Object rightKey, Object rightValue) {
		if (Util.equiv(leftKey, rightKey)) {
			// TODO - how does this fit with use of a resolver ?
			// duplication
			counts.sameKey++;
			return null;
		} else {
                    final int leftHash = BitmapIndexedNodeUtils.hash(leftHaveHash, leftHashCode, leftKey);
                    final int rightHash = BitmapIndexedNodeUtils.hash(rightHaveHash, rightHashCode, rightKey);
			if (leftHash == rightHash) {
				// hash collision
				return HashCollisionNodeUtils.create(leftHash, leftKey, leftValue, rightKey, rightValue);
			} else {
				// no collision
				return recurse(shift, leftHash, leftKey, leftValue, rightHash, rightKey, rightValue);
			}
		}
	}
        
}
