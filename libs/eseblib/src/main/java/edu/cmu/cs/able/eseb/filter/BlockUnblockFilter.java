package edu.cmu.cs.able.eseb.filter;

import edu.cmu.cs.able.eseb.BusData;

/**
 * Filter that can block or unblock requests. The block/unblock control is
 * performed outside of the filter.
 */
public class BlockUnblockFilter extends
		StateBasedBlockerFilter<BlockedUnblockedState> {
	/**
	 * Creates a new filter.
	 * @param initial the initial state
	 */
	public BlockUnblockFilter(BlockedUnblockedState initial) {
		super(initial);
	}
	
	/**
	 * Blocks all events.
	 */
	public void block() {
		state(BlockedUnblockedState.BLOCK);
	}
	
	/**
	 * Unblocks all events.
	 */
	public void unblock() {
		state(BlockedUnblockedState.UNBLOCK);
	}

	@Override
	protected void handle(BusData d) {
		/*
		 * Nothing to do.
		 */
	}
}
