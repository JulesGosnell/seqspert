package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;
import clojure.lang.TestUtils.Hasher;

public class BitmapIndexedNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    int shift = 0;
    final Hasher hasher = new Hasher() {@Override
    public int hash(int i) { return ((i + 2) << 10) | ((i + 1) << 5) | i; }};
    Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();
        
    public void test(Object leftKey0, Object leftValue0, Object leftKey1, Object leftValue1,
                     Hasher rightHasher, int rightStart, int rightEnd, boolean leftSame, boolean rightSame) {

        final INode leftNode = TestUtils.create(shift, leftKey0, leftValue0, leftKey1, leftValue1);
        final INode rightNode = TestUtils.create(shift, rightHasher, rightStart, rightEnd);
                
        final IFn resolveFunction = leftSame ? Counts.resolveLeft : Counts.resolveRight;

        final Counts expectedCounts = new Counts(resolveFunction, 0, 0);
        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightHasher, rightStart, rightEnd, expectedCounts);
                
        final Counts actualCounts = new Counts(resolveFunction, 0, 0);
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (leftSame) assertSame(leftNode, actualNode); // expectedNode not as expected !
        if (rightSame) assertSame(rightNode, actualNode);
    }
        
    @Override
    @Test
    public void testDifferent() {
        // no promotion
        test(new HashCodeKey("key" + 1, hasher.hash(1)), "value1", new HashCodeKey("key" + 2, hasher.hash(2)), "value2", hasher, 3, 5, false, false);
        // promotion - HCN on LHS
        test(new HashCodeKey("key" + 1, hasher.hash(1)), "value1", new HashCodeKey("key" + 2, hasher.hash(2)), "value2", hasher, 3, 19, false, false);
        // promotion - HCN arising from splice
        test(new HashCodeKey("key" + 1, hasher.hash(1)), "value1", new HashCodeKey("key" + 3, hasher.hash(3)), "value2", hasher, 3, 19, false, false);
    }

    @Test
    public void testAargh() {
        // need test with initial HCN on right and promotion - no splice..
        final INode leftNode = TestUtils.create(0,
                                                new HashCodeKey("key1", hasher.hash(1)), "value1",
                                                hasher, 2, 17);

        assertEquals(Integer.bitCount(((BitmapIndexedNode)leftNode).bitmap), 16);

        final INode rightNode = TestUtils.create(0,
                                                 new HashCodeKey("key18.1", hasher.hash(18)), "value18",
                                                 new HashCodeKey("key18.2", hasher.hash(18)), "value18"
                                                 );

        assertEquals(Integer.bitCount(((BitmapIndexedNode)rightNode).bitmap), 1);
        assertTrue(((BitmapIndexedNode)rightNode).array[1] instanceof HashCollisionNode);

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assoc(shift, leftNode,
                                                   new HashCodeKey("key18.1", hasher.hash(18)), "value18",
                                                   new HashCodeKey("key18.2", hasher.hash(18)), "value18",
                                                   expectedCounts);

        assertTrue(expectedNode instanceof ArrayNode);
        assertEquals(((ArrayNode)expectedNode).count, 17);
        //assertTrue(((ArrayNode)expectedNode).array[19] instanceof HashCollisionNode);
                
        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);

    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        // no promotion
        test(new HashCodeKey("key" + 1, hasher.hash(3)), "value1", new HashCodeKey("key" + 2, hasher.hash(4)), "value2", hasher, 3, 5, false, false);
        // promotion
        test(new HashCodeKey("key" + 1, hasher.hash(3)), "value1", new HashCodeKey("key" + 2, hasher.hash(3)), "value2", hasher, 3, 19, false, false);
        // promotion
        TestUtils.wrapSplicers();
        splicer = new TestSplicer(splicer);
        shift = 5;
        test(new HashCodeKey("key" + 1, hasher.hash(1)), "value1", new HashCodeKey("key2.1", hasher.hash(2)), "value2", hasher, 2, 18, false, false);
        shift = 0;
        splicer = ((TestSplicer)splicer).splicer;
        TestUtils.unwrapSplicers();
    }

    @Test
    public void testAargh2() {
        // two BINS each containing an HCN. On splicing HCNs collide and resulting BIN is promoted to an AN...
        final INode leftNode = TestUtils.create(0,
                                                new HashCodeKey("key1.1", hasher.hash(1)), "value1",
                                                hasher, 1, 17);

        assertEquals(Integer.bitCount(((BitmapIndexedNode)leftNode).bitmap), 16);
        assertTrue(((BitmapIndexedNode)leftNode).array[1] instanceof HashCollisionNode);

        final INode rightNode = TestUtils.create(0,
                                                 new HashCodeKey("key1.2", hasher.hash(1)), "value1",
                                                 new HashCodeKey("key1.3", hasher.hash(1)), "value1",
                                                 new HashCodeKey("key17", hasher.hash(17)), "value17"
                                                 );

        assertEquals(Integer.bitCount(((BitmapIndexedNode)rightNode).bitmap), 2);
        assertTrue(((BitmapIndexedNode)rightNode).array[1] instanceof HashCollisionNode);

        final Counts expectedCounts = new Counts();
        final INode expectedNode = TestUtils.assoc(shift, leftNode,
                                                   new HashCodeKey("key1.2", hasher.hash(1)), "value1",
                                                   new HashCodeKey("key1.3", hasher.hash(1)), "value1",
                                                   new HashCodeKey("key17", hasher.hash(17)), "value17",
                                                   expectedCounts);

        assertTrue(expectedNode instanceof ArrayNode);
        assertEquals(((ArrayNode)expectedNode).count, 17);
        assertTrue(((ArrayNode)expectedNode).array[1] instanceof HashCollisionNode);
                
        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
    }

    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value1", new HashCodeKey("key" + 4, hasher.hash(4)), "value2", hasher, 3, 5, false, false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        // leftSame
        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value3", new HashCodeKey("key" + 4, hasher.hash(4)), "value4", hasher, 3, 5, true, false);
        // rightSame
        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value3", new HashCodeKey("key" + 4, hasher.hash(4)), "value4", hasher, 3, 6, false, true);

        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value3", new HashCodeKey("key" + 4, hasher.hash(4)), "value4", hasher, 4, 20, false, false);
        test(new HashCodeKey("key" + 3, hasher.hash(3)), "value3", new HashCodeKey("key4.1", hasher.hash(4)), "value4", hasher, 4, 20, false, false);
    }

    @Ignore
    @Test
    public void testPromotion() {

        // different numbers of nodes in left and right hand sides
        // hash nodes at different positions in left and right hand sides

        final int leftStart = 0;
        final int rightStart = 8;

        for (int leftEnd = 1; leftEnd < 16; leftEnd++) {
            for (int rightEnd = 9; rightEnd < 24; rightEnd++) {
                for (int h = 9; h < 24; h++) {
                    for (int bit = 0; bit < 2; bit++) {

                        System.out.println("HERE: " + leftEnd + " : " + rightEnd + " : " + h + " : " + bit);
                    
                        INode leftNode  = TestUtils.create(shift, leftStart, leftEnd);
                        final INode rightNode = TestUtils.create(shift, rightStart, rightEnd);

                        final int i = (bit == 0 ? leftStart : rightStart) + h;
                        final IFn resolveFunction = Counts.resolveLeft;
                        final Counts actualCounts = new Counts(resolveFunction, 0, 0);
                        leftNode = TestUtils.assoc(shift,
                                        bit == 0 ? leftNode : rightNode,
                                        new HashCodeKey("collision" + i, TestUtils.defaultHasher.hash(i)),
                                        ("value"+i), new Counts(resolveFunction, 0, 0));
                        
                        final Counts expectedCounts = new Counts(resolveFunction, 0, 0);
                        final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);
                
                        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

                        assertEquals(expectedCounts, actualCounts);
                        assertNodeEquals(expectedNode, actualNode);
                    }
                }
            }
        }
    }

}
