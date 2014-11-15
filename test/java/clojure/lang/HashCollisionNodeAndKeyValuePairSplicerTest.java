package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class HashCollisionNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {


    final Hasher hasher = new Hasher() {@Override
    public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};
    final int shift = 0;

    public void test(int leftHashCode,
                     Object leftKey0, Object leftValue0,
                     Object leftKey1, Object leftValue1,
                     Object rightKey, Object rightValue,
                     boolean same) {

        final INode leftNode =new HashCollisionNode(null,
                                                    leftHashCode,
                                                    2,
                                                    new Object[]{
                                                        leftKey0, leftValue0, leftKey1, leftValue1});

        final Counts expectedCounts = new Counts(Counts.resolveRight, 0, 0);
        final INode expectedNode = TestUtils.assoc(shift, leftNode, rightKey, rightValue, expectedCounts);

        final Counts actualCounts = new Counts(Counts.resolveRight, 0, 0); // TODO:  resolveLeft ?
        final INode actualNode =
            Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, rightKey, rightValue);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode); 
        if (same) assertSame(expectedNode, actualNode);
    }

    final int leftHashCode = hasher.hash(2);
    final Object leftKey0 = new HashCodeKey("key0", leftHashCode);
    final Object leftKey1 = new HashCodeKey("key1", leftHashCode);
    final Object leftValue0 = "value0";
    final Object leftValue1 = "value1";

    @Test
    @Override
    public void testDifferent() {
        final int rightHashCode = hasher.hash(3);
        final Object rightKey = new HashCodeKey("key2", rightHashCode);
        final Object rightValue = "value2";
        test(leftHashCode, leftKey0, leftValue0, leftKey1, leftValue1, rightKey, rightValue, false);
    }

    @Test
    public void testDifferentRecursive() {
        final int leftHashCode = (3 << 10) | (2 << 5);
        final int rightHashCode = (4 << 15) | leftHashCode;
        test(leftHashCode, 
             new HashCodeKey("key0", leftHashCode), "value0",
             new HashCodeKey("key1", leftHashCode), "value1",
             new HashCodeKey("key2", rightHashCode), "value2",
             false);             
    }

    @Test
    @Override
    public void testSameKeyHashCode() {
        final int rightHashCode = leftHashCode;
        final Object rightKey = new HashCodeKey("key2", rightHashCode);
        final Object rightValue = "value2";
        test(leftHashCode, leftKey0, leftValue0, leftKey1, leftValue1, rightKey, rightValue, false);
    }

    @Test
    @Override
    public void testSameKey() {
        final int rightHashCode = leftHashCode;
        final Object rightKey = new HashCodeKey("key1", rightHashCode);
        final Object rightValue = "value2";
        test(leftHashCode, leftKey0, leftValue0, leftKey1, leftValue1, rightKey, rightValue, false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
        final int rightHashCode = leftHashCode;
        final Object rightKey = new HashCodeKey("key1", rightHashCode);
        final Object rightValue = "value1";
        test(leftHashCode, leftKey0, leftValue0, leftKey1, leftValue1, rightKey, rightValue, true);
    }

}
