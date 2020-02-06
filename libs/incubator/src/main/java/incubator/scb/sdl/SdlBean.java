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
	 * Name of bean SDL property with the default constructor method. This
	 * property is attached to the {@link SdlBean} and, if available, should
	 * contains a {@link JavaMethod}.
	 */
	public static final String SDL_PROP_DEFAULT_CONSTRUCTOR =
			"default_constructor";
	
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
	 * The parent bean.
	 */
	private SdlBean m_parent;
	
	
	/**
	 * Creates a new bean.
	 * @param name the bean name
	 * @param pkg the package this bean is in
	 * @param parent the parent bean, which may be <code>null</code>
	 */
	public SdlBean(String name, SdlPackage pkg, SdlBean parent) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(pkg, "pkg == null");
		m_name = name;
		m_generators = new ArrayList<>();
		m_attribute_names = new ArrayList<>();
		m_attributes = new HashMap<>();
		m_read_only = false;
		m_type = new SdlType(pkg.name() + "." + name);
		m_generator_properties = new HashMap<>();
		m_parent = parent;
	}
	
	/**
	 * Obtains the parent bean.
	 * @return the bean or <code>null</code> if there is none
	 */
	public SdlBean parent() {
		return m_parent;
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
	 * @return the result of generation
	 * @throws SdlGenerationException failed to generate the SDL code
	 */
	public GenerationInfo generate(final JavaCode jc, final JavaPackage pkg)
			throws SdlGenerationException {
		Ensure.not_null(jc, "jc == null");
		Ensure.not_null(pkg, "pkg == null");
		
		List<Generator> gens = new ArrayList<>();
		for (final SdlBeanGenerator g : m_generators) {
			gens.add(new Generator() {
				@Override
				public GenerationInfo generate() throws SdlGenerationException {
					Map<String, String> properties = m_generator_properties.get(
							g);
					return g.generate(SdlBean.this, jc, pkg, properties);
				}
			});
		}
		
		return GenerationAlgorithm.generate(gens);
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
