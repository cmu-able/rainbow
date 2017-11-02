package incubator.qxt;

/**
 * Interface implemented by classes that are capable of making copies of a bean.
 * 
 * @param <T> the bean type
 */
public interface BeanCopier<T> {
	/**
	 * Copies a bean.
	 * 
	 * @param t the bean to copy (guaranteed to be non-<code>null</code>)
	 * 
	 * @return the bean copy (can return <code>null</code> if the copier was
	 * unable to copy the bean)
	 */
	T copy(T t);

	/**
	 * Reverts a bean to the same state as one of its copies.
	 * 
	 * @param bean the bean to revert
	 * @param copy a previous copy of the bean
	 */
	void revert(T bean, T copy);
}
