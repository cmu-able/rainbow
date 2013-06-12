package incubator;

import incubator.pval.Ensure;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that is able to read a directory and list all files recursively.
 */
public class DirectoryReader {
	/**
	 * Utility class: no constructor.
	 */
	private DirectoryReader() {
	}
	
	/**
	 * Lists all files in a directory recursively.
	 * @param directory the directory
	 * @return all files; an empty list if none is found
	 */
	public static Set<File> listAllRecursively(File directory) {
		Ensure.notNull(directory);
		Ensure.isTrue(directory.isDirectory());
		
		Set<File> result = new HashSet<>();
		
		File[] files = directory.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				result.addAll(listAllRecursively(f));
			} else {
				result.add(f);
			}
		}
		
		return result;
	}
}
