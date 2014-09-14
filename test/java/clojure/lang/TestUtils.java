package clojure.lang;

import static org.junit.Assert.*;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;

public class TestUtils {

	//-----------------------------------------------------
	
	// BIN(a) | BIN(b) -> BIN(a, b)
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456}
	
	
	
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456} when key hash codes collide
	
	
	
	// {"duplicate" 123 "duplicate" 456} should= {"duplicate" 123} + {"duplicate" 456}
	
	
	
	//-----------------------------------------------------
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when right key hash codes collide
	
	
	
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when all key hash codes collide
	
	
	
	// {"left" 123 "left" 456 "right" 789} should= {"left" 123} + {"left" 456 "right" 789} when all key hash codes collide and duplicate keys are present
	
	
	
	//-------------------------------------------------------------
	
	public static void assertBitmapIndexedNodesEqual(BitmapIndexedNode actual, BitmapIndexedNode expected) {
	    assertEquals(actual.bitmap, expected.bitmap);
	    assertEquals(actual.array[0], expected.array[0]);
	    assertEquals(actual.array[1], expected.array[1]);
	}

	//-----------------------------------------------------
	
	// BIN(a) | BIN(b) -> BIN(a, b)
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456}
	
	
	
	
	// {"left" 123 "right" 456} should= {"left" 123} + {"right" 456} when key hash codes collide
	
	
	
	// {"duplicate" 123 "duplicate" 456} should= {"duplicate" 123} + {"duplicate" 456}
	
	
	
	//-----------------------------------------------------
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when right key hash codes collide
	
	
	
	
	// {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when all key hash codes collide
	
	
	
	// {"left" 123 "left" 456 "right" 789} should= {"left" 123} + {"left" 456 "right" 789} when all key hash codes collide and duplicate keys are present
	
	
	
	//-------------------------------------------------------------
	
	public static void assertHashCollisionNodesEqual(HashCollisionNode actual, HashCollisionNode expected) {
	    assertEquals(actual.hash, expected.hash);
	    assertEquals(actual.count, expected.count);
	    assertArrayEquals(actual.array, expected.array);
	}
	
}
