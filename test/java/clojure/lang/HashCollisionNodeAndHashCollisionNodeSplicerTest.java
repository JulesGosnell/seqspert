package clojure.lang;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import clojure.lang.PersistentHashMap.HashCollisionNode;

public class HashCollisionNodeAndHashCollisionNodeSplicerTest
//implements SplicerTestInterface 
{

	

	//-------------------------------------------------------------------
	
	
	
	//-------------------------------------------------------------------
	
	@Test
	public void testCollision() {
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
	    
	    final HashCollisionNode leftNode   = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
	    final HashCollisionNode rightNode =  new HashCollisionNode(null, hashCode, 2, new Object[]{key2, value2, key3, value3});
	
	    final AtomicReference<Thread> edit = new AtomicReference<Thread>();
	    final Box addedLeaf = new Box(null);
	    final HashCollisionNode expected = (HashCollisionNode) leftNode.
	            assoc(edit, shift, hashCode, key2, value2, addedLeaf).
	            assoc(edit, shift, hashCode, key3, value3, addedLeaf);
	
	    final Duplications duplications = new Duplications(0);
	    final HashCollisionNode actual =  (HashCollisionNode) NodeUtils.splice(shift, duplications, null, leftNode, 0, null, rightNode);
	    assertEquals(0, duplications.duplications);
	    
	    assertEquals(expected.count, actual.count);
	    assertArrayEquals(expected.array, actual.array);
	}

	//-------------------------------------------------------------------
	
	
	
	//-------------------------------------------------------------------
	
	
	
	//-------------------------------------------------------------------
	
	
	
	//-------------------------------------------------------------------
	
	@Test
	public void testDuplication() {
	    final int shift = 0;
	    final int hashCode = 2;
	    final Object key0 = new HashCodeKey("key0", hashCode);
	    final Object key1 = new HashCodeKey("key1", hashCode);
	    final Object key2 = new HashCodeKey("key2", hashCode);
	    final Object value0 = "value0";
	    final Object value1 = "value1";
	    final Object value2 = "value2";
	    
	    final HashCollisionNode leftNode   = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
	    final HashCollisionNode rightNode =  new HashCollisionNode(null, hashCode, 2, new Object[]{key1, value1, key2, value2});
	
	    final AtomicReference<Thread> edit = new AtomicReference<Thread>();
	    final Box addedLeaf = new Box(null);
	    final HashCollisionNode expected = (HashCollisionNode) leftNode.
	            assoc(edit, shift, hashCode, key1, value1, addedLeaf).
	            assoc(edit, shift, hashCode, key2, value2, addedLeaf);
	
	    final Duplications duplications = new Duplications(0);
	    final HashCollisionNode actual =  (HashCollisionNode) NodeUtils.splice(shift, duplications, null, leftNode, 0, null, rightNode);
	    assertEquals(1, duplications.duplications);
	    
	    assertEquals(expected.count, actual.count);
	    assertArrayEquals(expected.array, actual.array);
	}

}
