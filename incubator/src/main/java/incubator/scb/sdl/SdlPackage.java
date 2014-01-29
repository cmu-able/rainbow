package incubator.scb.sdl;

import incubator.Pair;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaPackage;
import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Definition of a package in SDL.
 */
public class SdlPackage {
	/**
	 * The package name.
	 */
	private String m_name;
	
	/**
	 * The package beans.
	 */
	private Map<String, SdlBean> m_beans;
	
	/**
	 * Creates a new package.
	 * @param name the package name
	 */
	public SdlPackage(String name) {
		Ensure.not_null(name, "name == null");
		m_name = name;
		m_beans = new HashMap<>();
	}
	
	/**
	 * Obtains the package name.
	 * @return the package name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Obtains the names of all beans in the package.
	 * @return all beans
	 */
	public Set<String> bean_names() {
		return new HashSet<>(m_beans.keySet());
	}
	
	/**
	 * Obtains the bean with a given name.
	 * @param name the bean name
	 * @return the bean or <code>null</code> if none
	 */
	public SdlBean bean(String name) {
		Ensure.not_null(name, "name == null");
		return m_beans.get(name);
	}
	
	/**
	 * Adds a bean to the package.
	 * @param b the bean
	 */
	public void add_bean(SdlBean b) {
		Ensure.not_null(b, "b == null");
		Ensure.is_false(m_beans.containsKey(b.name()));
		m_beans.put(b.name(), b);
	}
	
	/**
	 * Generates <em>java</em> code for this package.
	 * @param jc where to generate the java code
	 * @return the generation info
	 * @throws SdlGenerationException failed to generate the SDL code
	 */
	public GenerationInfo generate(JavaCode jc) throws SdlGenerationException {
		Ensure.not_null(jc, "jc == null");
		
		boolean generated = false;
		
		JavaPackage jp = jc.pkg(m_name);
		if (jp == null) {
			String n = m_name;
			Pair<String, String> r;
			do {
				r = JavaPackage.split_1_fqn(n);
				if (jp == null) {
					jp = jc.pkg(r.first());
					if (jp == null) {
						jp = jc.make_package(r.first());
						generated = true;
					}
				} else {
					JavaPackage jpc = jp.child(r.first());
					if (jpc == null) {
						jpc = jp.make_child(r.first());
						generated = true;
					}
					
					jp = jpc;
				}
				
				n = r.second();
			} while (r.second() != null);
		}
		
		List<Generator> gens = new ArrayList<>();
		for (final SdlBean b : m_beans.values()) {
			final JavaPackage jpp = jp;
			final JavaCode jcc = jc;
			gens.add(new Generator() {
				@Override
				public GenerationInfo generate() throws SdlGenerationException {
					return b.generate(jcc, jpp);
				}
			});
		}
		
		GenerationInfo gi = GenerationAlgorithm.generate(gens);
		if (gi.result() == GenerationResult.NOTHING_TO_DO && generated) {
			return new GenerationInfo(GenerationResult.GENERATED_CODE);
		} else {
			return gi;
		}
	}
}
