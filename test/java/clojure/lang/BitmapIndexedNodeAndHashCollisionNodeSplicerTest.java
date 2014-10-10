package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndHashCollisionNodeSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new BitmapIndexedNodeAndHashCollisionNodeSplicer();
    final int shift = 0;

    public void test(Object leftKey, Object leftValue,
                     int rightHash,
                     Object rightKey0, Object rightValue0, Object rightKey1, Object rightValue1,
                     boolean same) {

        final INode leftNode = NodeUtils.create(shift, leftKey, leftValue);
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
        final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(expectedNode, actualNode);
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("key0", 0), "value0",
             1,
             new HashCodeKey("key1.0", 1), "value1.0",
             new HashCodeKey("key1.1", 1), "value1.1",
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1.0", 1), "value1.0",
             1,
             new HashCodeKey("key1.1", 1), "value1.1",
             new HashCodeKey("key1.2", 1), "value1.2",
             false);
    }
        
    @Ignore
    @Override
    @Test
    public void testSameKey() {
        test(new HashCodeKey("key1.0", 1), "value1.0.0",
             1,
             new HashCodeKey("key1.0", 1), "value1.0.1",
             new HashCodeKey("key1.1", 1), "value1.1",
             false);
    }


    @Ignore
    @Override
    @Test
    public void testSameKeyAndValue() {
        test(new HashCodeKey("key1.0", 1), "value1.0",
             1,
             new HashCodeKey("key1.0", 1), "value1.0",
             new HashCodeKey("key1.1", 1), "value1.1",
             false);
    }
    
}
