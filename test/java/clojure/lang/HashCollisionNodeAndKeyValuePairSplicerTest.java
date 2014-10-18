package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final int hashCode = 2;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";

    // TODO: refactor
    public void test(int rightHashCode, Object rightKey, Object rightValue, boolean same) {
        final INode leftNode = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});

        final Box addedLeaf = new Box(null);
        final INode expectedNode = leftNode.assoc(shift, rightHashCode , rightKey, rightValue, addedLeaf);
        final int expectedCounts = (addedLeaf.val == addedLeaf) ? 0 : 1;
        final Splicer splicer = new HashCollisionNodeAndKeyValuePairSplicer();
        final Counts actualCounts = new Counts(NodeUtils.resolveRight, 0, 0); // TODO: what about resolveLeft ?
        final INode actualNode =  splicer.splice(shift, actualCounts, null, leftNode, rightKey, rightValue);

        assertEquals(expectedCounts, actualCounts.sameKey);
        if (same) 
            assertSame(expectedNode, actualNode);
        else
            assertNodeEquals(expectedNode, actualNode);
    }

    @Test
    @Override
    public void testDifferent() {
        final int rightHashCode = 3;
        final Object rightKey = new HashCodeKey("key2", rightHashCode);
        final Object rightValue = "value2";
        test(rightHashCode, rightKey, rightValue, false);
    }

    @Test
    @Override
    public void testSameKeyHashCode() {
        final int rightHashCode = hashCode;
        final Object rightKey = new HashCodeKey("key2", rightHashCode);
        final Object rightValue = "value2";
        test(rightHashCode, rightKey, rightValue, false);
    }

    @Test
    @Override
    public void testSameKey() {
        final int rightHashCode = hashCode;
        final Object rightKey = new HashCodeKey("key1", rightHashCode);
        final Object rightValue = "value2";
        test(rightHashCode, rightKey, rightValue, false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
        final int rightHashCode = hashCode;
        final Object rightKey = new HashCodeKey("key1", rightHashCode);
        final Object rightValue = "value1";
        test(rightHashCode, rightKey, rightValue, true);
    }

}
