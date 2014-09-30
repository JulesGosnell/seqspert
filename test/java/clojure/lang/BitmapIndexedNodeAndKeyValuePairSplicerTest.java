package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.assertNodeEquals;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {

    @Test
    public void testNoCollision() {
	
	final Duplications duplications = new Duplications(0);
	final int shift = 5;
	
	final Object leftKey = "leftKey";
	final Object leftValue = "leftValue";
	final int hashCode = 1;
	final Object rightKey = new HashCodeKey("rightKey", hashCode);
	final Object rightValue = "rightValue";

	final INode leftNode = NodeUtils.create(shift, leftKey, leftValue);
	final INode expected = leftNode.assoc(shift, hashCode, rightKey, rightValue, new Box(null));
	
	final Splicer splicer = new BitmapIndexedNodeAndKeyValuePairSplicer();
	final INode actual = splicer.splice(shift, duplications, null, leftNode, 0, rightKey, rightValue);

	assertEquals(0, duplications.duplications);
	assertNodeEquals(expected, actual);
    }

    @Test
    public void testCollision() {
	
	//     final Duplications duplications = new Duplications(0);
	//     final int shift = 5;
	
	//     final int hashCode = PersistentHashMap.hash("hashCode");
	//     final Object leftKey = new HashCodeKey("left", hashCode);
	//     final Object leftValue = 123;
	//     final Object rightKey = new HashCodeKey("right0", hashCode);
	//     final Object rightValue = 456;
	//     final Object right1Key = new HashCodeKey("right1", hashCode);
	//     final Object right1Value = 789;
	
	//     final INode leftNode = NodeUtils.create(shift, leftKey, leftValue);
	//     final INode rightNode1 = HashCollisionNodeUtils.create(hashCode, rightKey, rightValue, right1Key, right1Value);
	
	//     final Splicer splicer = new BitmapIndexedNodeAndHashCollisionNodeSplicer();
	//     final BitmapIndexedNode actualNode0 = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode, 0, null, rightNode1);
	//     assertEquals(0, duplications.duplications);
	
	//     final BitmapIndexedNode leftNode2 = (BitmapIndexedNode) leftNode.assoc(shift, hashCode, rightKey, rightValue, new Box(null));
	//     final BitmapIndexedNode expectedNode0 = (BitmapIndexedNode) leftNode2.assoc(shift, hashCode, right1Key, right1Value, new Box(null));
	
	//     assertEquals(expectedNode0.bitmap, actualNode0.bitmap);
	//     final HashCollisionNode expectedNode1 = (HashCollisionNode) expectedNode0.array[1];
	//     final HashCollisionNode actualNode1   = (HashCollisionNode) actualNode0.array[1];
	//     assertEquals(actualNode1.hash, expectedNode1.hash);
	//     assertEquals(actualNode1.count, expectedNode1.count);
	//     assertArrayEquals(actualNode1.array, expectedNode1.array);
    }
	
    @Test
    public void testDuplication() {
	
	//     final Duplications duplications = new Duplications(0);
	//     final int shift = 5;
	
	//     final int hashCode = PersistentHashMap.hash("hashCode");
	//     final Object leftKey = new HashCodeKey("left", hashCode);
	//     final Object leftValue = 123;
	//     final Object rightKey = new HashCodeKey("left", hashCode);
	//     final Object rightValue = 456;
	//     final Object right1Key = new HashCodeKey("right", hashCode);
	//     final Object right1Value = 789;
	//     final INode leftNode = NodeUtils.create(shift, leftKey, leftValue);
	//     final INode rightNode1 = HashCollisionNodeUtils.create(hashCode, rightKey, rightValue, right1Key, right1Value);
	
	//     final Splicer splicer = new BitmapIndexedNodeAndHashCollisionNodeSplicer();
	//     final BitmapIndexedNode actualNode0 = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode, 0, null, rightNode1);
	//     assertEquals(1, duplications.duplications);
	
	//     final BitmapIndexedNode leftNode2 = (BitmapIndexedNode) leftNode.assoc(shift, hashCode, rightKey, rightValue, new Box(null));
	//     final BitmapIndexedNode expectedNode0 = (BitmapIndexedNode) leftNode2.assoc(shift, hashCode, right1Key, right1Value, new Box(null));
	
	//     assertEquals(actualNode0.bitmap, expectedNode0.bitmap);
	//     final HashCollisionNode expectedNode1 = (HashCollisionNode) expectedNode0.array[1];
	//     final HashCollisionNode actualNode1 = (HashCollisionNode) actualNode0.array[1];
	
	//     assertEquals(actualNode1.hash, expectedNode1.hash);
	//     assertEquals(actualNode1.count, expectedNode1.count);
	//     assertArrayEquals(actualNode1.array, expectedNode1.array);
    }

}
