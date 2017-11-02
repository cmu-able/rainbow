package incubator.qxt;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Property that renders a value as an icon. The value it is based upon is
 * another property. The icon is chosen from a table that maps property values
 * to icons.
 *
 * @param <T> the type of property value
 */
public class QxtIconProperty<T> extends QxtRealProperty<Icon> {
	/**
	 * Maps the property values to the icons to display.
	 */
	private Map<T, Icon> icons;
	
	/**
	 * A reader that will read the property value.
	 */
	private QxtRealProperty<T> propertyReader;
	
	/**
	 * Creates a new icon property.
	 * 
	 * @param name the property name
	 * @param display the title to display
	 * @param propertyClass the class of the property
	 * @param icons a mapping of property values to icons
	 */
	public QxtIconProperty(String name, String display, Class<T> propertyClass,
			Map<T, Icon> icons) {
		super(name, display, true, Icon.class);
		
		if (icons == null) {
			throw new IllegalArgumentException("icons == null");
		}
		
		this.icons = new HashMap<>(icons);
		propertyReader = new QxtRealProperty<>(name, display, propertyClass);
	}

	@Override
	public Icon getValue(Object bean) {
		T t = propertyReader.getValue(bean);
		return icons.get(t);
	}

	@Override
	<E> void init(Class<E> beanClass, QxtTableModel<E> model) {
		/*
		 * Nothing to init in this class but we need to initialize the real
		 * property reader.
		 */
		propertyReader.init(beanClass, model);
	}

	@Override
	void setup(TableColumnExt tc) {
		tc.setCellRenderer(new DefaultTableCellRenderer() {
			/**
			 * Version for serialization.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row,
					int column) {
				Component c = super.getTableCellRendererComponent(table, null,
						isSelected, hasFocus, row, column);
				assert c instanceof JLabel;
				((JLabel) c).setIcon((Icon) value);
				
				return c;
			}
		});
	}
}
