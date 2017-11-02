package incubator.scb.ui;

import incubator.pval.Ensure;
import incubator.scb.ScbField;
import incubator.ui.IconResourceLoader;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

/**
 * SCB table model field that shows icons for enumeration values. The
 * images for the enumeration are obtained from resources loaded using
 * {@link IconResourceLoader}. Each enumeration value should have a name
 * which is the enumeration class (no package) followed by a dash and
 * followed by the enumeration value. All images are expected to be PNGs
 * with a <code>.png</code> suffix.
 * @param <E> the enumeration type
 * @param <T> the object type
 */
public class ScbTableModelEnumIrlField<T, E extends Enum<E>>
		extends ScbTableModelEnumIconField<T, E> {
	/**
	 * Resource suffix.
	 */
	private static final String SUFFIX = ".png";
	
	/**
	 * Creates a new field.
	 * @param field the field to extract the enumeration value from the
	 * object
	 * @param locator class used to locate icons (see
	 * {@link IconResourceLoader})
	 */
	public ScbTableModelEnumIrlField(ScbField<T, E> field, Class<?> locator) {
		super(field, load_icons(Ensure.not_null(
				field, "field == null").value_type(), locator));
	}
	
	/**
	 * Loads the icons corresponding to an enumeration.
	 * @param e_cls the enumeration class
	 * @param locator the class used to locate the icons
	 * @param <E> the enumeration type
	 * @return the map with the loaded icons
	 */
	private static <E extends Enum<E>> Map<E, Icon> load_icons(Class<E> e_cls,
			Class<?> locator) {
		Ensure.not_null(e_cls, "e_cls == null");
		Ensure.not_null(locator, "locator == null");
		
		String pfx = e_cls.getSimpleName() + "-";
		
		Map<E, Icon> m = new HashMap<>();
		for (E e : e_cls.getEnumConstants()) {
			Icon i = IconResourceLoader.loadIcon(locator, pfx + e.name()
					+ SUFFIX);
			if (i != null) {
				m.put(e, i);
			}
		}
		
		return m;
	}
}
