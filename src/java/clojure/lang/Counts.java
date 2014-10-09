package clojure.lang;

public class Counts {
        
    public int sameKey;
    public int sameKeyAndValue;

    public Counts() {
        this.sameKey = 0;
        this.sameKeyAndValue = 0;
    }

    public Counts(int sameKey, int sameKeyAndValue) {
        this.sameKey = sameKey;
        this.sameKeyAndValue = sameKeyAndValue;
    }

    public boolean equals(Counts that) {
        return this.sameKey == that.sameKey && this.sameKeyAndValue == that.sameKeyAndValue;
    }

    public boolean equals(Object that) {
        return (that instanceof Counts) && this.equals((Counts) that);
    }
    
    public String toString() {
        return getClass().getSimpleName() + 
            "(sameKey=" + sameKey + ", sameKeyAndValue=" + sameKeyAndValue +")";
    }
}
