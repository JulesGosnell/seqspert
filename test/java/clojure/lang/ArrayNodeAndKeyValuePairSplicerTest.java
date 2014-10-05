package clojure.lang;

import static clojure.lang.TestUtils.assertNodeEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

public class ArrayNodeAndKeyValuePairSplicerTest implements SplicerTestInterface {
	
	final int shift = 0;
	final Splicer splicer = new ArrayNodeAndKeyValuePairSplicer();

	public void test(int leftStart, int leftEnd, Object rightKey, Object rightValue, boolean same) {

		INode leftNode = BitmapIndexedNode.EMPTY;
		for (int i = leftStart; i < leftEnd + 1; i++) {
			final int hashCode = i;
			final Object key = new HashCodeKey("key" + i, hashCode);
			final Object value = i;
			leftNode = leftNode.assoc(shift, hashCode, key, value, new Box(null));
		}
		INode expected = leftNode;
		int expectedCounts = 0;
		final Box addedLeaf = new Box(null);
		expected = expected.assoc(shift, NodeUtils.hash(rightKey) , rightKey, rightValue, addedLeaf);
		expectedCounts += (addedLeaf.val == addedLeaf) ? 0 : 1;

		final Counts actualCounts = new Counts(0, 0);
		final INode actual = splicer.splice(shift, actualCounts, null, leftNode, NodeUtils.hash(rightKey), rightKey, rightValue);

		assertEquals(expectedCounts, actualCounts.sameKey);
		assertNodeEquals(expected, actual);
	}

	@Test
	public void testDifferent() {
		test(2, 30, new HashCodeKey("key1", 1), 1, false);
	}

	@Ignore
	@Test
	public void testSameKeyHashCode() {
		test(1, 30, new HashCodeKey("collision", 1), 1, false);
	}

	@Ignore
	@Test
	public void testSameKey() {
		test(1, 30, new HashCodeKey("key1", 2), 1, false);
	}

	@Ignore
	@Test
	public void testSameKeyAndValue() {
		test(1, 30, new HashCodeKey("key1", 1), 1, true);
	}
}
