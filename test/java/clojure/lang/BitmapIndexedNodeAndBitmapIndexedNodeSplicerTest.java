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
        
    public void test(Object leftKey0, Object leftValue0, Object leftKey1, Object leftValue1,
                     int rightStart, int rightEnd, boolean leftSame, boolean rightSame) {

        final INode leftNode = TestUtils.create(shift, leftKey0, leftValue0, leftKey1, leftValue1);
        final INode rightNode = TestUtils.create(shift, rightStart, rightEnd);
                
        final IFn resolveFunction = leftSame ? NodeUtils.resolveLeft : NodeUtils.resolveRight;

        final Counts expectedCounts = new Counts(resolveFunction, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);
                
        final Counts actualCounts = new Counts(resolveFunction, 0, 0);
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (leftSame) assertSame(leftNode, actualNode); // expectedNode not as expected !
        if (rightSame) assertSame(rightNode, actualNode);
    }
        
    @Override
    @Test
    public void testDifferent() {
    	test(new HashCodeKey("key" + 1, 1), "value1", new HashCodeKey("key" + 2, 2), "value2", 3, 5, false, false);
        test(new HashCodeKey("key" + 1, 1), "value1", new HashCodeKey("key" + 2, 2), "value2", 3, 19, false, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key" + 1, 3), "value1", new HashCodeKey("key" + 2, 4), "value2", 3, 5, false, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key" + 3, 3), "value1", new HashCodeKey("key" + 4, 4), "value2", 3, 5, false, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        // leftSame
    	test(new HashCodeKey("key" + 3, 3), "value3", new HashCodeKey("key" + 4, 4), "value4", 3, 5, true, false);
        // rightSame
        test(new HashCodeKey("key" + 3, 3), "value3", new HashCodeKey("key" + 4, 4), "value4", 3, 6, false, true);

    	test(new HashCodeKey("key" + 3, 3), "value3", new HashCodeKey("key" + 4, 4), "value4", 4, 20, false, false);
    	test(new HashCodeKey("key" + 3, 3), "value3", new HashCodeKey("key4.1", 4), "value4", 4, 20, false, false);
    }
}
