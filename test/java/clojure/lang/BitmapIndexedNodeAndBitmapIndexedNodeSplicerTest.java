package clojure.lang;

import static org.junit.Assert.*;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

	

	//-----------------------------------------------------
	
	// BIN(a) | BIN(b) -> BIN(a, b)
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456}
	
	/* (non-Javadoc)
	 * @see clojure.lang.SplicerTest#testNoCollision()
	 */
	@Override
	@Test
	public void testNoCollision() {
	
	    final Duplications duplications = new Duplications(0);
	    final int shift = 0;
	
	    final Object leftKey = "left";
	    final Object leftValue = 123;
	    final Object rightKey = "right";
	    final Object rightValue = 456;
	    final INode leftNode1  = NodeUtils.create(shift, leftKey, leftValue);
	    final INode rightNode1 = NodeUtils.create(shift, rightKey, rightValue);
	
	    final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
	    final BitmapIndexedNode actual = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, PersistentHashMap.hash(rightKey), null, rightNode1);
	    assertEquals(0, duplications.duplications);
	
	    final BitmapIndexedNode expected = (BitmapIndexedNode)leftNode1.assoc(shift, PersistentHashMap.hash(rightKey), rightKey, rightValue, new Box(null));
	    assertEquals(expected.bitmap, actual.bitmap);
	    assertArrayEquals(expected.array, actual.array);
	}

	//-----------------------------------------------------
	
	// BIN(a) | BIN(b) -> BIN(a, b)
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456}
	
	
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456} when key hash codes collide
	
	/* (non-Javadoc)
	 * @see clojure.lang.SplicerTest#testCollision()
	 */
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
	
	    final INode leftNode1  = NodeUtils.create(shift, leftKey, leftValue);
	    final INode rightNode1 = NodeUtils.create(shift, rightKey, rightValue);
	
	    final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
	    final BitmapIndexedNode actual = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, hashCode, null, rightNode1);
	    assertEquals(0, duplications.duplications);
	
	    final BitmapIndexedNode expected = (BitmapIndexedNode)leftNode1.assoc(shift, hashCode, rightKey, rightValue, new Box(null));
	    assertEquals(expected.bitmap, actual.bitmap);
	    // TODO: why are arrays not of same length ?
	    assertEquals(expected.array[0], actual.array[0]);
	
	    final HashCollisionNode collision1 = (HashCollisionNode) expected.array[1];
	    final HashCollisionNode collision2 = (HashCollisionNode) actual.array[1];
	
	    assertEquals(collision1.hash, collision2.hash);
	    assertEquals(collision1.count, collision2.count);
	    assertArrayEquals(collision1.array, collision2.array);
	}

	//-----------------------------------------------------
	
	// BIN(a) | BIN(b) -> BIN(a, b)
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456}
	
	
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456} when key hash codes collide
	
	
	
	// {"duplicate" 123 "duplicate" 456} should= {"duplicate" 123} + {"duplicate" 456}
	
	/* (non-Javadoc)
	 * @see clojure.lang.SplicerTest#testDuplication()
	 */
	@Override
	@Test
	public void testDuplication() {
	
	    final Duplications duplications = new Duplications(0);
	    final int shift = 5;
	
	    final Object key = "duplicate";
	    final int hashCode = PersistentHashMap.hash(key);
	    final Object leftValue = 123;
	    final Object rightValue = 345;
	    final INode leftNode1  = NodeUtils.create(shift, key, leftValue);
	    final INode rightNode1 = NodeUtils.create(shift, key, rightValue);
	
	    final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
	    final BitmapIndexedNode actual = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
	    assertEquals(1, duplications.duplications);
	
	    final BitmapIndexedNode expected = (BitmapIndexedNode)leftNode1.assoc(shift, hashCode, key, rightValue, new Box(null));
	    assertEquals(expected.bitmap, actual.bitmap);
	    // TODO: why are arrays not of same length ?
	    assertEquals(expected.array[0], actual.array[0]);
	    assertEquals(expected.array[1], actual.array[1]);
	}

}
