package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new KeyValuePairAndArrayNodeSplicer();

    public void test(Object leftKey, Object leftValue, int rightStart, int rightEnd, boolean sameRight) {

        final INode rightNode = TestUtils.create(shift, rightStart, rightEnd);
        
        final Counts expectedCounts = new Counts(Counts.resolveRight, 0, 0);
        final INode expectedNode =
            TestUtils.assocN(shift, BitmapIndexedNodeUtils.create(shift, leftKey, leftValue), rightStart, rightEnd, expectedCounts);

        final Counts actualCounts = new Counts(Counts.resolveRight, 0, 0); // TODO: resolveLeft ?
        final INode actualNode = splicer.splice(shift, actualCounts, leftKey, leftValue, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key1", 1), "value1", 2, 31, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("collision", 1), "collision1", 1, 31, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key1", 1), "differentValue1", 1, 31, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key1", 1), "value1", 1, 31, true);
    }
}
