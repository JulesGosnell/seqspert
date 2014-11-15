package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class ArrayNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {@Override
    public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};

    public void test(Hasher leftHasher, int leftStart, int leftEnd,
                     Object leftKey, Object leftValue,
                     int rightHash,
                     Object rightKey0, Object rightValue0,
                     Object rightKey1, Object rightValue1,
                     boolean same) {
        final INode leftNode = TestUtils.create(shift, leftHasher, leftStart, leftEnd, leftKey, leftValue);

        final INode rightNode =
            HashCollisionNodeUtils.create(rightHash, rightKey0, rightValue0, rightKey1, rightValue1);
        
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
        final int rightHash = hasher.hash(1);
        test(hasher, 2, 31, null, null,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("collision1", rightHash), "collision1",
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        // HCN is getting buried in one too many BINs   
        final int rightHash = hasher.hash(1);
        test(hasher, 1, 31, null, null,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("collision1", rightHash), "collision1",
             false);
    }

    @Override
    @Test
    public void testSameKey() {
        final int rightHash = hasher.hash(1);
        test(hasher, 1, 31, null, null,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("key1", rightHash), "collision1",
             false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        final int hash = hasher.hash(1);
        test(hasher, 1, 31,
             new HashCodeKey("key1.1", hash), "value1.1",
             hash,
             new HashCodeKey("key1",   hash), "value1",
             new HashCodeKey("key1.1", hash), "value1.1",
             true);
    }

}
