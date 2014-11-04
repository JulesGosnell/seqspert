package clojure.lang;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import clojure.lang.TestUtils.Hasher;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeUtilsTest  {

    @Test
    public void testConstructor() {
        new ArrayNodeUtils();
    }

    // TODO: this should be covered by one of the Splicer tests
    @Test
    public void testPromoteNode() {
        final int shift = 0;
        final Object key = null;
        final Object value = BitmapIndexedNode.EMPTY;
        assertSame(value, ArrayNodeUtils.promote(shift, key, value));
    }

    @Test
    public void testmakeArrayNode() {
        ArrayNodeUtils.makeArrayNode(0, null);
    }

}
