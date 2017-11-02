package incubator.ui.bean;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

/**
 * Provider that shows a progress bar. There are 5 hints that can be defined:
 * um that contains the name of a boolean property indicating whether the
 * bar should be visible or invisible, another property indicating whether
 * the bar is defined (<code>true</code>) or undefined (<code>false</code>).
 * Two other contains the total value and current value of progress. A
 * fifth hint contains the text to show inside the progress bar.
 */
public class ProgressBarComponentProvider
		implements BeanTableComponentProvider {
	/**
	 * Name of the property that decides bar visibility.
	 */
	public static final String HINT_VISIBLE = "visible-property";
	
	/**
	 * Name of the property that decides whether the progress bar is
	 * defined or undefined.
	 */
	public static final String HINT_INDETERMINATE = "indeterminate-property";
	
	/**
	 * Hint with the name of the property deciding whether we show text or
	 * not.
	 */
	public static final String HINT_SHOW_TEXT = "show-text";
	
	/**
	 * Name of the property that defines the maximum progress.
	 */
	public static final String HINT_TOTAL = "total-property";
	
	/**
	 * Name of the property that defines current progress.
	 */
	public static final String HINT_CURRENT = "current-property";
	
	/**
	 * Name of the property that defines the progress bar text.
	 */
	public static final String HINT_TEXT = "text-property";
	
	/**
	 * The progress bar to show.
	 */
	private JProgressBar m_progress;

	/**
	 * Label that we use when the progress bar is invisible.
	 */
	private JLabel m_empty;
	
	/**
	 * Creates a new provider.
	 */
	public ProgressBarComponentProvider() {
		m_progress = new JProgressBar();
		m_progress.setOpaque(true);
		m_empty = new JLabel("");
		m_empty.setOpaque(true);
	}
	
	@Override
	public Component getComponentForBean(JTable table, BeanRendererInfo info,
			boolean is_selected, boolean has_focus, int row, int column) {
		if (!read_boolean_hint(info, HINT_VISIBLE, true)) {
			return m_empty;
		}
		
		boolean ind = read_boolean_hint(info, HINT_INDETERMINATE, false);
		m_progress.setIndeterminate(ind);
		
		int max = 0;
		int val = 0;
		if (!ind) {
			max = read_int_hint(info, HINT_TOTAL, 0);
			val = read_int_hint(info, HINT_CURRENT, 0);
			m_progress.setMinimum(0);
			m_progress.setMaximum(max);
			m_progress.setValue(val);
		}
		
		if ("true".equals(info.hint(HINT_SHOW_TEXT)) ) {
			m_progress.setStringPainted(true);
			
			String text = read_string_hint(info, HINT_TEXT, val + " / " + max);
			m_progress.setString(text);
		} else {
			m_progress.setStringPainted(false);
		}
		
		return m_progress;
	}
	
	/**
	 * Obtains a hint value that represents a boolean value.
	 * @param info rendering information
	 * @param hint_name hint name
	 * @param default_value default value
	 * @return the value
	 */
	private boolean read_boolean_hint(BeanRendererInfo info, String hint_name,
			boolean default_value) {
		assert info != null;
		assert hint_name != null;

		String prop = info.hint(hint_name);
		if (prop == null) {
			return default_value;
		}
		
		try {
			Object bean = info.bean();
			Class<?> cls = bean.getClass();
			String pname = "is" + Character.toUpperCase(prop.charAt(0))
					+ prop.substring(1);
			Method m = cls.getMethod (pname);
			if (m == null) {
				return default_value;
			}

			Object r = m.invoke (bean);
			if (r == null || !(r instanceof Boolean)) {
				return default_value;
			}
			
			return ((Boolean) r).booleanValue();
		} catch (Exception e) {
			return default_value;
		}
	}
	
	/**
	 * Obtains a hint value that represents an integer value.
	 * @param info rendering information
	 * @param hint_name hint name
	 * @param default_value default value
	 * @return the value
	 */
	private int read_int_hint(BeanRendererInfo info, String hint_name,
			int default_value) {
		assert info != null;
		assert hint_name != null;

		String prop = info.hint(hint_name);
		if (prop == null) {
			return default_value;
		}
		
		try {
			Object bean = info.bean();
			Class<?> cls = bean.getClass();
			String pname = "get" + Character.toUpperCase(prop.charAt(0))
					+ prop.substring(1);
			Method m = cls.getMethod (pname);
			if (m == null) {
				return default_value;
			}

			Object r = m.invoke (bean);
			if (r == null || !(r instanceof Integer)) {
				return default_value;
			}
			
			return ((Integer) r).intValue();
		} catch (Exception e) {
			return default_value;
		}
	}
	
	/**
	 * Obtains a hint value that represents a string value.
	 * @param info rendering information
	 * @param hint_name hint name
	 * @param default_value default value
	 * @return the value
	 */
	private String read_string_hint(BeanRendererInfo info, String hint_name,
			String default_value) {
		assert info != null;
		assert hint_name != null;

		String prop = info.hint(hint_name);
		if (prop == null) {
			return default_value;
		}
		
		try {
			Object bean = info.bean();
			Class<?> cls = bean.getClass();
			String pname = "get" + Character.toUpperCase(prop.charAt(0))
					+ prop.substring(1);
			Method m = cls.getMethod (pname);
			if (m == null) {
				return default_value;
			}

			Object r = m.invoke (bean);
			if (r == null || !(r instanceof String)) {
				return default_value;
			}
			
			return (String) r;
		} catch (Exception e) {
			return default_value;
		}
	}
}
