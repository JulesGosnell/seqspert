package clojure.lang;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static clojure.lang.TestUtils.assertNodeEquals;

import org.junit.Test;
import clojure.lang.PersistentHashMap.INode;

public class SeqspertTest {

    @Test
    public void testConstructor() {
        new Seqspert();
    }

    @Test
    public void testCreatePersistentVector() {
        Seqspert.createPersistentVector(0, 0, null, null);
    }

    @Test
    public void testCreatePersistentHashMap() {
        Seqspert.createPersistentHashMap(0, null);
    }

    @Test
    public void testAssoc() {
        Seqspert.assoc(BitmapIndexedNodeUtils.EMPTY, 0, 1, "key", "value", new Box(null));
    }

    @Test
    public void testSpliceHashMaps() {
        Seqspert.spliceHashMaps(PersistentHashMap.EMPTY, PersistentHashMap.EMPTY);
        Seqspert.spliceHashMaps(PersistentHashMap.create("key", "value"), PersistentHashMap.EMPTY);
        Seqspert.spliceHashMaps(PersistentHashMap.create("key", "value"), PersistentHashMap.create("key", "value"));
    }

    @Test
    public void testSpliceHashSets() {
        Seqspert.spliceHashSets(PersistentHashSet.EMPTY, PersistentHashSet.EMPTY);
        Seqspert.spliceHashSets(PersistentHashSet.create("value"), PersistentHashSet.EMPTY);
        Seqspert.spliceHashSets(PersistentHashSet.create("value"), PersistentHashSet.create("value"));
    }

    public static INode assocN(int shift, INode node, int start, int end, Counts counts) {
	for (int i = start; i < end; i++)
	    node = TestUtils.assoc(shift, node , "key" + i, "value" + i, counts);
	return node;
    }
    
    public INode createN(int shift, int start, int end) {
        return assocN(shift, BitmapIndexedNodeUtils.EMPTY, start, end, new Counts());
    }

    public void test(int shift, int leftStart, int leftEnd, int rightStart, int rightEnd) {
        final INode leftNode = createN(shift, leftStart, leftEnd);
        final INode rightNode = createN(shift, rightStart, rightEnd);
        
        final Counts expectedCounts = new Counts();
        final INode expectedNode = assocN(shift, leftNode, 551, 935, expectedCounts);
        
        final Counts actualCounts = new Counts();
        final INode actualNode = Seqspert.splice(shift, actualCounts, false, 0, null, leftNode, false, 0, null, rightNode);
        
        assertEquals(expectedCounts, actualCounts);
        assertNodeEquals(expectedNode, actualNode);
    }
    
    @Test
    public void testUnknown() {
        test(0, 551, 552, 551, 935);
        //test(0, 0, 3, 110000, 130000);
    }
}
