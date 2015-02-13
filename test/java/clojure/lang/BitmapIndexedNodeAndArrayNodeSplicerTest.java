package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class BitmapIndexedNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Hasher hasher = new Hasher() {@Override
    public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};

    public void test(Object leftKey0, Object leftValue0, Object leftKey1, Object leftValue1, 
                     Hasher rightHasher, int rightStart, int rightEnd, boolean sameRight) {
        final INode leftNode = TestUtils.create(shift, leftKey0, leftValue0, leftKey1, leftValue1);
        final INode rightNode = TestUtils.create(shift, rightHasher, rightStart, rightEnd);

        final Counts expectedCounts = new Counts(Counts.resolveRight, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightHasher, rightStart, rightEnd, expectedCounts);
                
        final Counts actualCounts = new Counts(Counts.resolveRight, 0, 0); // TODO - resolveLeft ?
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }
        
    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key" + 1, hasher.hash(1)), "value1", new HashCodeKey("key" + 2, hasher.hash(2)), "value2", hasher, 3, 31, false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key" + 1, hasher.hash(3)), "value1", new HashCodeKey("key" + 2, hasher.hash(4)), "value2", hasher, 3, 31, false);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value1", new HashCodeKey("key" + 4, hasher.hash(4)), "value2", hasher, 3, 31, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value3", new HashCodeKey("key" + 4, hasher.hash(4)), "value4", hasher, 3, 31, true);
    }

    interface Keyer {HashCodeKey key(int i);}

    Keyer leftKeyer  = new Keyer(){@Override public HashCodeKey key(int i){return new HashCodeKey("leftKey" + i, TestUtils.defaultHasher.hash(i));}};
    Keyer rightKeyer = new Keyer(){@Override public HashCodeKey key(int i){return new HashCodeKey("rightKey" + i, TestUtils.defaultHasher.hash(i));}};
        
    private INode makeNode(int shift, int start, int end, Keyer keyer) {
        INode node = BitmapIndexedNode.EMPTY;
        for (int i = start; i < end; i++)
            node = TestUtils.assoc(shift, node , keyer.key(i), "value" + i, new Counts());
        return node;
    }

    private INode assocN(int shift, INode node, int start, int end, Keyer keyer, Counts counts) {
        for (int i = start; i < end; i++)
            node = TestUtils.assoc(shift, node , keyer.key(i), "value" + i, counts);
        return node;
    }

    @Test
    public void testPromotionBoth() {

        // hash collision node do not arise until left and right children are merged...
        
        final INode leftNode  = makeNode(shift, 0, 16, leftKeyer);
        assertTrue(leftNode instanceof BitmapIndexedNode);

        final INode rightNode = makeNode(shift, 0, 32, rightKeyer);
        assertTrue(rightNode instanceof ArrayNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift, leftNode, 0, 32, rightKeyer, expectedCounts);
        assertTrue(expectedNode instanceof ArrayNode);
                
        final Counts actualCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);
        assertTrue(actualNode instanceof ArrayNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
    }

    @Test
    public void testPromotionLeft() {

        // hash collision node arises from left hand node
        
        final INode leftNode = assocN(shift,
                                      makeNode(shift,  0, 15, leftKeyer),
                                      0,
                                      15,
                                      rightKeyer,
                                      new Counts());
        assertTrue(leftNode instanceof BitmapIndexedNode);
        
        final INode rightNode = makeNode(shift, 15, 32, rightKeyer);
        assertTrue(rightNode instanceof ArrayNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift, leftNode, 15, 32, rightKeyer, expectedCounts);
        assertTrue(expectedNode instanceof ArrayNode);
                
        final Counts actualCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);
        assertTrue(actualNode instanceof ArrayNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
    }

    @Test
    public void testPromotionRight() {

        // hash collision node arises from right hand node
        
        final INode leftNode = makeNode(shift,  0, 15, leftKeyer);
        assertTrue(leftNode instanceof BitmapIndexedNode);

        final INode rightNode = assocN(shift,
                                       makeNode(shift, 15, 32, leftKeyer),
                                       15,
                                       32,
                                       rightKeyer,
                                       new Counts());
        assertTrue(rightNode instanceof ArrayNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift,
                                          assocN(shift, leftNode, 15, 32, leftKeyer, expectedCounts),
                                          15,
                                          32,
                                          rightKeyer,
                                          expectedCounts);
        assertTrue(expectedNode instanceof ArrayNode);
                
        final Counts actualCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);
        assertTrue(actualNode instanceof ArrayNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
    }

}
