package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import clojure.lang.TestUtils.Hasher;
import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5); }};
    
    public void test(Object leftKey, Object leftValue,
                     Hasher hasher, int rightStart, int rightEnd, boolean sameRight) {
        
        final INode leftNode  = TestUtils.create(shift, leftKey, leftValue);
        final INode rightNode = TestUtils.create(shift, hasher, rightStart, rightEnd);
        
        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assocN(shift, leftNode, hasher, rightStart, rightEnd, expectedCounts);
        
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
        test(new HashCodeKey("key1", (3 << 10) | (2 << 5)), "value1", hasher, 2, 4, false);
        test(new HashCodeKey("key1", (3 << 10) | (2 << 5)), "value1", hasher, 2, 18, false);

        // Test Node Promotion
        
        // provide 3 ranks in which consecutive nodes are collapsed into hash collisions...
        final Hasher hasher = new Hasher() {
                public int hash(int i) {return ((i / 2 + 2) << 10) | ((i / 2 + 1) << 5) | (i / 2);}};

        // use the Hasher above to create a 16 node BIN with children BIN, HCN, ..., HCN, BIN
        // then splice it into a KVP causing the promotion of all children...
        test(new HashCodeKey("key1", (3 << 10) | (2 << 5) | 1), "value1", hasher, 5, 35, false);
    }

    @Test
    @Override
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1", (4 << 10) | (3 << 5)), "value1", hasher, 2, 4, false);
        test(new HashCodeKey("key1", (4 << 10) | (3 << 5)), "value1", hasher, 2, 18, false);
    }

    @Test
    @Override
    public void testSameKey() {
        test(new HashCodeKey("key2", (4 << 10) | (3 << 5)), "value1", hasher, 2, 4, false);
        test(new HashCodeKey("key2", (4 << 10) | (3 << 5)), "value1", hasher, 2, 18, false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key2", (4 << 10) | (3 << 5)), "value2", hasher, 2, 4, false);
        test(new HashCodeKey("key2", (4 << 10) | (3 << 5)), "value2", hasher, 2, 18, false);
    }

}
