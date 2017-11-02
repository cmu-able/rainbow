package incubator.ui;

import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Document that only accepts characters if the text validates a regular
 * expression.
 */
public class RegexValidationDocument extends PlainDocument {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1;

	/**
	 * The compiled pattern.
	 */
	private final Pattern pattern;

	/**
	 * Creates a new document.
	 * 
	 * @param regex the regular expression
	 */
	public RegexValidationDocument(String regex) {
		assert regex != null;

		pattern = Pattern.compile(regex);
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		String text = readText();
		String nt = text.substring(0, offs) + str + text.substring(offs);
		if (pattern.matcher(nt).matches()) {
			super.insertString(offs, str, a);
		}
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		String text = readText();
		String nt = text.substring(0, offs) + text.substring(offs + len);
		if (pattern.matcher(nt).matches()) {
			super.remove(offs, len);
		}
	}

	@Override
	public void replace(int offs, int len, String str, AttributeSet attrs)
			throws BadLocationException {
		String text = readText();
		String nt = text.substring(0, offs) + str + text.substring(
				offs + len);
		if (pattern.matcher(nt).matches()) {
			super.replace(offs, len, str, attrs);
		}
	}

	/**
	 * Obtains the text in the document.
	 * 
	 * @return the text
	 */
	private String readText() {
		int len = getLength();
		if (len == 0) {
			return "";
		}

		String text = null;
		try {
			text = getText(0, len);
		} catch (BadLocationException e) {
			assert false;
		}

		return text;
	}
}
