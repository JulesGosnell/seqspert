package clojure.lang;

import static clojure.lang.ArrayNodeUtils.promote;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.INode;

import static org.junit.Assert.assertEquals;

// TODO: rename
class TestArrayNodeSplicer implements Splicer {

    public final Splicer splicer;

    public TestArrayNodeSplicer(Splicer splicer) {
        this.splicer = splicer;
    }

    public INode splice(final int shift, Counts counts,
                        boolean leftHaveHash, int leftHash, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        final Counts expectedCounts = new Counts();

        final IFn assocFunction = new AFn() {
                public Object invoke(Object result, Object key, Object value) {
                    final int hash = BitmapIndexedNodeUtils.hash(key);
                    final Box box = new Box(null);
                    final INode node = ((INode)result).assoc(shift, hash, key, value, box);
                    expectedCounts.sameKey += (box.val == box) ? 0 : 1;
                    return node;
                }};
            
        final INode expectedNode = (INode)
            promote(shift, rightKey, rightValue)
            .kvreduce(assocFunction,
                      promote(shift, leftKey, leftValue));
        
        final Counts actualCounts = new Counts();
        final INode actualNode = 
            splicer.splice(shift, counts,
                           leftHaveHash, leftHash, leftKey, leftValue,
                           rightHaveHash, rightHash, rightKey, rightValue);
        
        assertEquals(expectedCounts, actualCounts);
        TestUtils.assertNodeEquals(expectedNode, actualNode);

        counts.sameKey += actualCounts.sameKey;
        return actualNode;
    }
}
