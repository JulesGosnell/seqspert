package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndArrayNodeSplicer();

    public void test(Object leftKey, Object leftValue, int leftStart, int leftEnd,
                     int rightStart, int rightEnd, boolean leftSame, boolean rightSame) {
        final INode leftNode = TestUtils.create(shift, leftKey, leftValue, leftStart, leftEnd);
        assertTrue(leftNode instanceof ArrayNode);

        final INode rightNode = TestUtils.create(shift, rightStart, rightEnd);
        assertTrue(rightNode instanceof ArrayNode);

        final IFn resolveFunction = rightSame ? NodeUtils.resolveRight: NodeUtils.resolveLeft;
        
        final Counts expectedCounts = new Counts(resolveFunction, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);

        final Counts actualCounts = new Counts(resolveFunction, 0, 0);
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts.sameKey, actualCounts.sameKey);
        assertNodeEquals(expectedNode, actualNode);
        if (leftSame) assertSame(leftNode, actualNode); // expectedNode is not always same !
        if (rightSame) assertSame(rightNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key0", 0), "value0", 1, 18, 15, 32, false, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key17.1", 17), "value17.1", 0, 18, 15, 32, false, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key17", 17), "value17.1", 0, 18, 15, 32, false, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key0", 0), "value0", 1, 31, 0, 31, true, false);
        test(new HashCodeKey("key0", 0), "value0", 1, 30, 0, 31, false, true);
    }
}
