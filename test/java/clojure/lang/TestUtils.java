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

    public static INode assocN(int shift, INode node, int start, int end, Counts counts) {

	for (int i = start; i < end + 1; i++) {
	    final int hashCode = i;
	    final Object key = new HashCodeKey("left" + i, hashCode);
	    final Object value = i;
	    final Box box = new Box(null);
	    node = node.assoc(shift, hashCode , key, value, box);
	    counts.sameKey += (box.val == box) ? 0 : 1;
	}
	
	return node;
    }

}
