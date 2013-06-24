package incubator.rcli;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import incubator.pval.Ensure;


public abstract class AbstractCommand implements Command {
	/**
	 * The name of the command.
	 */
	private String m_name;
	
	/**
	 * Creates a new command.
	 * @param name the name of the command
	 */
	protected AbstractCommand(String name) {
		Ensure.not_null(name);
		m_name = name;
	}
	
	/**
	 * Obtains the name of the command.
	 * @return the name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Writes a number to the session.
	 * @param prefix the prefix before the number is written (can be
	 * empty or <code>null</code>)
	 * @param suffix the suffix after the number is written (can be
	 * empty or <code>null</code>)
	 * @param number the number to write
	 * @param ses the session
	 * @throws IOException failed to write to the session
	 */
	protected void write_number(String prefix, String suffix, int number,
			Session ses) throws IOException {
		Ensure.not_null(ses);
		
		StringBuilder txt = new StringBuilder();
		if (prefix != null) {
			txt.append(prefix);
		}
		
		txt.append(Integer.toString(number));
		if (suffix != null) {
			txt.append(suffix);
		}
		
		ses.output(txt.toString());
	}
	
	/**
	 * Reads a number from the session.
	 * @param prefix the prefix before the number is written; this is a regular
	 * expression but cannot contain the dollar sign or capture groups; it
	 * may be <code>null</code>
	 * @param suffix the suffix after the number; this is a regular expression
	 * but cannot contain the hat sign or capture groups; it may be
	 * <code>null</code>
	 * @param ses the session
	 * @return the number read
	 * @throws CannotInterpretOutputException the line read from the session
	 * cannot be interpreted
	 * @throws IOException failed to read from the session
	 */
	protected int read_number(String prefix, String suffix, Session ses)
			throws CannotInterpretOutputException, IOException {
		Ensure.not_null(ses);
		
		if (prefix == null) {
			prefix = "";
		}
		
		if (suffix == null) {
			suffix = "";
		}
		
		String reg_ex = "^" + prefix + "(\\d+)" + suffix + "$";
		Pattern p = Pattern.compile(reg_ex);
		String line = ses.input();
		Matcher m = p.matcher(line);
		if (!m.matches()) {
			throw new CannotInterpretOutputException("Line read: '" + line
					+ "' does not match regular expression '" + reg_ex + "'.");
		}
		
		try {
			return Integer.parseInt(m.group(1));
		} catch (NumberFormatException e) {
			throw new CannotInterpretOutputException("Value '" + m.group(1)
					+ "' read from input '" + line + "' cannot be parsed "
					+ "as an integer.");
		}
	}
}
