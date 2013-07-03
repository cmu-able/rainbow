package edu.cmu.cs.able.eseb.filter;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import auxtestlib.DefaultTCase;

/**
 * Superclass for filter test cases.
 */
public class FilterTestCase extends DefaultTCase {
	/**
	 * Types to use.
	 */
	protected PrimitiveScope m_scope;
	
	/**
	 * An empty filter.
	 */
	protected TestEventFilter m_filter;
	
	/**
	 * An empty sink.
	 */
	protected SaveSink m_sink;
	
	/**
	 * Sets up the test fixture.
	 */
	@Before
	public void filter_set_up() {
		m_scope = new PrimitiveScope();
		m_sink = new SaveSink();
		m_filter = new TestEventFilter();
	}
	
	/**
	 * Creates a random data value.
	 * @return the value
	 */
	protected BusData bus_data() {
		return new BusData(m_scope.int32().make(RandomUtils.nextInt()));
	}
}
