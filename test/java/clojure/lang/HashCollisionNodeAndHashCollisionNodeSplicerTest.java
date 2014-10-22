package clojure.lang;

import static clojure.lang.TestUtils.assertHashCollisionNodeEquals;
import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    // TODO: refactor

    @Override
    @Test
    public void testDifferent() {
        final int shift = 0;
        final int leftHashCode = 1;
        final int rightHashCode = 33;
        final Object key0 = new HashCodeKey("key0", leftHashCode);
        final Object key1 = new HashCodeKey("key1", leftHashCode);
        final Object key2 = new HashCodeKey("key2", rightHashCode);
        final Object key3 = new HashCodeKey("key3", rightHashCode);
        final Object value0 = "value0";
        final Object value1 = "value1";
        final Object value2 = "value2";
        final Object value3 = "value3";
        final HashCollisionNode leftNode   = new HashCollisionNode(null, leftHashCode,  2, new Object[]{key0, value0, key1, value1});
        final HashCollisionNode rightNode =  new HashCollisionNode(null, rightHashCode, 2, new Object[]{key2, value2, key3, value3});

        final AtomicReference<Thread> edit = new AtomicReference<Thread>();
        INode expected = BitmapIndexedNode.EMPTY;
        Box addedLeaf = null;
        int expectedCounts = 0;
        addedLeaf = new Box(null);
        expected = expected.assoc(edit, shift, key0.hashCode(), key0, value0, addedLeaf);
        expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
        addedLeaf = new Box(null);      
        expected = expected.assoc(edit, shift, key1.hashCode(), key1, value1, addedLeaf);
        expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
        addedLeaf = new Box(null);
        expected = expected.assoc(edit, shift, key2.hashCode(), key2, value2, addedLeaf);
        expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
        addedLeaf = new Box(null);      
        expected = expected.assoc(edit, shift, key3.hashCode(), key3, value3, addedLeaf);
        expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;

        final Counts counts = new Counts(NodeUtils.resolveRight, 0, 0); // TODO: what about resolveLeft ?
        final INode actual = NodeUtils.splice(shift, counts, null, leftNode, null, rightNode);
        assertNodeEquals(expected, actual);
        assertEquals(expectedCounts, counts.sameKey);
    }

    final int shift = 0;
    final int hashCode = 2;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object key2 = new HashCodeKey("key2", hashCode);
    final Object key3 = new HashCodeKey("key3", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";
    final Object value2 = "value2";
    final Object value3 = "value3";

    public void test(Object key0, Object value0, Object key1, Object value1,
                     Object key2, Object value2, Object key3, Object value3, Object key4, Object value4,
                     boolean sameLeft, boolean sameRight) {

        final INode leftNode = TestUtils.create(shift, key0, value0, key1, value1);

        final INode rightTmpNode =  TestUtils.create(shift, key2, value2, key3, value3);
        final INode rightNode =
            (key4 == null && value4 == null) ?
            rightTmpNode :
            TestUtils.assoc(shift, rightTmpNode, key4, value4, new Counts());

        final Counts expectedCounts = new Counts();
        final INode expectedTmpNode = 
            TestUtils.assoc(shift, 
                            TestUtils.assoc(shift, leftNode, key2, value2, expectedCounts),
                            key3, value3, expectedCounts);
        final INode expectedNode =
            (key4 == null && value4 == null) ?
            expectedTmpNode :
            TestUtils.assoc(shift, expectedTmpNode, key4, value4, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = NodeUtils.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameLeft) TestUtils.assertSame(leftNode, expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        // differing keys all have same hashcode but values are different...
        test(key0, value0, key1, value1, key2, value2, key3, value3, null, null, false, false);
        test(key0, value0, key1, value1, key3, value3, key2, value2, null, null, false, false);
    }

    @Override
    @Test
    public void testSameKey() {
        // as above, but one pair of keys is identical...
        final Object leftValue1 = "left-" + (String) value1;
        final Object rightValue1 = "right-" + (String) value1;
        test(key0, value0, key1, leftValue1, key1, rightValue1, key2, value2, null, null, false, false);
        test(key0, value0, key1, leftValue1, key2, value2, key1, rightValue1, null, null, false, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        // as above but one pair of values is also identical...
        // some
        test(key0, value0, key1, value1, key1, value1, key2, value2, null, null, false, false);
        test(key0, value0, key1, value1, key2, value2, key1, value1, null, null, false, false);
        // all
        test(key0, value0, key1, value1, key0, value0, key1, value1, null, null, true, false);
        test(key0, value0, key1, value1, key0, value0, key1, value1, key2, value2, false, true);
    }

}
