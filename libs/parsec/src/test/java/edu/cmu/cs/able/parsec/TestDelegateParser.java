package edu.cmu.cs.able.parsec;

import java.util.ArrayList;
import java.util.List;

/**
 * Delegate parser that will record its invocations and will fire
 * pre-programmed exceptions.
 */
public class TestDelegateParser implements DelegateParser<Integer> {
	/**
	 * Collects all statements that the parser was invoked to parse.
	 */
	public List<String> m_statements;
	
	/**
	 * If, contains an item which is not <code>null</code>, will throw the
	 * exception at the respective invocation.
	 */
	public List<LocalizedParseException> m_throw_statements;
	
	/**
	 * Context of the invocations.
	 */
	public List<Integer> m_statement_contexts;
	
	/**
	 * Collects all block headers that the parser was invoked to parse.
	 */
	public List<String> m_block_headers;
	
	/**
	 * Collects all block texts that the parser was invoked to parse.
	 */
	public List<String> m_block_texts;
	
	/**
	 * If, contains an item which is not <code>null</code>, will throw the
	 * exception at the respective invocation.
	 */
	public List<LocalizedParseException> m_throw_h_blocks;
	
	/**
	 * If, contains an item which is not <code>null</code>, will throw the
	 * exception at the respective invocation.
	 */
	public List<LocalizedParseException> m_throw_t_blocks;
	
	/**
	 * Context of the invocations.
	 */
	public List<Integer> m_block_contexts;

	/**
	 * Creates a new parser.
	 */
	public TestDelegateParser() {
		m_statements = new ArrayList<>();
		m_throw_statements = new ArrayList<>();
		m_statement_contexts = new ArrayList<>();
		m_block_headers = new ArrayList<>();
		m_block_texts = new ArrayList<>();
		m_throw_h_blocks = new ArrayList<>();
		m_throw_t_blocks = new ArrayList<>();
		m_block_contexts = new ArrayList<>();
	}
	
	@Override
	public void parse_statement(String statement, Integer context)
			throws LocalizedParseException {
		int idx = m_statements.size();
		m_statements.add(statement);
		m_statement_contexts.add(context);
		if (m_throw_statements.size() > idx
				&& m_throw_statements.get(idx) != null) {
			throw m_throw_statements.get(idx);
		}
	}

	@Override
	public void parse_block(String header, String text, Integer context)
			throws BlockHeaderParseException, BlockTextParseException {
		int idx = m_block_headers.size();
		m_block_headers.add(header);
		m_block_texts.add(text);
		m_block_contexts.add(context);
		if (m_throw_h_blocks.size() > idx
				&& m_throw_h_blocks.get(idx) != null) {
			throw new BlockHeaderParseException(m_throw_h_blocks.get(idx));
		}
		
		if (m_throw_t_blocks.size() > idx
				&& m_throw_t_blocks.get(idx) != null) {
			throw new BlockTextParseException(m_throw_t_blocks.get(idx));
		}
	}
}
