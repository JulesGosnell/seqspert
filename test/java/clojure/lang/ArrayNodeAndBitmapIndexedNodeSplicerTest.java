package clojure.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static clojure.lang.TestUtils.assertNodeEquals;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {
	
    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndBitmapIndexedNodeSplicer();
    
    public void test(int leftStart, int leftEnd,
		     Object rightKey0, Object rightValue0,
		     Object rightKey1, Object rightValue1,
		     boolean same) {

	
	final INode empty = BitmapIndexedNode.EMPTY;
	final INode leftNode = TestUtils.assocN(shift, empty, leftStart, leftEnd, new Counts());

	INode expected = leftNode;
	int expectedCounts = 0;
	Box addedLeaf = null;
	addedLeaf = new Box(null);
	expected = expected.assoc(shift, NodeUtils.hash(rightKey0) , rightKey0, rightValue0, addedLeaf);
	expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	addedLeaf = new Box(null);
	expected = expected.assoc(shift, NodeUtils.hash(rightKey1) , rightKey1, rightValue1, addedLeaf);
	expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	
	final INode rightNode = BitmapIndexedNode.EMPTY
	    .assoc(shift, NodeUtils.hash(rightKey0), rightKey0, rightValue0, new Box(null))
	    .assoc(shift, NodeUtils.hash(rightKey1), rightKey1, rightValue1, new Box(null));

	// do the splice
	final Counts actualCounts = new Counts(0, 0);
	final INode actual = splicer.splice(shift, actualCounts, null, leftNode,
					    NodeUtils.nodeHash(rightNode), null, rightNode);

	// check everything is as expected...
	assertEquals(expectedCounts, actualCounts.sameKey);
	assertNodeEquals(expected, actual);
	if (same) assertSame(expected, actual);
    }

    @Test
    public void testDifferent() {
    	test(3, 30,
	     new HashCodeKey("key1", 1), "value1",
	     new HashCodeKey("key2", 2), "value2",
	     false);
    }

    @Test
    public void testSameKeyHashCode() {
    	test(2, 30,
	     new HashCodeKey("key1", 1), "value1",
	     new HashCodeKey("collisionKey2", 2), "collisionValue2",
	     false);
    }
	
    @Test
    public void testSameKey() {
    	test(2, 30,
	     new HashCodeKey("key1", 1), "value1",
	     new HashCodeKey("key2", 2), "duplicationValue2",
	     false);
    }

    @Test
    public void testSameKeyAndValue() {
	// rhs is two KVPs
    	test(1, 30,
	     new HashCodeKey("key1", 1), "value1",
	     new HashCodeKey("key2", 2), "value2",
	     // TODO: should be true
	     false);
	// TODO: might work
	// rhs is an HCN
    	// test(1, 30,
	//      new HashCodeKey("key1", 1), "value1",
	//      new HashCodeKey("key2", 1), "value2",
	//      true);
    }
}
