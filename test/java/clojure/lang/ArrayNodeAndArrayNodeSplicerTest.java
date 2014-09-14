package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.*;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndArrayNodeSplicerTest
// implements SplicerTestInterface
{
	
	@Test
	public void testDuplication() {
	
	    final int shift = 5;
	
	    INode leftNode1 = BitmapIndexedNode.EMPTY;
	    for (int i = 0; i < 17; i++) {
	        leftNode1 = leftNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	
	    INode leftNode2 = leftNode1;
	    for (int i = 16; i < 33; i++) {
	        leftNode2 = leftNode2.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	    final ArrayNode expected = (ArrayNode) leftNode2;
	
	    INode rightNode1 = BitmapIndexedNode.EMPTY;
	    for (int i = 16; i < 33; i++) {
	        rightNode1 = rightNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	
	    final Duplications duplications = new Duplications(0);
	    final ArrayNode actual = (ArrayNode) new ArrayNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
	    // TODO: numDuplicates
	
	    assertEquals(actual.count, expected.count);
	    for (int i = 0; i < 32; i++) {
	        assertBitmapIndexedNodesEqual((BitmapIndexedNode) actual.array[i], (BitmapIndexedNode) expected.array[i]);
	    }
	}

}
