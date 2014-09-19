package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.*;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndArrayNodeSplicerTest
// implements SplicerTestInterface
{
	
    @Test
    public void testDuplication() {
	
	final int shift = 5;
	
	INode leftNode = BitmapIndexedNode.EMPTY;
	for (int i = 0; i < 17; i++) {
	    leftNode = leftNode.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
	}

	INode expected = leftNode;
	INode rightNode = BitmapIndexedNode.EMPTY;
	for (int i = 16; i < 33; i++) {
	    final Object key = new HashCodeKey("left" + i, i * 32);
	    final int hash = key.hashCode();
	    final Object value = i;
	    expected = expected.assoc(shift, hash , key, value, new Box(null));
	    rightNode = rightNode.assoc(shift, hash , key, value, new Box(null));
	}
	
	final Duplications duplications = new Duplications(0);
	final INode actual = new ArrayNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode, 0, null, rightNode);

	assertEquals(1, duplications.duplications);
	assertNodeEquals(actual, expected);
    }
}
