package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();

    public void test(Object leftKey, Object leftValue, Object rightKey, Object rightValue, boolean same) {

        final INode leftNode = TestUtils.create(shift, leftKey, leftValue);

        final Counts expectedCounts = new Counts();
        final INode expected = TestUtils.assoc(shift, leftNode, rightKey, rightValue, expectedCounts);

        final INode rightNode = TestUtils.create(shift, rightKey, rightValue);

        final Counts actualCounts = new Counts(0, 0);
        final INode actual = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expected, actual);
        if (same) assertSame(expected, actual); 
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("leftKey", 2), "leftValue",
             new HashCodeKey("rightKey", 4), "rightValue",
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        final int hashCode = 3;
        test(new HashCodeKey("leftKey", hashCode), "leftValue",
             new HashCodeKey("rightKey", hashCode), "rightValue",
             false);
    }

    @Override
    @Test
    public void testSameKey() {
        final int hashCode = 3;
        test(new HashCodeKey("key", hashCode), "leftValue",
             new HashCodeKey("key", hashCode), "rightValue",
             false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        final int hashCode = 3;
        test(new HashCodeKey("key", hashCode), "value",
             new HashCodeKey("key", hashCode), "value",
             false);
    }
}
