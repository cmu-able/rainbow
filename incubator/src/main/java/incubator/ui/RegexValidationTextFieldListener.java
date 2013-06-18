package incubator.ui;

/**
 * Interface implemented by objects that are informed of changed in a
 * {@link RegexValidationTextField}.
 */
public interface RegexValidationTextFieldListener {
	/**
	 * Invoked when the text field has changed.
	 */
	public void text_field_changed();
}
