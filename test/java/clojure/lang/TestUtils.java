package clojure.lang;

import static org.junit.Assert.assertEquals;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class TestUtils {

    public static void assertNodeEquals(INode expected, INode actual) {
	if (expected instanceof BitmapIndexedNode) {
	    assertBitmapIndexedNodeEquals((BitmapIndexedNode) expected, (BitmapIndexedNode) actual);
	} else if (expected instanceof HashCollisionNode) {
	    assertHashCollisionNodeEquals((HashCollisionNode) expected, (HashCollisionNode) actual);
	} else {
	    assertArrayNodeEquals((ArrayNode) expected, (ArrayNode) actual);
	}
    }

    public static void assertValueEquals(Object expected, Object actual) {
        if (expected != actual) {
	    if (expected instanceof INode) {
		assertEquals(expected.getClass(), actual.getClass());
		assertNodeEquals((INode) expected, (INode) actual);
	    }
	    else
		assertEquals(expected, actual);
	}
    }

    public static void assertKeyValuePairArrayEquals(Object[] expected, Object[] actual, int length) {
	for (int i = 0; i < length;) {
	    assertEquals(expected[i], actual[i++]);
	    assertValueEquals(expected[i], actual[i++]);
	}
    }

    public static void assertArrayNodeEquals(ArrayNode expected, ArrayNode actual) {
    	if (expected != actual) {
	    assertEquals(expected.count, actual.count);
	    for (int i = 0; i < actual.count;) {
		assertNodeEquals(expected.array[i], actual.array[i++]);
	    }
    	}
    }

    public static void assertBitmapIndexedNodeEquals(BitmapIndexedNode expected, BitmapIndexedNode actual) {
	if (expected != actual) {
	    assertEquals(expected.bitmap, actual.bitmap);
	    assertKeyValuePairArrayEquals(expected.array, actual.array, Integer.bitCount(expected.bitmap) * 2);
	}
    }

    public static void assertHashCollisionNodeEquals(HashCollisionNode expected, HashCollisionNode actual) {
	if (expected != actual) {
	    assertEquals(expected.hash, actual.hash);
	    assertEquals(expected.count, actual.count);
	    assertKeyValuePairArrayEquals(expected.array, actual.array, expected.count * 2);
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
