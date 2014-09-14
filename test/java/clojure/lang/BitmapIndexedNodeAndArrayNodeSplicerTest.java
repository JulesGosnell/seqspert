package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.*;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndArrayNodeSplicerTest
//implements SplicerTestInterface 
{

	

	//-----------------------------------------------------
	
	// BIN(a) | BIN(b) -> BIN(a, b)
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456}
	
	
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456} when key hash codes collide
	
	
	
	// {"duplicate" 123 "duplicate" 456} should= {"duplicate" 123} + {"duplicate" 456}
	
	
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when right key hash codes collide
	
	
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when all key hash codes collide
	
	
	
	// {"left" 123 "left" 456 "right" 789} should= {"left" 123} + {"left" 456 "right" 789} when all key hash codes collide and duplicate keys are present
	
	@Test
	public void testBitmapIndexedNodeAndArrayNodeSplicerNoCollision() {
	
	    final int shift = 5;
	
	    final INode leftNode1  = NodeUtils.create(shift, new HashCodeKey("left" + 0, 0 * 32), 0);
	
	    INode leftNode2 = leftNode1;
	    for (int i = 1; i < 18; i++) {
	        leftNode2 = leftNode2.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	    final ArrayNode expected = (ArrayNode) leftNode2;
	
	    INode rightNode1 = BitmapIndexedNode.EMPTY;
	    for (int i = 1; i < 18; i++) {
	        rightNode1 = rightNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	
	    final Duplications duplications = new Duplications(0);
	    final ArrayNode actual = (ArrayNode)  new BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
	    // TODO: numDuplicates
	
	    assertEquals(actual.count, expected.count);
	    for (int i = 0; i < 18; i++) {
	        assertBitmapIndexedNodesEqual((BitmapIndexedNode) actual.array[i], (BitmapIndexedNode) expected.array[i]);
	    }
	}

	//-----------------------------------------------------
	
	// BIN(a) | BIN(b) -> BIN(a, b)
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456}
	
	
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456} when key hash codes collide
	
	
	
	// {"duplicate" 123 "duplicate" 456} should= {"duplicate" 123} + {"duplicate" 456}
	
	
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when right key hash codes collide
	
	
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when all key hash codes collide
	
	
	
	// {"left" 123 "left" 456 "right" 789} should= {"left" 123} + {"left" 456 "right" 789} when all key hash codes collide and duplicate keys are present
	
	@Test
	public void testBitmapIndexedNodeAndArrayNodeSplicerCollision() {
	
	    final int shift = 5;
	
	    final INode leftNode1  = NodeUtils.create(shift, new HashCodeKey("initial-left" + 0, 0 * 32), 0);
	
	    INode leftNode2 = leftNode1;
	    for (int i = 0; i < 17; i++) {
	        leftNode2 = leftNode2.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	    final ArrayNode expected = (ArrayNode) leftNode2;
	
	    INode rightNode1 = BitmapIndexedNode.EMPTY;
	    for (int i = 0; i < 17; i++) {
	        rightNode1 = rightNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	
	    final Duplications duplications = new Duplications(0);
	    final ArrayNode actual = (ArrayNode) new BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
	    // TODO: numDuplicates
	
	    assertEquals(actual.count, expected.count);
	    assertHashCollisionNodesEqual((HashCollisionNode) actual.array[0], (HashCollisionNode) expected.array[0]);
	    for (int i = 1; i < 17; i++) {
	        assertBitmapIndexedNodesEqual((BitmapIndexedNode) actual.array[i], (BitmapIndexedNode) expected.array[i]);
	    }
	}

	//-----------------------------------------------------
	
	// BIN(a) | BIN(b) -> BIN(a, b)
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456}
	
	
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456} when key hash codes collide
	
	
	
	// {"duplicate" 123 "duplicate" 456} should= {"duplicate" 123} + {"duplicate" 456}
	
	
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when right key hash codes collide
	
	
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when all key hash codes collide
	
	
	
	// {"left" 123 "left" 456 "right" 789} should= {"left" 123} + {"left" 456 "right" 789} when all key hash codes collide and duplicate keys are present
	
	@Test
	public void testBitmapIndexedNodeAndArrayNodeSplicerDuplication() {
	
	    final int shift = 5;
	
	    final INode leftNode1  = NodeUtils.create(shift, new HashCodeKey("left" + 0, 0 * 32), 0);
	
	    INode leftNode2 = leftNode1;
	    for (int i = 0; i < 17; i++) {
	        leftNode2 = leftNode2.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	    final ArrayNode expected = (ArrayNode) leftNode2;
	
	    INode rightNode1 = BitmapIndexedNode.EMPTY;
	    for (int i = 0; i < 17; i++) {
	        rightNode1 = rightNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	    }
	
	    final Duplications duplications = new Duplications(0);
	    final ArrayNode actual = (ArrayNode) new BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
	    // TODO: numDuplicates
	
	    assertEquals(actual.count, expected.count);
	    for (int i = 0; i < 17; i++) {
	        assertBitmapIndexedNodesEqual((BitmapIndexedNode) actual.array[i], (BitmapIndexedNode) expected.array[i]);
	    }
	}

}
