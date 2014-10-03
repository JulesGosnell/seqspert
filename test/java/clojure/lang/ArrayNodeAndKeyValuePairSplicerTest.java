package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.*;
import static clojure.lang.NodeUtils.*;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {
    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndKeyValuePairSplicer();
    
// TODO: reorder param list
    public void test(Object rightKey, Object rightValue, int leftStart, int leftEnd, boolean same) {

	// set up lhs and expected
	INode leftNode = BitmapIndexedNode.EMPTY;
	for (int i = leftStart; i < leftEnd + 1; i++) {
	    final int hashCode = i;
	    final Object key = new HashCodeKey("key" + i, hashCode);
	    final Object value = i;
	    leftNode = leftNode.assoc(shift, hashCode, key, value, new Box(null));
	}
	INode expected = leftNode;
	int expectedCounts = 0;
	final Box addedLeaf = new Box(null);
	expected = expected.assoc(shift, NodeUtils.hash(rightKey) , rightKey, rightValue, addedLeaf);
	expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;
	
	// do the splice
	final Counts actualCounts = new Counts(0, 0);
	final INode actual = splicer.splice(shift, actualCounts, null, leftNode, NodeUtils.hash(rightKey), rightKey, rightValue);

	// check everything is as expected...
	assertEquals(expectedCounts, actualCounts.sameKey);
	assertNodeEquals(expected, actual);
    }

    @Test
    public void testNoCollision() {
	test(new HashCodeKey("key1", 1), 1, 2, 30, false);
    }

    @Test
    public void testCollision() {
	test(new HashCodeKey("collision", 1), 1, 1, 30, false);
    }
	
    @Test
    public void testDuplication() {
	test(new HashCodeKey("key1", 2), 1, 1, 30, false);
    }

    @Test
    public void testSame() {
	test(new HashCodeKey("key1", 1), 1, 1, 30, true);
    }
}
