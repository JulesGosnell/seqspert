package clojure.lang;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import clojure.lang.PersistentHashMap.INode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;

public class NodeUtilsTest  {

    @Test
    public void testCtor() {
        assertNotNull(new NodeUtils());
    }

    @Test
    public void testPromoteKeyValuePair() {
        final int shift = 0;
        final Object key = "a";
        final Object value = "1";
        assertTrue(NodeUtils.promote(shift, key, value) instanceof INode);
    }

    @Test
    public void testPromoteNode() {
        final int shift = 0;
        final Object key = null;
        final Object value = BitmapIndexedNode.EMPTY;
        assertSame(value, NodeUtils.promote(shift, key, value));
    }
}
