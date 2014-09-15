package clojure.lang;

import static org.junit.Assert.*;
import static clojure.lang.TestUtils.*;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;

public class TestUtils {

    public static void assertBitmapIndexedNodesEqual(BitmapIndexedNode actual, BitmapIndexedNode expected) {
	assertEquals(actual.bitmap, expected.bitmap);
	assertEquals(actual.array[0], expected.array[0]);
	assertEquals(actual.array[1], expected.array[1]);
    }

    public static void assertSubarrayEquals(Object[] actual, Object[] expected, int length) {
	for (int i = 0; i < length; i++)
	    assertEquals(actual[i], expected[i]);
    }

    public static void assertHashCollisionNodesEqual(HashCollisionNode actual, HashCollisionNode expected) {
	assertEquals(actual.hash, expected.hash);
	assertEquals(actual.count, expected.count);
	assertSubarrayEquals(actual.array, expected.array, expected.count * 2);
    }
	
}
