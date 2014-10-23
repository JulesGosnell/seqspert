package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new BitmapIndexedNodeAndArrayNodeSplicer();
        
    public void test(Object leftKey0, Object leftValue0, Object leftKey1, Object leftValue1, 
                     int rightStart, int rightEnd, boolean sameRight) {
        final INode leftNode = TestUtils.create(shift, leftKey0, leftValue0, leftKey1, leftValue1);
        final INode rightNode = TestUtils.create(shift, rightStart, rightEnd);

        final Counts expectedCounts = new Counts(NodeUtils.resolveRight, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);
                
        final Counts actualCounts = new Counts(NodeUtils.resolveRight, 0, 0); // TODO - resolveLeft ?
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }
        
    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key" + 1, 1), "value1", new HashCodeKey("key" + 2, 2), "value2", 3, 31, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key" + 1, 3), "value1", new HashCodeKey("key" + 2, 4), "value2", 3, 31, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key" + 3, 3), "value1", new HashCodeKey("key" + 4, 4), "value2", 3, 31, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key" + 3, 3), "value3", new HashCodeKey("key" + 4, 4), "value4", 3, 31, true);
    }
    
}
