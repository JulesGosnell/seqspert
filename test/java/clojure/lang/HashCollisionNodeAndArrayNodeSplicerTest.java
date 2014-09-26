package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.*;

import org.junit.Test;
import org.junit.Ignore;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndArrayNodeSplicerTest
// implements SplicerTestInterface
{
	
    final int shift = 0;
    final int hashCode = 32;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";
    
    @Ignore
    @Test
    public void testNoCollision() {
	
	// The HashCollisionNode
	final INode leftNode = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
	// The ArrayNode...
	INode rightNode = BitmapIndexedNode.EMPTY;
	for (int i = 2; i < 20; i++) {
	    rightNode = rightNode.assoc(shift, i * 32 , new HashCodeKey("value" + i, i * 32), i, new Box(null));
	}

	// The expected ArrayNode
	INode expected = rightNode;
	for (int i = 0; i < 2; i++) {
	    expected = expected.assoc(shift, i * 32 , new HashCodeKey("value" + i, i * 32), i, new Box(null));
	}


	// The actual ArrayNode
	final Duplications duplications = new Duplications(0);
	final Splicer splicer = new HashCollisionNodeAndArrayNodeSplicer();
	final INode actual = splicer.splice(shift, duplications, null, leftNode, 0, null, rightNode);

	assertEquals(0, duplications.duplications);
	assertNodeEquals(actual, expected);
    }
}
