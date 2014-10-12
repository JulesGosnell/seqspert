package clojure.lang;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;


// TODO - if no changes are made, original node, tree should be returned...

public class SeqspertTest {


    @Test
    public void testCreateSingletonNode() {
        final int shift = 0;
        final Object value = "value";
        {
            final Object key = new HashCodeKey("key", 123456);
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.assoc(shift,  PersistentHashMap.hash(key), key, value, new Box(null));
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key, value);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
        {
            final Object key = new HashCodeKey("key", -123456);
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.assoc(shift,  PersistentHashMap.hash(key), key, value, new Box(null));
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key, value);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
    }

    @Test
    public void testCreateBinaryNode() {
        final int shift = 0;
        final Object value0 = "value0";
        final Object value1 = "value1";
        {
            final Object key0 = new HashCodeKey("key0", 2);
            final Object key1 = new HashCodeKey("key1", 3);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final Box addedLeaf = new Box(null);
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
        {
            final Object key0 = new HashCodeKey("key0", 3);
            final Object key1 = new HashCodeKey("key1", 2);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final Box addedLeaf = new Box(null);
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
        {
            final Object key0 = new HashCodeKey("key0", 2);
            final Object key1 = new HashCodeKey("key1", -3);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final Box addedLeaf = new Box(null);
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
        {
            final Object key0 = new HashCodeKey("key0", -2);
            final Object key1 = new HashCodeKey("key1", 3);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final Box addedLeaf = new Box(null);
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
        {
            final Object key0 = new HashCodeKey("key0", -2);
            final Object key1 = new HashCodeKey("key1", -3);
            final Box addedLeaf = new Box(null);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
        {
            final Object key0 = new HashCodeKey("key0", -3);
            final Object key1 = new HashCodeKey("key1", -2);
            final Box addedLeaf = new Box(null);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
        {
            final Object key0 = new HashCodeKey("key0", 2);
            final Object key1 = new HashCodeKey("key1", 66); // (2 << 5) + 2
            final Box addedLeaf = new Box(null);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            final BitmapIndexedNode expectedNested = (BitmapIndexedNode) expected.array[1];
            final BitmapIndexedNode actualNested = (BitmapIndexedNode) actual.array[1];
            assertEquals(expectedNested.bitmap, actualNested.bitmap);
            assertArrayEquals(expectedNested.array, actualNested.array);
        }
        {
            final Object key0 = new HashCodeKey("key0", 2);
            final Object key1 = new HashCodeKey("key1", 2);
            final Box addedLeaf = new Box(null);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            final HashCollisionNode expectedNested = (HashCollisionNode) expected.array[1];
            //final HashCollisionNode actualNested = (HashCollisionNode) actual.array[1];
            assertEquals(expectedNested.hash, expectedNested.hash);
            assertEquals(expectedNested.count, expectedNested.count);
            assertArrayEquals(expectedNested.array, expectedNested.array);
        }

        {
            int shift2 = 20;
            final Object key0 = new HashCodeKey("key0", 938796064);
            final Object key1 = new HashCodeKey("key1", 1346626592);
            final Box addedLeaf = new Box(null);
            final AtomicReference<Thread> edit = new AtomicReference<Thread>();
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.
                assoc(edit, shift2, PersistentHashMap.hash(key0), key0, value0, addedLeaf).
                assoc(edit, shift2, PersistentHashMap.hash(key1), key1, value1, addedLeaf);
            final BitmapIndexedNode actual = (BitmapIndexedNode) NodeUtils.create(shift2, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
    }

    @Test
    public void testKeyValuePairAndKeyValuePairSplicerNoCollision() {

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


    

    //-------------------------------------------------------------------

    

    //-------------------------------------------------------------------

    
}
