package clojure.lang;

// cannot be called SplicerTest or picked up by JunitRunner
public interface SplicerTestInterface {

	public abstract void testNoCollision();

	public abstract void testCollision();

	public abstract void testDuplication();
	
	// TODO
	//public abstract void testIdentical();

}