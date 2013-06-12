package incubator.scb;

import incubator.scb.ValidationResult;

/**
 * Result of a validation.
 */
public class ValidationResult {
	/**
	 * Is the validation ok?
	 */
	private boolean m_valid;
	
	/**
	 * An explanation for the failure.
	 */
	private String m_explanation;
	
	/**
	 * Creates a new validation result.
	 */
	private ValidationResult() {
	}
	
	/**
	 * Did validation succeed?
	 * @return succeeded?
	 */
	public boolean valid() {
		return m_valid;
	}
	
	/**
	 * What is the explanation for the failure?
	 * @return the explanation, if any
	 */
	public String explanation() {
		return m_explanation;
	}
	
	/**
	 * Creates a new validation result representing a successful validation.
	 * @return the result
	 */
	public static ValidationResult make_valid() {
		ValidationResult r = new ValidationResult();
		r.m_valid = true;
		r.m_explanation = null;
		return r;
	}
	
	/**
	 * Makes a new validation result representing an unsuccessful validation.
	 * @param explanation the explanation result, if any
	 * @return the result
	 */
	public static ValidationResult make_invalid(String explanation) {
		ValidationResult r = new ValidationResult();
		r.m_valid = false;
		r.m_explanation = explanation;
		return r;
	}
}
