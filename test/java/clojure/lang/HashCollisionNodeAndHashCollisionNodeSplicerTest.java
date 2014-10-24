package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;

// we can't build an HCN directly via INode.assoc, which is what we
// want to do, but we can indirectly, i.e. build a BIN-HCN
// graph. Splicing a similar graph into this will have the desired
// consequences.
public class HashCollisionNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;

    public void test(Object key0, Object value0, Object key1, Object value1,
                     Object key2, Object value2, Object key3, Object value3, Object key4, Object value4,
                     boolean sameLeft, boolean sameRight) {

        final INode leftNode = TestUtils.create(shift, key0, value0, key1, value1);
        final INode rightNode = TestUtils.create(shift, key2, value2, key3, value3, key4, value4);

        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assoc(shift, leftNode, key2, value2, key3, value3, key4, value4, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = NodeUtils.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameLeft) TestUtils.assertSame(leftNode, expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }

    final int hashCode = 2;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object key2 = new HashCodeKey("key2", hashCode);
    final Object key3 = new HashCodeKey("key3", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";
    final Object value2 = "value2";
    final Object value3 = "value3";

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

        test(key0, value0, key1, value1, key2, value2, key3, value3, null, null, false, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        // differing keys all have same hashcode but values are different...
        test(key0, value0, key1, value1, key2, value2, key3, value3, null, null, false, false);
        test(key0, value0, key1, value1, key3, value3, key2, value2, null, null, false, false);

        final int leftHashCode = (hashCode << 10) | (hashCode << 5) | hashCode;
        final int rightHashCode = (hashCode << 15) | (hashCode << 10) | (hashCode << 5) | hashCode;

        test(new HashCodeKey("key0", leftHashCode), value0,
             new HashCodeKey("key1", leftHashCode), value1,
             new HashCodeKey("key3", rightHashCode), value3,
             new HashCodeKey("key2", rightHashCode), value2,
             null, null,
             false, false);
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
