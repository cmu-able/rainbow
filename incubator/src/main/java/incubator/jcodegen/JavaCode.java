package incubator.jcodegen;

import incubator.Pair;
import incubator.pval.Ensure;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>JavaCode</code> class contains the whole <em>java</em> code to
 * generate.
 */
public class JavaCode {
	/**
	 * Packages in the code.
	 */
	private Map<String, JavaPackage> m_packages;
	
	/**
	 * Creates a new representation of the <em>java code</em>.
	 */
	public JavaCode() {
		m_packages = new HashMap<>();
	}
	
	/**
	 * Creates a new java package with the given name.
	 * @param name the java package name
	 * @return the package
	 */
	public JavaPackage make_package(String name) {
		Ensure.not_null(name, "name == null");
		Ensure.is_false(m_packages.containsKey(name));
		JavaPackage pkg = new JavaPackage(name);
		m_packages.put(name, pkg);
		return pkg;
	}
	
	/**
	 * Obtains the java package with the given name. Fully qualified names are
	 * accepted
	 * @param name the name
	 * @return the package or <code>null</code> if not found
	 */
	public JavaPackage pkg(String name) {
		Ensure.not_null(name, "name == null");
		Pair<String, String> r = JavaPackage.split_1_fqn(name);
		JavaPackage pkg = m_packages.get(r.first());
		if (pkg != null && r.second() != null) {
			pkg = pkg.child(r.second());
		}
		
		return pkg;
	}
	
	/**
	 * Generates the <em>java</em> source code to the given directory. All
	 * files in the directory are deleted.
	 * @param directory the directory
	 * @throws IOException failed to write in the directory
	 */
	public void generate(File directory) throws IOException {
		Ensure.not_null(directory, "directory == null");
		Ensure.is_true(directory.isDirectory(), "'"
				+ directory.getAbsolutePath() + "' is not a directory.");
		
		erase_directory_contents(directory);
		
		for (JavaPackage p : m_packages.values()) {
			p.generate(directory);
		}
	}
	
	/**
	 * Erases the contents of a directory recursively.
	 * @param directory the directory
	 * @throws IOException failed to erase the directory
	 */
	private void erase_directory_contents(File directory) throws IOException {
		Ensure.not_null(directory, "directory == null");
		Ensure.is_true(directory.isDirectory(), "'"
				+ directory.getAbsolutePath() + "' is not a directory.");
		
		for (File f : directory.listFiles()) {
			if (!f.isDirectory()) {
				if (!f.delete()) {
					throw new IOException("Failed to delete file '"
							+ f.getAbsolutePath() + "'.");
				}
			} else {
				erase_directory_contents(f);
				if (!f.delete()) {
					throw new IOException("Failed to delete directory '"
							+ f.getAbsolutePath() + "'.");
				}
			}
		}
	}
}
