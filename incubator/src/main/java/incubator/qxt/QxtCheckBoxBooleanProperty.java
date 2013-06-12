package incubator.qxt;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Boolean property edited with a checkbox.
 */
public class QxtCheckBoxBooleanProperty extends QxtRealProperty<Boolean> {
	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param readOnly is the property read only?
	 */
	public QxtCheckBoxBooleanProperty(String name, String display,
			boolean readOnly) {
		super(name, display, readOnly, Boolean.class);
	}

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 */
	public QxtCheckBoxBooleanProperty(String name, String display) {
		super(name, display, Boolean.class);
	}

	@Override
	void setup(TableColumnExt tc) {
		tc.setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
		JCheckBox ech = new JCheckBox();
		ech.setHorizontalAlignment(SwingConstants.CENTER);
		tc.setCellEditor(new DefaultCellEditor(ech));
	}

	@Override
	public boolean supportsTab() {
		/*
		 * FIXME: Checkboxes don't really behave property with the tab added as
		 * a key listener. This is probably work-aroundable but I don't have the
		 * time to investigate it further.
		 */
		return false;
	}
}
