package auxtestlib;

import java.io.File;
import java.io.IOException;

/**
 * Class that creates a temporary file or directory. This file or directory is
 * removed when the JVM terminates (it may be deleted sooner if the
 * {@link #delete()} method is called explicitly). If the created file is a
 * directory, it will be removed recursively.
 */
public class TemporaryFile {
	/**
	 * Created file. If <code>null</code> means it was already removed.
	 */
	private File created;

	/**
	 * Creates a new temporary file (or directory). This is equivalent to call
	 * {@link #TemporaryFile(File, boolean, String, String)} with all other
	 * arguments as <code>null</code>.
	 * 
	 * @param directory should a directory be created? If <code>false</code> a
	 * file is created
	 * 
	 * @throws IOException failed to create the file
	 */
	public TemporaryFile(boolean directory) throws IOException {
		this(null, directory, null, null);
	}

	/**
	 * Creates a new file (or directory).
	 * 
	 * @param path path to the parent directory. If <code>null</code> the
	 * file/directory will be created on the default location.
	 * @param directory should a directory be created? If <code>false</code> a
	 * file is created
	 * @param prefix an optional prefix for the file (it a prefix is provided it
	 * must contain at least 3 characters).
	 * @param suffix an optional suffix for the file
	 * 
	 * @throws IOException failed to create the file
	 */
	public TemporaryFile(File path, boolean directory, String prefix,
			String suffix) throws IOException {
		String pfx = prefix;

		if (pfx == null) {
			pfx = "junit_";
		}

		if (pfx.length() < 3) {
			throw new IllegalArgumentException("prefix must have at least "
					+ "3 characters");
		}

		created = File.createTempFile(pfx, suffix, path);
		if (directory) {
			if (!created.delete()) {
				throw new IOException("Failed to delete file '"
						+ created.getAbsolutePath() + "'");
			}

			if (!created.mkdir()) {
				throw new IOException("Failed to create directory '"
						+ created.getAbsolutePath() + "'");
			}
		}

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				shutdownDelete();
			}
		});

		Runtime.getRuntime().addShutdownHook(t);
	}

	/**
	 * Obtains a reference to the created file.
	 * 
	 * @return the created file (or directory).
	 */
	public File getFile() {
		if (created == null) {
			throw new IllegalStateException("File/directory already deleted.");
		}

		return created;
	}

	/**
	 * Deletes the file or directory. If this represents a directory, all its
	 * contents are recursively deleted.
	 */
	public void delete() {
		if (created == null) {
			throw new IllegalStateException("File already deleted.");
		}

		shutdownDelete();
	}

	/**
	 * Removes the file or directory if it hasn't been removed yet. If this
	 * represents a directory, all its contents are recursively deleted.
	 */
	private void shutdownDelete() {
		if (created == null) {
			return;
		}

		doDelete(created);

		created = null;
	}

	/**
	 * Deletes a file. If the file is a directory, it is removed with all its
	 * contents.
	 * 
	 * @param f the file or directory to remove
	 */
	private void doDelete(File f) {
		assert f != null;

		if (f.exists()) {
			if (f.isDirectory()) {
				doDeleteDir(f);
			}
			boolean res = f.delete();
			assert res || !res; // Otherwise findbugs complains.
		}
	}

	/**
	 * Removes a directory and all its contents
	 * 
	 * @param d the directory to remove
	 */
	private void doDeleteDir(File d) {
		assert d != null;
		assert d.isDirectory();

		File f[] = d.listFiles();
		for (int i = 0; i < f.length; i++) {
			doDelete(f[i]);
		}
	}
}
