package incubator.jcodegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import incubator.pval.Ensure;

/**
 * Represents a <em>java</em> class.
 */
public class JavaClass {
	/**
	 * Extension for class files.
	 */
	private static final String JAVA_EXTENSION = ".java";
	
	/**
	 * The class name,
	 */
	private String m_name;
	
	/**
	 * The package the class belongs to.
	 */
	private JavaPackage m_package;
	
	/**
	 * The class fields.
	 */
	private Map<String, JavaField> m_fields;
	
	/**
	 * The field names, sorted.
	 */
	private List<String> m_field_names;
	
	/**
	 * The methods, sorted.
	 */
	private List<JavaMethod> m_methods;
	
	/**
	 * Methods implemented by this class.
	 */
	private List<String> m_implements;
	
	/**
	 * The super class.
	 */
	private JavaClass m_super_class;
	
	/**
	 * Creates a new class.
	 * @param name the class name
	 * @param pkg the package this class belongs to
	 * @param super_class an optional superclass
	 */
	public JavaClass(String name, JavaPackage pkg, JavaClass super_class) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(pkg, "pkg == null");
		
		m_name = name;
		m_package = pkg;
		m_fields = new HashMap<>();
		m_field_names = new ArrayList<>();
		m_methods = new ArrayList<>();
		m_implements = new ArrayList<>();
		m_super_class = super_class;
	}
	
	/**
	 * Obtains the super class.
	 * @return the super classor <code>null</code> if none
	 */
	public JavaClass super_class() {
		return m_super_class;
	}
	
	/**
	 * Obtains the class name.
	 * @return the class name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Obtains the <em>java</em> package.
	 * @return the package
	 */
	public JavaPackage pkg() {
		return m_package;
	}
	
	/**
	 * Creates a new field in the class.
	 * @param name the field name
	 * @param type the field type
	 * @return the field
	 */
	public JavaField make_field(String name, JavaType type) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(type, "type == null");
		Ensure.is_false(m_fields.containsKey(name), "Field '" + name
				+ "' already exists.");
		JavaField f = new JavaField(name, type);
		m_fields.put(name, f);
		m_field_names.add(name);
		return f;
	}
	
	/**
	 * Obtains the names of all fields.
	 * @return the names of all fields
	 */
	public Set<String> field_names() {
		return new HashSet<>(m_field_names);
	}
	
	/**
	 * Obtains the field with a given name.
	 * @param name the field name
	 * @return the field or <code>null</code> if there is no field with the
	 * given name
	 */
	public JavaField field(String name) {
		Ensure.not_null(name, "name == null");
		return m_fields.get(name);
	}
	
	/**
	 * Creates a new method.
	 * @param name the method name
	 * @param type the method type
	 * @return the created method
	 */
	public JavaMethod make_method(String name, JavaType type) {
		Ensure.not_null(name, "name == null");
		
		JavaMethod m = new JavaMethod(name, type);
		m_methods.add(m);
		return m;
	}
	
	/**
	 * Obtains all methods of the class.
	 * @return all methods
	 */
	public List<JavaMethod> methods() {
		return new ArrayList<>(m_methods);
	}
	
	/**
	 * Adds a new interface implemented by this class.
	 * @param i the interface
	 */
	public void add_implements(String i) {
		Ensure.not_null(i, "i == null");
		m_implements.add(i);
	}
	
	/**
	 * Obtains the list of interfaces implemented by this class.
	 * @return the list of interfaces
	 */
	public List<String> interfaces() {
		return new ArrayList<>(m_implements);
	}
	
	/**
	 * Generates this class.
	 * @param package_directory the directory where the class should be
	 * generated to
	 * @throws IOException failed to generate the class
	 */
	void generate(File package_directory) throws IOException {
		Ensure.not_null(package_directory, "package_directory == null");
		Ensure.is_true(package_directory.isDirectory(), "'"
				+ package_directory.getAbsolutePath() + "' is not a "
				+ "directory.");
		
		File class_file = new File(package_directory, m_name + JAVA_EXTENSION);
		try (FileWriter fw = new FileWriter(class_file)) {
			fw.write("package " + m_package.fqn() + ";\n");
			fw.write("public class " + m_name);
			if (m_super_class != null) {
				fw.write(" extends " + m_super_class.m_package.fqn()
						+ "." + m_super_class.m_name);
			}
			
			if (m_implements.size() > 0) {
				fw.write(" implements " + m_implements.get(0));
				for (int i = 1; i < m_implements.size(); i++) {
					fw.write(", " + m_implements.get(i));
				}
			}

			fw.write(" {\n");
			for (String fn : m_field_names) {
				JavaField f = m_fields.get(fn);
				fw.write(f.generate());
			}
			
			for (JavaMethod m : m_methods) {
				fw.write(m.generate());
			}
			
			fw.write("}\n");
		}
	}
}
