package auxtestlib;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class with methods to help dealing with text files.
 */
public final class FileContentWorker {
	/**
	 * Utility class: no constructor.
	 */
	private FileContentWorker() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Reads a file's contents.
	 * @param file the file
	 * @return a string with the whole file's contents
	 * @throws IOException failed to read the file
	 * @deprecated use {@link #read_contents(File)}
	 */
	@Deprecated
	public static String readContents(File file) throws IOException {
		return read_contents(file);
	}
	
	/**
	 * Reads a file's contents.
	 * @param file the file
	 * @return a string with the whole file's contents
	 * @throws IOException failed to read the file
	 */
	public static String read_contents(File file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("file == null");
		}

		StringBuffer sb = new StringBuffer();
		try (FileReader fr = new FileReader(file)) {
			int ch;
			while ((ch = fr.read()) != -1) {
				sb.append((char) ch);
			}
		}

		return sb.toString();
	}

	/**
	 * Obtains the contents of a file as a binary array.
	 * @param binFile the file
	 * @return an array with the file's contents
	 * @throws IOException failed to read the file
	 * @deprecated use {@link #read_contents_bin(File)}
	 */
	@Deprecated
	public static byte[] readContentsBin(File binFile) throws IOException {
		return read_contents_bin(binFile);
	}

	/**
	 * Obtains the contents of a file as a binary array.
	 * @param binFile the file
	 * @return an array with the file's contents
	 * @throws IOException failed to read the file
	 */
	public static byte[] read_contents_bin(File binFile) throws IOException {
		if (binFile == null) {
			throw new IllegalArgumentException("binFile == null");
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (FileInputStream fis = new FileInputStream(binFile)) {
			int ch;
			while ((ch = fis.read()) != -1) {
				os.write(ch);
			}
		}

		return os.toByteArray();
	}

	/**
	 * Changes the contents of a file (or creates a new file if it doesn't
	 * exist).
	 * @param f the file to change
	 * @param s the file's new contents
	 * @throws IOException failed to write the file
	 * @deprecated use {@link #set_contents(File, String)}
	 */
	@Deprecated
	public static void setContents(File f, String s) throws IOException {
		set_contents(f, s);
	}
	
	/**
	 * Changes the contents of a file (or creates a new file if it doesn't
	 * exist).
	 * @param f the file to change
	 * @param s the file's new contents
	 * @throws IOException failed to write the file
	 */
	public static void set_contents(File f, String s) throws IOException {
		if (f == null) {
			throw new IllegalArgumentException("f == null");
		}

		if (s == null) {
			throw new IllegalArgumentException("s == null");
		}

		try (FileWriter fw = new FileWriter(f)) {
			fw.write(s);
		}
	}

	/**
	 * Changes the contents of a file (or creates a new file if it doesn't
	 * exist).
	 * @param f the file to change
	 * @param data the new file's contents
	 * @throws IOException failed to write the file
	 * @deprecated use {@link #set_contents_bin(File, byte[])}
	 */
	@Deprecated
	public static void setContentsBin(File f, byte data[]) throws IOException {
		set_contents_bin(f, data);
	}
	
	/**
	 * Changes the contents of a file (or creates a new file if it doesn't
	 * exist).
	 * @param f the file to change
	 * @param data the new file's contents
	 * @throws IOException failed to write the file
	 */
	public static void set_contents_bin(File f, byte data[])
			throws IOException {
		if (f == null) {
			throw new IllegalArgumentException("f == null");
		}

		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}

		try (FileOutputStream fo = new FileOutputStream(f)) {
			fo.write(data);
		}
	}

	/**
	 * Reads a file's contents (the file is expected to be a resource).
	 * @param name the resource name
	 * @return the resource's contents as a string
	 * @throws IOException failed to read the resource
	 * @deprecated use {@link #read_resource(String)}
	 */
	@Deprecated
	public static String readResourceContents(String name) throws IOException {
		return read_resource(name);
	}
	
	/**
	 * Reads a file's contents (the file is expected to be a resource).
	 * @param name the resource name
	 * @return the resource's contents as a string
	 * @throws IOException failed to read the resource
	 */
	public static String read_resource(String name) throws IOException {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		StringBuffer sb = new StringBuffer();
		try (InputStream is =
				FileContentWorker.class.getResourceAsStream(name)) {
			if (is == null) {
				throw new IllegalArgumentException("Resource '" + name + "' "
						+ "is not available.");
			}
	
			try (InputStreamReader isr = new InputStreamReader(is)) {
				int ch;
				while ((ch = isr.read()) != -1) {
					sb.append((char) ch);
				}
			}
		}
		
		return sb.toString();
	}

	/**
	 * Reads a file's contents (the file is expected to be a resource).
	 * @param cls a class defining the package where the resource is located
	 * @param name the resource name
	 * @return the resource's contents as a string
	 * @throws IOException failed to read the resource
	 */
	public static String read_resource(Class<?> cls, String name)
			throws IOException {
		if (cls == null) {
			throw new IllegalArgumentException("cls == null");
		}
		
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		StringBuffer sb = new StringBuffer();
		try (InputStream is = cls.getResourceAsStream(name)) {
			if (is == null) {
				throw new IllegalArgumentException("Resource '" + name + "' "
						+ "is not available.");
			}
	
			try (InputStreamReader isr = new InputStreamReader(is)) {
				int ch;
				while ((ch = isr.read()) != -1) {
					sb.append((char) ch);
				}
			}
		}
		
		return sb.toString();
	}

	/**
	 * Reads a file's contents as binary data (the file is expected to be a
	 * resource).
	 * @param name the resource name
	 * @return the resource's contents
	 * @throws IOException failed to read the resource
	 * @deprecated use {@link #read_resource_bin(String)}
	 */
	@Deprecated
	public static byte[] readResourceContentsBin(String name)
			throws IOException {
		return read_resource_bin(name);
	}
	
	/**
	 * Reads a file's contents as binary data (the file is expected to be a
	 * resource).
	 * @param name the resource name
	 * @return the resource's contents
	 * @throws IOException failed to read the resource
	 */
	public static byte[] read_resource_bin(String name)
			throws IOException {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (InputStream is =
				FileContentWorker.class.getResourceAsStream(name)) {
			if (is == null) {
				throw new IllegalArgumentException("Resource '" + name + "' is "
						+ "not available.");
			}
	
			int ch;
			while ((ch = is.read()) != -1) {
				os.write(ch);
			}
		}
		
		return os.toByteArray();
	}

	/**
	 * Checks that two files are equal.
	 * @param f1 first file
	 * @param f2 second file
	 * @return are the files equal?
	 * @throws IOException failed to read the files
	 * @deprecated use {@link #files_equal(File, File)}
	 */
	@Deprecated
	public static boolean fileEquals(File f1, File f2) throws IOException {
		return files_equal(f1, f2);
	}
	
	/**
	 * Checks that two files are equal.
	 * @param f1 first file
	 * @param f2 second file
	 * @return are the files equal?
	 * @throws IOException failed to read the files
	 */
	public static boolean files_equal(File f1, File f2) throws IOException {
		if (f1 == null) {
			throw new IllegalArgumentException("f1 == null");
		}

		if (f2 == null) {
			throw new IllegalArgumentException("f2 == null");
		}

		byte dt1[] = FileContentWorker.readContentsBin(f1);
		byte dt2[] = FileContentWorker.readContentsBin(f2);
		return ComparisonUtils.arrayEquals(dt1, dt2);
	}
}
