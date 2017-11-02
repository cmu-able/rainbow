package edu.cmu.cs.able.eseb.filter;

/**
 * Interface implemented by filter states that can block requests. All
 * enumerations representing filter states to be used by
 * {@link StateBasedBlockerFilter} have to implement this interface.
 */
public interface Blocker {
	/**
	 * Is this state a state in which requests should be blocked?
	 * @return should requests be blocked?
	 */
	boolean block();
}
