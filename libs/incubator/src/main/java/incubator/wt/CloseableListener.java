package incubator.wt;

import java.io.IOException;

/**
 * Interface implemented by listeners of the {@link CloseableWorkerThread} that
 * want to be informed when the closeable is closed.
 */
public interface CloseableListener {
	/**
	 * The closeable has been closed.
	 * @param e the exception that caused the closing (or <code>null</code> if
	 * closing occurred by user command)
	 */
	void closed(IOException e);
}
