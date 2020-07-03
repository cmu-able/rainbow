package incubator.ui;

import java.awt.BorderLayout;

import incubator.pval.Ensure;

import javax.swing.JComponent;

/**
 * Panel part that shows a component.
 */
@SuppressWarnings("serial")
public class ComponentPanelPart extends PanelPart {
	/**
	 * Creates a new panel part.
	 * @param component the component being shown
	 */
	public ComponentPanelPart(JComponent component) {
		Ensure.not_null(component, "component == null");
		
		setLayout(new BorderLayout());
		add(component, BorderLayout.CENTER);
	}
}
