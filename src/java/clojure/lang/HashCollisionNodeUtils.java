package clojure.lang;

import clojure.lang.PersistentHashMap.HashCollisionNode;


public class HashCollisionNodeUtils {

    public static HashCollisionNode create(int hashCode, Object key0, Object value0, Object key1, Object value1) {
        return new HashCollisionNode(null, hashCode, 2, new Object[]{key0, value0, key1, value1});
    }
        
    /*
     * return a copy of oldArray of 'newLength' - content may be truncated
     */
    public static Object[] clone(Object[] oldArray, int oldLength, int newLength) {
        final Object[] newArray = new Object[newLength];
        System.arraycopy(oldArray, 0, newArray, 0, oldLength);
        return newArray;
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
    public static Object[] maybeSet(Object[] array, int index, Object value, Counts counts) {
        counts.sameKey++;
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
    public static Object[] maybeAdd(Object[] array, int length, Object key, Object value, Counts counts) {
        final int i = keyIndex(array, length, key);
        if (i == -1) {
            return append(array, length, length + 2, key, value);
        } else {
            return maybeSet(array, i, value, counts);
        }
    }

    /*
     * return an array, the set of which's key:value pairs is
     * equivalent to the union of 'leftArray' and 'rightArray'
     */
    public static Object[] maybeAddAll(Object[] leftArray, int leftLength,
                                       Object[] rightArray, int rightLength, Counts counts) {
        final Object[]  newArray = new Object[leftLength + rightLength];
        // insert lhs first
        System.arraycopy(leftArray, 0, newArray, 0, leftLength);
        // append rhs to end
        int leftDifferences = 0;
        int rightDifferences = 0;
        int j = leftLength;
        for (int i = 0; i < rightLength; i += 2) {
            final Object rightKey = rightArray[i];
            final Object rightValue = rightArray[i + 1];
            final int index = keyIndex(leftArray, leftLength, rightKey);
            if (-1 == index) {
                leftDifferences++;
                if (i != j) rightDifferences++;
                newArray[j++] = rightKey;
                newArray[j++] = rightValue;
            } else {
                counts.sameKey++;
                final Object leftKey = leftArray[index];
                final Object leftValue = leftArray[index + 1];
                final Object newValue = counts.resolveFunction.invoke(leftKey, leftValue, rightValue);
                if (newValue != leftValue) {
                    leftDifferences++;
                    newArray[index + 1] = newValue;
                }
                if (newValue != rightValue || i != index) rightDifferences++;
            }
        }
        return
            leftDifferences == 0 ?
            leftArray :
            rightDifferences == 0 ?
            rightArray :
            newArray;            
    }

}
