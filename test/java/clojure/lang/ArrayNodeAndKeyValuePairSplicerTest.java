package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {
        
    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndKeyValuePairSplicer();

    public void test(int leftStart, int leftEnd, Object rightKey, Object rightValue, boolean same) {
        final INode leftNode = TestUtils.create(shift, leftStart, leftEnd);

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assoc(shift, leftNode, rightKey, rightValue, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, false, 0, null, leftNode,
                                                false, 0, rightKey, rightValue);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(leftNode, actualNode); // expected node differs ?
    }

    @Override
    @Test
    public void testDifferent() {
        test(2, 31, new HashCodeKey("key1", 1), "value1", false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(1, 31, new HashCodeKey("collision1", 1), "collision1", false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(1, 31, new HashCodeKey("key1", 1), "differentValue", false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(1, 31, new HashCodeKey("key1", 1), "value1", true);
    }
}
