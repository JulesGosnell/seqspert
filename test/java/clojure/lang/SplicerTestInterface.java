package clojure.lang;

// cannot be called SplicerTest or picked up by JunitRunner
public interface SplicerTestInterface {
    void testDifferent();
    void testSameKeyHashCode();
    void testSameKey();
    void testSameKeyAndValue();
}
