package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

@Ignore // TODO: why are these tests all broken ?
public class KeyValuePairAndKeyValuePairSplicerTest implements SplicerTestInterface {

    final int shift = 0;

    public void test(Object leftKey, Object leftValue,
                     Object rightKey, Object rightValue, boolean same) {

        final INode leftNode = BitmapIndexedNodeUtils.create(shift, leftKey, leftValue);
        final INode rightNode = BitmapIndexedNodeUtils.create(shift, rightKey, rightValue);

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assoc(shift, leftNode, rightKey, rightValue, expectedCounts);
            
        final Counts actualCounts = new Counts();
        final INode actualNode =
            Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        if (same) TestUtils.assertSame(leftNode, expectedNode, actualNode);
        assertNodeEquals(expectedNode, actualNode);
    }
    
    @Test
    @Override
    public void testDifferent() {
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key2", 2), "value2", false);
        test(new HashCodeKey("key1", (1 << 5) | 1), "value1", new HashCodeKey("key2", 1), "value2", false);
    }

    @Test
    @Override
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key2", 1), "value2", false);
    }

    @Test
    @Override
    public void testSameKey() {
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key1", 1), "value2", false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
    	test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key1", 1), "value1", true);
    }

}
