package clojure.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class TestUtils {

    public static void assertHashMapEquals(PersistentHashMap expected, PersistentHashMap actual) {
        assertEquals(expected.count, actual.count);
        assertNodeEquals(expected.root, actual.root);
        assertEquals(expected.hasNull, actual.hasNull);
        assertEquals(expected.nullValue, actual.nullValue);
    }

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
                assertTrue(actual instanceof INode);
                assertNodeEquals((INode) expected, (INode) actual);
            } else {
                assertEquals(expected, actual);
            }
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
            for (int i = 0; i < 32; i++) {
                final INode e = expected.array[i];
                final INode a = actual.array[i];
                if (e != null || a != null)
                    assertNodeEquals(e, a);
            }
        }
    }

    private static String toBinaryString(int bitmap) {
        final String tmp = Integer.toBinaryString(bitmap);
        return tmp;
        //return "0000000000000000".substring(16 - tmp.length()) + tmp;
    }

    public static void assertBitmapIndexedNodeEquals(BitmapIndexedNode expected, BitmapIndexedNode actual) {
        if (expected != actual) {
            assertEquals(toBinaryString(expected.bitmap), toBinaryString(actual.bitmap));
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
            node = node.assoc(shift, BitmapIndexedNodeUtils.hash(key), key, value, box);
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


    public static interface Hasher {public int hash(int i);}

    public static final Hasher defaultHasher = new Hasher() {@Override
    public int hash(int i) { return i; }};

    public static interface Factory {
        public Object makeKey(int i);
        public Object makeValue(int i);
        public int hash(int i);
    }

    public static class SimpleFactory {
        public Object key(int i) {return new HashCodeKey("key" + i, hash(i));}
        public Object value(int i) {return "value" + i;}
        public int hash(int i) {return ((i + 2) << 10) | ((i + 1) << 5) | i;}
    } 
        
    public static INode assocN(int shift, INode node, int start, int end, Counts counts) {
        return assocN(shift, node, defaultHasher, start, end, counts);
    }

    public static INode assocN(int shift,
                               INode node,
                               Hasher hasher, int start, int end,
                               Counts counts) {
        for (int i = start; i < end; i++)
            node = assoc(shift, node , new HashCodeKey("key" + i, hasher.hash(i)), ("value"+i), counts);
        return node;
    }

    public static INode assocN(int shift, INode node,
                               Object key0, Object value0,
                               int start, int end,
                               Counts counts) {
        return assocN(shift, assoc(shift, node, key0, value0, counts), start, end, counts); 
    }

    public static INode assocN(int shift, INode node,
                               Object key0, Object value0,
                               Hasher hasher, int start, int end,
                               Counts counts) {
        return assocN(shift, assoc(shift, node, key0, value0, counts), hasher, start, end, counts);
    }
    
    public static INode assocN(int shift, INode node,
                               Hasher hasher, int start, int end,
                               Object key0, Object value0,
                               Counts counts) {
        return assoc(shift, assocN(shift, node, hasher, start, end, counts), key0, value0, counts); 
    }
    
    public static INode assocN(int shift, INode node,
                               int start, int end,
                               Object key0, Object value0,
                               Counts counts) {
        return assocN(shift, node, defaultHasher, start, end, key0, value0, counts); 
    }

    public static INode assocN(int shift, INode node,
                               Hasher hasher, int start, int end,
                               Object key0, Object value0,
                               Object key1, Object value1,
                               Counts counts) {
        return assoc(shift, assocN(shift, node, hasher, start, end, key0, value0, counts), key1, value1, counts);
    }
    
    public static INode assocN(int shift, INode node,
                               int start, int end,
                               Object key0, Object value0,
                               Object key1, Object value1,
                               Counts counts) {
        return assocN(shift, node, defaultHasher, start, end, key0, value0, key1, value1, counts);
    }

    public static INode create(int shift,
                               Object key, Object value) {
        return assoc(shift, BitmapIndexedNode.EMPTY, key, value, new Counts());
    }
    
    public static INode create(int shift,
                               Object key0, Object value0,
                               Object key1, Object value1) {
        return assoc(shift, BitmapIndexedNode.EMPTY, key0, value0, key1, value1, new Counts());
    }
    
    public static INode create(int shift,
                               Object key0, Object value0,
                               Object key1, Object value1,
                               Object key2, Object value2) {
        return assoc(shift, BitmapIndexedNode.EMPTY, key0, value0, key1, value1, key2, value2, new Counts());
    }

    public static INode create(int shift,
                               Object key, Object value,
                               Hasher hasher,
                               int start, int end) {
        return assocN(shift, BitmapIndexedNode.EMPTY, key, value, hasher, start, end, new Counts());
    }

    public static INode create(int shift,
                               Object key, Object value,
                               int start, int end) {
        return create(shift, key, value, defaultHasher, start, end);
    }

    public static INode create(int shift,
                               int start, int end,
                               Object key, Object value) {
        return create(shift, defaultHasher, start, end, key, value);
    }

    public static INode create(int shift,
                               Hasher hasher, int start, int end,
                               Object key, Object value) {
        return assocN(shift, BitmapIndexedNode.EMPTY, hasher, start, end, key, value, new Counts());
    }

    public static INode create(int shift,
                               int start, int end,
                               Object key0, Object value0,
                               Object key1, Object value1) {
        return create(shift, defaultHasher, start, end, key0, value0, key1, value1);
    }

    public static INode create(int shift,
                               Hasher hasher,
                               int start, int end,
                               Object key0, Object value0,
                               Object key1, Object value1) {
        return assocN(shift, BitmapIndexedNode.EMPTY, hasher, start, end, key0, value0, key1, value1, new Counts());
    }

    public static INode create(int shift,
                               int start, int end) {
        return create(shift, defaultHasher, start, end);
    }

    public static INode create(int shift,
                               Hasher hasher, int start, int end) {
        return assocN(shift, BitmapIndexedNode.EMPTY, hasher, start, end, new Counts());
    }

    public static void wrapSplicers() {
        Seqspert.keyValuePairAndKeyValuePairSplicer             = new TestSplicer(Seqspert.keyValuePairAndKeyValuePairSplicer);
        Seqspert.keyValuePairAndBitmapIndexedNodeSplicer        = new TestSplicer(Seqspert.keyValuePairAndBitmapIndexedNodeSplicer);
        Seqspert.keyValuePairAndArrayNodeSplicer                = new TestSplicer(Seqspert.keyValuePairAndArrayNodeSplicer);
        Seqspert.keyValuePairAndHashCollisionNodeSplicer        = new TestSplicer(Seqspert.keyValuePairAndHashCollisionNodeSplicer);
        Seqspert.bitmapIndexedNodeAndKeyValuePairSplicer        = new TestSplicer(Seqspert.bitmapIndexedNodeAndKeyValuePairSplicer);
        Seqspert.bitmapIndexedNodeAndBitmapIndexedNodeSplicer   = new TestSplicer(Seqspert.bitmapIndexedNodeAndBitmapIndexedNodeSplicer);
        Seqspert.bitmapIndexedNodeAndArrayNodeSplicer           = new TestSplicer(Seqspert.bitmapIndexedNodeAndArrayNodeSplicer);
        Seqspert.bitmapIndexedNodeAndHashCollisionNodeSplicer   = new TestSplicer(Seqspert.bitmapIndexedNodeAndHashCollisionNodeSplicer);
        Seqspert.arrayNodeAndKeyValuePairSplicer                = new TestSplicer(Seqspert.arrayNodeAndKeyValuePairSplicer);
        Seqspert.arrayNodeAndBitmapIndexedNodeSplicer           = new TestSplicer(Seqspert.arrayNodeAndBitmapIndexedNodeSplicer);
        Seqspert.arrayNodeAndArrayNodeSplicer                   = new TestSplicer(Seqspert.arrayNodeAndArrayNodeSplicer);
        Seqspert.arrayNodeAndHashCollisionNodeSplicer           = new TestSplicer(Seqspert.arrayNodeAndHashCollisionNodeSplicer);
        Seqspert.hashCollisionNodeAndKeyValuePairSplicer        = new TestSplicer(Seqspert.hashCollisionNodeAndKeyValuePairSplicer);
        Seqspert.hashCollisionNodeAndBitmapIndexedNodeSplicer   = new TestSplicer(Seqspert.hashCollisionNodeAndBitmapIndexedNodeSplicer);
        Seqspert.hashCollisionNodeAndArrayNodeSplicer           = new TestSplicer(Seqspert.hashCollisionNodeAndArrayNodeSplicer);
        Seqspert.hashCollisionNodeAndHashCollisionNodeSplicer   = new TestSplicer(Seqspert.hashCollisionNodeAndHashCollisionNodeSplicer);
    }    

    public static void unwrapSplicers() {
        Seqspert.keyValuePairAndKeyValuePairSplicer             = ((TestSplicer)Seqspert.keyValuePairAndKeyValuePairSplicer).splicer;
        Seqspert.keyValuePairAndBitmapIndexedNodeSplicer        = ((TestSplicer)Seqspert.keyValuePairAndBitmapIndexedNodeSplicer).splicer;
        Seqspert.keyValuePairAndArrayNodeSplicer                = ((TestSplicer)Seqspert.keyValuePairAndArrayNodeSplicer).splicer;
        Seqspert.keyValuePairAndHashCollisionNodeSplicer        = ((TestSplicer)Seqspert.keyValuePairAndHashCollisionNodeSplicer).splicer;
        Seqspert.bitmapIndexedNodeAndKeyValuePairSplicer        = ((TestSplicer)Seqspert.bitmapIndexedNodeAndKeyValuePairSplicer).splicer;
        Seqspert.bitmapIndexedNodeAndBitmapIndexedNodeSplicer   = ((TestSplicer)Seqspert.bitmapIndexedNodeAndBitmapIndexedNodeSplicer).splicer;
        Seqspert.bitmapIndexedNodeAndArrayNodeSplicer           = ((TestSplicer)Seqspert.bitmapIndexedNodeAndArrayNodeSplicer).splicer;
        Seqspert.bitmapIndexedNodeAndHashCollisionNodeSplicer   = ((TestSplicer)Seqspert.bitmapIndexedNodeAndHashCollisionNodeSplicer).splicer;
        Seqspert.arrayNodeAndKeyValuePairSplicer                = ((TestSplicer)Seqspert.arrayNodeAndKeyValuePairSplicer).splicer;
        Seqspert.arrayNodeAndBitmapIndexedNodeSplicer           = ((TestSplicer)Seqspert.arrayNodeAndBitmapIndexedNodeSplicer).splicer;
        Seqspert.arrayNodeAndArrayNodeSplicer                   = ((TestSplicer)Seqspert.arrayNodeAndArrayNodeSplicer).splicer;
        Seqspert.arrayNodeAndHashCollisionNodeSplicer           = ((TestSplicer)Seqspert.arrayNodeAndHashCollisionNodeSplicer).splicer;
        Seqspert.hashCollisionNodeAndKeyValuePairSplicer        = ((TestSplicer)Seqspert.hashCollisionNodeAndKeyValuePairSplicer).splicer;
        Seqspert.hashCollisionNodeAndBitmapIndexedNodeSplicer   = ((TestSplicer)Seqspert.hashCollisionNodeAndBitmapIndexedNodeSplicer).splicer;
        Seqspert.hashCollisionNodeAndArrayNodeSplicer           = ((TestSplicer)Seqspert.hashCollisionNodeAndArrayNodeSplicer).splicer;
        Seqspert.hashCollisionNodeAndHashCollisionNodeSplicer   = ((TestSplicer)Seqspert.hashCollisionNodeAndHashCollisionNodeSplicer).splicer;
    }    
}
