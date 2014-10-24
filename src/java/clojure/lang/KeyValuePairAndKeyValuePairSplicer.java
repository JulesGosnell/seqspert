package clojure.lang;

import java.util.concurrent.atomic.AtomicReference;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

class KeyValuePairAndKeyValuePairSplicer implements Splicer {

    public INode splice(int shift, Counts counts,
                        Object leftKey, Object leftValue,
                        Object rightKey, Object rightValue) {
        
        // TODO: expensive - can we pass this down ?
        final int leftHash = BitmapIndexedNodeUtils.hash(leftKey);
        final int rightHash = BitmapIndexedNodeUtils.hash(rightKey);
        // TODO: might be more efficient to check for reference equality first...
        if (leftHash == rightHash) {
            if (Util.equiv(leftKey, rightKey)) {
                // duplication
                counts.sameKey++;
                //counts.sameKeyAndValue += Util.equiv(leftValue, rightValue) ? 1 : 0;
                return null;
            } else {
                // collision
                return HashCollisionNodeUtils.create(leftHash, leftKey, leftValue, rightKey, rightValue);
            }
        } else{
            // no collision
            return create(shift, leftKey, leftValue, rightHash, rightKey, rightValue);
        }
    }

    // TODO refactor...
    // N.B. - this does NOT handle duplicate keys !!
    static INode create(int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
        final AtomicReference<Thread> edit = new AtomicReference<Thread>();
        int key1hash = BitmapIndexedNodeUtils.hash(key1);
        int p1 = PersistentHashMap.mask(key1hash, shift);
        int p2 = PersistentHashMap.mask(key2hash, shift);
        int bit1 = 1 << p1;
        int bit2 = 1 << p2;
        int bitmap = bit1 | bit2;
        return new BitmapIndexedNode(edit,
                                     bitmap,
                                     (bit1 == bit2) ?
                                     new Object[]{null,
                                                  (key1hash == key2hash) ?
                                                  new HashCollisionNode(null, key1hash, 2, new Object[] {key1, val1, key2, val2}) :
                                                  create(shift + 5, key1, val1, key2hash, key2, val2), null, null, null, null, null, null} :
                                     (p1 <= p2) ?
                                     new Object[]{key1, val1, key2, val2, null, null, null, null} :
                                     new Object[]{key2, val2, key1, val1, null, null, null, null});
    }
        
}
