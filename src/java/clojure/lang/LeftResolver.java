package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

class LeftResolver implements Resolver {
    public static IFn resolveLeft  = new AFn() {
        @Override public Object invoke(Object key, Object leftValue, Object rightValue) {
            return (Util.equiv(leftValue, rightValue)) ? leftValue : rightValue;
        }};
        
        public IFn getResolver() { return resolveLeft; }
        
        public INode resolveNodes(int leftDifferences, INode left, int rightDifferences, INode right) {
        	return leftDifferences == 0 ? left : rightDifferences == 0 ? right : null;
        }
        
        public Object[] resolveArrays(int leftDifferences, Object[] left, int rightDifferences, Object[] right) {
        	return leftDifferences == 0 ? left : rightDifferences == 0 ? right : null;
        }
}