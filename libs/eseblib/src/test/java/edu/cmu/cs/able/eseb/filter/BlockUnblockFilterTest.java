package edu.cmu.cs.able.eseb.filter;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite that verifies that the block/unblock filter works.
 */
@SuppressWarnings("javadoc")
public class BlockUnblockFilterTest extends FilterTestCase {
	/**
	 * A block/unblock filter.
	 */
	private BlockUnblockFilter m_buf;
	
	@Before
	public void set_up() throws Exception {
		m_buf = new BlockUnblockFilter(BlockedUnblockedState.UNBLOCK);
	}
	
	@Test
	public void filter_starts_with_initial() throws Exception {
		assertEquals(BlockedUnblockedState.BLOCK,
				new BlockUnblockFilter(BlockedUnblockedState.BLOCK).state());
		assertEquals(BlockedUnblockedState.UNBLOCK,
				new BlockUnblockFilter(BlockedUnblockedState.UNBLOCK).state());
	}
	
	@Test
	public void can_unblock_unblocked() throws Exception {
		assertEquals(BlockedUnblockedState.UNBLOCK, m_buf.state());
		m_buf.unblock();
		assertEquals(BlockedUnblockedState.UNBLOCK, m_buf.state());
	}
	
	@Test
	public void can_block_blocked() throws Exception {
		m_buf.block();
		assertEquals(BlockedUnblockedState.BLOCK, m_buf.state());
		m_buf.block();
		assertEquals(BlockedUnblockedState.BLOCK, m_buf.state());
	}
	
	@Test
	public void unblocked_events_are_accepted() throws Exception {
		m_buf.connect(m_sink);
		m_buf.sink(bus_data());
		assertEquals(1, m_sink.m_data.size());
	}
	
	@Test
	public void blocked_events_are_rejected() throws Exception {
		m_buf.block();
		m_buf.connect(m_sink);
		m_buf.sink(bus_data());
		assertEquals(0, m_sink.m_data.size());
	}
	
	@Test
	public void obtaining_blocked_info() throws Exception {
		m_buf.block();
		@SuppressWarnings("unchecked")
		StateBasedBlockerFilterInfo<BlockedUnblockedState> st
			= (StateBasedBlockerFilterInfo<BlockedUnblockedState>) m_buf.info();
		assertEquals(BlockedUnblockedState.BLOCK, st.state());
	}
	
	@Test
	public void obtaining_unblocked_info() throws Exception {
		@SuppressWarnings("unchecked")
		StateBasedBlockerFilterInfo<BlockedUnblockedState> st
			= (StateBasedBlockerFilterInfo<BlockedUnblockedState>) m_buf.info();
		assertEquals(BlockedUnblockedState.UNBLOCK, st.state());
	}
	
	@Test
	public void block_unblock_enumeration_coverage() throws Exception {
		cover_enumeration(BlockedUnblockedState.class);
	}
}
