package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndHashCollisionNodeSplicer();

    public void test(int leftStart, int leftEnd,
                     int rightHash,
                     Object rightKey0, Object rightValue0,
                     Object rightKey1, Object rightValue1,
                     boolean same) {

        final INode empty = BitmapIndexedNode.EMPTY;

        final INode leftNode = TestUtils.assocN(shift, empty, leftStart, leftEnd, new Counts());

        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assoc(shift,
                            TestUtils.assoc(shift, leftNode, rightKey0, rightValue0, expectedCounts),
                            rightKey1, rightValue1, expectedCounts);

        final INode rightNode = HashCollisionNodeUtils.create(rightHash, rightKey0, rightValue0, rightKey1, rightValue1);
        
        final Counts actualCounts = new Counts();
        final INode actualNode =
            splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);
        
        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) TestUtils.assertSame(leftNode, expectedNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        final int rightHash = 1;
        test(2, 30,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("collision1", rightHash), "collision1",
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        // HCN is getting buried in one too many BINs   
        final int rightHash = 1;
        test(1, 30,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("collision1", rightHash), "collision1",
             false);
    }

    @Override
    @Test
    public void testSameKey() {
        final int rightHash = 1;
        test(1, 30,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("key1", rightHash), "collision1",
             false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        final int rightHash = 1;
        test(1, 30,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("key1", rightHash), "value1",
             false);
        // to test this we need an HCN in the same place on the lhs
    }
}
