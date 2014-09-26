package clojure.lang;

import static clojure.lang.PersistentHashMap.hash;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class BitmapIndexedNodeAndBitmapIndexedNodeSplicer extends AbstractSplicer {
	public INode splice(int shift, Duplications duplications, Object leftKey, Object leftValue, int rightHash, Object rightKey, Object rightValue) {
        // TODO: BIN or AN ?
	    final BitmapIndexedNode l = (BitmapIndexedNode) leftValue;
	    final BitmapIndexedNode r = (BitmapIndexedNode) rightValue;

        int lBitmap = l.bitmap;
        int rBitmap = r.bitmap;
        Object[] lArray = l.array;
        Object[] rArray = r.array;

        int oBitmap = lBitmap | rBitmap;
        Object[] oArray = new Object[Integer.bitCount(oBitmap) * 2]; // nasty - but we need to know before we start the loop
        int lPosition = 0;
        int rPosition = 0;
        int oPosition = 0;
        for (int i = 0; i < 32; i++)
        {
            int mask = 1 << i;
            boolean lb = ((lBitmap & mask) != 0);
            boolean rb = ((rBitmap & mask) != 0);

            if (lb && !rb) {
                oArray[oPosition++] = lArray[lPosition++];
                oArray[oPosition++] = lArray[lPosition++];
            } else if (rb && !lb) {
                oArray[oPosition++] = rArray[rPosition++];
                oArray[oPosition++] = rArray[rPosition++];
            } else if  (lb && rb) {
                Object lk = lArray[lPosition++];
                Object lv = lArray[lPosition++];
                Object rk = rArray[rPosition++];
                Object rv = rArray[rPosition++];
                Object ok= null;
                Object ov = null;

                // splice two nodes...
                boolean lIsNode = lv instanceof INode;
                boolean rIsNode = rv instanceof INode;
                if (lIsNode) {
                    if (rIsNode) {
                        ov = NodeUtils.splice(shift + 5, duplications, null, (INode) lv, rightHash, null, (INode) rv);
                    } else {
			throw new RuntimeException("NYI");
                        //ov = NodeUtils.assoc(((INode) lv), shift + 5, hash(rk), rk, rv, duplications);
                    }
                } else {
                    if (rIsNode) {
                        // TODO: may cause unnecessary copying ? think...
			throw new RuntimeException("NYI");
                        //ov = NodeUtils.assoc(((INode) rv), shift + 5, hash(lk), lk, lv, duplications);
                    } else {
                        if (Util.equiv(lk, rk)) {
                            ok = lk;
                            ov = rv; // overwrite from right ?
                            duplications.duplications++;
                        } else {
                            // TODO - does not work anymore...
                            final int lkHash = hash(lk);
                            final int rkHash = hash(rk);
                            ov = (lkHash == rkHash) ?
                                    new HashCollisionNode(null, lkHash, 2, new Object[]{lk, lv, rk, rv}) :
                                    NodeUtils.create(shift + 5, lk, lv, rkHash, rk, rv);
                        }
                    }
                }

                oArray[oPosition++] = ok;
                oArray[oPosition++] = ov;
            }
        }

        return new PersistentHashMap.BitmapIndexedNode(new AtomicReference<Thread>(), oBitmap, oArray);
	}
}
