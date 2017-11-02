package incubator.scb.sdl;

import incubator.jcodegen.JavaCode;
import incubator.pval.Ensure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class representing an SDL definition.
 */
public class SdlDefinition {
	/**
	 * Packages defined.
	 */
	private Map<String, SdlPackage> m_packages;
	
	/**
	 * Creates a new definition.
	 */
	public SdlDefinition() {
		m_packages = new HashMap<>();
	}
	
	/**
	 * Adds a package to the definition.
	 * @param p the package
	 */
	public void add_package(SdlPackage p) {
		Ensure.not_null(p, "p == null");
		Ensure.is_false(m_packages.containsKey(p.name()));
		m_packages.put(p.name(), p);
	}
	
	/**
	 * Obtains all package names.
	 * @return all package names
	 */
	public Set<String> package_names() {
		return new HashSet<>(m_packages.keySet());
	}
	
	/**
	 * Obtains the package with the given name.
	 * @param name the package name
	 * @return the package or <code>null</code> if none
	 */
	public SdlPackage pkg(String name) {
		Ensure.not_null(name, "name == null");
		return m_packages.get(name);
	}
	
	/**
	 * Generates java code for this SDL definition.
	 * @param jc where to generate code
	 * @throws SdlGenerationException failed to generate the SDL code
	 */
	public void generate(JavaCode jc) throws SdlGenerationException {
		Ensure.not_null(jc, "jc == null");
		for (SdlPackage p : m_packages.values()) {
			GenerationInfo gi = p.generate(jc);
			Ensure.not_null(gi, "gi == null");
			if (gi.result() == GenerationResult.CANNOT_RUN) {
				throw new SdlGenerationException("Failed to generate java "
						+ "code from SDL: " + gi.message());
			}
		}
	}
	
	/**
	 * Finds a bean with its fully qualified name.
	 * @param fqn the fully qualified bean name
	 * @return the bean
	 */
	public SdlBean find_bean(String fqn) {
		Ensure.not_null(fqn, "fqn == null");
		
		int idx = fqn.lastIndexOf('.');
		if (idx == -1) {
			return null;
		}
		
		String p = fqn.substring(0, idx);
		String b = fqn.substring(idx + 1);
		
		SdlPackage pk = pkg(p);
		if (pk == null) {
			return null;
		}
		
		return pk.bean(b);
	}
}
