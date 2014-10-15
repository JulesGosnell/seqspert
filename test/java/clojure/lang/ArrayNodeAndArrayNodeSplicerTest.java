package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndArrayNodeSplicer();

    public void test(Object leftKey, Object leftValue, int leftStart, int leftEnd,
                     int rightStart, int rightEnd, boolean same) {
        final INode empty = BitmapIndexedNode.EMPTY;

        final INode leftNode =
            TestUtils.assocN(shift,
                             TestUtils.assoc(shift, empty, leftKey, leftValue, new Counts()),
                             leftStart, leftEnd, new Counts());
        assertTrue(leftNode instanceof ArrayNode);

        final INode rightNode = TestUtils.assocN(shift, empty, rightStart, rightEnd, new Counts());
        assertTrue(rightNode instanceof ArrayNode);

        final Counts expectedCounts = new Counts(same, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);

        final Counts actualCounts = new Counts(same, 0, 0);
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts.sameKey, actualCounts.sameKey);
        assertNodeEquals(expectedNode, actualNode);
        if (same) TestUtils.assertSame(leftNode, expectedNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key0", 0), "value0", 1, 17, 15, 31, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key17.1", 17), "value17.1", 0, 17, 15, 31, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key17", 17), "value17.1", 0, 17, 15, 31, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key0", 0), "value0", 1, 31, 0, 31, true);
    }
}
