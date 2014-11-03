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
    public void testPromoteKeyValuePair() {
        final int shift = 0;
        final Object key = "a";
        final Object value = "1";
        assertTrue(ArrayNodeUtils.promote(shift, key, value) instanceof INode);
    }

    @Test
    public void testPromoteNode() {
        final int shift = 0;
        final Object key = null;
        final Object value = BitmapIndexedNode.EMPTY;
        assertSame(value, ArrayNodeUtils.promote(shift, key, value));
    }

    @Test
    public void testGetArrayNodePartition() {
        final int shift = 0;
        final int partition = 16;
        final Hasher hasher = new Hasher() {public int hash(int i) { return ((i + 1) << 5) | partition; }};
        final INode node = TestUtils.create(shift, hasher, 1, 19);
        assertEquals(partition, ArrayNodeUtils.getPartition(shift, null, node));
    }

    @Test
    public void testGetBitmapIndexedNodePartition() {
        final int shift = 0;
        final int partition = 8;
        final Hasher hasher = new Hasher() {public int hash(int i) { return ((i + 1) << 5) | partition; }};
        final INode node = TestUtils.create(shift, hasher, 0, 16);
        assertEquals(partition, ArrayNodeUtils.getPartition(shift, null, node));
    }

    @Test
    public void testGetHashCollisionNodePartition() {
        final int shift = 0;
        final int partition = 8;
        final Hasher hasher = new Hasher() {public int hash(int i) { return (1 << 5) | partition; }};
        final INode node = TestUtils.create(shift, hasher, 0, 3);
        assertEquals(partition, ArrayNodeUtils.getPartition(shift, null, node));
    }

    @Test
    public void testmakeArrayNode() {
        ArrayNodeUtils.makeArrayNode(0, null);
    }

}
