package edu.cmu.cs.able.typelib.scope;

/**
 * Simple scoped object used for testing purposes.
 */
public class TestScope extends Scope<ScopedObject> {
	/**
	 * Creates a new unnamed test scope.
	 */
	public TestScope() {
		super(null);
	}
	
	/**
	 * Creates a new test scope.
	 * @param name the scope name
	 */
	public TestScope(String name) {
		super(name);
	}
}
