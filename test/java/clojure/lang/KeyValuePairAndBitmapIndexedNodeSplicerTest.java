package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import clojure.lang.TestUtils.Hasher;
import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {public int hash(int i) { return i << 10; }};
    
    public void test(Object leftKey, Object leftValue,
                     Hasher hasher, int rightStart, int rightEnd, boolean sameRight) {
        
        final INode leftNode  = TestUtils.create(shift, leftKey, leftValue);
        final INode rightNode = TestUtils.create(shift, hasher, rightStart, rightEnd);
        
        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assocN(shift, hasher, leftNode, rightStart, rightEnd, expectedCounts);
        
        final Counts actualCounts = new Counts();
        final INode actualNode = 
            Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        // TODO - debug sameRight
        //if (sameRight) assertSame(rightNode, actualNode);
    }

    @Test
    @Override
    public void testDifferent() {
        test(new HashCodeKey("key1", 1 << 5), "value1", hasher, 2, 3, false);

        // promotion
        test(new HashCodeKey("key1", 1 << 5), "value1", hasher, 2, 18, false);
    }

    @Test
    @Override
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1",   2 << 5), "value1", hasher, 2, 3, false);
        test(new HashCodeKey("key1.1", 1 << 5), "value1", hasher, 1, 3, false);
    }

    @Test
    @Override
    public void testSameKey() {
        test(new HashCodeKey("key2",   2 << 5), "value1", hasher, 2, 3, false);
        test(new HashCodeKey("key2.1", 2 << 5), "value2", hasher, 2, 4, false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
        // TODO:
        // There are two cases to tested here.
        // 1. BIN only contains one kvp - identical to lhs - return lhs ?
        // 2. BIN contains >1 kvp - including lhs - return rhs ?
        
        test(new HashCodeKey("key1", 1 << 5), "value1", hasher, 1, 2, true);
        test(new HashCodeKey("key1", 1 << 5), "value1", hasher, 1, 3, true);
    }

}
