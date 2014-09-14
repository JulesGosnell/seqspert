package clojure.lang;

import static clojure.lang.PersistentHashMap.hash;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndHashCollisionNodeSplicer extends AbstractSplicer {
        public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
            final BitmapIndexedNode l = (BitmapIndexedNode) leftValue;
            final HashCollisionNode r = (HashCollisionNode) rightValue;
            int bit = BitmapIndexedNodeUtils.bitpos(r.hash, shift);
            int idx = l.index(bit);
            int kIdx = 2 * idx;
            int vIdx = kIdx + 1;
            if((l.bitmap & bit) == 0) {
                // TODO: BIN or AN ?
                // TODO: extract into BIN-insert()
                // nothing in same slot on left hand side - clone existing node and add right hand side...
                Object[] array = new Object[l.array.length + 2];
                System.arraycopy(l.array, 0, array, 0, kIdx);
                array[vIdx] = r;
                System.arraycopy(l.array, kIdx, array, kIdx + 2, l.array.length - kIdx);
                int bitmap = l.bitmap | bit;
                return new BitmapIndexedNode(null, bitmap, array);
            } else {
                // left hand side already occupied...
                final Object lk = l.array[kIdx];
                final Object lVal = l.array[vIdx];
                final int lHash = hash(lk);
                if (lVal instanceof INode) {
                    // TODO: UNTESTED
                    throw new UnsupportedOperationException("NYI");
                    // could try this:
//                    return assoc(((INode)lVal), shift + 5, r.hash, null, rNode, duplications);
                } else if (lHash == r.hash) {
                    // collision (maybe duplication)
                    // replace lhs with an HCN, and tip rhs into it, checking for duplication...
                    final HashCollisionNode node = (HashCollisionNode)NodeUtils.splice(shift + 5, duplications, lk, lVal, rightHash, null, r);
                    final Object[] array = l.array.clone();
                    array[vIdx] = node;
                    return new BitmapIndexedNode(null, l.bitmap, array);
                } else {
                    // l & r share slot, but not hashcode - we need to drop down another level and merge the two nodes...
                    // we are only dealing with vals on the right, not nodes...
                    // REWRITE
                    final Object[] array = l.array.clone();
                    array[vIdx] = NodeUtils.assoc(((INode) rightValue), shift + 5, lHash, l.array[kIdx], l.array[vIdx], duplications); // TODO - why the cast ?
                    return new BitmapIndexedNode(new AtomicReference<Thread>(), l.bitmap, array);
                }
            }
        }
    }