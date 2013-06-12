package incubator.bpref;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Class that keeps a bean synchronized with a preference (it will save the
 * preference as soon as the bean is changed).
 */
public class BeanPrefsSynchronizer {
	/**
	 * Logger to use.
	 */
	private static final Logger LOG = Logger.getLogger(
			BeanPrefsSynchronizer.class);
	
	/**
	 * Creates a new synchronizer (see {@link BeanPreference} for more details
	 * on the arguments).
	 * 
	 * @param prefs the preferences
	 * @param bean the bean
	 * @param prefix the prefix
	 * 
	 * @throws BeanPrefsSynchronizerException failed to initialize the
	 * synchronizer
	 */
	public BeanPrefsSynchronizer(final Preferences prefs, final Object bean,
			final String prefix) throws BeanPrefsSynchronizerException {
		if (prefs == null) {
			throw new IllegalArgumentException("prefs == null");
		}
		
		if (bean == null) {
			throw new IllegalArgumentException("bean == null");
		}
		
		try {
			Method m = bean.getClass().getMethod("addPropertyChangeListener",
					PropertyChangeListener.class);
			if (m == null) {
				throw new BeanPrefsSynchronizerException("No method "
						+ "'addPropertyChangeListener(PropertyChangeListener)' "
						+ "found in class '"
						+ bean.getClass().getCanonicalName() + "'.");
			}
			
			m.invoke(bean, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent pce) {
					try {
						new BeanPreference().saveInNode(prefs, bean, prefix);
					} catch (Exception e) {
						LOG.error("Failed to update preferences.", e);
					}
				}
			});
		} catch (Exception e) {
			throw new BeanPrefsSynchronizerException("Failed to invoke '"
					+ "addPropertyChangeListener(PropertyChangeListener)' in "
					+ "class '" + bean.getClass().getCanonicalName() + "'.", e);
		}
	}
}
