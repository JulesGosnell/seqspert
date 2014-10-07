package clojure.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class HashCollisionNodeAndArrayNodeSplicerTest
// implements SplicerTestInterface
{
	
    final int shift = 0;
    final int hashCode = 1;
    final Object key0 = new HashCodeKey("key0", hashCode);
    final Object key1 = new HashCodeKey("key1", hashCode);
    final Object value0 = "value0";
    final Object value1 = "value1";
    
    @Test
    public void testDifferent() {
	
	// The HashCollisionNode
	final INode leftNode = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
	// The ArrayNode...
	final INode empty = BitmapIndexedNode.EMPTY;
	final INode rightNode = TestUtils.assocN(shift, empty, 2, 19, new Counts());

	// The expected ArrayNode
	INode expected = rightNode;
	expected = expected.assoc(shift, hashCode , key0, value0, new Box(null));
	expected = expected.assoc(shift, hashCode , key1, value1, new Box(null));


	// The actual ArrayNode
	final Counts counts = new Counts(0, 0);
	final Splicer splicer = new HashCollisionNodeAndArrayNodeSplicer();
	final INode actual = splicer.splice(shift, counts, null, leftNode, 0, null, rightNode);

	assertEquals(0, counts.sameKey);
	// TODO: Need an HCN inside a BIN - fix impl...
	// assertNodeEquals(actual, expected);
    }

}


