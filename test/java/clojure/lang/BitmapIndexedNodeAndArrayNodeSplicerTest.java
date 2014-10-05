package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    @Override
    @Test
    public void testDifferent() {
	
	final int shift = 5;
	
	final INode leftNode  = NodeUtils.create(shift, new HashCodeKey("left" + 0, 0 * 32), 0);
	
	INode expected = leftNode;
	INode rightNode = BitmapIndexedNode.EMPTY;
	for (int i = 1; i < 18; i++) {
	    final int hash = i * 32;
	    final Object key = new HashCodeKey("left" + i, hash);
	    final Object value = i;
	    expected = expected.assoc(shift, hash , key, value, new Box(null));
	    rightNode = rightNode.assoc(shift, hash , key, value, new Box(null));
	}
	
	final Counts counts = new Counts(0, 0);
	final INode actual = new BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, counts, null, leftNode, 0, null, rightNode);

	assertEquals(0, counts.sameKey);
	assertNodeEquals(actual, expected);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
	
	final int shift = 5;
	
	final INode leftNode  = NodeUtils.create(shift, new HashCodeKey("initial-left" + 0, 0 * 32), 0);
	
	INode expected = leftNode;
	INode rightNode = BitmapIndexedNode.EMPTY;
	for (int i = 0; i < 17; i++) {
	    final int hash = i * 32;
	    final Object key = new HashCodeKey("left" + i, hash);
	    final Object value = i;
	    expected = expected.assoc(shift, hash , key, value, new Box(null));
	    rightNode = rightNode.assoc(shift, hash , key, value, new Box(null));
	}
	
	final Counts counts = new Counts(0, 0);
	final INode actual = new BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, counts, null, leftNode, 0, null, rightNode);

	assertEquals(0, counts.sameKey);
	assertNodeEquals(actual, expected);
    }

    @Override
    @Test
    public void testSameKey() {
	
	final int shift = 5;
	
	final INode leftNode  = NodeUtils.create(shift, new HashCodeKey("left" + 0, 0 * 32), 0);
	
	INode expected = leftNode;
	INode rightNode = BitmapIndexedNode.EMPTY;
	for (int i = 0; i < 17; i++) {
	    final int hash = i * 32;
	    final Object key = new HashCodeKey("left" + i, hash);
	    final Object value = i;
	    expected = expected.assoc(shift, hash , key, value, new Box(null));
	    rightNode = rightNode.assoc(shift, hash , key, value, new Box(null));
	}
	
	final Counts counts = new Counts(0, 0);
	final INode actual = new BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, counts, null, leftNode, 0, null, rightNode);

	assertEquals(1, counts.sameKey);
	assertNodeEquals(actual, expected);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
	// TODO
    }
    
}
