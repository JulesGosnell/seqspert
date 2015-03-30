package clojure.lang;


public class Counts {
        
	public static Resolver leftResolver = new LeftResolver();
	public static Resolver rightResolver = new RightResolver();
	
    public final Resolver resolver;
    public int sameKey;
    public int sameKeyAndValue;


    public Counts() {
        this.resolver = Counts.leftResolver;
        this.sameKey = 0;
        this.sameKeyAndValue = 0;
    }

    public Counts(Resolver resolver, int sameKey, int sameKeyAndValue) {
        this.resolver = resolver;
        this.sameKey = sameKey;
        this.sameKeyAndValue = sameKeyAndValue;
    }

    public boolean equals(Counts that) {
        return
            this.resolver.equals(that.resolver) &&
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
            "(resolver=" + resolver +
            ", sameKey=" + sameKey +
            ", sameKeyAndValue=" + sameKeyAndValue +")";
    }
}
