package incubator.scb.sdl;

import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaPackage;
import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing an SDL bean.
 */
public class SdlBean extends PropertyObject {
	/**
	 * Name of bean SDL property with the copy constructor method. This
	 * property is attached to the {@link SdlBean} and, if available, should
	 * contains a {@link JavaMethod}.
	 */
	public static final String SDL_PROP_COPY_CONSTRUCTOR = "copy_constructor";
	
	/**
	 * The bean name.
	 */
	private String m_name;
	
	/**
	 * The bean generators.
	 */
	private List<SdlBeanGenerator> m_generators;
	
	/**
	 * The names of the attributes (in order).
	 */
	private List<String> m_attribute_names;
	
	/**
	 * Attributes mapped by their names.
	 */
	private Map<String, SdlAttribute> m_attributes;
	
	/**
	 * Is the bean read only.
	 */
	private boolean m_read_only;
	
	/**
	 * The SDL type that refers to this bean.
	 */
	private SdlType m_type;
	
	/**
	 * Properties for generation.
	 */
	private Map<SdlBeanGenerator, Map<String, String>> m_generator_properties;
	
	
	/**
	 * Creates a new bean.
	 * @param name the bean name
	 * @param pkg the package this bean is in
	 */
	public SdlBean(String name, SdlPackage pkg) {
		Ensure.not_null(name, "name == null");
		m_name = name;
		m_generators = new ArrayList<>();
		m_attribute_names = new ArrayList<>();
		m_attributes = new HashMap<>();
		m_read_only = false;
		m_type = new SdlType(pkg.name() + "." + name);
		m_generator_properties = new HashMap<>();
	}
	
	/**
	 * Marks this bean as being read only.
	 */
	public void set_read_only() {
		m_read_only = true;
	}
	
	/**
	 * Checks whether this bean is a read-only bean.
	 * @return is it read only?
	 */
	public boolean read_only() {
		return m_read_only;
	}
	
	/**
	 * Obtains the bean name.
	 * @return the bean name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Creates a new attribute in this bean.
	 * @param name the attribute name
	 * @param type the attribute type
	 * @return the created attribute
	 */
	public SdlAttribute make_attribute(String name, SdlType type) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(type, "type == null");
		Ensure.is_false(m_attributes.containsKey(name));
		SdlAttribute attr = new SdlAttribute(name, type);
		m_attributes.put(name, attr);
		m_attribute_names.add(name);
		return attr;
	}
	
	/**
	 * Obtains the names of all attributes.
	 * @return the names
	 */
	public List<String> attribute_names() {
		return new ArrayList<>(m_attribute_names);
	}
	
	/**
	 * Obtains an attribute with a name.
	 * @param name the attribute name
	 * @return the attribute, or <code>null</code> if there is none
	 */
	public SdlAttribute attribute(String name) {
		Ensure.not_null(name, "name == null");
		return m_attributes.get(name);
	}
	
	/**
	 * Generates the bean in the given code.
	 * @param jc the code to generate
	 * @param pkg the package where the bean is to be generated
	 * @throws SdlGenerationException failed to generate the SDL code
	 */
	public void generate(JavaCode jc, JavaPackage pkg)
			throws SdlGenerationException {
		Ensure.not_null(jc, "jc == null");
		Ensure.not_null(pkg, "pkg == null");
		
		do {
			int generated_count = 0;
			int cannot_run_count = 0;
			
			StringBuilder sb = new StringBuilder();
			
			for (SdlBeanGenerator g : m_generators) {
				Map<String, String> properties = m_generator_properties.get(g);
				switch (g.generate(this, jc, pkg, properties)) {
				case CANNOT_RUN:
					cannot_run_count++;
					if (sb.length() > 0) {
						sb.append(",");
					}
					
					sb.append(g.getClass().getCanonicalName());
					break;
				case GENERATED_CODE:
					generated_count++;
					break;
				case NOTHING_TO_DO:
					break;
				}
			}
			
			if (generated_count == 0 && cannot_run_count == 0) {
				break;
			}
			
			if (generated_count == 0 && cannot_run_count > 0) {
				throw new SdlGenerationException("Failed to generate bean "
						+ "because some generators were not able to run: "
						+ sb);
			}
		} while (true);
	}
	
	/**
	 * Adds a generator to the bean.
	 * @param g the generator
	 * @param p the generation properties
	 */
	public void add_generator(SdlBeanGenerator g, Map<String, String> p) {
		Ensure.not_null(g, "g == null");
		Ensure.not_null(p, "p == null");
		m_generators.add(g);
		m_generator_properties.put(g,p);
	}
	
	/**
	 * Obtains the SDL type that refers to this bean.
	 * @return the SDL type
	 */
	public SdlType type() {
		return m_type;
	}
}
