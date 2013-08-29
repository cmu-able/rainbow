package edu.cmu.cs.able.typelib.struct;

import incubator.pval.Ensure;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * The value of a structure.
 */
public class StructureDataValue extends DataValue {
	/**
	 * Maps fields to values.
	 */
	private Map<Field, DataValue> m_values;
	
	/**
	 * Creates a new structure data value.
	 * @param type the structure data type
	 * @param values the values to set; all fields must have values defined
	 */
	protected StructureDataValue(StructureDataType type,
			Map<Field, DataValue> values) {
		super(type);
		Ensure.not_null(values);
		Ensure.is_false(type.is_abstract());
		
		Set<Field> fields = type.fields();
		m_values = new HashMap<>();
		
		/*
		 * Check that we only defined values for fields that belong to
		 * the structure.
		 */
		for (Field f : values.keySet()) {
			Ensure.is_true(fields.contains(f));
			Ensure.not_null(values.get(f));
			DataType field_type = f.description().type();
			Ensure.is_true(field_type.is_instance(values.get(f)));
			m_values.put(f, values.get(f));
		}
		
		/*
		 * Check that all fields have values.
		 */
		for (Field f : fields) {
			Ensure.is_true(values.containsKey(f));
		}
	}
	
	/**
	 * Obtains the value of a field.
	 * @param f the field
	 * @return the value of the field 
	 */
	public DataValue value(Field f) {
		Ensure.not_null(f);
		Ensure.is_true(f.structure() == type()
				|| f.structure().super_of(type()));
		DataValue v = m_values.get(f);
		Ensure.not_null(v);
		return v;
	}
	
	/**
	 * Sets the value of a field.
	 * @param f the field
	 * @param v the value to set; cannot be <code>null</code>
	 */
	public void value(Field f, DataValue v) {
		Ensure.not_null(f);
		Ensure.is_true(f.structure() == type()
				|| f.structure().super_of(type()));
		Ensure.not_null(v);
		Ensure.is_true(f.description().type().is_instance(v));
		m_values.put(f, v);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((m_values == null) ? 0 : m_values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StructureDataValue other = (StructureDataValue) obj;
		if (m_values == null) {
			if (other.m_values != null)
				return false;
		} else if (!m_values.equals(other.m_values))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		b.append(type().name() + "{");
		boolean first = true;
		
		TreeSet<String> sorted = new TreeSet<>();
		for (Field f : m_values.keySet()) {
			String f_name_val = f.structure().name() + "::" + f.name()
					+ "=" + m_values.get(f);
			sorted.add(f_name_val);
		}
		
		
		for (String s : sorted) {
			if (!first) {
				b.append(",");
			} else {
				first = false;
			}
			
			b.append(s);
		}
		
		b.append("}");
		
		return b.toString();
	}
	
	@Override
	public StructureDataValue clone() throws CloneNotSupportedException {
		StructureDataType stype = (StructureDataType) type();
		
		Map<Field, DataValue> fvalues = new HashMap<>();
		for (Field f : stype.fields()) {
			fvalues.put(f, value(f).clone());
		}
		
		return new StructureDataValue(stype, fvalues);
	}
}
