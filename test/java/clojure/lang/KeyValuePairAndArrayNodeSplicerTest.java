package clojure.lang;

import static clojure.lang.NodeUtils.create;
import static clojure.lang.NodeUtils.nodeHash;
import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndArrayNodeSplicerTest implements SplicerTestInterface {
	
    final int shift = 0;
    final Splicer splicer = new KeyValuePairAndArrayNodeSplicer();
    
    public void test(Object leftKey, Object leftValue, int rightStart, int rightEnd, boolean same) {

	// set up rhs and expected
	INode expected = create(shift, leftKey, leftValue);
	int expectedCounts = 0;
	INode rightNode = BitmapIndexedNode.EMPTY;
	for (int i = rightStart; i < rightEnd + 1; i++) {
	    final int hashCode = i;
	    final Object key = new HashCodeKey("key" + i, hashCode);
	    final Object value = i;
	    rightNode = rightNode.assoc(shift, hashCode, key, value, new Box(null));
	    final Box addedLeaf = new Box(null);
	    expected = expected.assoc(shift, hashCode , key, value, addedLeaf);
	    expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	}
	
	// do the splice
	final Counts actualCounts = new Counts(0, 0);
	final INode actual = splicer.splice(shift, actualCounts, leftKey, leftValue, nodeHash(rightNode), null, rightNode);

	// check everything is as expected...
	assertEquals(expectedCounts, actualCounts.sameKey);
	assertNodeEquals(expected, actual);
    }

    @Test
    public void testDifferent() {
	test(new HashCodeKey("key1", 1), 1, 2, 30, false);
    }

    @Test
    public void testSameKeyHashCode() {
	test(new HashCodeKey("collision", 1), 1, 1, 30, false);
    }
	
    @Test
    public void testSameKey() {
	test(new HashCodeKey("key1", 2), 1, 1, 30, false);
    }

    @Test
    public void testSameKeyAndValue() {
	test(new HashCodeKey("key1", 1), 1, 1, 30, true);
    }
}
