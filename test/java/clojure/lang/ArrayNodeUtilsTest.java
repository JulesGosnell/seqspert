package clojure.lang;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeUtilsTest  {

    @Test
    public void testPromoteKeyValuePair() {
        final int shift = 0;
        final Object key = "a";
        final Object value = "1";
        assertTrue(TestUtils.promote(shift, key, value) instanceof INode);
    }

    @Test
    public void testPromoteNode() {
        final int shift = 0;
        final Object key = null;
        final Object value = BitmapIndexedNode.EMPTY;
        assertSame(value, TestUtils.promote(shift, key, value));
    }
}
