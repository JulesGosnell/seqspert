package clojure.lang;

public class Counts {
        
    public final IFn resolveFunction;
    public int sameKey;
    public int sameKeyAndValue;

    public Counts() {
        this.resolveFunction = null;
        this.sameKey = 0;
        this.sameKeyAndValue = 0;
    }

    public Counts(IFn resolveFn, int sameKey, int sameKeyAndValue) {
        this.resolveFunction = resolveFn;
        this.sameKey = sameKey;
        this.sameKeyAndValue = sameKeyAndValue;
    }

    public boolean equals(Counts that) {
        return this.resolveFunction == that.resolveFunction && this.sameKey == that.sameKey && this.sameKeyAndValue == that.sameKeyAndValue;
    }

    public boolean equals(Object that) {
        return (that instanceof Counts) && this.equals((Counts) that);
    }
    
    public String toString() {
        return getClass().getSimpleName() + 
            "(resolveFunction=" + resolveFunction + ", sameKey=" + sameKey + ", sameKeyAndValue=" + sameKeyAndValue +")";
    }
}
