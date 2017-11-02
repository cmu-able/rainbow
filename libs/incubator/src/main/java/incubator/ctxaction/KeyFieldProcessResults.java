package incubator.ctxaction;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Structure which keeps some information on the process of filling keys in
 * objects. There are five types of data kept by this class: coersion
 * failures (incompatible types), <code>null</code> values in context but
 * whose keys are primitive types, successfully filled in values with non
 * <code>null</code> values, successfully filled in values with
 * <code>null</code> values and fields whose values have changed.
 */
public class KeyFieldProcessResults {
	/**
	 * Names of fields for which coersion failed.
	 */
	private Set<String> coersionFailed;
	
	/**
	 * Names of fields which have primitive types but have <code>null</code>
	 * values.
	 */
	private Set<String> primitiveNulls;
	
	/**
	 * Names of fields whose filling without <code>null</code> was successful.
	 */
	private Set<String> successNonNull;
	
	/**
	 * Names of fields whose filling with <code>null</code> was successful.
	 */
	private Set<String> successNull;
	
	/**
	 * Names of fields whose value has changed.
	 */
	private Set<String> changed;
	
	/**
	 * Creates a new structure.
	 */
	KeyFieldProcessResults() {
		coersionFailed = new HashSet<>();
		primitiveNulls = new HashSet<>();
		successNonNull = new HashSet<>();
		successNull = new HashSet<>();
		changed = new HashSet<>();
	}
	
	/**
	 * Adds information that coersion for a filed failed.
	 * 
	 * @param field the field name
	 */
	void addCoersionFailed(Field field) {
		assert field != null;
		
		coersionFailed.add(field.getName());
	}
	
	/**
	 * Adds information that a <code>null</code> in context could not be
	 * mapped to a field because the field has a primitive value.
	 * 
	 * @param field the field name
	 */
	void addPrimitiveNull(Field field) {
		assert field != null;
		
		primitiveNulls.add(field.getName());
	}
	
	/**
	 * Adds information that a field has been successfully filled in with a
	 * non-<code>null</code> value.
	 * 
	 * @param field the field
	 */
	void addSuccessNonNull(Field field) {
		assert field != null;
		
		successNonNull.add(field.getName());
	}
	
	/**
	 * Adds information that a field has been successfully filled in with a
	 * <code>null</code> value.
	 * 
	 * @param field the field
	 */
	void addSuccessNull(Field field) {
		assert field != null;
		
		successNull.add(field.getName());
	}
	
	/**
	 * Adds information about a field whose value has changed.
	 * 
	 * @param field the field
	 */
	void addChanged(Field field) {
		assert field != null;
		
		changed.add(field.getName());
	}
	
	/**
	 * Determines if any errors have happened during processing.
	 * 
	 * @return have any errors happed (failed coersions or <code>null</code>
	 * in primitive fields)?
	 */
	public boolean hasErrors() {
		return coersionFailed.size() > 0 || primitiveNulls.size() > 0;
	}
	
	/**
	 * Obtains the names of fields whose type coersion have failed.
	 *  
	 * @return the names of fields
	 */
	public Set<String> getCoersionFailed() {
		return Collections.unmodifiableSet(new HashSet<>(coersionFailed));
	}
	
	/**
	 * Obtains the names of fields that are primitive types but there is no
	 * value in context.
	 *  
	 * @return the names of fields
	 */
	public Set<String> getPrimitiveNulls() {
		return Collections.unmodifiableSet(new HashSet<>(primitiveNulls));
	}
	
	/**
	 * Obtains the names of the fields that have been filled in with non
	 * <code>null</code> values.
	 * 
	 * @return the names of fields
	 */
	public Set<String> getSuccessNonNull() {
		return Collections.unmodifiableSet(new HashSet<>(successNonNull));
	}
	
	/**
	 * Obtains the names of the fields that have been filled in with
	 * <code>null</code> values.
	 * 
	 * @return the names of fields
	 */
	public Set<String> getSuccessNull() {
		return Collections.unmodifiableSet(new HashSet<>(successNull));
	}
	
	/**
	 * Obtains the names of the fields whose value has changed.
	 * 
	 * @return the names of fields
	 */
	public Set<String> getChanged() {
		return Collections.unmodifiableSet(new HashSet<>(changed));
	}
}
