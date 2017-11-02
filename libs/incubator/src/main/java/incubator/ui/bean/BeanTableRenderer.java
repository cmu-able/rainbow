package incubator.ui.bean;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer used in bean tables.
 */
public class BeanTableRenderer extends DefaultTableCellRenderer {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Maps class names followed by "::" followed by the column name into a
	 * provider.
	 */
	private Map<String, BeanTableComponentProvider> m_providers;
	
	/**
	 * Builds a new renderer.
	 */
	public BeanTableRenderer() {
		m_providers = new HashMap<>();
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean is_selected, boolean has_focus, int row, int column) {
		Component component = null;
		
		if (value != null && value instanceof BeanRendererInfo) {
			BeanTableComponentProvider provider;
			provider = provider(((BeanRendererInfo) value), column);
			if (provider == null) {
				value = ((BeanRendererInfo) value).value();
			} else {
				component = provider.getComponentForBean(table,
						(BeanRendererInfo) value, is_selected, has_focus, row,
						column);
			}
		}
		
		if (component == null) {
			component = super.getTableCellRendererComponent(table, value,
					is_selected, has_focus, row, column);
		}
		
		return component;
	}
	
	/**
	 * Obtains the provider associated with the given information.
	 * @param info rendering information
	 * @param column column number
	 * @return the provider or <code>null</code> if none
	 */
	private BeanTableComponentProvider provider(BeanRendererInfo info,
			int column) {
		String providerName = info.provider();
		if (providerName == null) {
			return null;
		}
		
		providerName = providerName + "::" + column;
		
		BeanTableComponentProvider provider;
		provider = m_providers.get(providerName);
		if (provider != null) {
			return provider;
		}
		
		Class<?> providerClass;
		try {
			providerClass = Class.forName(info.provider());
		} catch (Exception e) {
			return null;
		}
		
		try {
			provider = (BeanTableComponentProvider) providerClass.newInstance();
		} catch (Exception e) {
			return null;
		}
		
		m_providers.put(providerName, provider);
		return provider;
	}
}
