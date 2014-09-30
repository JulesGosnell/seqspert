package clojure.lang;

import static clojure.lang.PersistentHashMap.hash;
import static clojure.lang.NodeUtils.cloneAndInsert;
import static clojure.lang.NodeUtils.cloneAndSet;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

// TODO: this is pretty much identical to BINAndHCNSplicer - can we reuse the same code ?

class BitmapIndexedNodeAndKeyValuePairSplicer extends AbstractSplicer {

    public INode splice(int shift, Duplications duplications, 
			Object leftKey, Object leftValue,
			int rightHash, Object rightKey, Object rightValue) {
	final BitmapIndexedNode leftNode = (BitmapIndexedNode) leftValue;

	int bit = BitmapIndexedNodeUtils.bitpos(rightHash, shift);
	int index = leftNode.index(bit);
	int keyIndex = index * 2;
	int valueIndex = keyIndex + 1;
	if((leftNode.bitmap & bit) == 0) {
	    // left hand side unoccupied
	    // TODO: BIN or AN ?
	    return new BitmapIndexedNode(null,
					 leftNode.bitmap | bit,
					 cloneAndInsert(leftNode.array,
							Integer.bitCount(leftNode.bitmap) * 2,
							keyIndex,
							rightKey,
							rightValue));

	} else {
	    // left hand side already occupied...
	    final Object subKey = leftNode.array[keyIndex];
	    final Object subVal = leftNode.array[valueIndex];
	    return new BitmapIndexedNode(null,
					 leftNode.bitmap,
					 cloneAndSet(leftNode.array,
						     valueIndex,
						     NodeUtils.splice(shift, duplications,
								      subKey, subVal,
								      rightHash, rightKey, rightValue)));

	}
    }

}
