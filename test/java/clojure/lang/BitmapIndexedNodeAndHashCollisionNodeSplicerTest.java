package clojure.lang;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

	@Ignore
    @Override
    @Test
    public void testDifferent() {
	
	final Counts counts = new Counts(0, 0);
	final int shift = 5;
	
	final Object leftKey = "left";
	final Object leftValue = 123;
	final int hashCode = 1; //TODO - make it PersistentHashMap.hash("hashCode")
	final Object right0Key = new HashCodeKey("right0", hashCode);
	final Object right0Value = 456;
	final Object right1Key = new HashCodeKey("right1", hashCode);
	final Object right1Value = 789;
	final INode leftNode1 = NodeUtils.create(shift, leftKey, leftValue);
	final INode rightNode1 = HashCollisionNodeUtils.create(hashCode, right0Key, right0Value, right1Key, right1Value);
	
	final Splicer splicer = new BitmapIndexedNodeAndHashCollisionNodeSplicer();
	final BitmapIndexedNode actual0 = (BitmapIndexedNode) splicer.splice(shift, counts, null, leftNode1, 0, null, rightNode1);
	assertEquals(0, counts.sameKey);
	
	final BitmapIndexedNode leftNode2 = (BitmapIndexedNode) leftNode1.assoc(shift, hashCode, right0Key, right0Value, new Box(null));
	final BitmapIndexedNode expected0 = (BitmapIndexedNode) leftNode2.assoc(shift, hashCode, right1Key, right1Value, new Box(null));
	
	assertEquals(expected0.bitmap, actual0.bitmap);
	final HashCollisionNode expected1 = (HashCollisionNode) expected0.array[1];
	final HashCollisionNode actual1   = (HashCollisionNode) actual0.array[1];
	
	assertEquals(actual1.hash, expected1.hash);
	assertEquals(actual1.count, expected1.count);
	assertArrayEquals(actual1.array, expected1.array);
	assertEquals(actual1.array[2], expected1.array[2]);
	assertEquals(actual1.array[3], expected1.array[3]);
    }

    @Ignore
    @Override
    @Test
    public void testSameKeyHashCode() {
	
	final Counts counts = new Counts(0, 0);
	final int shift = 5;
	
	final int hashCode = PersistentHashMap.hash("hashCode");
	final Object leftKey = new HashCodeKey("left", hashCode);
	final Object leftValue = 123;
	final Object right0Key = new HashCodeKey("right0", hashCode);
	final Object right0Value = 456;
	final Object right1Key = new HashCodeKey("right1", hashCode);
	final Object right1Value = 789;
	
	final INode leftNode1 = NodeUtils.create(shift, leftKey, leftValue);
	final INode rightNode1 = HashCollisionNodeUtils.create(hashCode, right0Key, right0Value, right1Key, right1Value);
	
	final Splicer splicer = new BitmapIndexedNodeAndHashCollisionNodeSplicer();
	final BitmapIndexedNode actualNode0 = (BitmapIndexedNode) splicer.splice(shift, counts, null, leftNode1, 0, null, rightNode1);
	assertEquals(0, counts.sameKey);
	
	final BitmapIndexedNode leftNode2 = (BitmapIndexedNode) leftNode1.assoc(shift, hashCode, right0Key, right0Value, new Box(null));
	final BitmapIndexedNode expectedNode0 = (BitmapIndexedNode) leftNode2.assoc(shift, hashCode, right1Key, right1Value, new Box(null));
	
	assertEquals(expectedNode0.bitmap, actualNode0.bitmap);
	final HashCollisionNode expectedNode1 = (HashCollisionNode) expectedNode0.array[1];
	final HashCollisionNode actualNode1   = (HashCollisionNode) actualNode0.array[1];
	assertEquals(actualNode1.hash, expectedNode1.hash);
	assertEquals(actualNode1.count, expectedNode1.count);
	assertArrayEquals(actualNode1.array, expectedNode1.array);
    }
	
    @Ignore
    @Override
    @Test
    public void testSameKey() {
	
	final Counts counts = new Counts(0, 0);
	final int shift = 5;
	
	final int hashCode = PersistentHashMap.hash("hashCode");
	final Object leftKey = new HashCodeKey("left", hashCode);
	final Object leftValue = 123;
	final Object right0Key = new HashCodeKey("left", hashCode);
	final Object right0Value = 456;
	final Object right1Key = new HashCodeKey("right", hashCode);
	final Object right1Value = 789;
	final INode leftNode1 = NodeUtils.create(shift, leftKey, leftValue);
	final INode rightNode1 = HashCollisionNodeUtils.create(hashCode, right0Key, right0Value, right1Key, right1Value);
	
	final Splicer splicer = new BitmapIndexedNodeAndHashCollisionNodeSplicer();
	final BitmapIndexedNode actualNode0 = (BitmapIndexedNode) splicer.splice(shift, counts, null, leftNode1, 0, null, rightNode1);
	assertEquals(1, counts.sameKey);
	
	final BitmapIndexedNode leftNode2 = (BitmapIndexedNode) leftNode1.assoc(shift, hashCode, right0Key, right0Value, new Box(null));
	final BitmapIndexedNode expectedNode0 = (BitmapIndexedNode) leftNode2.assoc(shift, hashCode, right1Key, right1Value, new Box(null));
	
	assertEquals(actualNode0.bitmap, expectedNode0.bitmap);
	final HashCollisionNode expectedNode1 = (HashCollisionNode) expectedNode0.array[1];
	final HashCollisionNode actualNode1 = (HashCollisionNode) actualNode0.array[1];
	
	assertEquals(actualNode1.hash, expectedNode1.hash);
	assertEquals(actualNode1.count, expectedNode1.count);
	assertArrayEquals(actualNode1.array, expectedNode1.array);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
	// TODO
    }

}
