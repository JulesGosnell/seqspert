package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.TestUtils.Hasher;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final Hasher hasher = new Hasher() {public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};
    final Splicer splicer = new HashCollisionNodeAndArrayNodeSplicer();
    final int shift = 0;

    public void test(int leftHash,
                     Object leftKey0, Object leftValue0,
                     Object leftKey1, Object leftValue1,
                     Hasher rightHasher, int rightStart, int rightEnd,
                     Object rightKey0, Object rightValue0,
                     boolean sameRight) {

        final INode leftNode =
            HashCollisionNodeUtils.create(leftHash, leftKey0, leftValue0, leftKey1, leftValue1);
        assertTrue(leftNode instanceof HashCollisionNode);

        final INode rightNode = TestUtils.create(shift, rightHasher, rightStart, rightEnd, rightKey0, rightValue0);
        assertTrue(rightNode instanceof ArrayNode);

        final IFn resolver = sameRight ? Counts.resolveRight : Counts.resolveLeft;

        final Counts expectedCounts = new Counts(resolver, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode,
                                                    rightHasher, rightStart, rightEnd, rightKey0, rightValue0,
                                                    expectedCounts);

        final Counts actualCounts = new Counts(resolver, 0, 0);
        final INode actualNode = splicer.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        final int leftHash = hasher.hash(1);
        test(leftHash,
             new HashCodeKey("key1.0", leftHash), "value1.0",
             new HashCodeKey("key1.1", leftHash), "value1.1",
             hasher, 2, 32,
             null, null,
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        final int leftHash = hasher.hash(1);
        test(leftHash,
             new HashCodeKey("key1.1", leftHash), "value1.1",
             new HashCodeKey("key1.2", leftHash), "value1.2",
             hasher, 1, 32,
             null, null,
             false);
    }

    @Override
    @Test
    public void testSameKey() {
        final int leftHash = hasher.hash(1);
        test(leftHash,
             new HashCodeKey("key1", leftHash), "value1.0.1",
             new HashCodeKey("key1.1", leftHash), "value1.1",
             hasher, 1, 32,
             null, null,
             false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        final int leftHash = hasher.hash(1);
//        test(1,
//             new HashCodeKey("key1", 1), "value1",
//             new HashCodeKey("key1.1", 1), "value1.1",
//             1, 32,
//             null, null,
//             false);

        // TODO: needs debugging...
        test(leftHash,
             new HashCodeKey("key1", leftHash), "value1",
             new HashCodeKey("key1.1", leftHash), "value1.1",
             hasher, 1, 32,
             new HashCodeKey("key1.1", leftHash), "value1.1",
             true);
    }
    
}
