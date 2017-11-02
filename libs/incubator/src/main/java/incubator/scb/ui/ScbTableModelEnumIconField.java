package incubator.scb.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import incubator.pval.Ensure;
import incubator.scb.ScbField;

/**
 * SCB table model field that shows an enumeration as an icon.
 * @param <T> the type of SCB
 * @param <E> the enumeration type
 */
public class ScbTableModelEnumIconField<T, E extends Enum<E>>
		extends ScbTableModelField<T, E, ScbField<T, E>> {
	/**
	 * Maps enumeration items to icons.
	 */
	private Map<E, Icon> m_icons;
	
	/**
	 * Creates a new field.
	 * @param field the field that obtains the enumeration
	 * @param icons the icons to show; enumeration values which have no
	 * corresponding icon are shown blank; if the enumeration value itself
	 * is <code>null</code>, blank is also shown
	 */
	public ScbTableModelEnumIconField(ScbField<T, E> field,
			Map<E, Icon> icons) {
		super(field);
		
		Ensure.not_null(icons, "icons == null");
		m_icons = new HashMap<>(icons);
	}
	
	@Override
	public Object display_object(T obj) {
		Ensure.not_null(obj);
		
		E e = cof().get(obj);
		Icon i = null;
		if (e == null || ((i = m_icons.get(e)) == null)) {
			return null;
		}
		
		return i;
	}
}
