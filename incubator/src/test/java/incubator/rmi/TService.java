package incubator.rmi;

/**
 * Test service implementation.
 */
public class TService implements TServiceI {
	/**
	 * Value this service will return.
	 */
	int m_val;
	
	@Override
	public int val() {
		return m_val;
	}
}
