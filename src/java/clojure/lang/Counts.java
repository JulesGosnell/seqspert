package clojure.lang;

public class Counts {
        
    public final IFn resolveFunction;
    public int sameKey;
    public int sameKeyAndValue;
    public static IFn resolveLeft  = new AFn() {
            @Override public Object invoke(Object key, Object leftValue, Object rightValue) {
                return (Util.equiv(leftValue, rightValue)) ? leftValue : rightValue;
            }};
    public static IFn resolveRight = new AFn() {
            @Override public Object invoke(Object key, Object leftValue, Object rightValue) {
                return rightValue; 
            }};

    public Counts() {
        this.resolveFunction = Counts.resolveLeft;
        this.sameKey = 0;
        this.sameKeyAndValue = 0;
    }

    public Counts(IFn resolveFn, int sameKey, int sameKeyAndValue) {
        this.resolveFunction = resolveFn;
        this.sameKey = sameKey;
        this.sameKeyAndValue = sameKeyAndValue;
    }

    public boolean equals(Counts that) {
        return
            this.resolveFunction == that.resolveFunction &&
            this.sameKey == that.sameKey &&
            this.sameKeyAndValue == that.sameKeyAndValue;
    }

    @Override
    public boolean equals(Object that) {
        return (that instanceof Counts) && this.equals((Counts) that);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + 
            "(resolveFunction=" + resolveFunction +
            ", sameKey=" + sameKey +
            ", sameKeyAndValue=" + sameKeyAndValue +")";
    }
}
