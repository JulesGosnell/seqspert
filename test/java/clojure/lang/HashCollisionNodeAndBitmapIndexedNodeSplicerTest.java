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
                     Object rightKey1, Object rightValue1,
                     boolean sameLeft, boolean sameRight) {

        final INode leftNode = HashCollisionNodeUtils.create(leftHash,
                                                             leftKey0, leftValue0, leftKey1, leftValue1);
        assertTrue(leftNode instanceof HashCollisionNode);

        final INode right1TmpNode = TestUtils.assocN(shift, BitmapIndexedNode.EMPTY, rightStart, rightEnd, new Counts());
        final INode rightTmp2Node = (rightKey0 != null && rightValue0 != null) ? TestUtils.assoc(shift, right1TmpNode, rightKey0, rightValue0, new Counts()) : right1TmpNode;
        final INode rightNode = (rightKey1 != null && rightValue1 != null) ? TestUtils.assoc(shift, rightTmp2Node, rightKey1, rightValue1, new Counts()) : rightTmp2Node;
        assertTrue(rightNode instanceof BitmapIndexedNode);

        final Counts expectedCounts = new Counts(sameRight ? NodeUtils.resolveRight :  NodeUtils.resolveLeft, 0, 0);
        final INode expectedTmp1Node = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);
        final INode expectedmp2Node = (rightKey0 != null && rightValue0 != null) ? TestUtils.assoc(shift, expectedTmp1Node, rightKey0, rightValue0, expectedCounts) : expectedTmp1Node;
        final INode expectedNode = (rightKey1 != null && rightValue1 != null) ? TestUtils.assoc(shift, expectedmp2Node, rightKey1, rightValue1, expectedCounts) : expectedmp2Node;

        final Counts actualCounts = new Counts(sameRight ? NodeUtils.resolveRight :  NodeUtils.resolveLeft, 0, 0);
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameLeft) assertSame(leftNode, actualNode); // expectedNode not as expected !
        if (sameRight) assertSame(rightNode, actualNode);
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
             null, null, false, false);
        // non-singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             3, 5,
             null, null,
             null, null, false, false);
        // promotion to AN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             3, 19,
             null, null,
             null, null, false, false);
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
             null, null, false, false);
        // non-singleton BIN
        test(1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             1, 3,
             null, null,
             null, null, false, false);
        // promotion to AN
        test(1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             1, 17,
             null, null,
             null, null, false, false);
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
             null, null, false, false);
        // non-singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1.1",
             new HashCodeKey("key1.1", 1), "value1.1.1",
             1, 3,
             null, null,
             null, null, false, false);
        // promotion to AN
        test(1,
             new HashCodeKey("key1", 1), "value1.0",
             new HashCodeKey("key1.1", 1), "value1.1",
             1, 17,
             null, null,
             null, null, false, false);
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
             null, null,
             false, false);
        // non-singleton BIN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             1, 3,
             null, null,
             null, null,
             false, false);
        // promotion to AN
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key2", 1), "value2",
             1, 17,
             null, null,
             null, null,
             false, false);
        
        // leftSame
        test(1,
                new HashCodeKey("key1", 1), "value1",
                new HashCodeKey("key2", 1), "value2",
                1, 2,
                new HashCodeKey("key2", 1), "value2",
                null, null,
                true, false);
        // rightSame
        test(1,
                new HashCodeKey("key1", 1), "value1",
                new HashCodeKey("key2", 1), "value2",
                1, 3,
                new HashCodeKey("key2", 1), "value2",
                new HashCodeKey("key3", 1), "value3",
                false, true);
    }
    
}
