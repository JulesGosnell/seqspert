package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new BitmapIndexedNodeAndHashCollisionNodeSplicer();
    final int shift = 0;

    public void test(Object leftKey, Object leftValue,
                     int rightHash,
                     Object rightKey0, Object rightValue0, Object rightKey1, Object rightValue1,
                     boolean sameLeft, boolean sameRight) {

        final INode leftNode = NodeUtils.create(shift, leftKey, leftValue);
        final INode rightNode = HashCollisionNodeUtils.create(rightHash,
                                                              rightKey0, rightValue0,
                                                              rightKey1, rightValue1);
        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assoc(shift, leftNode, rightKey0, rightValue0, rightKey1, rightValue1, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameLeft) TestUtils.assertSame(leftNode, expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }

    public void test(int leftStart, int leftEnd,
                     int rightHash,
                     Object rightKey0, Object rightValue0, Object rightKey1, Object rightValue1,
                     boolean sameLeft, boolean sameRight) {

        final INode empty = BitmapIndexedNode.EMPTY;
        final INode leftNode = TestUtils.assocN(shift, empty, leftStart, leftEnd, new Counts());
        final INode rightNode = HashCollisionNodeUtils.create(rightHash,
                                                              rightKey0, rightValue0,
                                                              rightKey1, rightValue1);
        final Counts expectedCounts = new Counts();
        final INode expectedNode =
            TestUtils.assoc(shift,
                            TestUtils.assoc(shift, leftNode, rightKey0, rightValue0, expectedCounts),
                            rightKey1, rightValue1,
                            expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (sameLeft) TestUtils.assertSame(leftNode, expectedNode, actualNode);
        if (sameRight) assertSame(rightNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key0", 0), "value0",
             1,
             new HashCodeKey("key1.0", 1), "value1.0",
             new HashCodeKey("key1.1", 1), "value1.1",
             false, false);
         test(3, 19,
              1,
              new HashCodeKey("key1.0", 1), "value1.0",
              new HashCodeKey("key1.1", 1), "value1.1",
              false, false);

    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1.0", 1), "value1.0",
             1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             false, false);
        test(1, 17,
             1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             false, false);
    }
        
    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key1.0", 1), "value1.0.0",
             1,
             new HashCodeKey("key1.0", 1), "value1.0.1",
             new HashCodeKey("key1.1", 1), "value1.1",
             false, false);
        test(1, 17,
             1,
             new HashCodeKey("key1",   1), "value1.0",
             new HashCodeKey("key1.1", 1), "value1.1",
             false, false);
    }


    @Override
    @Test
    public void testSameKeyAndValue() {
        // TODO: test left and right sameness
        test(new HashCodeKey("key1.0", 1), "value1.0",
             1,
             new HashCodeKey("key1.0", 1), "value1.0",
             new HashCodeKey("key1.1", 1), "value1.1",
             false, false);
        test(1, 17,
             1,
             new HashCodeKey("key1",   1), "value1",
             new HashCodeKey("key1.1", 1), "value1.1",
             false, false);
    }
    
}
