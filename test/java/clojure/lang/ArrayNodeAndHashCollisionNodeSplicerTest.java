package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndHashCollisionNodeSplicer();

    public void test(int leftStart, int leftEnd,
                     Object leftKey, Object leftValue,
                     int rightHash,
                     Object rightKey0, Object rightValue0,
                     Object rightKey1, Object rightValue1,
                     boolean same) {
        final INode leftNode = TestUtils.create(shift, leftStart, leftEnd, leftKey, leftValue);

        final INode rightNode =
            HashCollisionNodeUtils.create(rightHash, rightKey0, rightValue0, rightKey1, rightValue1);
        
        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assoc(shift, leftNode, rightKey0, rightValue0, rightKey1, rightValue1, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);
        
        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(leftNode, actualNode); // expectedNode not as expected !
    }

    @Override
    @Test
    public void testDifferent() {
        final int rightHash = 1;
        test(2, 31, null, null,
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
        test(1, 31, null, null,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("collision1", rightHash), "collision1",
             false);
    }

    @Override
    @Test
    public void testSameKey() {
        final int rightHash = 1;
        test(1, 31, null, null,
             rightHash, 
             new HashCodeKey("collision0", rightHash), "collision0",
             new HashCodeKey("key1", rightHash), "collision1",
             false);
    }

    @Override
    @Test
    public void testSameKeyAndValue() {
        test(1, 31,
             new HashCodeKey("key1.1", 1), "value1.1",
             1, 
             new HashCodeKey("key1",   1), "value1",
             new HashCodeKey("key1.1", 1), "value1.1",
             true);
    }

}
