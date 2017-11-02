package incubator.qxt;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Property editing dates. Dates are displayed using a provided date format and
 * are edited using an <code>JXDatePicker</code> control.
 */
public class QxtDateProperty extends QxtRealProperty<Date> {
	/**
	 * Converter used to transform dates into strings.
	 */
	private StringValue converter;

	/**
	 * Date format to use.
	 */
	private DateFormat format;

	/**
	 * Creates a new date property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param dateFormat the date format to use
	 * @param readOnly is the date read only?
	 */
	public QxtDateProperty(String name, String display, DateFormat dateFormat,
			boolean readOnly) {
		super(name, display, readOnly, Date.class);

		init(dateFormat);
	}

	/**
	 * Initializes the date property.
	 * 
	 * @param dateFormat the date format to use
	 */
	private void init(DateFormat dateFormat) {
		if (dateFormat == null) {
			throw new IllegalArgumentException("dateFormat == null");
		}

		format = dateFormat;
		converter = new StringValue() {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getString(Object dt) {
				if (!(dt instanceof Date)) {
					return null;
				}

				return format.format((Date) dt);
			}
		};
	}

	/**
	 * Creates a new date property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param dateFormat the date format to use
	 */
	public QxtDateProperty(String name, String display, DateFormat dateFormat) {
		super(name, display, Date.class);

		init(dateFormat);
	}

	@Override
	void setup(TableColumnExt tc) {
		tc.setCellRenderer(new DefaultTableRenderer(converter));
		tc.setCellEditor(new CellEditor());
	}

	@Override
	public boolean supportsTab() {
		return false;
	}

	/**
	 * Cell editor used to edit date fields.
	 */
	private static final class CellEditor extends AbstractCellEditor implements
			TableCellEditor {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The date picker control.
		 */
		private final JXDatePicker picker;

		/**
		 * Creates a new cell editor.
		 */
		private CellEditor() {
			picker = new JXDatePicker();
			KeyListener kl = new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						escapePressed();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
					/*
					 * Nothing to do.
					 */
				}

				@Override
				public void keyTyped(KeyEvent e) {
					/*
					 * Nothing to do.
					 */
				}
			};

			@SuppressWarnings("unused")
			DeepKeyListenerMaintainer dklm =
					new DeepKeyListenerMaintainer(picker, kl);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (!(value instanceof Date)) {
				picker.setDate(new Date());
			} else {
				picker.setDate((Date) value);
			}

			return picker;
		}

		@Override
		public Object getCellEditorValue() {
			return picker.getDate();
		}

		/**
		 * The user has pressed escape: cancel editing.
		 */
		private void escapePressed() {
			cancelCellEditing();
		}
	}
}
