package clojure.lang;

import static clojure.lang.NodeUtils.hash;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndBitmapIndexedNodeSplicer implements Splicer {

		public INode splice(int shift, Counts counts,
												Object leftKey, Object leftValue,
												int _, Object rightKey, Object rightValue) {

				final BitmapIndexedNode rightNode = (BitmapIndexedNode) rightValue;
				final int leftHash = hash(leftKey);
				final int bit = BitmapIndexedNodeUtils.bitpos(leftHash, shift);
				if((rightNode.bitmap & bit) == 0) {
						// rhs unoccupied
						// TODO - do not use assoc here - reference similar code
						// TODO: need a BIN insert fn.... how is that different from assoc ?
						// TODO: consider whether to return a BIN or an AN
						// TODO: inline logic and lose Box churn ...
						// TODO: this must be wrong - we are overlaying left over right ?
						final Box addedLeaf = new Box(null);
						final INode n = rightNode.assoc(shift, leftHash, leftKey, leftValue, addedLeaf);
						counts.sameKey += (addedLeaf.val == null ? 1 : 0);
						return n;
				} else {
						// rhs occupied...
						final int index = rightNode.index(bit) * 2;
						final Object[] rightArray = rightNode.array;
						final Object subKey = rightArray[index];
						final Object subValue = rightArray[index + 1];
						final int oldSameKey = counts.sameKey;
						final int oldSameKeyAndValue = counts.sameKeyAndValue;
						final INode spliced = NodeUtils.splice(shift + 5, counts,
																									 leftKey, leftValue,
																									 NodeUtils.nodeHash(subKey), subKey, subValue);
						if ((~bit & rightNode.bitmap) > 0) {
								// the BIN contains more than just this entry
								if (spliced == null) {
										System.out.println("AAARGH! - splice() returned null - other entries");
										// the LHS key and maybe value are the same as those in the RHS
										// if the value were different it would replace the one on the LHS
										// if the value were the same, then we do not need to change the one in the RHS
										// since the BIN contains more than this entry we must return it...
										return rightNode;
								} else {
										System.out.println("AAARGH! - splice() didn't return null - other entries");
										// we have successfully merged the LHS and RHS entry
										// we need to copy over the rest of the RHS and return a new BIN...
										return new BitmapIndexedNode(null,
																								 rightNode.bitmap | bit,
																								 NodeUtils.cloneAndInsert(rightArray,
																																					Integer.bitCount(rightNode.bitmap) * 2,
																																					index,
																																					null,
																																					spliced));
								}
						} else {
								// the BIN only contains this entry
								if (spliced == null) {
										System.out.println("AAARGH! - splice() returned null - only entry");
										// we must only return a single KVP which we cannot do, so we return null
										
										// return spliced;
								
										// TODO: I think that we should return spliced
										// here - but I have lots of tests expecting rightNode

										return rightNode;
								} else {
										System.out.println("AAARGH! - splice() didn't return null - only entry");
										// we only need to return this single spliced node
										return spliced;
								}
						}
				}
		}
}
