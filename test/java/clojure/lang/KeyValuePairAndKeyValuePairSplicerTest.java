package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndKeyValuePairSplicerTest implements SplicerTestInterface {

    final Splicer splicer = new KeyValuePairAndKeyValuePairSplicer();
    final int shift = 0;

    public void test(Object leftKey, Object leftValue,
                     Object rightKey, Object rightValue, boolean same) {

        final Counts expectedCounts = new Counts();
        final INode expectedNode = 
            TestUtils.assoc(shift, BitmapIndexedNode.EMPTY,
                            leftKey, leftValue, rightKey, rightValue, expectedCounts);
                            
                            
            
        final Counts actualCounts = new Counts();
        final INode actualNode =
            splicer.splice(shift, actualCounts, leftKey, leftValue, rightKey, rightValue);
        
        assertEquals(expectedCounts, actualCounts);
        if (same) {
        	assertNull(actualNode);
        } else {
            assertNodeEquals(expectedNode, actualNode);
        }
    }
    
    @Test
    @Override
    public void testDifferent() {
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key2", 2), "value2", false);
        test(new HashCodeKey("key1", (1 << 5) | 1), "value1", new HashCodeKey("key2", 1), "value2", false);
    }

    @Ignore // TODO: this returns an HCN - expected is a BIN containing an HCN - needs some thought...
    @Test
    @Override
    public void testSameKeyHashCode() {
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key2", 1), "value2", false);
    }

    @Test
    @Override
    public void testSameKey() {
        test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key1", 1), "value2", true);
    }

    @Test
    @Override
    public void testSameKeyAndValue() {
    	test(new HashCodeKey("key1", 1), "value1", new HashCodeKey("key1", 1), "value1", true);
    }

}
