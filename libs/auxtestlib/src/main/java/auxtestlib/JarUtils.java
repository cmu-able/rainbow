package auxtestlib;

import java.io.File;
import java.io.IOException;

/**
 * Class that provides utility methods to work with jars.
 */
public final class JarUtils {
	/**
	 * Utility class: no constructor
	 */
	private JarUtils() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Creates a jar from a directory. If the file already exists, this method
	 * will throw an exception.
	 * 
	 * @param directory the jar's base directory
	 * @param jarFile the file to create. If it is <code>null</code> the jar's
	 * name will be the directory name followed by <code>.jar</code> (the file
	 * is created in the same parent directory as the base directory)
	 * 
	 * @return o the file with the jar
	 * 
	 * @throws IOException the file already exists
	 */
	public static File makeJar(File directory, File jarFile) throws IOException {
		if (directory == null) {
			throw new IllegalArgumentException("directory == null");
		}

		if (!directory.exists()) {
			throw new IllegalArgumentException("Directory '"
					+ directory.getAbsolutePath() + "' doesnt't exist.");
		}

		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("File '"
					+ directory.getAbsolutePath() + "' is not a directory.");
		}

		File parent = directory.getParentFile();
		if (parent == null) {
			throw new IllegalArgumentException("Directory '"
					+ directory.getAbsolutePath() + "' does not name a parent.");
		}

		File jfile = jarFile;

		if (jfile == null) {
			jfile = new File(parent, directory.getName() + ".jar");
		}

		if (jfile.exists()) {
			throw new IllegalArgumentException("Jar file '"
					+ jfile.getAbsolutePath() + "' already exists.");
		}

		String args[] = new String[] { "jar", "cf", jfile.getAbsolutePath(),
				directory.getName() };

		CommandRunner cr = new CommandRunner();

		// TODO: We must find some way to handle the timeout.
		cr.run_command(args, parent, 60);
		return jfile;
	}

	/**
	 * Creates a jar from a directory contents (not the directory itself). If
	 * teh file already exists, the method will throw an exception.
	 * 
	 * @param directory the jar's base directory
	 * @param jarFile jar file to create. If <code>null</code> the jar is
	 * created with the same parent as the directory and it's name is the
	 * directory name followed by <code>.jar</code>
	 * 
	 * @return the created jar file
	 * 
	 * @throws IOException file already exists
	 */
	public static File makeFullJar(File directory, File jarFile)
			throws IOException {
		if (directory == null) {
			throw new IllegalArgumentException("directory == null");
		}

		if (jarFile == null) {
			throw new IllegalArgumentException("jarFile == null");
		}

		if (!directory.exists()) {
			throw new IllegalArgumentException("Directory '"
					+ directory.getAbsolutePath() + "' doesnt't exist.");
		}

		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("File '"
					+ directory.getAbsolutePath() + "' is not a directory.");
		}

		if (jarFile.exists()) {
			throw new IllegalArgumentException("Jar file '"
					+ jarFile.getAbsolutePath() + "' already exists.");
		}

		File files[] = directory.listFiles();
		String args[] = new String[3 + files.length];
		args[0] = "jar";
		args[1] = "cf";
		args[2] = jarFile.getAbsolutePath();
		for (int i = 0; i < files.length; i++) {
			args[3 + i] = files[i].getName();
		}

		CommandRunner cr = new CommandRunner();

		// TODO: We must handle the timeout
		cr.run_command(args, directory, 60);
		return jarFile;
	}
}
