package incubator.bpref;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.ClassUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Class capable of saving beans to preferences and populating beans from
 * preference values.
 */
public class BeanPreference {
	/**
	 * Separates bean prefixes from the property names.
	 */
	public static final String PREFIX_SEPARATOR = ":";

	/**
	 * Handlers for each class type.
	 */
	private static Map<Class<?>, PropertyHandler<?>> handlers;

	/*
	 * Static initialization: populates the handlers variable.
	 */
	static {
		handlers = new HashMap<>();
		handlers.put(String.class, new StringPropertyHandler());
		handlers.put(Integer.class, new IntegerPropertyHandler());
		handlers.put(Boolean.class, new BooleanPropertyHandler());
	}

	/**
	 * Creates a new bean preference.
	 */
	public BeanPreference() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Iterates all properties on the bean invoking the runner for each one of
	 * them.
	 * 
	 * @param prefs the preferences where to save
	 * @param bean the bean
	 * @param prefix an optional prefix for all bean properties
	 * @param runner the object to invoke for each bean property
	 * 
	 * @throws Exception failed to invoke
	 */
	private void doForEachProperty(Preferences prefs, Object bean,
			String prefix, PropertyRunner runner) throws Exception {
		BeanInfo info = Introspector.getBeanInfo(bean.getClass());
		PropertyDescriptor[] descriptors = info.getPropertyDescriptors();

		for (PropertyDescriptor pd : descriptors) {
			if (pd.getReadMethod() == null || pd.getWriteMethod() == null) {
				continue;
			}

			Class<?> type = pd.getPropertyType();
			if (type == null) {
				throw new Exception("Property '" + pd.getName() + "' has no "
						+ "type.");
			}

			type = ClassUtils.primitiveToWrapper(type);
			PropertyHandler<?> ph = handlers.get(type);
			if (ph == null) {
				throw new Exception("No property handler for property of type "
						+ "'" + pd.getName() + "' (type: " + type.getName()
						+ "')");
			}

			String pk = (prefix != null ? prefix + PREFIX_SEPARATOR : "")
					+ pd.getName();
			runner.run(prefs, bean, pk, pd.getName(), ph);
		}
	}

	/**
	 * Saves a bean in a preferences node.
	 * 
	 * @param prefs the preferences node
	 * @param bean the bean to save
	 * @param prefix an optional prefix for bean properties
	 * 
	 * @throws Exception failed to save
	 */
	public void saveInNode(Preferences prefs, Object bean, String prefix)
			throws Exception {
		if (prefs == null) {
			throw new IllegalArgumentException("prefs == null");
		}

		if (bean == null) {
			throw new IllegalArgumentException("bean == null");
		}

		doForEachProperty(prefs, bean, prefix, new PropertyRunner() {
			@Override
			public void run(Preferences prefs, Object bean, String pkey,
					String pname, PropertyHandler<?> handler) throws Exception {
				PropertyUtilsBean pub = BeanUtilsBean.getInstance()
						.getPropertyUtils();
				handler.save(prefs, pkey, pub.getSimpleProperty(bean, pname));
			}
		});
	}

	/**
	 * Populates a bean from a preferences node.
	 * 
	 * @param prefs the preferences node
	 * @param bean the bean to populate
	 * @param prefix an optional prefix for bean properties
	 * 
	 * @throws Exception failed to load
	 */
	public void loadFromNode(Preferences prefs, Object bean, String prefix)
			throws Exception {
		if (prefs == null) {
			throw new IllegalArgumentException("prefs == null");
		}

		if (bean == null) {
			throw new IllegalArgumentException("bean == null");
		}

		doForEachProperty(prefs, bean, prefix, new PropertyRunner() {
			@Override
			public void run(Preferences prefs, Object bean, String pkey,
					String pname, PropertyHandler<?> handler) throws Exception {
				BeanUtils.setProperty(bean, pname, handler.read(prefs, pkey));
			}
		});
	}

	/**
	 * Interface that applies some operation to a bean property.
	 */
	private interface PropertyRunner {
		/**
		 * Performs the operation.
		 * 
		 * @param prefs the preferences node
		 * @param bean the bean
		 * @param pkey the property key in the preferences node
		 * @param pname the property name
		 * @param handler the property handler
		 * 
		 * @throws Exception failed to perform the operation
		 */
		void run (Preferences prefs, Object bean, String pkey,
				  String pname, PropertyHandler<?> handler) throws Exception;
	}
}
