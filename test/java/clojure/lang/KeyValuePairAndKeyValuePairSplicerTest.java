package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndKeyValuePairSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new KeyValuePairAndKeyValuePairSplicer();
    final int shift = 0;

    public void test(Object leftKey, Object leftValue,
                     Object rightKey, Object rightValue,
                     boolean same) {

        final Counts expectedCounts = new Counts();
        final INode expectedNode = 
            TestUtils.assoc(shift,
                            TestUtils.assoc(shift, BitmapIndexedNode.EMPTY,
                                            leftKey, leftValue, expectedCounts),
                            rightKey, rightValue, expectedCounts);
                            
                            
            
        final Counts actualCounts = new Counts();
        final INode actualNode = splicer.splice(shift, actualCounts, leftKey, leftValue,
                                                NodeUtils.nodeHash(rightKey), rightKey, rightValue);
        
        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
        if (same) assertSame(expectedNode, actualNode);
    }
    
    @Test
    @Override
    public void testDifferent() {
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key2", 2), "value2", false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key2", 1), "value2", false);
    }

    @Ignore
    @Test
    @Override
    public void testSameKey() {
        // TODO: returns null...
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key1", 1), "value2", false);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
        // TODO: not sure that this makes sense...
        //test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key1", 1), "value1", true);
    }

}
