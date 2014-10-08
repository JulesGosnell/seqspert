package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new KeyValuePairAndBitmapIndexedNodeSplicer();
    final int shift = 0;

    public void test(Object leftKey, Object leftValue,
		     Object rightKey, Object rightValue,
		     boolean same) {

	final INode empty = BitmapIndexedNode.EMPTY;

	final INode leftNode = TestUtils.assoc(shift, empty, leftKey, leftValue, new Counts());
	final INode rightNode = TestUtils.assoc(shift, empty, rightKey, rightValue, new Counts());
	
	final Counts expectedCounts = new Counts();
	final INode expectedNode = TestUtils.assoc(shift, leftNode, rightKey, rightValue, expectedCounts);
	    
	final Counts actualCounts = new Counts();
	final INode actualNode = splicer.splice(shift, actualCounts, leftKey, leftValue, 0, null, rightNode);
	
	assertEquals(expectedCounts, actualCounts);
	assertNodeEquals(expectedNode, actualNode); // TODO - except when null
	if (same) assertSame(expectedNode, actualNode); // ??
    }
    
    @Test
    @Override
    public void testDifferent() {
	test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key2", 2), "value2", false);
    }

    @Test
    @Override
    public void testSameKeyHashCode() {
	test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key2", 1), "value2", false);
    }

    @Test
    @Override
    public void testSameKey() {
	test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key1", 1), "value2", false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
	test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key1", 1), "value1", false);
    }


}
