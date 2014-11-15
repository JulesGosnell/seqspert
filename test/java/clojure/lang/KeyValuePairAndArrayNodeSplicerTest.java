package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class KeyValuePairAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {@Override
    public int hash(int i) { return ((i + 1) << 10) | (i << 5); }};

    public void test(Object leftKey, Object leftValue,
                     Hasher hasher, int rightStart, int rightEnd, boolean sameRight) {

        final INode leftNode  = TestUtils.create(shift, leftKey, leftValue);
        final INode rightNode = TestUtils.create(shift, hasher, rightStart, rightEnd);
        
        final Counts expectedCounts = new Counts(Counts.resolveRight, 0, 0);
        final INode expectedNode = 
            TestUtils.assocN(shift, leftNode, hasher, rightStart, rightEnd, expectedCounts);

        final Counts actualCounts = new Counts(Counts.resolveRight, 0, 0); // TODO: resolveLeft ?
        final INode actualNode =
            Seqspert.splice(shift, actualCounts, false, 0, leftKey, leftValue, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key1", (2 << 10) | (1 << 5)), "value1", hasher, 2, 31, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1.1", (2 << 10) | (1 << 5)), "value1.1", hasher, 1, 31, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key1", (2 << 10) | (1 << 5)), "value1.1", hasher, 1, 31, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key1", (2 << 10) | (1 << 5)), "value1", hasher, 1, 31, true);
    }
}
