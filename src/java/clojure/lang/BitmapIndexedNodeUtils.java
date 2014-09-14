package clojure.lang;

public class BitmapIndexedNodeUtils {
	
	static int bitpos(int hash, int shift){
	    return 1 << PersistentHashMap.mask(hash, shift);
	}

}
