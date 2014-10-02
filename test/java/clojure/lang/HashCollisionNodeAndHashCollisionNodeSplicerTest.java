package clojure.lang;

import static clojure.lang.TestUtils.assertHashCollisionNodeEquals;
import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    @Test
    @Override
    public void testNoCollision() {
	final int shift = 0;
	final int leftHashCode = 1;
	final int rightHashCode = 33;
	final Object key0 = new HashCodeKey("key0", leftHashCode);
	final Object key1 = new HashCodeKey("key1", leftHashCode);
	final Object key2 = new HashCodeKey("key2", rightHashCode);
	final Object key3 = new HashCodeKey("key3", rightHashCode);
	final Object value0 = "value0";
	final Object value1 = "value1";
	final Object value2 = "value2";
	final Object value3 = "value3";
	final HashCollisionNode leftNode   = new HashCollisionNode(null, leftHashCode,  2, new Object[]{key0, value0, key1, value1});
	final HashCollisionNode rightNode =  new HashCollisionNode(null, rightHashCode, 2, new Object[]{key2, value2, key3, value3});

	final AtomicReference<Thread> edit = new AtomicReference<Thread>();
	INode expected = BitmapIndexedNode.EMPTY;
	Box addedLeaf = null;
	int expectedDuplications = 0;
	addedLeaf = new Box(null);
	expected = expected.assoc(edit, shift, key0.hashCode(), key0, value0, addedLeaf);
	expectedDuplications += (addedLeaf.val == addedLeaf) ? 0 : 1;
	addedLeaf = new Box(null);	
	expected = expected.assoc(edit, shift, key1.hashCode(), key1, value1, addedLeaf);
	expectedDuplications += (addedLeaf.val == addedLeaf) ? 0 : 1;
	addedLeaf = new Box(null);
	expected = expected.assoc(edit, shift, key2.hashCode(), key2, value2, addedLeaf);
	expectedDuplications += (addedLeaf.val == addedLeaf) ? 0 : 1;
	addedLeaf = new Box(null);	
	expected = expected.assoc(edit, shift, key3.hashCode(), key3, value3, addedLeaf);
	expectedDuplications += (addedLeaf.val == addedLeaf) ? 0 : 1;

	final Counts duplications = new Counts(0, 0);
	final INode actual = NodeUtils.splice(shift, duplications, null, leftNode, rightHashCode, null, rightNode);
	assertNodeEquals(expected, actual);
	assertEquals(expectedDuplications, duplications.sameKey);
    }

    final int shift = 0;
    final int hashCode = 2;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object key2 = new HashCodeKey("key2", hashCode);
    final Object key3 = new HashCodeKey("key3", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";
    final Object value2 = "value2";
    final Object value3 = "value3";

    public void test(Object key0, Object value0, Object key1, Object value1,
		     Object key2, Object value2, Object key3, Object value3,
		     boolean same) {
	final HashCollisionNode leftNode   = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
	final HashCollisionNode rightNode =  new HashCollisionNode(null, hashCode, 2, new Object[]{key2, value2, key3, value3});

	final AtomicReference<Thread> edit = new AtomicReference<Thread>();
	HashCollisionNode expected = (HashCollisionNode) leftNode;
	Box addedLeaf = null;
	int expectedDuplications = 0;
	addedLeaf = new Box(null);
	expected = (HashCollisionNode) expected.assoc(edit, shift, hashCode, key2, value2, addedLeaf);
	expectedDuplications += (addedLeaf.val == addedLeaf) ? 0 : 1;
	addedLeaf = new Box(null);	
	expected = (HashCollisionNode) expected.assoc(edit, shift, hashCode, key3, value3, addedLeaf);
	expectedDuplications += (addedLeaf.val == addedLeaf) ? 0 : 1;

	final Counts duplications = new Counts(0, 0);
	final HashCollisionNode actual =  (HashCollisionNode) NodeUtils.splice(shift, duplications, null, leftNode, 0, null, rightNode);
	assertEquals(expectedDuplications, duplications.sameKey);
	assertHashCollisionNodeEquals(expected, actual);
	if (same) assertSame(expected, actual);
    }

    @Test
    @Override
    public void testCollision() {
	// differing keys all have same hashcode but values are different...
	test(key0, value0, key1, value1, key2, value2, key3, value3, false);
	test(key0, value0, key1, value1, key3, value3, key2, value2, false);
    }

    @Test
    @Override
    public void testDuplication() {
	// as above, but one pair of keys is identical...
	final Object leftValue1 = "left-" + (String) value1;
	final Object rightValue1 = "right-" + (String) value1;
	test(key0, value0, key1, leftValue1, key1, rightValue1, key2, value2, false);
	test(key0, value0, key1, leftValue1, key2, value2, key1, rightValue1, false);
    }

    @Test
    //@Override
    public void testSomeIdentical() {
	// as above but one pair of values is also identical...
	test(key0, value0, key1, value1, key1, value1, key2, value2, false);
	test(key0, value0, key1, value1, key2, value2, key1, value1, false);
    }

    @Test
    //@Override
    public void testAllIdentical() {
	// all keys and values are also identical...
	test(key0, value0, key1, value1, key0, value0, key1, value1, true);
    }

}
