package clojure.lang;

import org.junit.Test;

public class BitmapIndexedNodeUtilsTest {

    @Test
    public void testConstructor() {
        new BitmapIndexedNodeUtils();
    }

    @Test
    public void testMakeBitmapIndexedNode() {
        BitmapIndexedNodeUtils.makeBitmapIndexedNode(0, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testHash() {
        BitmapIndexedNodeUtils.hash(null);
    }
}
