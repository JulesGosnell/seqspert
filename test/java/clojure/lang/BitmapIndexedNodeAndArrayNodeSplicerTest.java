package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class BitmapIndexedNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {@Override
	public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};

    public void test(Object leftKey0, Object leftValue0, Object leftKey1, Object leftValue1, 
                     Hasher rightHasher, int rightStart, int rightEnd, boolean sameRight) {
        final INode leftNode = TestUtils.create(shift, leftKey0, leftValue0, leftKey1, leftValue1);
        final INode rightNode = TestUtils.create(shift, rightHasher, rightStart, rightEnd);

        final Counts expectedCounts = new Counts(Counts.resolveRight, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightHasher, rightStart, rightEnd, expectedCounts);
                
        final Counts actualCounts = new Counts(Counts.resolveRight, 0, 0); // TODO - resolveLeft ?
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }
        
    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key" + 1, hasher.hash(1)), "value1", new HashCodeKey("key" + 2, hasher.hash(2)), "value2", hasher, 3, 31, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key" + 1, hasher.hash(3)), "value1", new HashCodeKey("key" + 2, hasher.hash(4)), "value2", hasher, 3, 31, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value1", new HashCodeKey("key" + 4, hasher.hash(4)), "value2", hasher, 3, 31, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value3", new HashCodeKey("key" + 4, hasher.hash(4)), "value4", hasher, 3, 31, true);
    }
    
}
