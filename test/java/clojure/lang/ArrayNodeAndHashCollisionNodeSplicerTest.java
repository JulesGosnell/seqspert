package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.*;
import static clojure.lang.NodeUtils.*;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {
    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndHashCollisionNodeSplicer();
    
    public void test(int leftStart, int leftEnd,
		     int rightHash,
		     Object rightKey0, Object rightValue0,
		     Object rightKey1, Object rightValue1,
		     boolean same) {

	// set up lhs and expected
	INode leftNode = BitmapIndexedNode.EMPTY;
	for (int i = leftStart; i < leftEnd + 1; i++) {
	    final int hashCode = i;
	    final Object key = new HashCodeKey("key" + i, hashCode);
	    final Object value = i;
	    leftNode = leftNode.assoc(shift, hashCode, key, value, new Box(null));
	}
	INode expected = leftNode;
	int expectedCounts = 0;
	Box addedLeaf = null;
	addedLeaf = new Box(null);
	expected = expected.assoc(shift, NodeUtils.hash(rightKey0) , rightKey0, rightValue0, addedLeaf);
	expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	addedLeaf = new Box(null);
	expected = expected.assoc(shift, NodeUtils.hash(rightKey1) , rightKey1, rightValue1, addedLeaf);
	expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	
	final INode rightNode = HashCollisionNodeUtils.create(rightHash, rightKey0, rightValue0, rightKey1, rightValue1);

	// do the splice
	final Counts actualCounts = new Counts(0, 0);
	final INode actual = splicer.splice(shift, actualCounts, null, leftNode, rightHash, null, rightNode);

	// check everything is as expected...
	assertEquals(expectedCounts, actualCounts.sameKey);
	assertNodeEquals(expected, actual);
    }

    @Test
    public void testDifferent() {
	final int rightHash = 1;
    	test(2, 30,
	     rightHash, 
	     new HashCodeKey("collision0", rightHash), "collision0",
	     new HashCodeKey("collision1", rightHash), "collision1",
	     false);
    }

    @Test
    public void testSameKeyHashCode() {
	final int rightHash = 1;
    	test(1, 30,
	     rightHash, 
	     new HashCodeKey("collision0", rightHash), "collision0",
	     new HashCodeKey("collision1", rightHash), "collision1",
	     false);
    }
	
    @Test
    public void testSameKey() {
	final int rightHash = 1;
    	test(1, 30,
	     rightHash, 
	     new HashCodeKey("collision0", rightHash), "collision0",
	     new HashCodeKey("key1", rightHash), "collision1",
	     false);
    }

    @Test
    public void testSameKeyAndValue() {
	final int rightHash = 1;
    	test(1, 30,
	     rightHash, 
	     new HashCodeKey("collision0", rightHash), "collision0",
	     new HashCodeKey("key1", rightHash), "value1",
	     false);
	// to test this we need an HCN in the same place on the lhs
    }
}
