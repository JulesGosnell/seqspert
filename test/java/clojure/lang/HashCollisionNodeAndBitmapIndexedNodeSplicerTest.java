package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new HashCollisionNodeAndBitmapIndexedNodeSplicer();
    final int shift = 0;

    public void test(int leftHash,
                     Object leftKey0, Object leftValue0, Object leftKey1, Object leftValue1,
                     int rightStart, int rightEnd,
                     Object rightKey0, Object rightValue0,
                     boolean sameLeft) { // TODO: sameRight

        final INode leftNode = HashCollisionNodeUtils.create(leftHash,
                                                             leftKey0, leftValue0, leftKey1, leftValue1);
        assertTrue(leftNode instanceof HashCollisionNode);

        final INode rightNode = TestUtils.assocN(shift, BitmapIndexedNode.EMPTY, rightStart, rightEnd, new Counts());
        assertTrue(rightNode instanceof BitmapIndexedNode);

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameLeft) TestUtils.assertSame(leftNode, expectedNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        // singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             3, 4,
             null, null,
             false);
        // non-singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             3, 5,
             null, null,
             false);
        // promotion to AN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             3, 19,
             null, null,
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        // singleton BIN
        test(1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             1, 2,
             null, null,
             false);
        // non-singleton BIN
        test(1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             1, 3,
             null, null,
             false);
        // promotion to AN
        test(1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             1, 17,
             null, null,
             false);
    }

    @Override
    @Test
    public void testSameKey() {
        // singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1.1",
             new HashCodeKey("key1.1", 1), "value1.1.1",
             1, 2,
             null, null,
             false);
        // non-singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1.1",
             new HashCodeKey("key1.1", 1), "value1.1.1",
             1, 3,
             null, null,
             false);
        // promotion to AN
        test(1,
             new HashCodeKey("key1", 1), "value1.0",
             new HashCodeKey("key1.1", 1), "value1.1",
             1, 17,
             null, null,
             false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        // singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             1, 2,
             null, null,
             false);
        // non-singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             1, 3,
             null, null,
             false);
        // promotion to AN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             1, 17,
             null, null,
             false);
        // TODO: we need to be able to put an HCN under the RHS...
    }
    
}
