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
	
	final Counts expectedCounts = new Counts();
	final INode expectedNode =
	    TestUtils.assoc(shift,
			    TestUtils.assoc(shift, leftNode, rightKey0, rightValue0, expectedCounts),
			    rightKey1, rightValue1, expectedCounts);
	
	final Counts rightCounts = new Counts();
	final INode rightNode = 
	    TestUtils.assoc(shift,
			    TestUtils.assoc(shift, empty, rightKey0, rightValue0, rightCounts),
			    rightKey1, rightValue1, rightCounts);
	
	final Counts actualCounts = new Counts();
	final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode,
						NodeUtils.nodeHash(rightNode), null, rightNode);
	
	assertEquals(expectedCounts, actualCounts);
	assertNodeEquals(expectedNode, actualNode);
	if (same) assertSame(expectedNode, actualNode);
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
