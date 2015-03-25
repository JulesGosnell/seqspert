package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
        final INode expectedNode = TestUtils.merge(shift, leftNode, rightNode, expectedCounts);
                
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
        
        final INode leftNode = makeNode(shift, 8, 24, leftKeyer);
        assertTrue(leftNode instanceof BitmapIndexedNode);

        final INode rightNode = makeNode(shift, 12, 28, rightKeyer);
        assertTrue(rightNode instanceof BitmapIndexedNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift, leftNode, 12, 28, rightKeyer, expectedCounts);
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
                                      makeNode(shift,  0, 16, leftKeyer),
                                      0,
                                      16,
                                      rightKeyer,
                                      new Counts());
        assertTrue(leftNode instanceof BitmapIndexedNode);

        final INode rightNode = makeNode(shift, 16, 32, rightKeyer);
        assertTrue(rightNode instanceof BitmapIndexedNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift, leftNode, 16, 32, rightKeyer, expectedCounts);
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
        
        final INode leftNode = makeNode(shift,  0, 16, leftKeyer);
        assertTrue(leftNode instanceof BitmapIndexedNode);

        final INode rightNode = assocN(shift,
                                       makeNode(shift, 16, 32,leftKeyer),
                                       16,
                                       32,
                                       rightKeyer,
                                       new Counts());
        assertTrue(rightNode instanceof BitmapIndexedNode);
            
        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode expectedNode = assocN(shift,
                                          assocN(shift, leftNode, 16, 32, leftKeyer, expectedCounts),
                                          16,
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

    public void testPromotionNew(int leftStart, int leftEnd, int rightStart, int rightEnd, int collision1, boolean side1, int collision2, boolean side2) {

        //System.out.println(leftStart + "-" + leftEnd + ", " + rightStart + "-" + rightEnd + ", " + h + ", " + left);
        final Object extraKey1 = new HashCodeKey("collision1" + collision1, TestUtils.defaultHasher.hash(collision1));
        final Object extraValue1 = "value1" + collision1;
        final Object extraKey2 = new HashCodeKey("collision2" + collision2, TestUtils.defaultHasher.hash(collision2));
        final Object extraValue2 = "value2" + collision1;

        INode leftNode = BitmapIndexedNode.EMPTY;
        final Counts leftCounts = new Counts(Counts.resolveLeft, 0, 0);
        for (int l = leftStart; l < leftEnd; l++) {
            leftNode = TestUtils.assoc(shift, leftNode, leftKeyer.key(l), "value" + l, leftCounts);
            if (side1 && l == collision1) leftNode = TestUtils.assoc(shift, leftNode, extraKey1, extraValue1, leftCounts);
            if (side2 && l == collision2) leftNode = TestUtils.assoc(shift, leftNode, extraKey2, extraValue2, leftCounts);
        }
        
        INode rightNode = BitmapIndexedNode.EMPTY;
        final Counts rightCounts = new Counts(Counts.resolveLeft, 0, 0);
        for (int r = rightStart; r < rightEnd; r++) {
            rightNode = TestUtils.assoc(shift, rightNode, rightKeyer.key(r), "value" + r, rightCounts);
            if (!side1 && r == collision1) rightNode = TestUtils.assoc(shift, rightNode, extraKey1, extraValue1, rightCounts);
            if (!side2 && r == collision2) rightNode = TestUtils.assoc(shift, rightNode, extraKey2, extraValue2, rightCounts);
        }
            
        //final IFn resolveFunction = Counts.resolveLeft;

        final Counts expectedCounts = new Counts(Counts.resolveLeft, 0, 0);
        INode expectedNode = leftNode;
        
        // TODO: iterate over right hand BIN, adding associations to left hand BIN...
        for (ISeq seq = rightNode.nodeSeq(); seq != null; seq = seq.next()) {
        	final MapEntry entry = (MapEntry) seq.first();
        	expectedNode = TestUtils.assoc(shift, expectedNode, entry.key(), entry.val(), expectedCounts);
        }

        final Counts actualCounts = new Counts(Counts.resolveLeft, 0, 0);
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
    }

    @Test
    public void testPromotionNew() {
        final int leftStart = 0;
        final int maxLeft = 32;
        final int rightStart = 0;
        final int maxRight = 32;
        final int collisionMax1 = 32;
        final int collisionMax2 = 32;
        
        for (int leftEnd = 1; leftEnd < maxLeft; leftEnd++) {
        	for (int rightEnd = 1; rightEnd < maxRight; rightEnd++) {
        		for (int collision1 = 0; collision1 < collisionMax1; collision1++) {
        			for (int collision2 = 0; collision2 < collisionMax2; collision2++) {
        				for (int side1 = 0; side1 < 2; side1++) {
        					for (int side2 = 0; side2 < 2; side2++) {
        						testPromotionNew(leftStart, leftEnd, rightStart, rightEnd, collision1, side1 == 0, collision2, side2 == 0);
        					}}}}}}
        
        // BIN/BIN
        //testPromotionNew(0, 2, 8, 23, 22, false); // count > 15
        //testPromotionNew(0, 9, 8, 17, 15, false); // count > 16
        //testPromotionNew(0, 3, 8, 23, 22, false);
        //BIN/AN
        //testPromotionNew(0, 1, 8, 25, 23, false);
        //testPromotionNew(0, 1, 8, 25, 8, false);
        // AN/BIN
        //testPromotionNew(0, 17, 8, 1, 1, false);
        //testPromotionNew(0, 17, 8, 18, 17, false);
        // AN/AN
        //testPromotionNew(0, 17, 8, 25, 17, false);
    }  

    // @Test
    // public void testPromotion() {

    //     // different numbers of nodes in left and right hand sides
    //     // hash nodes at different positions in left and right hand sides

    //     final int leftStart = 0;
    //     final int rightStart = 8;

    //     for (int leftEnd = 1; leftEnd < 16; leftEnd++) {
    //         for (int rightEnd = 9; rightEnd < 24; rightEnd++) {
    //             for (int h = 9; h < 24; h++) {
    //                 for (int bit = 0; bit < 2; bit++) {

    //                     ///                             2                 23               9           1
                        
    //                     System.out.println("HERE: " + leftEnd + " : " + rightEnd + " : " + h + " : " + bit);
                    
    //                     INode leftNode  = TestUtils.create(shift, leftStart, leftEnd);
    //                     assertTrue(leftNode instanceof BitmapIndexedNode);
                        
    //                     INode rightNode = TestUtils.create(shift, rightStart, rightEnd);
    //                     assertTrue(rightNode instanceof BitmapIndexedNode);

    //                     final int i = (bit == 0 ? leftStart : rightStart) + h;
    //                     final IFn resolveFunction = Counts.resolveLeft;
    //                     final Counts actualCounts = new Counts(resolveFunction, 0, 0);

    //                     final Object extraKey = new HashCodeKey("collision" + i, TestUtils.defaultHasher.hash(i));
    //                     final Object extraValue = "value" + i;
                        
    //                     if (bit == 0) {
    //                         leftNode = TestUtils.assoc(shift, leftNode, extraKey, extraValue,
    //                                                    new Counts(resolveFunction, 0, 0));
    //                         assertTrue(leftNode instanceof BitmapIndexedNode);
    //                     } else {
    //                         rightNode = TestUtils.assoc(shift, rightNode, extraKey, extraValue,
    //                                                     new Counts(resolveFunction, 0, 0));
    //                         assertTrue(rightNode instanceof BitmapIndexedNode);
    //                     }
                        
    //                     final Counts expectedCounts = new Counts(resolveFunction, 0, 0);
    //                     INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);
    //                     if (bit == 1) {
    //                         expectedNode = TestUtils.assoc(shift, expectedNode, extraKey, extraValue,
    //                                                        expectedCounts);
    //                     }
                
    //                     final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

    //                     assertEquals(expectedCounts, actualCounts);
    //                     assertNodeEquals(expectedNode, actualNode);
    //                 }
    //             }
    //         }
    //     }
    // }
    
    @Test
    public void testPromotionAgain() {
    		final HashCodeKey[] left = new HashCodeKey[]{
    		new HashCodeKey("black-" + 3, 3),
    		new HashCodeKey("white-" + 67, 67),
    		new HashCodeKey("white-" + 663651, 663651),
    		new HashCodeKey("black-" + 131, 131),
    		new HashCodeKey("white-" + 26858691, 26858691),
    		new HashCodeKey("white-" + 551139, 551139),
    		new HashCodeKey("white-" + 19826979, 19826979),
    		new HashCodeKey("white-" + 2467, 2467),
    		new HashCodeKey("white-" + 451, 451),
    		new HashCodeKey("white-" + 418307, 418307),
    		new HashCodeKey("white-" + 611, 611),
    		new HashCodeKey("black-" + 4739, 4739),
    		new HashCodeKey("black-" + 14201507, 14201507),
    		new HashCodeKey("black-" + 12995, 12995),
    		new HashCodeKey("white-" + 13527747, 13527747),
    		new HashCodeKey("black-" + 15171, 15171),
    		new HashCodeKey("white-" + 9128771, 9128771),
    		new HashCodeKey("black-" + 892835, 892835),
    		};
    		
    		final HashCodeKey[] right = new HashCodeKey[]{
    		new HashCodeKey("black-" + 3, 3),
    		new HashCodeKey("white-" + 3, 3),
    		new HashCodeKey("white-" + 14832643, 14832643),
    		new HashCodeKey("white-" + 35, 35),
    		new HashCodeKey("white-" + 7531555, 7531555),
    		new HashCodeKey("white-" + 67, 67),
    		new HashCodeKey("white-" + 663651, 663651),
    		new HashCodeKey("black-" + 32684131, 32684131),
    		new HashCodeKey("black-" + 131, 131),
    		new HashCodeKey("white-" + 131, 131),
    		new HashCodeKey("white-" + 195, 195),
    		new HashCodeKey("white-" + 26858691, 26858691),
    		new HashCodeKey("white-" + 227, 227),
    		new HashCodeKey("white-" + 551139, 551139),
    		new HashCodeKey("white-" + 19826979, 19826979),
    		new HashCodeKey("white-" + 11555, 11555),
    		new HashCodeKey("black-" + 2929955, 2929955),
    		new HashCodeKey("black-" + 323, 323),
    		new HashCodeKey("black-" + 355, 355),
    		new HashCodeKey("white-" + 16739, 16739),
    		new HashCodeKey("white-" + 387, 387),
    		new HashCodeKey("black-" + 21891, 21891),
    		new HashCodeKey("white-" + 2467, 2467),
    		new HashCodeKey("white-" + 451, 451),
    		new HashCodeKey("black-" + 16867, 16867),
    		new HashCodeKey("white-" + 418307, 418307),
    		new HashCodeKey("black-" + 697891, 697891),
    		new HashCodeKey("white-" + 611, 611),
    		new HashCodeKey("white-" + 1656419, 1656419),
    		new HashCodeKey("black-" + 4739, 4739),
    		new HashCodeKey("black-" + 14201507, 14201507),
    		new HashCodeKey("black-" + 25251, 25251),
    		new HashCodeKey("black-" + 12995, 12995),
    		new HashCodeKey("white-" + 25283, 25283),
    		new HashCodeKey("white-" + 13527747, 13527747),
    		new HashCodeKey("black-" + 28702403, 28702403),
    		new HashCodeKey("black-" + 15171, 15171),
    		new HashCodeKey("white-" + 9128771, 9128771),
    		new HashCodeKey("black-" + 759683, 759683),
    		new HashCodeKey("white-" + 23028611, 23028611),
    		new HashCodeKey("white-" + 7075, 7075),
    		new HashCodeKey("black-" + 892835, 892835),
    		new HashCodeKey("black-" + 963, 963),
    		new HashCodeKey("white-" + 8163, 8163),
    		};
    		
    		INode leftNode = BitmapIndexedNode.EMPTY;
    		final Box lbox = new Box(null);
    		for (HashCodeKey k: left) leftNode.assoc(0, k.hashCode(), k, "value", lbox);
    		
    		INode rightNode = BitmapIndexedNode.EMPTY;
    		final Box rbox = new Box(null);
    		for (HashCodeKey k: right) rightNode.assoc(0, k.hashCode(), k, "value", rbox);
    		
    		INode expectedNode = leftNode;
    		final Counts expectedCounts = new Counts();
            // TODO: iterate over right hand BIN, adding associations to left hand BIN...
            for (ISeq seq = rightNode.nodeSeq(); seq != null; seq = seq.next()) {
            	final MapEntry entry = (MapEntry) seq.first();
            	expectedNode = TestUtils.assoc(shift, expectedNode, entry.key(), entry.val(), expectedCounts);
            }
            
    		final Counts actualCounts = new Counts();
            final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

            assertNodeEquals(expectedNode, actualNode);
    }

}
