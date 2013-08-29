package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Data value containing an ordered set of other types.
 */
public class TupleDataValue extends DataValue {
	/**
	 * Tuple data.
	 */
	private List<DataValue> m_data;
	
	/**
	 * Creates a new tuple.
	 * @param type the type
	 * @param data the tuple data
	 */
	TupleDataValue(TupleDataType type, List<DataValue> data) {
		super(type);
		Ensure.notNull(data);
		m_data = new ArrayList<>(data);
	}
	
	/**
	 * Obtains all values in the tuple.
	 * @return all values
	 */
	public List<DataValue> data() {
		return new ArrayList<>(m_data);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_data == null) ? 0 : m_data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TupleDataValue other = (TupleDataValue) obj;
		if (m_data == null) {
			if (other.m_data != null)
				return false;
		} else if (!m_data.equals(other.m_data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		boolean first = true;
		for (DataValue v : m_data) {
			if (!first) {
				sb.append(',');
			} else {
				first = false;
			}
			
			sb.append(v);
		}
		
		sb.append('>');
		
		return sb.toString();
	}
	
	@Override
	public TupleDataValue clone() throws CloneNotSupportedException {
		List<DataValue> values = new ArrayList<>();
		for (DataValue v : m_data) {
			values.add(v.clone());
		}
		
		return new TupleDataValue((TupleDataType) type(), values);
	}
}
