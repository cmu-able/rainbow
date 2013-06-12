package edu.cmu.cs.able.parsec;

import edu.cmu.cs.able.parsec.parser.ParseException;

/**
 * Parse exception that includes a location for the error using a
 * {@link LCCoord}.
 */
@SuppressWarnings("serial")
public class LocalizedParseException extends ParseException {
	/**
	 * The location.
	 */
	private LCCoord m_location;
	
	/**
	 * Creates a new exception.
	 * @param message the exception message which should <em>not</em> include
	 * a location
	 * @param location the location which may <em>not</em> be <code>null</code>
	 */
	public LocalizedParseException(String message, LCCoord location) {
		super(message);
		m_location = location;
	}
	
	/**
	 * Obtains the location of this exception.
	 * @return the location
	 */
	public LCCoord location() {
		return m_location;
	}
}
