package clojure.lang;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static clojure.lang.TestUtils.assertNodeEquals;

import org.junit.Test;
import org.junit.Ignore;
import clojure.lang.PersistentHashMap.INode;

public class SeqspertTest {

    @Test
    public void test() {
        assertTrue(true);
    }

    public static INode assocN(int shift, INode node, int start, int end, Counts counts) {
	for (int i = start; i < end; i++)
	    node = TestUtils.assoc(shift, node , "key" + i, "value" + i, counts);
	return node;
    }
    
    public INode createN(int shift, int start, int end) {
        return assocN(shift, BitmapIndexedNodeUtils.EMPTY, start, end, new Counts());
    }

    @Ignore
    @Test
    public void testUnknown() {
        final int shift = 0;
        final INode leftNode = createN(0, 551, 552);
        final INode rightNode = createN(0, 551, 935);
        
        final Counts expectedCounts = new Counts();
        final INode expectedNode = assocN(shift, leftNode, 551, 935, expectedCounts);

        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);

        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
    }
}
