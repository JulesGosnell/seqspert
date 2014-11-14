package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.TestUtils.Hasher;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};

    public void test(Hasher leftHasher, int leftStart, int leftEnd, Object rightKey, Object rightValue, boolean same) {

        final INode leftNode = TestUtils.create(shift, leftHasher, leftStart, leftEnd);
        assertTrue(leftNode instanceof BitmapIndexedNode);

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assoc(shift, leftNode, rightKey, rightValue, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, rightKey, rightValue);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(leftNode, actualNode); // expectedNode not as expected !
    }

    @Override
    @Test
    public void testDifferent() {
        test(hasher, 2, 4, new HashCodeKey("key1", hasher.hash(1)), "value1", false);
        test(hasher, 2, 18, new HashCodeKey("key1", hasher.hash(1)), "value1", false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(hasher, 2, 4, new HashCodeKey("key1", hasher.hash(2)), "value1", false);
        test(hasher, 2, 18, new HashCodeKey("key1", hasher.hash(2)), "value1", false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(hasher, 2, 4, new HashCodeKey("key2", hasher.hash(2)), "value1", false);
        test(hasher, 2, 18, new HashCodeKey("key2", hasher.hash(2)), "value1", false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(hasher, 2, 4, new HashCodeKey("key2", hasher.hash(2)), "value2", true);
        test(hasher, 2, 18, new HashCodeKey("key2", hasher.hash(2)), "value2", true);
    }
    
}
