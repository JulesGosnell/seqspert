package clojure.lang;


public class HashCollisionNodeUtils {

	/*
	 * return a copy of oldArray of 'newLength' - content may be truncated
	 */
	public static Object[] clone(Object[] oldArray, int oldLength, int newLength) {
		final Object[] newArray = new Object[newLength];
		System.arraycopy(oldArray, 0, newArray, 0, oldLength);
		return newArray;
	}

	// trimming is used to keep unit test happy (uses
	// AssertArrayEquals) - could be dropped to make things faster...
	/*
	 * return an array, maybe 'oldArray', of 'newLength' and with the same contents as 'oldArray'
	 */
	public static Object[] trim(Object[] oldArray, int newLength) {
		return (oldArray.length == newLength) ? oldArray : clone(oldArray, newLength, newLength);
	}

	/*
	 * return a copy of 'oldArray' of 'newLength' with 'key' and 'value' appended at 'oldLength'
	 */
	public static Object[] append(Object[] oldArray, int oldLength, int newLength, Object key, Object value) {
		final Object[] newArray = clone(oldArray, oldLength, newLength);
		newArray[oldLength + 0] = key;
		newArray[oldLength + 1] = value;
		return newArray;
	}

	public static int keyIndex(Object[] array, int length, Object key) {
		for (int i = 0; i < length; i += 2) if (Util.equiv(array[i], key)) return i;
		return -1;
	}

	/*
	 * return an array equivalent to 'array' with the value at 'index'
	 * set to 'value'.
	 */
	public static Object[] maybeSet(Object[] array, int index, Object value, Duplications duplications) {
		duplications.duplications++;
		if (Util.equiv(array[index + 1], value)) {
			return array;
		} else {
			final Object[] newArray = array.clone();
			newArray[index + 1] = value;
			return newArray;
		}
	}

	/*
	 * return an array, the set of which's key:value pairs is
	 * equivalent to the union of 'array' and {'key':'value'}
	 */
	public static Object[] maybeAdd(Object[] array, int length, Object key, Object value, Duplications duplications) {
		final int i = keyIndex(array, length, key);
		return (i == -1) ?
				append(array, length, length + 2, key, value) :
					maybeSet(array, i, value, duplications);
	}


	/*
	 * return an array, the set of which's key:value pairs is
	 * equivalent to the union of 'leftArray' and 'rightArray'
	 */
	public static Object[] maybeAddAll(Object[] leftArray, int leftLength,
			Object[] rightArray, int rightLength, Duplications duplications) {
		// start with the assumption that no kvps will be added.
		Object[] newArray = leftArray;
		int l = leftLength;
		// walk rightArray potentially adding each kvp
		for (int r = 0; r < rightLength; r += 2) {
			// check whether key is already present in leftArray
			final Object rightKey = rightArray[r];
			final int i = keyIndex(leftArray, leftLength, rightKey);
			if (i == -1) {
				// key is not present
				// the first time this happens we need to clone leftArray to create space for additions
				if (newArray == leftArray)
					newArray = clone(leftArray, leftLength, leftLength + rightLength - r);
				// append the kvp
				newArray[l++] = rightKey;
				newArray[l++] = rightArray[r + 1];
			} else {
				// key is present
				final Object rightValue = rightArray[r + 1];
				duplications.duplications++;
				// is the value the same as well ?
				if (Util.equiv(leftArray[i + 1], rightValue)) {
					// value is same
					// leave as is
				} else {
					// value is different
					// the first time this happens we need to clone leftArray to create space for additions
					if (newArray == leftArray)
						newArray = clone(leftArray, leftLength, leftLength + rightLength - r);
					// overwrite old value
					newArray[i + 1] = rightValue;
				}
			}
		}
		return newArray;
	}

}
