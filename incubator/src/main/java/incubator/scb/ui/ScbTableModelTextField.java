package incubator.scb.ui;

import incubator.Pair;
import incubator.pval.Ensure;
import incubator.scb.ScbField;
import incubator.scb.ValidationResult;

/**
 * Field in an SCB table which contains text.
 * @param <T> the bean type
 */
public class ScbTableModelTextField<T>
		extends ScbTableModelField<T, String, ScbField<T, String>> {
	/**
	 * Creates a new, non-editable, field.
	 * @param cof the configuration object text field
	 */
	public ScbTableModelTextField(ScbField<T, String> cof) {
		super(cof);
	}
	
	/**
	 * Creates a new field.
	 * @param cof the configuration object text field
	 * @param editable is the field editable?
	 */
	public ScbTableModelTextField(ScbField<T, String> cof, boolean editable) {
		super(cof, editable);
		Ensure.is_true(!editable || cof.can_set());
	}

	@Override
	public Object display_object(T obj) {
		return cof().get(obj);
	}
	
	@Override
	public Pair<ValidationResult, String> from_display(T obj, Object display) {
		if (display == null || display instanceof String) {
			String dstr = null;
			ValidationResult vr = cof().valid((String) display);
			if (vr.valid()) {
				cof().set(obj, (String) display);
				dstr = (String) display;
			}
			
			return new Pair<>(vr, dstr);
		} else {
			throw new IllegalArgumentException("Cannot set text field to a "
					+ "non string value (" + display.getClass().getName()
					+ ").");
		}
	}
}
