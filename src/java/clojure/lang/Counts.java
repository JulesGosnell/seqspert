package clojure.lang;

public class Counts {
        
    public final boolean preserveIdentity;
    public int sameKey;
    public int sameKeyAndValue;

    public Counts() {
        this.preserveIdentity = false;
        this.sameKey = 0;
        this.sameKeyAndValue = 0;
    }

    public Counts(boolean preserveIdentity, int sameKey, int sameKeyAndValue) {
        this.preserveIdentity = preserveIdentity;
        this.sameKey = sameKey;
        this.sameKeyAndValue = sameKeyAndValue;
    }

    public boolean equals(Counts that) {
        return this.preserveIdentity == that.preserveIdentity && this.sameKey == that.sameKey && this.sameKeyAndValue == that.sameKeyAndValue;
    }

    public boolean equals(Object that) {
        return (that instanceof Counts) && this.equals((Counts) that);
    }
    
    public String toString() {
        return getClass().getSimpleName() + 
            "(preserveIdentity=" + preserveIdentity + ", sameKey=" + sameKey + ", sameKeyAndValue=" + sameKeyAndValue +")";
    }
}
