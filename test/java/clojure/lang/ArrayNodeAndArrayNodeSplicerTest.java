package clojure.lang;

import static clojure.lang.TestUtils.assertArrayNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndArrayNodeSplicerTest implements SplicerTestInterface {
    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndArrayNodeSplicer();
    
    public void test(int leftStart, int leftEnd, int rightStart, int rightEnd, boolean same) {

	// set up lhs
	INode leftNode = BitmapIndexedNode.EMPTY;
	for (int i = leftStart; i < leftEnd + 1; i++) {
	    final int hashCode = i;
	    final Object key = new HashCodeKey("left" + i, hashCode);
	    final Object value = i;
	    leftNode = leftNode.assoc(shift, hashCode , key, value, new Box(null));
	}
	
	// set up rhs and expected
	ArrayNode expected = (ArrayNode) leftNode;
	int expectedCounts = 0;
	INode rightNode = BitmapIndexedNode.EMPTY;
	for (int i = rightStart; i < rightEnd + 1; i++) {
	    final int hash = i;
	    final Object key = new HashCodeKey("left" + i, hash);
	    final Object value = i;
	    rightNode = rightNode.assoc(shift, hash , key, value, new Box(null));
	    final Box addedLeaf = new Box(null);
	    expected = (ArrayNode) expected.assoc(shift, hash , key, value, addedLeaf);
	    expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	}
	
	// do the splice
	final Counts actualCounts = new Counts(0, 0);
	final ArrayNode actual = (ArrayNode)splicer.splice(shift, actualCounts, null, leftNode, 0, null, rightNode);

	// check everything is as expected...
	assertEquals(expectedCounts, actualCounts.sameKey);
	assertArrayNodeEquals(actual, expected);
	if (same) assertSame(actual, expected);
    }

    @Test
    public void testDifferent() {
	test(0, 17, 17 << 5, 31 << 5, false);	// overlap - but keys and hashcodes different
    }

    @Test
    public void testSameKeyHashCode() {
	//test(0, 17, 17, 31, false);	// overlap - hashcodes same but keys not equal
    }
	
    @Test
    public void testSameKey() {
	test(0, 18, 13, 30, false);	// overlap a few entries and leave empty cells at end of array
    }

    @Test
    public void testSameKeyAndValue() {
	test(0, 31, 0, 31, true);	// overlap all entries
    }
}
