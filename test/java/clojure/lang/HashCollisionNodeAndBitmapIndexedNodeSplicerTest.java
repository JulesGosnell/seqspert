package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final int hashCode = 1;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";

    public void test(Object key2, Object value2,
		     Object key3, Object value3,
		     boolean same) {

	final INode leftNode = BitmapIndexedNode.EMPTY
	    .assoc(shift, NodeUtils.hash(key0), key0, value0, new Box(null))
	    .assoc(shift, NodeUtils.hash(key1), key1, value1, new Box(null));
	
	INode expected = leftNode;
	Box addedLeaf = null;
	int expectedCounts = 0;
	addedLeaf = new Box(null);
	expected = expected.assoc(shift, NodeUtils.hash(key0), key0, value0, addedLeaf);
	expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	addedLeaf = new Box(null);
	expected = expected.assoc(shift, NodeUtils.hash(key1), key1, value1, addedLeaf);
	expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	
	final INode rightNode = BitmapIndexedNode.EMPTY
	    .assoc(shift, NodeUtils.hash(key2), key2, value2, new Box(null))
	    .assoc(shift, NodeUtils.hash(key3), key3, value3, new Box(null));

	final Counts counts = new Counts(0, 0);
	final INode actual = NodeUtils.splice(shift, counts, null, leftNode, 0, null, rightNode);
	
	assertEquals(expectedCounts, counts.sameKey);
	if (same)
	    assertSame(expected, actual);
	else
	    assertNodeEquals(expected, actual);
    }

    @Test
    @Override
    public void testDifferent() {
	// TODO
    }

    @Test
    @Override
    public void testSameKeyHashCode() {
	// TODO
    }

    @Test
    @Override
    public void testSameKey() {
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
	// TODO
    }

}
