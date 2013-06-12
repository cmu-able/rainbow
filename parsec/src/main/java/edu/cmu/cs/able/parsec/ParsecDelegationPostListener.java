package edu.cmu.cs.able.parsec;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.parsec.parser.ParseException;

/**
 * The delegation post-listener is invoked to inform the delegates that a
 * statement or block has been parsed.
 * @param <T> the type of context to provide to parse delegates
 */
class ParsecDelegationPostListener<T> extends ParsecParserPostListener {
	/**
	 * The list of delegates to inform.
	 */
	private List<DelegateParser<T>> m_delegates;
	
	/**
	 * The context to pass to the delegates
	 */
	private T m_context;
	
	/**
	 * Creates a new post-listener.
	 * @param delegates the list of delegates to inform
	 * @param context the context to pass to the delegates which may be
	 * <code>null</code>
	 */
	ParsecDelegationPostListener(List<DelegateParser<T>> delegates, T context) {
		Ensure.not_null(delegates);
		m_delegates = new ArrayList<>(delegates);
		m_context = context;
	}

	@Override
	void statement_recognized(String text, TextRegionMatch loc)
			throws ParseException {
		List<LocalizedParseException> exceptions = new ArrayList<>();
		for (DelegateParser<T> p : m_delegates) {
			try {
				p.parse_statement(text, m_context);
				return;
			} catch (LocalizedParseException ex) {
				exceptions.add(ex);
			}
		}
		
		handle_exceptions(exceptions, loc);
	}

	@Override
	void block_recognized(String block_header, TextRegionMatch h_loc,
			String block_text, TextRegionMatch t_loc) throws ParseException {
		List<LocalizedParseException> h_exceptions = new ArrayList<>();
		List<LocalizedParseException> t_exceptions = new ArrayList<>();
		for (DelegateParser<T> p : m_delegates) {
			try {
				p.parse_block(block_header, block_text, m_context);
				return;
			} catch (BlockHeaderParseException ex) {
				h_exceptions.add((LocalizedParseException) ex.getCause());
			} catch (BlockTextParseException ex) {
				t_exceptions.add((LocalizedParseException) ex.getCause());
			}
		}
		
		/*
		 * We only care about exceptions thrown when parsing the header if
		 * none of the parsers was able to parse it. Otherwise, we'll ignore
		 * header errors and just report block text errors.
		 */
		
		if (t_exceptions.size() != 0) {
			handle_exceptions(t_exceptions, t_loc);
		}
		
		handle_exceptions(h_exceptions, h_loc);
	}
	
	/**
	 * A statement or block failed to parse in all delegate parsers. This
	 * method will pick which exception to throw and converts the coordinates.
	 * @param exceptions the exceptions of all parsers, in the order by which
	 * they were fired
	 * @param loc the location where the text is
	 * @throws LocalizedParseException the exception to throw; this is always
	 * thrown in all invocations of this method
	 */
	private void handle_exceptions(List<LocalizedParseException> exceptions,
			TextRegionMatch loc) throws LocalizedParseException {
		assert exceptions != null;
		assert loc != null;
		
		LocalizedParseException last = null;
		LCCoord last_c = null;
		for (LocalizedParseException e : exceptions) {
			LCCoord e_c = e.location();
			if (last == null || last_c.line() < e_c.line()
					|| (last_c.line() == e_c.line()
					&& last_c.column() < e_c.column())) {
				last = e;
				last_c = e_c;
			}
		}
		
		LCCoord report_c;
		if (last_c.line() == 1) {
			report_c = new LCCoord(loc.coord_in_region().line(),
					loc.coord_in_region().column() + last_c.column() - 1);
		} else {
			report_c = new LCCoord(loc.coord_in_region().line()
					+ last_c.line() - 1, last_c.column());
		}
		
		throw new LocalizedParseException(last.getMessage(), report_c);
	}
}
