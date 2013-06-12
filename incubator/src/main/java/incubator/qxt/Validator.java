package incubator.qxt;

/**
 * Interface implemented by objects that validate if a bean being edited is
 * valid.
 * 
 * @param <T> the bean type
 * 
 * @see QxtTable#setValidator(Validator)
 */
public interface Validator<T> {
	/**
	 * Is the object valid?
	 * 
	 * @param t the object to test
	 * 
	 * @return is valid?
	 */
	boolean isValid(T t);
}
