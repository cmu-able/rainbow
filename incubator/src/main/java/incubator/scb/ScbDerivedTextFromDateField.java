package incubator.scb;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import incubator.pval.Ensure;

/**
 * Derived text field which derives from a date by formatting according to a
 * date format.
 * @param <T> the bean type
 */
public class ScbDerivedTextFromDateField<T>
		extends ScbDerivedTextField<T, Date, ScbField<T, Date>> {
	/**
	 * The date format.
	 */
	private DateFormat m_format;
	
	/**
	 * Creates a new derived field.
	 * @param field the field this field derives from
	 */
	public ScbDerivedTextFromDateField(ScbField<T, Date> field) {
		this(field.name(), true, null, field, DateFormat.getDateTimeInstance());
	}
	
	/**
	 * Creates a new derived field.
	 * (see {@link ScbDerivedTextField#ScbDerivedTextField(String, boolean,
	 * String, ScbField)}
	 * @param name the field name
	 * @param can_set can the field be set? 
	 * @param help an optional help text
	 * @param field the field this field derives from
	 * @param format the date format to use
	 */
	public ScbDerivedTextFromDateField(String name, boolean can_set,
			String help, ScbField<T, Date> field, DateFormat format) {
		super(name, can_set, help, field);
		Ensure.not_null(format);
		m_format = format;
	}
	
	@Override
	protected Date convert_to_derived(String v)
			throws ConversionFailedException {
		if (v == null) {
			return null;
		}
		
		try {
			return m_format.parse(v);
		} catch (ParseException e) {
			throw new ConversionFailedException("Failed to parse date '"
					+ v + "'.", e);
		}
	}
	
	@Override
	protected String convert_from_dervied(Date v) {
		if (v == null) {
			return null;
		}
		
		return m_format.format(v);
	}
}
