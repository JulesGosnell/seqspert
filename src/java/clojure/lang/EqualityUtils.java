package clojure.lang;

import clojure.lang.PersistentHashMap.ArrayNode;
import clojure.lang.PersistentHashMap.BitmapIndexedNode;
import clojure.lang.PersistentHashMap.HashCollisionNode;
import clojure.lang.PersistentHashMap.INode;

public class EqualityUtils {


    public static boolean checkArray(Object[] l, Object[] r) {
        int length = Math.max(l.length, r.length);
        for (int i = 0; i < length; i++) {
            Object lv = i < l.length ? l[i] : null;
            Object rv = i < r.length ? r[i] : null;
            if (!check(lv, rv)) return false;
        }
        return true;
    }

    public static boolean checkNodeArray(INode[] l, INode[] r) {
        int length = Math.max(l.length, r.length);
        for (int i = 0; i < length; i++) {
            INode lv = i < l.length ? l[i] : null;
            INode rv = i < r.length ? r[i] : null;
            if (!check(lv, rv)) return false;
        }
        return true;
    }

    public static boolean check(Object l, Object r) {
        if (l == r) return true;
        if (l instanceof INode) {
            if (l instanceof BitmapIndexedNode) {
                BitmapIndexedNode ln = (BitmapIndexedNode) l;
                BitmapIndexedNode rn = (BitmapIndexedNode) r;
                return ln.bitmap == rn.bitmap && checkArray(ln.array, rn.array);
            } else if (l instanceof HashCollisionNode) {
                HashCollisionNode ln = (HashCollisionNode) l;
                HashCollisionNode rn = (HashCollisionNode) r;
                return ln.hash == rn.hash && ln.count == rn.count && checkArray(ln.array, rn.array);
            } else {
                ArrayNode ln = (ArrayNode) l;
                ArrayNode rn = (ArrayNode) r;
                return ln.count == rn.count && checkNodeArray(ln.array, rn.array);
            }
        } else {
            boolean tmp = l.equals(r);
            if (!tmp) throw new RuntimeException("" + l + " != " + r);
            return tmp;
        }
    }

}
