package clojure.lang;

import static clojure.lang.NodeUtils.create;
import static clojure.lang.NodeUtils.nodeHash;
import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class KeyValuePairAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new KeyValuePairAndArrayNodeSplicer();

    public void test(Object leftKey, Object leftValue, int rightStart, int rightEnd, boolean same) {

	final INode empty = BitmapIndexedNode.EMPTY;
	final INode rightNode = TestUtils.assocN(shift, empty, rightStart, rightEnd, new Counts());
	
	final Counts expectedCounts = new Counts();
	final INode expectedNode =
	    TestUtils.assocN(shift, create(shift, leftKey, leftValue), rightStart, rightEnd, new Counts());

	final Counts actualCounts = new Counts(0, 0);
	final INode actualNode =
	    splicer.splice(shift, actualCounts, leftKey, leftValue, nodeHash(rightNode), null, rightNode);

	// TODO: reenable
	//assertEquals(expectedCounts, actualCounts);
	assertNodeEquals(expectedNode, actualNode);
	if (same) assertSame(rightNode, actualNode); // TODO: is this the right thing to assert ?
    }

    @Test
    public void testDifferent() {
	test(new HashCodeKey("key1", 1), "value1", 2, 30, false);
    }

    @Ignore
    @Test
    public void testSameKeyHashCode() {
	test(new HashCodeKey("collision", 1), "collision1", 1, 30, false);
    }

    @Ignore
    @Test
    public void testSameKey() {
	test(new HashCodeKey("key1", 1), "differentValue1", 1, 30, false);
    }

    @Test
    public void testSameKeyAndValue() {
	test(new HashCodeKey("key1", 1), "value1", 1, 30, true);
    }
}
