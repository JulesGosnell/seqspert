package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class BitmapIndexedNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {@Override
    public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};
    public void test(Object leftKey, Object leftValue,
                     int rightHash,
                     Object rightKey0, Object rightValue0, Object rightKey1, Object rightValue1,
                     boolean sameLeft, boolean sameRight) {

        final INode leftNode = TestUtils.create(shift, leftKey, leftValue);
        final INode rightNode = HashCollisionNodeUtils.create(rightHash,
                                                              rightKey0, rightValue0,
                                                              rightKey1, rightValue1);
        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.merge(shift, leftNode, rightNode, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameLeft) TestUtils.assertSame(leftNode, expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }

    public void test(Hasher leftHasher, int leftStart, int leftEnd,
                     int rightHash,
                     Object rightKey0, Object rightValue0, Object rightKey1, Object rightValue1,
                     boolean sameLeft, boolean sameRight) {

        final INode leftNode = TestUtils.create(shift, leftHasher, leftStart, leftEnd);
        final INode rightNode = HashCollisionNodeUtils.create(rightHash,
                                                              rightKey0, rightValue0,
                                                              rightKey1, rightValue1);
        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assoc(shift, leftNode, rightKey0, rightValue0, rightKey1, rightValue1, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameLeft) TestUtils.assertSame(leftNode, expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key0", hasher.hash(0)), "value0",
             hasher.hash(1),
             new HashCodeKey("key1.0", hasher.hash(1)), "value1.0",
             new HashCodeKey("key1.1", hasher.hash(1)), "value1.1",
             false, false);
        test(hasher, 3, 19,
             hasher.hash(1),
             new HashCodeKey("key1.0", hasher.hash(1)), "value1.0",
             new HashCodeKey("key1.1", hasher.hash(1)), "value1.1",
             false, false);

    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1.0", hasher.hash(1)), "value1.0",
             hasher.hash(1),
             new HashCodeKey("key1.1", hasher.hash(1)), "value1.1",
             new HashCodeKey("key1.2", hasher.hash(1)), "value1.2",
             false, false);
        test(hasher, 1, 17,
             hasher.hash(1),
             new HashCodeKey("key1.1", hasher.hash(1)), "value1.1",
             new HashCodeKey("key1.2", hasher.hash(1)), "value1.2",
             false, false);
    }
        
    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key1.0", hasher.hash(1)), "value1.0.0",
             hasher.hash(1),
             new HashCodeKey("key1.0", hasher.hash(1)), "value1.0.1",
             new HashCodeKey("key1.1", hasher.hash(1)), "value1.1",
             false, false);
        test(hasher, 1, 17,
             hasher.hash(1),
             new HashCodeKey("key1",   hasher.hash(1)), "value1.0",
             new HashCodeKey("key1.1", hasher.hash(1)), "value1.1",
             false, false);
    }


    @Override
    @Test
    public void testSameKeyAndValue() {
        // TODO: test left and right sameness
        test(new HashCodeKey("key1.0", hasher.hash(1)), "value1.0",
             hasher.hash(1),
             new HashCodeKey("key1.0", hasher.hash(1)), "value1.0",
             new HashCodeKey("key1.1", hasher.hash(1)), "value1.1",
             false, false);
        test(hasher, 1, 17,
             hasher.hash(1),
             new HashCodeKey("key1",   hasher.hash(1)), "value1",
             new HashCodeKey("key1.1", hasher.hash(1)), "value1.1",
             false, false);
    }
    
}
