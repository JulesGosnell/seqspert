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
	    for (int i = 0; i < expected.count; i++) {
		assertNodeEquals(expected.array[i], actual.array[i]);
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

    public static void assertSame(Object value0, Object value1, Object value2) {
        org.junit.Assert.assertSame(value0, value1);
        org.junit.Assert.assertSame(value1, value2);
    }

    public static INode assoc(int shift, INode node,
                              Object key, Object value,
                              Counts counts) {
        if (key != null && value != null) {
            final Box box = new Box(null);
            node = node.assoc(shift, NodeUtils.hash(key), key, value, box);
            counts.sameKey += (box.val == box) ? 0 : 1;
        }
	return node;
    }

    public static INode assoc(int shift, INode node,
                              Object key0, Object value0,
                              Object key1, Object value1,
                              Counts counts) {
        return assoc(shift, assoc(shift, node, key0, value0, counts), key1, value1, counts);
    }
    
    public static INode assoc(int shift, INode node,
                              Object key0, Object value0,
                              Object key1, Object value1,
                              Object key2, Object value2,
                              Counts counts) {
        return assoc(shift, assoc(shift, node, key0, value0, key1, value1, counts), key2, value2, counts);
    }
    
    public static INode assocN(int shift, INode node,
                               int start, int end,
                               Counts counts) {
	for (int i = start; i < end; i++)
	    node = assoc(shift, node , new HashCodeKey("key" + i, i), ("value"+i), counts);
	return node;
    }

    public static INode assocN(int shift, INode node,
                               int start, int end,
                               Object key0, Object value0,
                               Counts counts) {
	return assoc(shift, assocN(shift, node, start, end, counts), key0, value0, counts); 
    }
    
    public static INode assocN(int shift, INode node, int start, int end,
    		Object optionalKey0, Object optionalValue0,
    		Object optionalKey1, Object optionalValue1,
    		Counts counts) {
	for (int i = start; i < end; i++) node = assoc(shift, node , new HashCodeKey("key" + i, i), ("value"+i), counts);
	if (optionalKey0 != null && optionalValue0 != null) node = assoc(shift, node, optionalKey0, optionalValue0, counts); 
	if (optionalKey1 != null && optionalValue1 != null) node = assoc(shift, node, optionalKey1, optionalValue1, counts); 
	return node;
    }
    
    public static INode create(int shift, Object key, Object value) {
        return assoc(shift, BitmapIndexedNode.EMPTY, key, value, new Counts());
    }
    
    public static INode create(int shift, Object key0, Object value0, Object key1, Object value1) {
        return assoc(shift, create(shift, key0, value0), key1, value1, new Counts());
    }
    
    public static INode create(int shift, Object key0, Object value0, Object key1, Object value1, Object key2, Object value2) {
        INode node = assoc(shift, create(shift, key0, value0), key1, value1, new Counts());
        if (key2 != null && value2 != null) node = assoc(shift, node, key2, value2, new Counts());
        return node;
    }
    
}
