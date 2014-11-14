package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.TestUtils.Hasher;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {
        
    final int shift = 0;
    final Hasher hasher = new Hasher() {public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};
    public void test(Hasher leftHasher, int leftStart, int leftEnd,
                     Object rightKey0, Object rightValue0,
                     Object rightKey1, Object rightValue1,
                     boolean same) {
        final INode leftNode = TestUtils.create(shift, leftHasher, leftStart, leftEnd);
        final INode rightNode = TestUtils.create(shift, rightKey0, rightValue0, rightKey1, rightValue1);
        
        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assoc(shift, leftNode, rightKey0, rightValue0, rightKey1, rightValue1, expectedCounts);
        
        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);
        
        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(leftNode, actualNode); // expectedNode not as expected !
    }

    @Override
    @Test
    public void testDifferent() {
        test(hasher, 3, 31,
             new HashCodeKey("key1", hasher.hash(1)), "value1",
             new HashCodeKey("key2", hasher.hash(2)), "value2",
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(hasher, 2, 31,
             new HashCodeKey("key1", hasher.hash(1)), "value1",
             new HashCodeKey("collisionKey2", hasher.hash(2)), "collisionValue2",
             false);
    }
        
    @Override
    @Test
    public void testSameKey() {
        test(hasher, 2, 31,
             new HashCodeKey("key1", hasher.hash(1)), "value1",
             new HashCodeKey("key2", hasher.hash(2)), "duplicationValue2",
             false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        // rhs is two KVPs
        test(hasher, 1, 31,
             new HashCodeKey("key1", hasher.hash(1)), "value1",
             new HashCodeKey("key2", hasher.hash(2)), "value2",
             true);
        // TODO: might work
        // rhs is an HCN
        // test(hasher, 1, 31,
        //      new HashCodeKey("key1", hasher.hash(1)), "value1",
        //      new HashCodeKey("key2", hasher.hash(1)), "value2",
        //      true);
    }
}
