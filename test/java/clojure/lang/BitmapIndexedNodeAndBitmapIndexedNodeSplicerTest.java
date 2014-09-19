package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.*;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    @Override
    @Test
    public void testNoCollision() {
	
	final Duplications duplications = new Duplications(0);
	final int shift = 0;
	
	final Object leftKey = "left";
	final Object leftValue = 123;
	final Object rightKey = "right";
	final Object rightValue = 456;
	final INode leftNode  = NodeUtils.create(shift, leftKey, leftValue);
	final INode rightNode = NodeUtils.create(shift, rightKey, rightValue);
	
	final INode expected = leftNode.assoc(shift, PersistentHashMap.hash(rightKey), rightKey, rightValue, new Box(null));

	final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
	final INode actual = splicer.splice(shift, duplications, null, leftNode, PersistentHashMap.hash(rightKey), null, rightNode);
	
	assertEquals(0, duplications.duplications);
	assertNodeEquals(expected, actual);
    }

    @Override
    @Test
    public void testCollision() {
	
	final Duplications duplications = new Duplications(0);
	final int shift = 5;
	
	final int hashCode = PersistentHashMap.hash("hashCode");
	final Object leftKey = new HashCodeKey("left", hashCode);
	final Object leftValue = 123;
	final Object rightKey = new HashCodeKey("right", hashCode);
	final Object rightValue = 345;
	
	final INode leftNode  = NodeUtils.create(shift, leftKey, leftValue);
	final INode rightNode = NodeUtils.create(shift, rightKey, rightValue);
	
	final INode expected = leftNode.assoc(shift, hashCode, rightKey, rightValue, new Box(null));

	final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
	final INode actual = splicer.splice(shift, duplications, null, leftNode, hashCode, null, rightNode);
	assertEquals(0, duplications.duplications);
	assertNodeEquals(expected, actual);
    }

    @Override
    @Test
    public void testDuplication() {
	
	final Duplications duplications = new Duplications(0);
	final int shift = 5;
	
	final Object key = "duplicate";
	final int hashCode = PersistentHashMap.hash(key);
	final Object leftValue = 123;
	final Object rightValue = 345;
	final INode leftNode  = NodeUtils.create(shift, key, leftValue);
	final INode rightNode = NodeUtils.create(shift, key, rightValue);
	
	final INode expected =leftNode.assoc(shift, hashCode, key, rightValue, new Box(null));

	final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
	final INode actual = splicer.splice(shift, duplications, null, leftNode, 0, null, rightNode);

	assertEquals(1, duplications.duplications);
	assertNodeEquals(expected, actual);
    }

}
