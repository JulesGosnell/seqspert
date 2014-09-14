package clojure.lang;

import clojure.lang.PersistentHashMap.INode;

/**
 * Seqspert needs access to a few Clojure internals. For the moment, this class seems like a sensible place to put them.
 * 
 * @author jules
 *
 */
public class Seqspert {

    // Vector

	static public PersistentVector createPersistentVector(int cnt, int shift, PersistentVector.Node root, Object[] tail) {
		return new PersistentVector(cnt, shift, root, tail);
	}

	// HashMap

    

   

    

    // this could be prettier and maybe faster if PersistentHashMap
    // was refactored but it is not part of seqspert :-(

    
	
    

    

    

    // TODO: consider that BINs may morph into ANs as they increase in size...

    

    

    // TODO - create new AtomicRef or not ?

    
	public static PersistentHashMap spliceHashMaps(PersistentHashMap lMap, PersistentHashMap rMap) {
		// check null config the same
		final INode lRoot = lMap.root;
		final INode rRoot = rMap.root;
		if (lRoot == null)
			return rMap;
		else if (rRoot == null)
			return lMap;

        final Duplications duplications = new Duplications(0);
        final PersistentHashMap.INode root = NodeUtils.splice(0, duplications, null, lRoot, 0, null, rRoot);
		final int count = lMap.count + rMap.count - duplications.duplications;
		return new PersistentHashMap(count, root, lMap.hasNull, lMap.nullValue);
	}
	
	// HashSet
	
	public static PersistentHashSet spliceHashSets(PersistentHashSet lSet, PersistentHashSet rSet) {
		final PersistentHashMap meta = PersistentHashMap.EMPTY; // TODO - consider merging METAs
		final IPersistentMap impl = spliceHashMaps((PersistentHashMap)lSet.impl, (PersistentHashMap)rSet.impl);
		return new PersistentHashSet(meta, impl);
	}


}
