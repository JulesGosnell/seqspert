package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Ignore;
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
        
        final INode rightNode = TestUtils.assocN(shift, empty, rightStart, rightEnd, new Counts());

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, 0, null, rightNode);

        assertEquals(expectedCounts.sameKey, actualCounts.sameKey);
        assertNodeEquals(actualNode, expectedNode);
        if (same) assertSame(actualNode, expectedNode);
    }

    @Ignore
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key0", 0), "value0", 1, 17, 17, 31, false);
    }

    @Ignore
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key17.1", 17), "value17.1", 0, 17, 17, 31, false);
    }

    @Ignore
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key17", 17), "value17.1", 0, 17, 17, 31, false);
    }

    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key0", 0), "value0", 1, 31, 0, 31, true);
    }
}
