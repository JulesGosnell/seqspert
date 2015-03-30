package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

interface Resolver {

	IFn getResolver();
	INode resolveNodes(int leftDifferences, INode left, int rightDifferences, INode right);
	Object[] resolveArrays(int leftDifferences, Object[] left, int rightDifferences, Object[] right);

	
}