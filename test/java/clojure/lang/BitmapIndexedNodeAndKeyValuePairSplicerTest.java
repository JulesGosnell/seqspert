package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;

public class BitmapIndexedNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {
    
    final Splicer splicer = new BitmapIndexedNodeAndKeyValuePairSplicer();
    final int shift = 0;

    public void test(int leftStart, int leftEnd, Object rightKey, Object rightValue, boolean same) {

        final INode empty = BitmapIndexedNode.EMPTY;
        final INode leftNode = TestUtils.assocN(shift, empty, leftStart, leftEnd, new Counts());
        assertTrue(leftNode instanceof BitmapIndexedNode);

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assoc(shift, leftNode, rightKey, rightValue, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, rightKey, rightValue);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(expectedNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(2, 3, new HashCodeKey("key1", 1), "value1", false);
        test(2, 17, new HashCodeKey("key1", 1), "value1", false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(2, 3, new HashCodeKey("key1", 2), "value1", false);
        test(2, 17, new HashCodeKey("key1", 2), "value1", false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(2, 3, new HashCodeKey("key2", 2), "value1", false);
        test(2, 17, new HashCodeKey("key2", 2), "value1", false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(2, 3, new HashCodeKey("key2", 2), "value2", false);
        test(2, 17, new HashCodeKey("key2", 2), "value2", false);
    }
    
}
