package edu.cmu.cs.able.eseb.filter;

/**
 * Possible states for a block/unblock filter.
 */
public enum BlockedUnblockedState implements Blocker {
	/**
	 * Filter is blocking data.
	 */
	BLOCK {
		@Override
		public boolean block() {
			return true;
		}
		
	},
	/**
	 * Filter is not blocking data.
	 */
	UNBLOCK {
		@Override
		public boolean block() {
			return false;
		}
	};

	@Override
	public abstract boolean block();
}
