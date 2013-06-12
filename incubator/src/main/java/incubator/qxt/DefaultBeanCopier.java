package incubator.qxt;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Default implementation of a bean copier. This class tries to use the
 * <code>clone</code> method to copy beans.
 * 
 * @param <T> the bean type
 */
class DefaultBeanCopier<T> implements BeanCopier<T>, Serializable {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bean data type.
	 */
	private transient Class<T> type;

	/**
	 * Creates a new copier.
	 * 
	 * @param clazz the bean class
	 */
	DefaultBeanCopier(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz == null");
		}

		this.type = clazz;
	}

	@Override
	public T copy(T t) {
		try {
			return type.cast(BeanUtils.cloneBean(t));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void revert(T bean, T copy) {
		if (bean == null) {
			throw new IllegalArgumentException("bean == null");
		}

		if (copy == null) {
			throw new IllegalArgumentException("copy == null");
		}

		try {
			BeanUtils.copyProperties(bean, copy);
		} catch (Exception e) {
			/*
			 * Bad luck: you're probably screwing something in the beans...
			 */
		}
	}
}
