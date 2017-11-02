package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.List;

import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Abstract data value of a collection of data values.
 */
public abstract class CollectionDataValue extends DataValue {
	/**
	 * Creates a new value.
	 * @param type the type this value belongs to
	 */
	protected CollectionDataValue(DataType type) {
		super(type);
	}
	
	/**
	 * Obtains a snapshot of all data values in the collection.
	 * @return a list with all data values; the order of the data values may be
	 * defined or not, depending on the type of collection
	 */
	public abstract List<DataValue> snapshot();
	
	/**
	 * Sets the contents of the collection. This method is equivalent to
	 * invoke {@link #add(DataValue)} to all values, one by one, which means
	 * that not all values will necessarily exist in the collection: for
	 * example, sets will remove duplicate elements.
	 * @param values the values to place inside the collection
	 */
	public void set_contents(List<DataValue> values) {
		Ensure.not_null(values);
		for (DataValue v : values) {
			add(v);
		}
	}
	
	/**
	 * Adds a value to a collection. The exact effects may vary according to
	 * collection type.
	 * @param value the value to add which cannot be <code>null</code>
	 * @return was the value added or was adding ignored?
	 */
	public abstract boolean add(DataValue value);
}
