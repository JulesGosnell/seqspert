package clojure.lang;

import static clojure.lang.NodeUtils.cloneAndInsert;
import static clojure.lang.NodeUtils.cloneAndSet;
import static clojure.lang.NodeUtils.hash;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class HashCollisionNodeAndBitmapIndexedNodeSplicer implements Splicer {
    
    public INode splice(int shift, Counts counts, 
                        Object leftKey, Object leftValue,
                        int _, Object rightKey, Object rightValue) {

        final HashCollisionNode leftNode = (HashCollisionNode) leftValue;
        final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;
        
        final int bit = BitmapIndexedNodeUtils.bitpos(leftNode.hash, shift);
        final int index = rightNode.index(bit) * 2;
        if((rightNode.bitmap & bit) == 0) {
            // different hash partitions
            // TODO: check and maybe promote to ArrayNode ?
            return new BitmapIndexedNode(null,
                                         rightNode.bitmap | bit,
                                         cloneAndInsert(rightNode.array,
                                                        Integer.bitCount(rightNode.bitmap) * 2,
                                                        index,
                                                        leftNode));
        } else {
            // same hash partitions
            final Object[] rightArray = rightNode.array;
            final int subKeyIndex = index * 2;
            final Object subKey = rightArray[subKeyIndex];
            final INode spliced = NodeUtils.splice(shift + 5, 
			                  counts,
			                  null,
			                  leftNode,
			                  subKey == null ? 0 : hash(subKey),
			                  subKey,
			                  rightArray[subKeyIndex + 1]);
            
            if ((~bit & rightNode.bitmap) > 0) {
            	// BIN contains other subNodes - return a new BIN containing them
            	return new BitmapIndexedNode(null,
            			rightNode.bitmap,
            			cloneAndSet(rightNode.array,
            					index,
            					spliced));
            } else {
            	// this was the only subNode, now it is spliced into the LHC - return the result
				return spliced;
			}
        }
        
    }
    
}
