package incubator.qxt;

/**
 * Interface representing a data transformation.
 * 
 * @param <T> the source type
 * @param <R> the destination type
 */
public interface TypeTransformation<T, R> {
	/**
	 * Performs the transformation.
	 * 
	 * @param t the value to transform
	 * 
	 * @return the transformed type
	 */
	R transform (T t);
}
