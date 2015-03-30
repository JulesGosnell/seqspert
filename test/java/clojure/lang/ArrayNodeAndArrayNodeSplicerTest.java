package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class ArrayNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {@Override
    public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};

    public void test(Object leftKey, Object leftValue, Hasher leftHasher, int leftStart, int leftEnd, boolean leftSame,
    		 		 Object rightKey, Object rightValue, Hasher rightHasher, int rightStart, int rightEnd, boolean rightSame) {
 
    	final INode leftNode = TestUtils.create(shift, leftKey, leftValue, leftHasher, leftStart, leftEnd);
        assertTrue(leftNode instanceof ArrayNode);

        final INode rightNode = TestUtils.create(shift, rightKey, rightValue, rightHasher, rightStart, rightEnd);
        assertTrue(rightNode instanceof ArrayNode);

        final Resolver resolver = rightSame ? Counts.rightResolver: Counts.leftResolver;
        
        final Counts expectedCounts = new Counts(resolver, 0, 0);
        final INode expectedNode = TestUtils.merge(shift, leftNode, rightNode, expectedCounts);

        final Counts actualCounts = new Counts(resolver, 0, 0);
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (leftSame) assertSame(leftNode, actualNode); // expectedNode is not always same !
        if (rightSame) assertSame(rightNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(
        		new HashCodeKey("key0", hasher.hash(0)), "value0", hasher, 1, 18, false,
        		new HashCodeKey("key15", hasher.hash(15)), "value15", hasher, 16, 32, false);
        
        test(
        		new HashCodeKey("key0", hasher.hash(0)), "value0", hasher, 1, 18, false,
        		new HashCodeKey("key31.1", hasher.hash(31)), "value31.1", hasher, 11, 32, false);
                
        test(
        		new HashCodeKey("key0", hasher.hash(0)), "value0", hasher, 16, 32, false,
        		// this produces a direct AN->HCN child
        		new HashCodeKey("key15.1", hasher.hash(15)), "value15.1", hasher, 0, 17, false);
    }
    
    @Override
    @Test
    public void testSameKeyHashCode() {
        test(
        		new HashCodeKey("key17.1", hasher.hash(17)), "value17.1", hasher, 0, 18, false,
        		new HashCodeKey("key15", hasher.hash(15)), "value15", hasher, 16, 32, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(
        		new HashCodeKey("key17", hasher.hash(17)), "value17.1", hasher, 0, 18, false,
        		new HashCodeKey("key15", hasher.hash(15)), "value15", hasher, 16, 32, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(
        		new HashCodeKey("key0", hasher.hash(0)), "value0", hasher, 1, 31, true,
        		new HashCodeKey("key0", hasher.hash(0)), "value0", hasher, 1, 31, false);
        test(
        		new HashCodeKey("key0", hasher.hash(0)), "value0", hasher, 1, 30, false,
        		new HashCodeKey("key0", hasher.hash(0)), "value0", hasher, 1, 31, true);
    }
}
