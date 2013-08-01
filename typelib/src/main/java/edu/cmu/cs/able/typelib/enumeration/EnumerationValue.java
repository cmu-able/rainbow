package edu.cmu.cs.able.typelib.enumeration;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Value of an enumeration.
 */
public class EnumerationValue extends DataValue {
	/**
	 * The enumeration value name.
	 */
	private String m_name;
	
	/**
	 * Creates a new value.
	 * @param type the enumeration
	 * @param name the value name
	 */
	EnumerationValue(EnumerationType type, String name) {
		super(Ensure.not_null(type));
		
		Ensure.not_null(name);
		m_name = name;
	}

	/**
	 * Obtains the name of the enumeration value.
	 * @return the name
	 */
	public String name() {
		return m_name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + m_name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public String toString() {
		return type().name() + ":" + m_name;
	}
}
