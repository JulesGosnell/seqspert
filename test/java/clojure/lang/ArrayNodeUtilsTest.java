package clojure.lang;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
