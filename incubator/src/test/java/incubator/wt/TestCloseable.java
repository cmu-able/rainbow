package incubator.wt;

import java.io.Closeable;
import java.io.IOException;

/**
 * Simple closeable that tracks whether it has been closed or not.
 */
public class TestCloseable implements Closeable {
	/**
	 * How many times has the closeable been closed?
	 */
	public int m_closed;

	/**
	 * Creates a new closeable.
	 */
	public TestCloseable() {
		m_closed = 0;
	}

	@Override
	public void close() throws IOException {
		m_closed++;
	}
}