package clojure.lang;

import clojure.lang.PersistentHashMap.BitmapIndexedNode;

public class BitmapIndexedNodeUtils {
        
    static int bitpos(int hash, int shift){
        return 1 << PersistentHashMap.mask(hash, shift);
    }
        
    public static  BitmapIndexedNode create(int index, Object key, Object value) {
        return new BitmapIndexedNode(null, 1 << index, new Object[]{key, value});
    }
    
    public static  BitmapIndexedNode create(int index0, Object key0, Object value0, int index1, Object key1, Object value1) {
        return new BitmapIndexedNode(null,
                                     1 << index0 | 1 << index1,
                                     (index0 <= index1) ?
                                     new Object[]{key0, value0, key1, value1} :
                                     new Object[]{key1, value1, key0, value0});
    }
    
}

