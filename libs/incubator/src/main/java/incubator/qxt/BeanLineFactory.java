package incubator.qxt;

/**
 * Line factory that handles java beans. Beans as expected to have a no-argument
 * public constructor and require no destruction.
 * 
 * @param <T> the type of bean
 */
public class BeanLineFactory<T> implements LineFactory<T> {
	/**
	 * The bean class.
	 */
	private Class<T> clazz;

	/**
	 * Creates a new line factory.
	 * 
	 * @param clazz the bean class
	 */
	public BeanLineFactory(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz == null");
		}

		this.clazz = clazz;
	}

	@Override
	public void destroyLine(T line) {
		/*
		 * No need to do anything to destroy a bean.
		 */
	}

	@Override
	public T makeLine() {
		try {
			return clazz.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			/*
			 * Note that null must never be returned.
			 */
			throw new RuntimeException(e);
		}
	}
}
