package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {
    
    
    final Splicer splicer = new HashCollisionNodeAndKeyValuePairSplicer();
    final int shift = 0;

    public void test(int rightHashCode, Object rightKey, Object rightValue, boolean same) {

        final INode leftNode = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});

        final Counts expectedCounts = new Counts(Counts.resolveRight, 0, 0);
        final INode expectedNode = 
            TestUtils.assoc(shift, leftNode, rightKey, rightValue, expectedCounts);

        final Counts actualCounts = new Counts(Counts.resolveRight, 0, 0); // TODO:  resolveLeft ?
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, rightKey, rightValue);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(expectedNode, actualNode);
    }

    // TODO: inline and tidy up

    final int hashCode = 2;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";

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
