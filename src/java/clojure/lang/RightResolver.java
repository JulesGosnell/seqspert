package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

class RightResolver implements Resolver {
    public static IFn resolveRight = new AFn() {
        @Override public Object invoke(Object key, Object leftValue, Object rightValue) {
            return rightValue; 
        }};
        
        public IFn getResolver() { return resolveRight; }
        
        public INode resolveNodes(int leftDifferences, INode left, int rightDifferences, INode right) {
        	return rightDifferences == 0 ? right : leftDifferences == 0 ? left : null;
        }
        
        public Object[] resolveArrays(int leftDifferences, Object[] left, int rightDifferences, Object[] right) {
        	return rightDifferences == 0 ? right : leftDifferences == 0 ? left : null;
        }
	
}