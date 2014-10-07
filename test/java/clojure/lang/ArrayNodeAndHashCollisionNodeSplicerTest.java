package clojure.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static clojure.lang.TestUtils.assertNodeEquals;

import org.junit.Ignore;
import org.junit.Test;

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
	final INode actualNode =
	    splicer.splice(shift, actualCounts, null, leftNode, rightHash, null, rightNode);
	
	assertEquals(expectedCounts, actualCounts);
	assertNodeEquals(expectedNode, actualNode);
	if (same) assertSame(expectedNode, actualNode);
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

    @Ignore
    @Test
    public void testSameKeyHashCode() {
	final int rightHash = 1;
	test(1, 30,
	     rightHash, 
	     new HashCodeKey("collision0", rightHash), "collision0",
	     new HashCodeKey("collision1", rightHash), "collision1",
	     false);
    }

    @Ignore
    @Test
    public void testSameKey() {
	final int rightHash = 1;
	test(1, 30,
	     rightHash, 
	     new HashCodeKey("collision0", rightHash), "collision0",
	     new HashCodeKey("key1", rightHash), "collision1",
	     false);
    }

    @Ignore
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
