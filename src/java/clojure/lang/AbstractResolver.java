package clojure.lang;

abstract class AbstractResolver implements Resolver {
	public boolean equals(Object that) {
		return this.getClass().equals(that.getClass()); // TODO: this falls short of what I was hoping for...
	}
}