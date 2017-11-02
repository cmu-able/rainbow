package edu.cmu.cs.able.eseb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import incubator.wt.CloseableListener;

/**
 * Test class that implements a closeable listener that keeps track of its
 * invocations.
 */
public class TestCloseableListener implements CloseableListener {
	/**
	 * List containing one item per invocation with the exception provided as
	 * parameter.
	 */
	public List<IOException> m_closed;
	
	/**
	 * Creates a new listener.
	 */
	public TestCloseableListener() {
		m_closed = new ArrayList<>();
	}

	@Override
	public void closed(IOException e) {
		m_closed.add(e);
	}
}
