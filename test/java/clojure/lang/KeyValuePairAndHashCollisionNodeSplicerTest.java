package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class KeyValuePairAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;

    public void test(Object leftKey, Object leftValue,
                     Hasher hasher, int rightStart, int rightEnd, boolean sameRight) {

        final INode leftNode = TestUtils.create(shift, leftKey, leftValue);
        final INode rightNode = TestUtils.create(shift, hasher, rightStart, rightEnd);

        final Counts expectedCounts = new Counts(Counts.resolveRight, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode,
                                                    hasher, rightStart, rightEnd, expectedCounts);
                
        final Counts actualCounts = new Counts(Counts.resolveRight, 0, 0); // TODO: resolveLeft ?
        final INode actualNode =  Seqspert.splice(shift, actualCounts, false, 0, leftKey, leftValue, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        // TODO
        // if (sameRight) assertSame(rightNode, actualNode);
    }

    final Hasher hasher = new Hasher() {public int hash(int i) { return (3 << 10) | (2 << 5); }};

    @Test
    @Override
    public void testDifferent() {
        test(new HashCodeKey("key1", (2 << 10) | (1 << 5)), "value1", hasher, 2, 4, false);
        test(new HashCodeKey("key1", (3 << 10) | (3 << 5)), "value1", hasher, 2, 4, false);

        final int leftHashCode = (4 << 15) | (3 << 10) | (2 << 5); // like rhs but 1 deeper
        test(new HashCodeKey("key1", leftHashCode), "value1", hasher, 2, 4, false);
    }

    @Test
    @Override
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1.1", (2 << 10) | (1 << 5)), "value1.1", hasher, 1, 3, false);
    }

    @Test
    @Override
    public void testSameKey() {
        test(new HashCodeKey("key1", (2 << 10) | (1 << 5)), "value1.1", hasher, 1, 3, false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key1", (2 << 10) | (1 << 5)), "value1", hasher, 1, 3, false);
    }

}
