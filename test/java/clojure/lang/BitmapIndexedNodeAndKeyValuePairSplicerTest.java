package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {
    
    final int shift = 0;
    final int leftHashCode = 1;
    final Object leftKey = new HashCodeKey("leftKey", leftHashCode);
    final Object leftValue = "leftValue";
    final INode leftNode = NodeUtils.create(shift, leftKey, leftValue);
    
    final Splicer splicer = new BitmapIndexedNodeAndKeyValuePairSplicer();
    
    @Test
    public void testNoCollision() {
	
	final int rightHashCode = 2;
	final Object rightKey = new HashCodeKey("rightKey", rightHashCode);
	final Object rightValue = "rightValue";

	final INode expected = leftNode.assoc(shift, rightHashCode, rightKey, rightValue, new Box(null));

	final Counts counts = new Counts(0, 0);
	final INode actual = splicer.splice(shift, counts, null, leftNode, rightHashCode, rightKey, rightValue);
	
	assertEquals(0, counts.sameKey);
	assertNodeEquals(expected, actual);
    }

    @Ignore
    @Test
    public void testCollision() {
	final Object rightKey = new HashCodeKey("rightKey", leftHashCode);
	final Object rightValue = "rightValue";

	final INode expected = leftNode.assoc(shift, leftHashCode, rightKey, rightValue, new Box(null));

	final Counts counts = new Counts(0, 0);
	final INode actual = splicer.splice(shift, counts, null, leftNode, leftHashCode, rightKey, rightValue);
	
	assertEquals(0, counts.sameKey);
	assertNodeEquals(expected, actual);
    }

    @Ignore
    @Test
    public void testDuplication() {
	
	final Object rightValue = "rightValue";

	final INode expected = leftNode.assoc(shift, leftHashCode, leftKey, rightValue, new Box(null));

	final Counts counts = new Counts(0, 0);
	final INode actual = splicer.splice(shift, counts, null, leftNode, leftHashCode, leftKey, rightValue);
	
	assertEquals(1, counts.sameKey);
	assertNodeEquals(expected, actual);
    }

}
