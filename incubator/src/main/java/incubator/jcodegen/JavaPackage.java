package incubator.jcodegen;

import incubator.Pair;
import incubator.pval.Ensure;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A java package represents a <em>java</em> package.
 */
public class JavaPackage {
	/**
	 * Package name divider.
	 */
	private static final char DIVIDER = '.';
	
	/**
	 * The package name.
	 */
	private String m_name;
	
	/**
	 * The parent package.
	 */
	private JavaPackage m_parent;
	
	/**
	 * The children packages.
	 */
	private Map<String, JavaPackage> m_children;
	
	/**
	 * Classes in this package.
	 */
	private Map<String, JavaClass> m_classes;
	
	/**
	 * Creates a package with the given name without a parent.
	 * @param name the package name
	 */
	public JavaPackage(String name) {
		Ensure.not_null(name, "name == null");
		
		m_name = name;
		m_parent = null;
		m_children = new HashMap<>();
		m_classes = new HashMap<>();
	}
	
	/**
	 * Creates a child package with the given name and parent.
	 * @param name the package name
	 * @return the package
	 */
	public JavaPackage make_child(String name) {
		Ensure.not_null(name, "name == null");
		Ensure.is_false(m_children.containsKey(name));
		
		JavaPackage child = new JavaPackage(name, this);
		m_children.put(name, child);
		return child;
	}
	
	/**
	 * Creates a class in this package.
	 * @param name the class name
	 * @return the class
	 */
	public JavaClass make_class(String name) {
		Ensure.not_null(name, "name == null");
		Ensure.is_false(m_classes.containsKey(name));
		
		JavaClass jc = new JavaClass(name, this);
		m_classes.put(name, jc);
		return jc;
	}
	
	/**
	 * Creates a package with the given name and parent.
	 * @param name the package name
	 * @param parent the package's parent
	 */
	private JavaPackage(String name, JavaPackage parent) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(parent, "parent == null");
		
		m_name = name;
		m_parent = parent;
		m_children = new HashMap<>();
		m_classes = new HashMap<>();
	}
	
	/**
	 * Obtains the package's name.
	 * @return the package's name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Obtains the package's parent.
	 * @return the parent or or <code>null</code> if the package has no parent
	 */
	public JavaPackage parent() {
		return m_parent;
	}
	
	/**
	 * Obtains the package's fully qualified name.
	 * @return the package's fully qualified name
	 */
	public String fqn() {
		if (m_parent == null) {
			return m_name;
		} else {
			return m_parent.fqn() + DIVIDER + m_name;
		}
	}
	
	/**
	 * Obtains the child package with the given name.
	 * @param name the name, which may be a fully qualified name
	 * @return the package or <code>null</code> if there is none
	 */
	public JavaPackage child(String name) {
		Ensure.not_null(name, "name == null");
		Pair<String, String> r = split_1_fqn(name);
		JavaPackage pkg = m_children.get(r.first());
		if (pkg != null && r.second() != null) {
			pkg = pkg.child(r.second());
		}
		
		return pkg;
	}
	
	/**
	 * Obtains the child class with the given name.
	 * @param name the class name
	 * @return the class or <code>null</code> if none was found
	 */
	public JavaClass child_class(String name) {
		Ensure.not_null(name, "name == null");
		return m_classes.get(name);
	}
	
	/**
	 * Breaks a package name into its first part and rest if it is a
	 * qualified package name.
	 * @param name the name
	 * @return the name parts; the second part will be empty if the name is
	 * not a fully qualified package name
	 */
	public static Pair<String, String> split_1_fqn(String name) {
		Ensure.not_null(name, "name == null");
		int idx = name.indexOf(DIVIDER);
		if (idx == -1) {
			return new Pair<>(name, null);
		} else {
			return new Pair<>(name.substring(0, idx), name.substring(idx + 1));
		}
	}
	
	/**
	 * Generates this package in the given directory.
	 * @param directory the directory
	 * @throws IOException failed to read/write
	 */
	void generate(File directory) throws IOException {
		Ensure.not_null(directory, "directory != null");
		Ensure.is_true(directory.isDirectory(), "'"
				+ directory.getAbsolutePath() + "' is not a directory.");
		
		File package_directory = new File(directory, m_name);
		if (package_directory.exists()) {
			throw new IOException("Directory '"
					+ package_directory.getAbsolutePath() + "' already "
					+ "exists.");
		}
		
		if (!package_directory.mkdir()) {
			throw new IOException("Failed to create directory '"
					+ package_directory.getAbsolutePath() + "'.");
		}
		
		for (JavaPackage sp : m_children.values()) {
			sp.generate(package_directory);
		}
		
		for (JavaClass jc : m_classes.values()) {
			jc.generate(package_directory);
		}
	}
}
