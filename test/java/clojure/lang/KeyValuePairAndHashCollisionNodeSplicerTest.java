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
    final int hashCode = 2;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";

    public void test(Object leftKey, Object leftValue, boolean sameRight) {

        final INode leftNode = NodeUtils.create(shift, leftKey, leftValue);

        Box addedLeaf = null;
        int expectedCounts = 0;
        INode expected = leftNode;
                
        addedLeaf = new Box(null);
        expected = expected.assoc(shift, key0.hashCode() , key0, value0, addedLeaf);
        expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;

        addedLeaf = new Box(null);
        expected = expected.assoc(shift, key1.hashCode() , key1, value1, addedLeaf);
        expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;

        final INode rightNode = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});

        final Counts counts = new Counts(NodeUtils.resolveRight, 0, 0); // TODO: what about resolveLeft ?
        final INode actual =  splicer.splice(shift, counts, leftKey, leftValue, null, rightNode);

        assertEquals(expectedCounts, counts.sameKey);
        assertNodeEquals(expected, actual);
        if (sameRight) assertSame(expected, actual);
    }

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
