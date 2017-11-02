package incubator.ui;

import incubator.pval.Ensure;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * UI panel that shows a message.
 */
@SuppressWarnings("serial")
public class MessagePanel extends JPanel {
	/**
	 * The label that actually shows the message.
	 */
	private JLabel m_label;
	
	/**
	 * Creates a new panel.
	 */
	public MessagePanel() {
		m_label = new JLabel();
		setLayout(new BorderLayout());
		add(m_label, BorderLayout.CENTER);
	}
	
	/**
	 * Shows a message.
	 * @param message the message to show
	 */
	public void show_message(String message) {
		Ensure.not_null(message);
		m_label.setText(message);
	}
	
	/**
	 * Clears the currently shown message.
	 */
	public void clear_message() {
		m_label.setText("");
	}
}
