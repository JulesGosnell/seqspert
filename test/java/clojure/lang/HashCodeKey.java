package clojure.lang;

class HashCodeKey {

    private Object key;
    private int hashCode;

    public HashCodeKey(Object key, int hashCode) {
        this.key = key;
        this.hashCode = hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object that) {
        return that != null &&
                that instanceof HashCodeKey &&
                ((HashCodeKey)that).hashCode == hashCode &&
                ((HashCodeKey)that).key.equals(key);
    }

}