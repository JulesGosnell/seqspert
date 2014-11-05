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

    @Test
    public void testmakeArrayNode() {
        assertTrue(ArrayNodeUtils.makeArrayNode(0, null) instanceof PersistentHashMap.ArrayNode);
    }

}
