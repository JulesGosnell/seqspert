package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new HashCollisionNodeAndArrayNodeSplicer();
    final int shift = 0;

    public void test(int leftHash,
                     Object leftKey0, Object leftValue0, Object leftKey1, Object leftValue1,
                     int rightStart, int rightEnd,
                     boolean same) {

        final INode leftNode = HashCollisionNodeUtils.create(leftHash,
                                                             leftKey0, leftValue0, leftKey1, leftValue1);
        assertTrue(leftNode instanceof HashCollisionNode);

        final INode rightNode = TestUtils.assocN(shift, BitmapIndexedNode.EMPTY, rightStart, rightEnd, new Counts());
        assertTrue(rightNode instanceof ArrayNode);

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(expectedNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(1,
             new HashCodeKey("key1.0", 1), "value1.0",
             new HashCodeKey("key1.1", 1), "value1.1",
             2, 31,
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             1, 31,
             false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(1,
             new HashCodeKey("key1", 1), "value1.0.1",
             new HashCodeKey("key1.1", 1), "value1.1",
             1, 31,
             false);
    }

    @Test
    public void testSameKeyAndValue() {
        test(1,
             new HashCodeKey("key1", 1), "value1",
             new HashCodeKey("key1.1", 1), "value1.1",
             1, 31,
             false);
    }
    
}
