package clojure.lang;

import static org.junit.Assert.assertEquals;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class TestUtils {

    public static void assertNodeEquals(INode l, INode r) {
	if (l instanceof BitmapIndexedNode) {
	    assertBitmapIndexedNodeEquals((BitmapIndexedNode) l, (BitmapIndexedNode) r);
	} else if (l instanceof HashCollisionNode) {
	    assertHashCollisionNodeEquals((HashCollisionNode) l, (HashCollisionNode) r);
	} else {
	    assertArrayNodeEquals((ArrayNode) l, (ArrayNode) r);
	}
    }

    public static void assertValueEquals(Object l, Object r) {
        if (l != r) {
	    if (l instanceof INode) {
		assertEquals(l.getClass(), r.getClass());
		assertNodeEquals((INode) l, (INode) r);
	    }
	    else
		assertEquals(l, r);
	}
    }

    public static void assertKeyValuePairArrayEquals(Object[] actual, Object[] expected, int length) {
	for (int i = 0; i < length;) {
	    assertEquals(actual[i], expected[i++]);
	    assertValueEquals(actual[i], expected[i++]);
	}
    }

    public static void assertArrayNodeEquals(ArrayNode actual, ArrayNode expected) {
    	if (actual != expected) {
	    assertEquals(actual.count, expected.count);
	    for (int i = 0; i < expected.count;) {
		assertNodeEquals(actual.array[i], expected.array[i++]);
	    }
    	}
    }

    public static void assertBitmapIndexedNodeEquals(BitmapIndexedNode actual, BitmapIndexedNode expected) {
	if (actual != expected) {
	    assertEquals(actual.bitmap, expected.bitmap);
	    assertKeyValuePairArrayEquals(actual.array, expected.array, Integer.bitCount(expected.bitmap) * 2);
	}
    }

    public static void assertHashCollisionNodeEquals(HashCollisionNode actual, HashCollisionNode expected) {
	if (actual != expected) {
	    assertEquals(actual.hash, expected.hash);
	    assertEquals(actual.count, expected.count);
	    assertKeyValuePairArrayEquals(actual.array, expected.array, expected.count * 2);
	}
    }

    public static INode assoc(int shift, INode node, Object key, Object value, Counts counts) {
	final Box box = new Box(null);
	node = node.assoc(shift, NodeUtils.hash(key), key, value, box);
	counts.sameKey += (box.val == box) ? 0 : 1;
	return node;
    }

    public static INode assocN(int shift, INode node, int start, int end, Counts counts) {
	// TODO: should not need to intern here - debug...
	for (int i = start; i < end + 1; i++)
	    node = assoc(shift, node , new HashCodeKey("key" + i, i), ("value"+i).intern(), counts);
	return node;
    }
    
    public static INode create(int shift, Object key, Object value) {
        return assoc(shift, BitmapIndexedNode.EMPTY, key, value, new Counts());
    }
    
    public static INode create(int shift, Object key0, Object value0, Object key1, Object value1) {
        return assoc(shift, create(shift, key0, value0), key1, value1, new Counts());
    }

}
