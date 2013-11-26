package incubator.scb.sdl;

import incubator.pval.Ensure;
import incubator.scb.sdl.generators.AttributesAsFieldsGenerator;
import incubator.scb.sdl.generators.BasicScbGenerator;
import incubator.scb.sdl.generators.ClassBeanGenerator;
import incubator.scb.sdl.generators.CopyConstructorGenerator;
import incubator.scb.sdl.generators.CowGenerator;
import incubator.scb.sdl.generators.HashcodeEqualsGenerator;
import incubator.scb.sdl.generators.MergeableScbGenerator;
import incubator.scb.sdl.generators.SimpleAttributeAccessorsGenerator;
import incubator.scb.sdl.generators.SimpleConstructorGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry that keeps track of which generators are known.
 */
public class GeneratorRegistry {
	/**
	 * All bean generators.
	 */
	private Map<String, SdlBeanGenerator> m_bean_generators;
	
	/**
	 * Creates a new registry.
	 */
	public GeneratorRegistry() {
		m_bean_generators = new HashMap<>();
		add_generator(ClassBeanGenerator.NAME, new ClassBeanGenerator());
		add_generator(AttributesAsFieldsGenerator.NAME,
				new AttributesAsFieldsGenerator());
		add_generator(SimpleConstructorGenerator.NAME, 
				new SimpleConstructorGenerator());
		add_generator(CopyConstructorGenerator.NAME,
				new CopyConstructorGenerator());
		add_generator(SimpleAttributeAccessorsGenerator.NAME,
				new SimpleAttributeAccessorsGenerator());
		add_generator(BasicScbGenerator.NAME, new BasicScbGenerator());
		add_generator(CowGenerator.NAME, new CowGenerator());
		add_generator(MergeableScbGenerator.NAME, new MergeableScbGenerator());
		add_generator(HashcodeEqualsGenerator.NAME,
				new HashcodeEqualsGenerator());
	}
	
	/**
	 * Adds a generator to the registry.
	 * @param name the generator name
	 * @param g the generator
	 */
	public void add_generator(String name, SdlBeanGenerator g) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(g, "g == null");
		Ensure.is_false(m_bean_generators.containsKey(name));
		m_bean_generators.put(name, g);
	}
	
	/**
	 * Obtains the bean generator with a given name.
	 * @param name the bean generator name
	 * @return the generator
	 */
	public SdlBeanGenerator bean_generator(String name) {
		Ensure.not_null(name, "name == null");
		return m_bean_generators.get(name);
	}
}
