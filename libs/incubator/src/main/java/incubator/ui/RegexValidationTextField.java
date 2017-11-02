package incubator.ui;

import incubator.pval.Ensure;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Text field that changes its background color to red when the contents do
 * not match a regular expression.
 */
public class RegexValidationTextField extends JTextField {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The pattern to check.
	 */
	private Pattern m_pattern;
	
	/**
	 * Are the contents valid?
	 */
	private boolean m_is_valid;
	
	/**
	 * The background color to use when the contents are valid.
	 */
	private Color m_good_color;
	
	/**
	 * The background color to use when the contents are not valid.
	 */
	private Color m_bad_color;
	
	/**
	 * Text field listeners.
	 */
	private List<RegexValidationTextFieldListener> m_listeners;
	
	/**
	 * Creates a new text field.
	 * @param pattern the pattern to use
	 */
	public RegexValidationTextField(Pattern pattern) {
		super();
		init(pattern);
	}
	
	/**
	 * Creates a new text field.
	 * @param pattern the pattern to use
	 * @param cols number of columns to set the text field to
	 */
	public RegexValidationTextField(Pattern pattern, int cols) {
		super(cols);
		init(pattern);
	}
	
	/**
	 * Adds a text field listener.
	 * @param l the listener
	 */
	public void add_listener(RegexValidationTextFieldListener l) {
		Ensure.not_null(l);
		m_listeners.add(l);
	}
	
	/**
	 * Removes a text field listener.
	 * @param l the listener
	 */
	public void remove_listener(RegexValidationTextFieldListener l) {
		Ensure.not_null(l);
		m_listeners.remove(l);
	}
	
	/**
	 * Initializes the text field.
	 * @param pattern the pattern to use to check the text field contents
	 */
	private void init(Pattern pattern) {
		Ensure.not_null(pattern);
		
		m_pattern = pattern;
		m_listeners = new ArrayList<>();
		getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				check_regex();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				check_regex();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				check_regex();
			}
		});
		
		m_good_color = getBackground();
		m_bad_color = Color.red;
		m_is_valid = true;
		check_regex();
	}
	
	/**
	 * Do the contents of the field match the regular expression?
	 * @return do they match?
	 */
	public boolean is_valid() {
		return m_is_valid;
	}
	
	/**
	 * Invoked when the contents have changed. Will update the validate
	 * flag and the background color.
	 */
	private void check_regex() {
		final boolean m = m_pattern.matcher(getText()).matches();
		if (m != m_is_valid) {
			m_is_valid = m;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (m) {
						setBackground(m_good_color);
					} else {
						setBackground(m_bad_color);
					}
				}
			});
		}
		
		final List<RegexValidationTextFieldListener> lcp =
				new ArrayList<>(m_listeners);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (RegexValidationTextFieldListener l : lcp) {
					l.text_field_changed();
				}
			}
		});
	}
}
