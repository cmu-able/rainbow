package incubator.scb.ui;

import incubator.Pair;
import incubator.pval.Ensure;
import incubator.scb.ScbField;
import incubator.scb.ValidationResult;

/**
 * Field in an SCB table which contains a boolean.
 * @param <T> the bean type
 */
public class ScbTableModelBooleanField<T>
		extends ScbTableModelField<T, Boolean, ScbField<T, Boolean>> {
	/**
	 * Creates a new, non-editable, field.
	 * @param cof the configuration object boolean field
	 */
	public ScbTableModelBooleanField(ScbField<T, Boolean> cof) {
		super(cof);
	}
	
	/**
	 * Creates a new field.
	 * @param cof the configuration object boolean field
	 * @param editable is the field editable?
	 */
	public ScbTableModelBooleanField(ScbField<T, Boolean> cof,
			boolean editable) {
		super(cof, editable);
		Ensure.is_true(!editable || cof.can_set());
	}

	@Override
	public Object display_object(T obj) {
		return cof().get(obj);
	}
	
	@Override
	public Pair<ValidationResult, Boolean> from_display(T obj, Object display) {
		if (display == null || display instanceof Boolean) {
			return new Pair<>(ValidationResult.make_valid(), (Boolean) display);
		} else {
			throw new IllegalArgumentException("Cannot set boolean field to a "
					+ "non boolean value (" + display.getClass().getName()
					+ ").");
		}
	}
}
