package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new KeyValuePairAndBitmapIndexedNodeSplicer();
    final int shift = 0;

    public void test(Object leftKey, Object leftValue, boolean same) {

	final INode empty = BitmapIndexedNode.EMPTY;

	final INode leftNode = TestUtils.assoc(shift, empty, leftKey, leftValue, new Counts());
	final INode rightNode = TestUtils.assocN(shift, empty, 1, 2, new Counts());
	
	final Counts expectedCounts = new Counts();
	final INode expectedNode = TestUtils.assocN(shift, leftNode, 1, 2, new Counts());
	    
	final Counts actualCounts = new Counts();
	final INode actualNode = splicer.splice(shift, actualCounts, leftKey, leftValue,
						NodeUtils.nodeHash(rightNode), null, rightNode);
	
	assertEquals(expectedCounts, actualCounts);
	assertNodeEquals(expectedNode, actualNode);
	if (same) assertSame(expectedNode, actualNode);
    }
    
    @Ignore
    @Test
    @Override
    public void testDifferent() {
	test(new HashCodeKey("key3", 3), "value3", false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKeyHashCode() {
	test(new HashCodeKey("key3", 1), "value3", false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKey() {
	test(new HashCodeKey("key1", 1), "differentValue1", false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKeyAndValue() {
	test(new HashCodeKey("key1", 1), "value1", true);
    }


}
