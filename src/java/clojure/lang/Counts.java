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
}
