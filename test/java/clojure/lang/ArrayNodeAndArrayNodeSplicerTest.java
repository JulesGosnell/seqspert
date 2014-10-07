package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndArrayNodeSplicerTest implements SplicerTestInterface {

    final int shift = 0;
    final Splicer splicer = new ArrayNodeAndArrayNodeSplicer();

    public void test(int leftStart, int leftEnd, int rightStart, int rightEnd, boolean same) {
	final INode empty = BitmapIndexedNode.EMPTY;

	final INode leftNode = TestUtils.assocN(shift, empty, leftStart, leftEnd, new Counts());
	final INode rightNode = TestUtils.assocN(shift, empty, rightStart, rightEnd, new Counts());

	final Counts expectedCounts = new Counts();
	final INode expectedNode = TestUtils.assocN(shift, leftNode, rightStart, rightEnd, expectedCounts);

	final Counts actualCounts = new Counts();
	final INode actualNode = splicer.splice(shift, actualCounts, null, leftNode, 0, null, rightNode);

	assertEquals(expectedCounts.sameKey, actualCounts.sameKey);
	assertNodeEquals(actualNode, expectedNode);
	if (same) assertSame(actualNode, expectedNode);
    }

    @Test
    public void testDifferent() {
	test(0, 17, 17 << 5, 31 << 5, false);	// overlap - but keys and hashcodes different
    }

    @Test
    public void testSameKeyHashCode() {
	// TODO
	//test(0, 17, 17, 31, false);	// overlap - hashcodes same but keys not equal
    }

    @Test
    public void testSameKey() {
	test(0, 18, 13, 30, false);	// overlap a few entries and leave empty cells at end of array
    }

    @Test
    public void testSameKeyAndValue() {
	test(0, 31, 0, 31, true);	// overlap all entries
    }
}
