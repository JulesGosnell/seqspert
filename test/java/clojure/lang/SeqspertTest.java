package clojure.lang;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;
import clojure.lang.Seqspert.Duplications;


// TODO - if no changes are made, original node, tree should be returned...

public class SeqspertTest {


    static class HashCodeKey {

        private Object key;
        private int hashCode;

        public HashCodeKey(Object key, int hashCode) {
            this.key = key;
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object that) {
            return that != null &&
                    that instanceof HashCodeKey &&
                    ((HashCodeKey)that).hashCode == hashCode &&
                    ((HashCodeKey)that).key.equals(key);
        }

    }

    @Test
    public void testCreateSingletonNode() {
        final int shift = 0;
        final Object value = "value";
        {
            final Object key = new HashCodeKey("key", 123456);
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.assoc(shift,  PersistentHashMap.hash(key), key, value, new Box(null));
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key, value);
            assertEquals(expected.bitmap, actual.bitmap);
            assertArrayEquals(expected.array, actual.array);
        }
        {
            final Object key = new HashCodeKey("key", -123456);
            final BitmapIndexedNode expected = (BitmapIndexedNode) BitmapIndexedNode.EMPTY.assoc(shift,  PersistentHashMap.hash(key), key, value, new Box(null));
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key, value);
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift, key0, value0, PersistentHashMap.hash(key1), key1, value1);
            assertEquals(expected.bitmap, actual.bitmap);
            final HashCollisionNode expectedNested = (HashCollisionNode) expected.array[1];
            final HashCollisionNode actualNested = (HashCollisionNode) actual.array[1];
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
            final BitmapIndexedNode actual = (BitmapIndexedNode) Seqspert.createNode(shift2, key0, value0, PersistentHashMap.hash(key1), key1, value1);
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

    @Test
    public void testBitmapIndexedNodeBitmapIndexedNodeSplicerNoCollision() {

        final Duplications duplications = new Duplications(0);
        final int shift = 0;

        final Object leftKey = "left";
        final Object leftValue = 123;
        final Object rightKey = "right";
        final Object rightValue = 456;
        final INode leftNode1  = Seqspert.createNode(shift, leftKey, leftValue);
        final INode rightNode1 = Seqspert.createNode(shift, rightKey, rightValue);

        final Splicer splicer = new Seqspert.BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
        final BitmapIndexedNode actual = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, PersistentHashMap.hash(rightKey), null, rightNode1);
        assertEquals(0, duplications.duplications);

        final BitmapIndexedNode expected = (BitmapIndexedNode)leftNode1.assoc(shift, PersistentHashMap.hash(rightKey), rightKey, rightValue, new Box(null));
        assertEquals(expected.bitmap, actual.bitmap);
        assertArrayEquals(expected.array, actual.array);
    }


    // {"left" 123 "right" 456} should= {"left" 123} + {"right" 456} when key hash codes collide

    @Test
    public void testBitmapIndexedNodeBitmapIndexedNodeSplicerCollision() {

        final Duplications duplications = new Duplications(0);
        final int shift = 5;

        final int hashCode = PersistentHashMap.hash("hashCode");
        final Object leftKey = new HashCodeKey("left", hashCode);
        final Object leftValue = 123;
        final Object rightKey = new HashCodeKey("right", hashCode);
        final Object rightValue = 345;

        final INode leftNode1  = Seqspert.createNode(shift, leftKey, leftValue);
        final INode rightNode1 = Seqspert.createNode(shift, rightKey, rightValue);

        final Splicer splicer = new Seqspert.BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
        final BitmapIndexedNode actual = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, hashCode, null, rightNode1);
        assertEquals(0, duplications.duplications);

        final BitmapIndexedNode expected = (BitmapIndexedNode)leftNode1.assoc(shift, hashCode, rightKey, rightValue, new Box(null));
        assertEquals(expected.bitmap, actual.bitmap);
        // TODO: why are arrays not of same length ?
        assertEquals(expected.array[0], actual.array[0]);

        final HashCollisionNode collision1 = (HashCollisionNode) expected.array[1];
        final HashCollisionNode collision2 = (HashCollisionNode) actual.array[1];

        assertEquals(collision1.hash, collision2.hash);
        assertEquals(collision1.count, collision2.count);
        assertArrayEquals(collision1.array, collision2.array);
    }

    // {"duplicate" 123 "duplicate" 456} should= {"duplicate" 123} + {"duplicate" 456}

    @Test
    public void testBitmapIndexedNodeBitmapIndexedNodeSplicerDuplication() {

        final Duplications duplications = new Duplications(0);
        final int shift = 5;

        final Object key = "duplicate";
        final int hashCode = PersistentHashMap.hash(key);
        final Object leftValue = 123;
        final Object rightValue = 345;
        final INode leftNode1  = Seqspert.createNode(shift, key, leftValue);
        final INode rightNode1 = Seqspert.createNode(shift, key, rightValue);

        final Splicer splicer = new Seqspert.BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
        final BitmapIndexedNode actual = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
        assertEquals(1, duplications.duplications);

        final BitmapIndexedNode expected = (BitmapIndexedNode)leftNode1.assoc(shift, hashCode, key, rightValue, new Box(null));
        assertEquals(expected.bitmap, actual.bitmap);
        // TODO: why are arrays not of same length ?
        assertEquals(expected.array[0], actual.array[0]);
        assertEquals(expected.array[1], actual.array[1]);
    }

    //-----------------------------------------------------

    HashCollisionNode createBinaryHashCollisionNode(int hashCode, Object key0, Object value0, Object key1, Object value1) {
        final AtomicReference<Thread> edit = null;
        final int count = 2;
        final int hash = hashCode;
        final Object[] array = new Object[]{key0, value0, key1, value1};
        final HashCollisionNode node  = new HashCollisionNode(edit, hash, count, array);
        assertEquals(hash, node.hash);
        assertEquals(count, node.count);
        assertArrayEquals(array, node.array);
        return node;
    }

    // {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when right key hash codes collide

    @Test
    public void testBitmapIndexedNodeHashCollisionNodeSplicerNoCollision() {

        final Duplications duplications = new Duplications(0);
        final int shift = 5;

        final Object leftKey = "left";
        final Object leftValue = 123;
        final int hashCode = 1; //TODO - make it PersistentHashMap.hash("hashCode")
        final Object right0Key = new HashCodeKey("right0", hashCode);
        final Object right0Value = 456;
        final Object right1Key = new HashCodeKey("right1", hashCode);
        final Object right1Value = 789;
        final INode leftNode1 = Seqspert.createNode(shift, leftKey, leftValue);
        final INode rightNode1 = createBinaryHashCollisionNode(hashCode, right0Key, right0Value, right1Key, right1Value);

        final Splicer splicer = new Seqspert.BitmapIndexedNodeAndHashCollisionNodeSplicer();
        final BitmapIndexedNode actual0 = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
        assertEquals(0, duplications.duplications);

        final BitmapIndexedNode leftNode2 = (BitmapIndexedNode) leftNode1.assoc(shift, hashCode, right0Key, right0Value, new Box(null));
        final BitmapIndexedNode expected0 = (BitmapIndexedNode) leftNode2.assoc(shift, hashCode, right1Key, right1Value, new Box(null));

        assertEquals(expected0.bitmap, actual0.bitmap);
        final HashCollisionNode expected1 = (HashCollisionNode) expected0.array[1];
        final HashCollisionNode actual1   = (HashCollisionNode) actual0.array[1];

        assertEquals(actual1.hash, expected1.hash);
        assertEquals(actual1.count, expected1.count);
        assertArrayEquals(actual1.array, expected1.array);
        assertEquals(actual1.array[2], expected1.array[2]);
        assertEquals(actual1.array[3], expected1.array[3]);
    }


    // {"left" 123 "right0" 456 "right1" 789} should= {"left" 123} + {"right0" 456 "right1" 789} when all key hash codes collide

    @Test
    public void testBitmapIndexedNodeHashCollisionNodeSplicerCollision() {

        final Duplications duplications = new Duplications(0);
        final int shift = 5;

        final int hashCode = PersistentHashMap.hash("hashCode");
        final Object leftKey = new HashCodeKey("left", hashCode);
        final Object leftValue = 123;
        final Object right0Key = new HashCodeKey("right0", hashCode);
        final Object right0Value = 456;
        final Object right1Key = new HashCodeKey("right1", hashCode);
        final Object right1Value = 789;

        final INode leftNode1 = Seqspert.createNode(shift, leftKey, leftValue);
        final INode rightNode1 = createBinaryHashCollisionNode(hashCode, right0Key, right0Value, right1Key, right1Value);

        final Splicer splicer = new Seqspert.BitmapIndexedNodeAndHashCollisionNodeSplicer();
        final BitmapIndexedNode actualNode0 = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
        assertEquals(0, duplications.duplications);

        final BitmapIndexedNode leftNode2 = (BitmapIndexedNode) leftNode1.assoc(shift, hashCode, right0Key, right0Value, new Box(null));
        final BitmapIndexedNode expectedNode0 = (BitmapIndexedNode) leftNode2.assoc(shift, hashCode, right1Key, right1Value, new Box(null));

        assertEquals(expectedNode0.bitmap, actualNode0.bitmap);
        final HashCollisionNode expectedNode1 = (HashCollisionNode) expectedNode0.array[1];
        final HashCollisionNode actualNode1   = (HashCollisionNode) actualNode0.array[1];
        assertEquals(actualNode1.hash, expectedNode1.hash);
        assertEquals(actualNode1.count, expectedNode1.count);
        assertArrayEquals(actualNode1.array, expectedNode1.array);
    }

    // {"left" 123 "left" 456 "right" 789} should= {"left" 123} + {"left" 456 "right" 789} when all key hash codes collide and duplicate keys are present

    @Test
    public void testBitmapIndexedNodeHashCollisionNodeSplicerDuplication() {

        final Duplications duplications = new Duplications(0);
        final int shift = 5;

        final int hashCode = PersistentHashMap.hash("hashCode");
        final Object leftKey = new HashCodeKey("left", hashCode);
        final Object leftValue = 123;
        final Object right0Key = new HashCodeKey("left", hashCode);
        final Object right0Value = 456;
        final Object right1Key = new HashCodeKey("right", hashCode);
        final Object right1Value = 789;
        final INode leftNode1 = Seqspert.createNode(shift, leftKey, leftValue);
        final INode rightNode1 = createBinaryHashCollisionNode(hashCode, right0Key, right0Value, right1Key, right1Value);

        final Splicer splicer = new Seqspert.BitmapIndexedNodeAndHashCollisionNodeSplicer();
        final BitmapIndexedNode actualNode0 = (BitmapIndexedNode) splicer.splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
        assertEquals(1, duplications.duplications);

        final BitmapIndexedNode leftNode2 = (BitmapIndexedNode) leftNode1.assoc(shift, hashCode, right0Key, right0Value, new Box(null));
        final BitmapIndexedNode expectedNode0 = (BitmapIndexedNode) leftNode2.assoc(shift, hashCode, right1Key, right1Value, new Box(null));

        assertEquals(actualNode0.bitmap, expectedNode0.bitmap);
        final HashCollisionNode expectedNode1 = (HashCollisionNode) expectedNode0.array[1];
        final HashCollisionNode actualNode1 = (HashCollisionNode) actualNode0.array[1];

        assertEquals(actualNode1.hash, expectedNode1.hash);
        assertEquals(actualNode1.count, expectedNode1.count);
        assertArrayEquals(actualNode1.array, expectedNode1.array);
    }

    //-------------------------------------------------------------

    public void assertBitmapIndexedNodesEqual(BitmapIndexedNode actual, BitmapIndexedNode expected) {
        assertEquals(actual.bitmap, expected.bitmap);
        assertEquals(actual.array[0], expected.array[0]);
        assertEquals(actual.array[1], expected.array[1]);
    }

    public void assertHashCollisionNodesEqual(HashCollisionNode actual, HashCollisionNode expected) {
        assertEquals(actual.hash, expected.hash);
        assertEquals(actual.count, expected.count);
        assertArrayEquals(actual.array, expected.array);
    }

    @Test
    public void testBitmapIndexedNodeAndArrayNodeSplicerNoCollision() {

        final int shift = 5;

        final INode leftNode1  = Seqspert.createNode(shift, new HashCodeKey("left" + 0, 0 * 32), 0);

        INode leftNode2 = leftNode1;
        for (int i = 1; i < 18; i++) {
            leftNode2 = leftNode2.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }
        final ArrayNode expected = (ArrayNode) leftNode2;

        INode rightNode1 = BitmapIndexedNode.EMPTY;
        for (int i = 1; i < 18; i++) {
            rightNode1 = rightNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }

        final Duplications duplications = new Duplications(0);
        final ArrayNode actual = (ArrayNode)  new Seqspert.BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
        // TODO: numDuplicates

        assertEquals(actual.count, expected.count);
        for (int i = 0; i < 18; i++) {
            assertBitmapIndexedNodesEqual((BitmapIndexedNode) actual.array[i], (BitmapIndexedNode) expected.array[i]);
        }
    }

    @Test
    public void testBitmapIndexedNodeAndArrayNodeSplicerCollision() {

        final int shift = 5;

        final INode leftNode1  = Seqspert.createNode(shift, new HashCodeKey("initial-left" + 0, 0 * 32), 0);

        INode leftNode2 = leftNode1;
        for (int i = 0; i < 17; i++) {
            leftNode2 = leftNode2.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }
        final ArrayNode expected = (ArrayNode) leftNode2;

        INode rightNode1 = BitmapIndexedNode.EMPTY;
        for (int i = 0; i < 17; i++) {
            rightNode1 = rightNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }

        final Duplications duplications = new Duplications(0);
        final ArrayNode actual = (ArrayNode) new Seqspert.BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
        // TODO: numDuplicates

        assertEquals(actual.count, expected.count);
        assertHashCollisionNodesEqual((HashCollisionNode) actual.array[0], (HashCollisionNode) expected.array[0]);
        for (int i = 1; i < 17; i++) {
            assertBitmapIndexedNodesEqual((BitmapIndexedNode) actual.array[i], (BitmapIndexedNode) expected.array[i]);
        }
    }

    @Test
    public void testBitmapIndexedNodeAndArrayNodeSplicerDuplication() {

        final int shift = 5;

        final INode leftNode1  = Seqspert.createNode(shift, new HashCodeKey("left" + 0, 0 * 32), 0);

        INode leftNode2 = leftNode1;
        for (int i = 0; i < 17; i++) {
            leftNode2 = leftNode2.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }
        final ArrayNode expected = (ArrayNode) leftNode2;

        INode rightNode1 = BitmapIndexedNode.EMPTY;
        for (int i = 0; i < 17; i++) {
            rightNode1 = rightNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }

        final Duplications duplications = new Duplications(0);
        final ArrayNode actual = (ArrayNode) new Seqspert.BitmapIndexedNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
        // TODO: numDuplicates

        assertEquals(actual.count, expected.count);
        for (int i = 0; i < 17; i++) {
            assertBitmapIndexedNodesEqual((BitmapIndexedNode) actual.array[i], (BitmapIndexedNode) expected.array[i]);
        }
    }

    //-------------------------------------------------------------------

    @Test
    public void testArrayNodeAndArrayNodeSplicerDuplication() {

        final int shift = 5;

        INode leftNode1 = BitmapIndexedNode.EMPTY;
        for (int i = 0; i < 17; i++) {
            leftNode1 = leftNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }

        INode leftNode2 = leftNode1;
        for (int i = 16; i < 33; i++) {
            leftNode2 = leftNode2.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }
        final ArrayNode expected = (ArrayNode) leftNode2;

        INode rightNode1 = BitmapIndexedNode.EMPTY;
        for (int i = 16; i < 33; i++) {
            rightNode1 = rightNode1.assoc(shift, i * 32 , new HashCodeKey("left" + i, i * 32), i, new Box(null));
        }

        final Duplications duplications = new Duplications(0);
        final ArrayNode actual = (ArrayNode) new Seqspert.ArrayNodeAndArrayNodeSplicer().splice(shift, duplications, null, leftNode1, 0, null, rightNode1);
        // TODO: numDuplicates

        assertEquals(actual.count, expected.count);
        for (int i = 0; i < 32; i++) {
            assertBitmapIndexedNodesEqual((BitmapIndexedNode) actual.array[i], (BitmapIndexedNode) expected.array[i]);
        }
    }

    //-------------------------------------------------------------------

    @Test
    public void testHashCollisionNodeAndHashCollisionNodeCollision() {
        final int shift = 0;
        final int hashCode = 2;
        final Object key0 = new HashCodeKey("key0", hashCode);
        final Object key1 = new HashCodeKey("key1", hashCode);
        final Object key2 = new HashCodeKey("key2", hashCode);
        final Object key3 = new HashCodeKey("key3", hashCode);
        final Object value0 = "value0";
        final Object value1 = "value1";
        final Object value2 = "value2";
        final Object value3 = "value3";
        
        final HashCollisionNode leftNode   = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
        final HashCollisionNode rightNode =  new HashCollisionNode(null, hashCode, 2, new Object[]{key2, value2, key3, value3});

        final AtomicReference<Thread> edit = new AtomicReference<Thread>();
        final Box addedLeaf = new Box(null);
        final HashCollisionNode expected = (HashCollisionNode) leftNode.
                assoc(edit, shift, hashCode, key2, value2, addedLeaf).
                assoc(edit, shift, hashCode, key3, value3, addedLeaf);

        final Duplications duplications = new Duplications(0);
        final HashCollisionNode actual =  (HashCollisionNode) Seqspert.spliceNodes(shift, duplications, null, leftNode, 0, null, rightNode);
        assertEquals(0, duplications.duplications);
        
        assertEquals(expected.count, actual.count);
        assertArrayEquals(expected.array, actual.array);
    }
    
    @Test
    public void testHashCollisionNodeAndHashCollisionNodeDuplication() {
        final int shift = 0;
        final int hashCode = 2;
        final Object key0 = new HashCodeKey("key0", hashCode);
        final Object key1 = new HashCodeKey("key1", hashCode);
        final Object key2 = new HashCodeKey("key2", hashCode);
        final Object value0 = "value0";
        final Object value1 = "value1";
        final Object value2 = "value2";
        
        final HashCollisionNode leftNode   = new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
        final HashCollisionNode rightNode =  new HashCollisionNode(null, hashCode, 2, new Object[]{key1, value1, key2, value2});

        final AtomicReference<Thread> edit = new AtomicReference<Thread>();
        final Box addedLeaf = new Box(null);
        final HashCollisionNode expected = (HashCollisionNode) leftNode.
                assoc(edit, shift, hashCode, key1, value1, addedLeaf).
                assoc(edit, shift, hashCode, key2, value2, addedLeaf);

        final Duplications duplications = new Duplications(0);
        final HashCollisionNode actual =  (HashCollisionNode) Seqspert.spliceNodes(shift, duplications, null, leftNode, 0, null, rightNode);
        assertEquals(1, duplications.duplications);
        
        assertEquals(expected.count, actual.count);
        assertArrayEquals(expected.array, actual.array);
    }
    
}
