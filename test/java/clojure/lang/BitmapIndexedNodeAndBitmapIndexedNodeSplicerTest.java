package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class BitmapIndexedNodeAndBitmapIndexedNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new BitmapIndexedNodeAndBitmapIndexedNodeSplicer();

    public void test(Object leftKey, Object leftValue, Object rightKey, Object rightValue, boolean same) {

        final int leftHash = NodeUtils.hash(leftKey);
        final int rightHash = NodeUtils.hash(rightKey);

        final INode leftNode =  BitmapIndexedNode.EMPTY
            .assoc(shift, leftHash, leftKey, leftValue, new Box(null));

        final Box addedLeaf = new Box(null);
        final INode expected = leftNode.
            assoc(shift, rightHash, rightKey, rightValue, addedLeaf);
        final int expectedSameKey = (addedLeaf.val == addedLeaf) ? 0 : 1;
        // TODO
        //final int expectedSameKeyAndValue = (expectedSameKey == 1 && Util.equiv(leftValue, rightValue))? 1:0;

        final INode rightNode =  BitmapIndexedNode.EMPTY
            .assoc(shift, rightHash, rightKey, rightValue, new Box(null));

        final Counts counts = new Counts(0, 0);
        final INode actual = splicer.splice(shift, counts, null, leftNode, rightHash, null, rightNode);

        assertEquals(expectedSameKey, counts.sameKey);
        //assertEquals(expectedSameKeyAndValue, counts.sameKeyAndValue);
        assertNodeEquals(expected, actual);
        if (same) assertSame(expected, actual); 
    }

    @Override
    @Test
    public void testDifferent() {
        test(new HashCodeKey("leftKey", 2), "leftValue",
             new HashCodeKey("rightKey", 4), "rightValue",
             false);
    }

    @Override
    @Test
    public void testSameKeyHashCode() {
        final int hashCode = 3;
        test(new HashCodeKey("leftKey", hashCode), "leftValue",
             new HashCodeKey("rightKey", hashCode), "rightValue",
             false);
    }

    @Override
    @Test
    public void testSameKey() {
        final int hashCode = 3;
        test(new HashCodeKey("key", hashCode), "leftValue",
             new HashCodeKey("key", hashCode), "rightValue",
             false);
    }

    @Test
    public void testSameKeyAndValue() {
        final int hashCode = 3;
        test(new HashCodeKey("key", hashCode), "value",
             new HashCodeKey("key", hashCode), "value",
             false);
    }
}
