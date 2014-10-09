package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new KeyValuePairAndBitmapIndexedNodeSplicer();
    final int shift = 0;

    public void test(Object leftKey, Object leftValue, int rightStart, int rightEnd, boolean same) {

        final INode empty = BitmapIndexedNode.EMPTY;

        final INode leftNode = TestUtils.assoc(shift, empty, leftKey, leftValue, new Counts());
        final INode rightNode = TestUtils.assocN(shift, empty, rightStart, rightEnd, new Counts());
        
        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);
            
        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, leftKey, leftValue, 0, null, rightNode);
        
        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode); // TODO - except when null
        if (same) assertSame(expectedNode, actualNode); // ??
    }
    
    @Test
    @Override
    public void testDifferent() {
        test(new HashCodeKey("key1", 1), "value1", 2, 2, false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1", 2), "value1", 2, 2, false);
    }

    @Test
    @Override
    public void testSameKey() {
        test(new HashCodeKey("key2", 2), "value1", 2, 2, false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKeyAndValue() {
        // TODO:
        // There are two cases to tested here.
        // 1. BIN only contains one kvp - identical to lhs - return lhs ?
        // 2. BIN contains >1 kvp - including lhs - return rhs ?
        
        test(new HashCodeKey("key1", 1), "value1", 1, 1, false);
        test(new HashCodeKey("key1", 1), "value1", 1, 2, true);
    }

}
