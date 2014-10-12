package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
        
    public void test(Object leftKey0, Object leftValue0, Object leftKey1, Object leftValue1, int rightStart, int rightEnd, boolean same) {
                
        final INode leftNode = TestUtils.assoc(shift,
                                               TestUtils.assoc(shift, BitmapIndexedNode.EMPTY, leftKey0, leftValue0, new Counts()),
                                               leftKey1, leftValue1, new Counts());

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);
                
        final INode rightNode = TestUtils.assocN(shift, BitmapIndexedNode.EMPTY, rightStart, rightEnd, new Counts());
                
        final Counts actualCounts = new Counts(0, 0);
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(rightNode, actualNode); // is this right ?
    }
        
    @Override
    @Test
    public void testDifferent() {
    	test(new HashCodeKey("key" + 1, 1), "value1", new HashCodeKey("key" + 2, 2), "value2", 3, 4, false);
        test(new HashCodeKey("key" + 1, 1), "value1", new HashCodeKey("key" + 2, 2), "value2", 3, 18, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key" + 1, 3), "value1", new HashCodeKey("key" + 2, 4), "value2", 3, 4, false);
        test(new HashCodeKey("key" + 1, 3), "value1", new HashCodeKey("key" + 2, 4), "value2", 3, 18, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key" + 3, 3), "value1", new HashCodeKey("key" + 4, 4), "value2", 3, 4, false);
        test(new HashCodeKey("key" + 3, 3), "value1", new HashCodeKey("key" + 4, 4), "value2", 3, 18, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key" + 3, 3), "value3", new HashCodeKey("key" + 4, 4), "value4", 3, 4, false); // TODO: FIXME
        test(new HashCodeKey("key" + 3, 3), "value3", new HashCodeKey("key" + 4, 4), "value4", 3, 18, false); // TODO: FIXME
    }
}
