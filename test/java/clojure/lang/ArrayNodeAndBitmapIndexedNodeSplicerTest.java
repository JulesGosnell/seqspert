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

public class ArrayNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {
        
    final int shift = 0;
    final Hasher hasher = new Hasher() {@Override
    public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};
    public void test(Hasher leftHasher, int leftStart, int leftEnd,
                     Object rightKey0, Object rightValue0,
                     Object rightKey1, Object rightValue1,
                     boolean same) {
        final INode leftNode = TestUtils.create(shift, leftHasher, leftStart, leftEnd);
        final INode rightNode = TestUtils.create(shift, rightKey0, rightValue0, rightKey1, rightValue1);
        
        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assoc(shift, leftNode, rightKey0, rightValue0, rightKey1, rightValue1, expectedCounts);
        
        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);
        
        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(leftNode, actualNode); // expectedNode not as expected !
    }

    @Override
    @Test
    public void testDifferent() {
        test(hasher, 3, 31,
             new HashCodeKey("key1", hasher.hash(1)), "value1",
             new HashCodeKey("key2", hasher.hash(2)), "value2",
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(hasher, 2, 31,
             new HashCodeKey("key1", hasher.hash(1)), "value1",
             new HashCodeKey("collisionKey2", hasher.hash(2)), "collisionValue2",
             false);
    }
        
    @Override
    @Test
    public void testSameKey() {
        test(hasher, 2, 31,
             new HashCodeKey("key1", hasher.hash(1)), "value1",
             new HashCodeKey("key2", hasher.hash(2)), "duplicationValue2",
             false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        // rhs is two KVPs
        test(hasher, 1, 31,
             new HashCodeKey("key1", hasher.hash(1)), "value1",
             new HashCodeKey("key2", hasher.hash(2)), "value2",
             true);
        //         TODO: investigate
        //         rhs is an HCN
        //         test(hasher, 1, 31,
        //              new HashCodeKey("key1", hasher.hash(1)), "value1",
        //              new HashCodeKey("key2", hasher.hash(1)), "value2",
        //              false);
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
        
        final INode leftNode = makeNode(shift, 0, 32, leftKeyer);
        assertTrue(leftNode instanceof ArrayNode);

        final INode rightNode = makeNode(shift, 0, 16, rightKeyer);
        assertTrue(rightNode instanceof BitmapIndexedNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift, leftNode, 0, 16, rightKeyer, expectedCounts);
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
                                      makeNode(shift,  15, 32, leftKeyer),
                                      15,
                                      32,
                                      rightKeyer,
                                      new Counts());
        assertTrue(leftNode instanceof ArrayNode);

        final INode rightNode = makeNode(shift, 0, 15, rightKeyer);
        assertTrue(rightNode instanceof BitmapIndexedNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift, leftNode, 0, 15, rightKeyer, expectedCounts);
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
        
        final INode leftNode = makeNode(shift,  15, 32, leftKeyer);
        assertTrue(leftNode instanceof ArrayNode);

        final INode rightNode = assocN(shift,
                                       makeNode(shift, 0, 15, leftKeyer),
                                       0,
                                       15,
                                       rightKeyer,
                                       new Counts());
        assertTrue(rightNode instanceof BitmapIndexedNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift,
                                          assocN(shift, leftNode, 0, 15, leftKeyer, expectedCounts),
                                          0,
                                          15,
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
