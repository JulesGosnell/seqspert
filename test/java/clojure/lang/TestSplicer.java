package clojure.lang;

import static org.junit.Assert.assertEquals;
import clojure.lang.PersistentHashMap.INode;

public class TestSplicer implements Splicer {

    public final Splicer splicer;

    public TestSplicer(Splicer splicer) {
        this.splicer = splicer;
    }

    public static Splicer savedSplicer = null;
    public static int savedShift = 0;
    public static INode left = null;
    public static INode right = null;
    public static INode expected = null;
    public static INode actual = null;

    //private String className(Object o) {return o == null ? "<null>" : o.getClass().getSimpleName();}

    @Override
    public INode splice(final int shift, Counts counts,
                        boolean leftHaveHash, int leftHash, Object leftKey, Object leftValue,
                        boolean rightHaveHash, int rightHash, Object rightKey, Object rightValue) {

        //System.out.println("SPLICE -> " + (shift / 5) + " : " + className(splicer) + "(" + className(leftKey) + ":" + className(leftValue) + ", " + className(rightKey) + ":" + className(rightValue) + ")");
        //System.out.flush();

        final Counts expectedCounts = new Counts();

        final IFn assocFunction = new AFn() {
                @Override
                public Object invoke(Object result, Object key, Object value) {
                    final int hash = BitmapIndexedNodeUtils.hash(key);
                    final Box box = new Box(null);
                    final INode node = ((INode)result).assoc(shift, hash, key, value, box);
                    expectedCounts.sameKey += (box.val == box) ? 0 : 1;
                    return node;
                }};
            
        final Counts actualCounts = new Counts();
        final INode actualNode = 
            splicer.splice(shift, actualCounts,
                           leftHaveHash, leftHash, leftKey, leftValue,
                           rightHaveHash, rightHash, rightKey, rightValue);

        //System.out.println("SPLICE <- " + (shift / 5) + " : " + className(splicer) + "(" + className(actualNode) + ")");
        //System.out.flush();

        // can only do this to node types
        if (leftKey == null && rightKey == null) {
            final INode leftNode = (INode) leftValue;
            final INode rightNode = (INode) rightValue;
            //System.out.println("SPLICE CHECK: " + leftNode.getClass().getSimpleName() + " / " + rightNode.getClass().getSimpleName());
            //System.out.flush();
            final INode expectedNode = (INode) rightNode.kvreduce(assocFunction, leftNode);


            savedSplicer = splicer;
            savedShift = shift;
            left = leftNode;
            right = rightNode;
            expected = expectedNode;
            actual = actualNode;


            assertEquals(expectedCounts, actualCounts);
            //TestUtils.assertNodeEquals(expectedNode, actualNode);
            //assertEquals(expectedNode, actualNode);
            System.out.flush();
            System.err.flush();
        }

        counts.sameKey += actualCounts.sameKey;
        return actualNode;
    }
}
