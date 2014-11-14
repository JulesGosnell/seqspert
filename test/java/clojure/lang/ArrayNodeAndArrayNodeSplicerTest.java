package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.TestUtils.Hasher;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};

    public void test(Object leftKey, Object leftValue,
                     Hasher leftHasher, int leftStart, int leftEnd,
                     Hasher rightHasher, int rightStart, int rightEnd, boolean leftSame, boolean rightSame) {
        final INode leftNode = TestUtils.create(shift, leftKey, leftValue, leftHasher, leftStart, leftEnd);
        assertTrue(leftNode instanceof ArrayNode);

        final INode rightNode = TestUtils.create(shift, rightHasher, rightStart, rightEnd);
        assertTrue(rightNode instanceof ArrayNode);

        final IFn resolveFunction = rightSame ? Counts.resolveRight: Counts.resolveLeft;
        
        final Counts expectedCounts = new Counts(resolveFunction, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightHasher, rightStart, rightEnd, expectedCounts);

        final Counts actualCounts = new Counts(resolveFunction, 0, 0);
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (leftSame) assertSame(leftNode, actualNode); // expectedNode is not always same !
        if (rightSame) assertSame(rightNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        final int hash = hasher.hash(0);
        test(new HashCodeKey("key0", hash), "value0", hasher, 1, 18, hasher, 15, 32, false, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        final int hash = hasher.hash(17);
        test(new HashCodeKey("key17.1", hash), "value17.1", hasher, 0, 18, hasher, 15, 32, false, false);
    }

    @Override
    @Test
    public void testSameKey() {
        final int hash = hasher.hash(17);
        test(new HashCodeKey("key17", hash), "value17.1", hasher, 0, 18, hasher, 15, 32, false, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        final int hash = hasher.hash(0);
        test(new HashCodeKey("key0", hash), "value0", hasher, 1, 31, hasher, 0, 31, true, false);
        test(new HashCodeKey("key0", hash), "value0", hasher, 1, 30, hasher, 0, 31, false, true);
    }
}
