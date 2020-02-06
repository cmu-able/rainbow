package incubator.ui.bean;

import java.awt.Component;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Provider that formats a number as decimal. The property value should be
 * a <code>java.lang.Number</code>.
 */
public class DecimalFormatComponentProvider
		implements BeanTableComponentProvider {
	/**
	 * Hint with the number format (see <code>java.text.DecimalFormat</code>).
	 */
	public static final String HINT_FORMAT = "format";
	
	/**
	 * Label used to format the number.
	 */
	private JLabel m_label;

	/**
	 * Maps the formats in the classes that actually format.
	 */
	private Map<String, DecimalFormat> m_formats;
	
	/**
	 * Creates a new provider.
	 */
	public DecimalFormatComponentProvider() {
		m_label = new JLabel();
		m_label.setOpaque(true);
		m_formats = new HashMap<>();
	}
	
	@Override
	public Component getComponentForBean(JTable table, BeanRendererInfo info,
			boolean is_selected, boolean has_focus, int row, int column) {
		m_label.setText("");
		if (info.value() != null && info.value() instanceof Number) {
			String format = info.hint(HINT_FORMAT);
			if (format == null) {
				format = "0.000";
			}
			
			DecimalFormat nf = m_formats.get(format);
			if (nf == null) {
				nf = new DecimalFormat(format);
				m_formats.put(format, nf);
			}
			
			double val = ((Number) info.value()).doubleValue();
			m_label.setText(nf.format(val));
		}
		
		ProviderUtils.set_text_selection_colors(table, m_label, is_selected);
		return m_label;
	}
}
