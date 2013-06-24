package incubator.scb.ui;

import incubator.Pair;
import incubator.pval.Ensure;
import incubator.scb.ScbIntegerField;
import incubator.scb.ValidationResult;

/**
 * Field in an SCB table which edits an integer value.
 * @param <T> the type of SCB Bean
 */
public class ScbTableModelIntegerField<T>
		extends ScbTableModelField<T, Integer, ScbIntegerField<T>> {
	/**
	 * Creates a new, non-editable, field.
	 * @param cof the configuration object integer field
	 */
	public ScbTableModelIntegerField(ScbIntegerField<T> cof) {
		super(cof);
	}
	
	/**
	 * Creates a new field.
	 * @param cof the configuration object integer field
	 * @param editable is the field editable?
	 */
	public ScbTableModelIntegerField(ScbIntegerField<T> cof, boolean editable) {
		super(cof, editable);
		Ensure.is_true(!editable || cof.can_set());
	}

	@Override
	public Object display_object(T obj) {
		return cof().get(obj);
	}
	
	@Override
	public Pair<ValidationResult, Integer> from_display(T obj, Object display) {
		if (display == null) {
			return new Pair<>(ValidationResult.make_invalid(
					"Cannot set to empty value."), null);
		}
		
		Ensure.is_instance(display, String.class);
		try {
			int value = Integer.parseInt((String) display);
			cof().set(obj, value);
			return new Pair<>(ValidationResult.make_valid(), value);
		} catch (NumberFormatException e) {
			return new Pair<>(ValidationResult.make_invalid("'" + display
					+ "' is not a valid integer number."), 0);
		}
	}
}
