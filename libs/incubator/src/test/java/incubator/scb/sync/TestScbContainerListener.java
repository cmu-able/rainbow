package incubator.scb.sync;

import incubator.scb.ScbContainerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Container listener that keeps track of all SCBs added, removed and updated.
 * @param <T> the type of SCB
 */
public class TestScbContainerListener<T> implements ScbContainerListener<T> {
	/**
	 * Receives all SCBs added.
	 */
	public List<T> m_added;
	
	/**
	 * Receives all SCBs removed.
	 */
	public List<T> m_removed;
	
	/**
	 * Receives all SCBs changed.
	 */
	public List<T> m_updated;
	
	/**
	 * Creates a new listener.
	 */
	public TestScbContainerListener() {
		m_added = new ArrayList<>();
		m_removed = new ArrayList<>();
		m_updated = new ArrayList<>();
	}

	@Override
	public synchronized void scb_added(T t) {
		m_added.add(t);
	}

	@Override
	public synchronized void scb_removed(T t) {
		m_removed.add(t);
	}

	@Override
	public synchronized void scb_updated(T t) {
		m_updated.add(t);
	}
}
