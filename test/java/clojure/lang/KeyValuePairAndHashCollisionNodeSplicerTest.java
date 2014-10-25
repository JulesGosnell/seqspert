package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new KeyValuePairAndHashCollisionNodeSplicer();
    final int shift = 0;

    public void test(Object leftKey, Object leftValue, boolean sameRight) {

        final INode leftNode = BitmapIndexedNodeUtils.create(shift, leftKey, leftValue);

        final INode rightNode =
            new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});

        final Counts expectedCounts = new Counts(Counts.resolveRight, 0, 0);
        final INode expectedNode =
            TestUtils.assoc(shift, leftNode, key0, value0, key1, value1, expectedCounts);
                
        final Counts actualCounts = new Counts(Counts.resolveRight, 0, 0); // TODO: resolveLeft ?
        final INode actualNode =  splicer.splice(shift, actualCounts, false, 0, leftKey, leftValue, false, 0, null, rightNode);

        final int leftHash = BitmapIndexedNodeUtils.hash(leftKey);
        final Counts actualCounts2 = new Counts(Counts.resolveRight, 0, 0); // TODO: resolveLeft ?
        final INode actualNode2 =  splicer.splice(shift, actualCounts2, true, leftHash, leftKey, leftValue, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertEquals(expectedCounts, actualCounts2);
        assertNodeEquals(expectedNode, actualNode);
        assertNodeEquals(expectedNode, actualNode2);
        if (sameRight) {
            assertSame(rightNode, actualNode);
            assertSame(rightNode, actualNode2);
        }
    }

    // TODO: inline and tidy...

    final int hashCode = 2;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";

    @Test
    @Override
    public void testDifferent() {
        test(new HashCodeKey("key2", 3), "value2", false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key2", hashCode), "value2", false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKey() {
        test(new HashCodeKey("key1", hashCode), "value2", false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
        // TODO
        //test(new HashCodeKey("key0", hashCode), "value0", true);
    }

}
