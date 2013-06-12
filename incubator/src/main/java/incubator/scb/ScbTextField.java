package incubator.scb;

import incubator.pval.Ensure;
import incubator.scb.ScbField;
import incubator.scb.ValidationResult;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Field in an SCB that holds a text value.
 * @param <T> the bean class
 */
public abstract class ScbTextField<T> extends ScbField<T, String> {
	/**
	 * Patterns that must be validated. They are mapped to the errors they
	 * generate.
	 */
	private Map<Pattern, String> m_must_pass;
	
	/**
	 * Patterns that cannot be validated. They are mapped to the errors they
	 * generate.
	 */
	private Map<Pattern, String> m_must_fail;
	
	/**
	 * Creates a new text field.
	 * @param name the field's name
	 * @param can_set can the field's value be set?
	 * @param help a help message for the field (optional)
	 */
	public ScbTextField(String name, boolean can_set, String help) {
		super(name, can_set, help);
		m_must_pass = new HashMap<>();
		m_must_fail = new HashMap<>();
	}
	
	/**
	 * Adds a field rule.
	 * @param p the pattern that the field must validate
	 * @param msg the message explaining when validation fails
	 */
	protected void add_pass_rule(Pattern p, String msg) {
		Ensure.notNull(p);
		Ensure.notNull(msg);
		m_must_pass.put(p, msg);
	}
	
	/**
	 * Adds a field rule.
	 * @param p the pattern that the field must <em>not</em> validate
	 * @param msg the message explaining when validation fails
	 */
	protected void add_fail_rule(Pattern p, String msg) {
		Ensure.notNull(p);
		Ensure.notNull(msg);
		m_must_fail.put(p, msg);
	}
	
	@Override
	public ValidationResult valid(String value) {
		for (Pattern p : m_must_pass.keySet()) {
			if (!p.matcher(value == null? "" : value).find()) {
				return ValidationResult.make_invalid(m_must_pass.get(p));
			}
		}
		
		for (Pattern p : m_must_fail.keySet()) {
			if (p.matcher(value == null? "" : value).find()) {
				return ValidationResult.make_invalid(m_must_fail.get(p));
			}
		}
		
		return ValidationResult.make_valid();
	}
}
