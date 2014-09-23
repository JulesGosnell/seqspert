package clojure.lang;

import static clojure.lang.TestUtils.assertHashCollisionNodeEquals;
import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final int hashCode = 2;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";

    public void test(int rightHashCode, Object rightKey, Object rightValue, boolean same) {
	final INode leftNode = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
	
	final Box addedLeaf = new Box(null);
	final INode expected = leftNode.assoc(shift, rightHashCode , rightKey, rightValue, addedLeaf);
	final int expectedDuplications = (addedLeaf.val == addedLeaf) ? 0 : 1;
	final Splicer splicer = new HashCollisionNodeAndKeyValuePairSplicer();
	final Duplications duplications = new Duplications(0);
	final INode actual =  splicer.splice(shift, duplications, null, leftNode, rightHashCode, rightKey, rightValue);
	
	assertEquals(expectedDuplications, duplications.duplications);
	if (same) 
	    assertSame(expected, actual);
	else
	    assertNodeEquals(expected, actual);
    }

    @Test
    @Override
    public void testNoCollision() {
	final int rightHashCode = 3;
	final Object rightKey = new HashCodeKey("key2", rightHashCode);
	final Object rightValue = "value2";
	test(rightHashCode, rightKey, rightKey, false);
    }

    @Test
    @Override
    public void testCollision() {
	final int rightHashCode = hashCode;
	final Object rightKey = new HashCodeKey("key2", rightHashCode);
	final Object rightValue = "value2";
	test(rightHashCode, rightKey, rightKey, false);
    }

    @Test
    @Override
    public void testDuplication() {
	final int rightHashCode = hashCode;
	final Object rightKey = new HashCodeKey("key2", rightHashCode);
	final Object rightValue = "value2";
	test(rightHashCode, rightKey, rightKey, false);
    }

    @Test
    //@Override
    public void testSomeIdentical() {
	final int rightHashCode = hashCode;
	final Object rightKey = new HashCodeKey("key1", rightHashCode);
	final Object rightValue = "value2";
	test(rightHashCode, rightKey, rightKey, false);
    }

    @Test
    //@Override
    public void testAllIdentical() {
	final int rightHashCode = hashCode;
	final Object rightKey = new HashCodeKey("key1", rightHashCode);
	final Object rightValue = "value1";
	test(rightHashCode, rightKey, rightKey, false);
    }

}
