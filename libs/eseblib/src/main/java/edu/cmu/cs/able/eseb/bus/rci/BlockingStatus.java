package edu.cmu.cs.able.eseb.bus.rci;

import incubator.pval.Ensure;
import edu.cmu.cs.able.eseb.filter.BlockUnblockFilter;
import edu.cmu.cs.able.eseb.filter.BlockedUnblockedState;
import edu.cmu.cs.able.eseb.filter.EventFilterChainInfo;
import edu.cmu.cs.able.eseb.filter.EventFilterInfo;
import edu.cmu.cs.able.eseb.filter.StateBasedBlockerFilterInfo;

/**
 * Status of client blocking.
 */
public enum BlockingStatus {
	/**
	 * No blocking filter is applied.
	 */
	NO_BLOCKING_FILTER,
	
	/**
	 * Blocking filter is applied and is currently blocking.
	 */
	BLOCKING,
	
	/**
	 * Blocking filter is applied and is not currently blocking.
	 */
	NOT_BLOCKING;
	
	/**
	 * Obtains the blocking status of an event chain.
	 * @param chain_info the chain information
	 * @return the blocking status
	 */
	public static BlockingStatus status_of(EventFilterChainInfo chain_info) {
		Ensure.not_null(chain_info);
		
		StateBasedBlockerFilterInfo<BlockedUnblockedState> sfi = null;
		for (EventFilterInfo fi : chain_info.filters()) {
			if (fi.filter_class().equals(
					BlockUnblockFilter.class.getName())) {
				@SuppressWarnings("unchecked")
				StateBasedBlockerFilterInfo<BlockedUnblockedState> i =
						(StateBasedBlockerFilterInfo
						<BlockedUnblockedState>) fi;
				sfi = i;
				break;
			}
		}
		
		if (sfi == null) {
			return BlockingStatus.NO_BLOCKING_FILTER;
		} else {
			if (sfi.state() == BlockedUnblockedState.BLOCK) {
				return BlockingStatus.BLOCKING;
			} else {
				return BlockingStatus.NOT_BLOCKING;
			}
		}
	}
}
