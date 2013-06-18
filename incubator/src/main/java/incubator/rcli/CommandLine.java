package incubator.rcli;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Class representing a command line, transforming it into a string and parsing
 * it. A command line always contains a command and may contain several
 * positional arguments or options.
 */
public class CommandLine {
	/**
	 * Prefix for options.
	 */
	public static final String OPTION_PREFIX = "--";
	
	/**
	 * The name of the command.
	 */
	private String m_command;
	
	/**
	 * All arguments and options (with the prefix removed).
	 */
	private List<String> m_args;
	
	/**
	 * Is the argument an option?
	 */
	private List<Boolean> m_option;
	
	/**
	 * Creates a new command line.
	 * @param command the command
	 * @param args all arguments and options, options must start with the
	 * {@link #OPTION_PREFIX}
	 */
	public CommandLine(String command, String... args) {
		Ensure.not_null(command);
		Ensure.not_null(args);
		m_command = command;
		m_args = new ArrayList<>();
		m_option = new ArrayList<>();
		for (String s : args) {
			Ensure.not_null(s);
			
			if (s.startsWith(OPTION_PREFIX)) {
				m_args.add(s.substring(OPTION_PREFIX.length()));
				m_option.add(true);
			} else {
				m_args.add(s);
				m_option.add(false);
			}
		}
	}
	
	/**
	 * Parses a command line.
	 * @param command_line the text to parse
	 * @return <code>null</code> if invalid or empty
	 */
	public static CommandLine parse(String command_line) {
		Ensure.not_null(command_line);
		String dcl = CliEncoding.decode(command_line);
		if (dcl == null) {
			return null;
		}
		
		List<String> all = new ArrayList<>();
		boolean in_quote = false;
		boolean backslashed = false;
		StringBuilder processed = null;
		for (int i = 0; i <= dcl.length(); i++) {
			boolean end = (i == dcl.length());
			
			char ch = 0;
			boolean ws = false;
			boolean qc = false;
			boolean bs = false;
			if (!end) {
				ch = dcl.charAt(i);
				ws = Character.isWhitespace(ch);
				qc = (ch == '"');
				bs = (ch == '\\');
				
				/*
				 * If backslashed, we're neither quote, backslash or
				 * whitespace.
				 */
				if (backslashed) {
					qc = false;
					bs = false;
					ws = false;
				}
			}
			
			if (end && (in_quote || backslashed)) {
				/*
				 * End-of-string while quoting or backslash.
				 */
				return null;
			}
			
			/*
			 * Even if we were backslashed, now we're not any more.
			 */
			backslashed = false;
			
			if (!end && !ws && processed == null) {
				/*
				 * Non-whitespace character found but not text was being
				 * processed. Start processing a new word.
				 */
				processed = new StringBuilder();
			}
			
			if (!end && processed != null && (!ws || in_quote)) {
				/*
				 * We're processing a word and the current character is either
				 * not a whitespace or is a whitespace and we're within quotes.
				 * Add the character unless it is a quote or a backslash.
				 */
				if (!qc && !bs) {
					processed.append(ch);
				} else if (qc) {
					in_quote = !in_quote;
				} else {
					Ensure.is_true(bs);
					backslashed = true;
				}
			} else {
				if (processed != null) {
					all.add(processed.toString());
					processed = null;
				}
			}
		}
		
		if (all.size() == 0) {
			return null;
		}
		
		return new CommandLine(all.get(0), all.subList(1,
				all.size()).toArray(new String[all.size() - 1]));
	}
	
	/**
	 * Ensures that an index is valid to access an argument or option.
	 * @param idx the argument
	 * @throws CommandSyntaxException argument is invalid
	 */
	private void ensure_valid_idx(int idx) throws CommandSyntaxException {
		Ensure.greater_equal(idx, 0);
		if (idx >= m_args.size()) {
			throw new CommandSyntaxException("No argument with index "
					+ idx + " exists.");
		}
	}
	
	/**
	 * Checks that an index is not an option.
	 * @param idx the index
	 * @throws CommandSyntaxException it is an option
	 */
	private void ensure_not_option(int idx) throws CommandSyntaxException {
		ensure_valid_idx(idx);
		if (m_option.get(idx)) {
			throw new CommandSyntaxException("An option was not expected in "
					+ "argument with index " + idx + ".");
		}
	}
	
	/**
	 * Checks that an index is an option.
	 * @param idx the index
	 * @throws CommandSyntaxException it is not an option
	 */
	private void ensure_option(int idx) throws CommandSyntaxException {
		ensure_valid_idx(idx);
		if (!m_option.get(idx)) {
			throw new CommandSyntaxException("An option was expected in "
					+ "argument with index " + idx + ".");
		}
	}
	
	/**
	 * Obtains the command in the command line.
	 * @return the command
	 */
	public String command() {
		return m_command;
	}
	
	/**
	 * Obtains the number of arguments and options.
	 * @return the total count of arguments and options
	 */
	public int arg_count() {
		return m_args.size();
	}
	
	/**
	 * Checks whether an index corresponds to an option.
	 * @param idx the index
	 * @return does it correspond to an option? If <code>false</code> the
	 * index corresponds to an argument
	 * @throws CommandSyntaxException invalid index
	 */
	public boolean is_option(int idx) throws CommandSyntaxException {
		ensure_valid_idx(idx);
		return m_option.get(idx);
	}
	
	/**
	 * Obtains the text of the option at index <code>idx</code>. The text is
	 * returned without the {@link #OPTION_PREFIX}.
	 * @param idx the index
	 * @return the option text
	 * @throws CommandSyntaxException the index is invalid or there is no
	 * option at index <code>idx</code>
	 */
	public String option(int idx) throws CommandSyntaxException {
		ensure_option(idx);
		return m_args.get(idx);
	}
	
	/**
	 * Obtains the text of the argument at index <code>idx</code>.
	 * @param idx the index
	 * @return the argument text
	 * @throws CommandSyntaxException the index is invalid or the index
	 * corresponds to an option
	 */
	public String arg(int idx) throws CommandSyntaxException {
		ensure_not_option(idx);
		return m_args.get(idx);
	}
	
	/**
	 * Obtains argument at index <code>idx</code> converted to an integer.
	 * @param idx the index
	 * @return the integer
	 * @throws CommandSyntaxException the index is invalid or the index
	 * corresponds to an option or the argument text is not a valid
	 * integer
	 */
	public int argi(int idx) throws CommandSyntaxException {
		String s = arg(idx);
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new CommandSyntaxException("Argument '" + s + "' is not "
					+ "a valid integer number.");
		}
	}
	
	/**
	 * Makes all replaces to a string to add it to a command line.
	 * @param cmd the string
	 * @return the string replaced
	 */
	private String replaces(String cmd) {
		cmd = StringUtils.replace(cmd, "\\", "\\\\");
		cmd = StringUtils.replace(cmd, "\"", "\\\"");
		return cmd;
	}
	
	/**
	 * Converts this command line to a line which can be parsed by
	 * {@link #parse(String)}.
	 * @return the text line
	 */
	public String to_single_line() {
		StringBuilder bldr = new StringBuilder();
		bldr.append('\"');
		bldr.append(replaces(m_command));
		bldr.append('\"');
		
		for (int i = 0; i < m_args.size(); i++) {
			bldr.append(" ");
			if (m_option.get(i)) {
				bldr.append(OPTION_PREFIX);
			}
			
			bldr.append('\"');
			bldr.append(replaces(m_args.get(i)));
			bldr.append('\"');
		}
		
		return CliEncoding.encode(bldr.toString());
	}
}
