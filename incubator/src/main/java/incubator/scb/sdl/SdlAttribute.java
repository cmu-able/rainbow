package incubator.scb.sdl;

import java.util.ArrayList;
import java.util.List;

import incubator.pval.Ensure;

/**
 * Class representing an SDL attribute.
 */
public class SdlAttribute extends PropertyObject {
	/**
	 * Attribute name.
	 */
	private String m_name;
	
	/**
	 * Attribute type.
	 */
	private SdlType m_type;
	
	/**
	 * Attribute invariants.
	 */
	private List<SdlAttributeInvariant> m_invariants;
	
	/**
	 * Creates a new attribute.
	 * @param name the attribute name
	 * @param type the attribute type
	 */
	public SdlAttribute(String name, SdlType type) {
		Ensure.not_null(name, "name == null");
		Ensure.not_null(type, "type == null");
		
		m_name = name;
		m_type = type;
		m_invariants = new ArrayList<>();
	}
	
	/**
	 * Obtains the attribute name.
	 * @return the attribute name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Obtains the attribute type.
	 * @return the attribute type
	 */
	public SdlType type() {
		return m_type;
	}
	
	/**
	 * Adds an attribute invariant.
	 * @param i the invariant
	 */
	public void add_invariant(SdlAttributeInvariant i) {
		Ensure.not_null(i, "i == null");
		m_invariants.add(i);
	}
	
	/**
	 * Obtains all attribute invariants.
	 * @return all invariants
	 */
	public List<SdlAttributeInvariant> invariants() {
		return new ArrayList<>(m_invariants);
	}
	
	/**
	 * Obtains all attribute invariants.
	 * @param v_name the variable name that contains the attrbute
	 * @return the invariants
	 */
	public String generate_invariants(String v_name) {
		Ensure.not_null(v_name, "v_name == null");
		StringBuilder sb = new StringBuilder();
		for (SdlAttributeInvariant i : m_invariants) {
			sb.append(i.generate_check(v_name));
		}
		
		return sb.toString();
	}
}
