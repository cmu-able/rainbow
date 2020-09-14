package incubator.ui.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Class with information of a bean for rendering. If there is any special
 * rule to format columns, it is an instance of this class that is returned
 * by the model and not the property value itself.
 */
public class BeanRendererInfo {
	/**
	 * The bean.
	 */
	private Object m_bean;
	
	/**
	 * The property to show.
	 */
	private String m_property;
	
	/**
	 * The property value.
	 */
	private Object m_value;
	
	/**
	 * Name of the class of use a provider, if any.
	 */
	private String m_provider;
	
	/**
	 * Provider hints.
	 */
	private Map<String, String> m_hints;
	
	/**
	 * Creates new rendering information.
	 * @param bean the bean to render
	 * @param property the property to render 
	 * @param value the property value
	 */
	public BeanRendererInfo(Object bean, String property, Object value) {
		if (bean == null) {
			throw new IllegalArgumentException("bean == null");
		}
		
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}
		
		this.m_bean = bean;
		this.m_property = property;
		this.m_value = value;
		this.m_provider = null;
		this.m_hints = new HashMap<>();
	}
	
	/**
	 * Obtains a reference to the bean.
	 * @return the bean
	 */
	public Object bean() {
		return m_bean;
	}
	
	/**
	 * Obtains the name of the property to show.
	 * @return the property name
	 */
	public String getProperty() {
		return m_property;
	}
	
	/**
	 * Obtains the value to show.
	 * @return the value
	 */
	public Object value() {
		return m_value;
	}
	
	/**
	 * Set the class to use a provider.
	 * @param provider the name of the class to use as a provider. It should
	 * be a subclass of {@link BeanTableComponentProvider}
	 */
	public void provider(String provider) {
		m_provider = provider;
	}
	
	/**
	 * Obtains the provider to use.
	 * @return the provider name. It may be <code>null</code> if no provider
	 * has been defined.
	 */
	public String provider() {
		return m_provider;
	}
	
	/**
	 * Adds a hint.
	 * @param key the hint key
	 * @param value the hint value (<code>null</code> removes the hint)
	 */
	public void add_hint(String key, String value) {
		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}
		
		if (value == null) {
			m_hints.remove(key);
		} else {
			m_hints.put(key, value);
		}
	}
	
	/**
	 * Obtains a hint value.
	 * @param key the hint key
	 * @return the hint value, <code>null</code> if none
	 */
	public String hint(String key) {
		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}
		
		return m_hints.get(key);
	}
}
