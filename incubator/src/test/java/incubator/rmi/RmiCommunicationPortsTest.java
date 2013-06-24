package incubator.rmi;

import incubator.rmi.RmiCommunicationPorts;

import java.util.Properties;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Checks communication port configuration.
 */
public class RmiCommunicationPortsTest extends DefaultTCase {
	/**
	 * Without any system properties defined, the ports returns should be the
	 * default.
	 * @throws Exception test failed
	 */
	@Test
	public void get_ports_with_no_properties_returns_default()
			throws Exception {
		RmiCommunicationPorts cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
	}
	
	/**
	 * If the system properties that define the ports exist, the ports
	 * obtained are those from the system properties.
	 * @throws Exception test failed
	 */
	@Test
	public void get_ports_honors_system_properties() throws Exception {
		int min = 2000 + RandomUtils.nextInt(5000);
		int max = min + RandomUtils.nextInt(5000);
		
		String base = RmiCommunicationPorts.class.getName();
		System.setProperty(base + ".min-port", "" + min);
		System.setProperty(base + ".max-port", "" + max);
		
		RmiCommunicationPorts cp = new RmiCommunicationPorts();
		assertEquals(min, cp.min_port());
		assertEquals(max, cp.max_port());
	}
	
	/**
	 * Instantiates the port definition class defining one of the properties
	 * with invalid values: negative, zero and non-numeric. It also tries
	 * with values above and below the maximum and minimum. In every case,
	 * the default values should be returned.
	 * @throws Exception test failed
	 */
	@Test
	public void one_port_invalid_returns_defaults() throws Exception {
		int min = 2000 + RandomUtils.nextInt(5000);
		int max = min + RandomUtils.nextInt(5000);
		
		String base = RmiCommunicationPorts.class.getName();
		RmiCommunicationPorts cp;
		
		System.setProperty(base + ".min-port", "-1");
		System.setProperty(base + ".max-port", "" + max);
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
		
		System.setProperty(base + ".min-port", "0");
		System.setProperty(base + ".max-port", "" + max);
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
		
		System.setProperty(base + ".min-port", "x");
		System.setProperty(base + ".max-port", "" + max);
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
		
		System.setProperty(base + ".min-port", "" + min);
		System.setProperty(base + ".max-port", "-1");
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
		
		System.setProperty(base + ".min-port", "" + min);
		System.setProperty(base + ".max-port", "0");
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
		
		System.setProperty(base + ".min-port", "" + min);
		System.setProperty(base + ".max-port", "x");
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
		
		System.setProperty(base + ".min-port", "" + max);
		System.setProperty(base + ".max-port", "" + min);
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
	}
	
	/**
	 * Instantiate the port communication object but only one of the ports is
	 * defined in system properties. The default ports should be returned.
	 * @throws Exception test failed
	 */
	@Test
	public void cannot_have_only_one_port_defined() throws Exception {
		int min = RmiCommunicationPorts.MINIMUM_PORT - 50;
		
		String base = RmiCommunicationPorts.class.getName();
		RmiCommunicationPorts cp;
		
		System.setProperty(base + ".min-port", "" + min);
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
		
		Properties p = System.getProperties();
		p.remove(base + ".min-port");
		System.setProperties(p);
		cp = new RmiCommunicationPorts();
		assertEquals(RmiCommunicationPorts.MINIMUM_PORT, cp.min_port());
		assertEquals(RmiCommunicationPorts.MAXIMUM_PORT, cp.max_port());
	}
}
