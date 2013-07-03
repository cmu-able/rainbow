package edu.cmu.cs.able.eseb;

import incubator.dispatch.DispatchHelper;

import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import auxtestlib.TestHelper;
import auxtestlib.TestPropertiesDefinition;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case that tests the performance of the eseblib.
 */
@SuppressWarnings("javadoc")
public class PerformanceTest extends EsebTestCase {
	private static final int VALUE_ARRAY_SIZE = 100;
	private static final int ROUND_COUNT = 1;
	private static final int STRING_SIZE = 50;
	
	@TestHelper
	private DispatchHelper m_dispatcher_helper;
	
	@Test
	public void performance_test() throws Exception {
		final PrimitiveScope pscope = new PrimitiveScope();
		short p = (short) TestPropertiesDefinition.getInt(
				"free-port-zone-start");
		
		abstract class TypeTester {
			abstract String name();
			abstract DataValue create();
		}
		
		TypeTester[] testers = new TypeTester[] {
			new TypeTester() {
				@Override public String name() { return "ascii"; }
				@Override public DataValue create() {
					return pscope.ascii().make(
							RandomStringUtils.randomAlphanumeric(
							STRING_SIZE));
				}
			},
			new TypeTester() {
				@Override public String name() { return "bool"; }
				@Override public DataValue create() {
					return pscope.bool().make(RandomUtils.nextBoolean());
				}
			},
			new TypeTester() {
				@Override public String name() { return "double"; }
				@Override public DataValue create() {
					return pscope.double_type().make(RandomUtils.nextDouble());
				}
			},
			new TypeTester() {
				@Override public String name() { return "float"; }
				@Override public DataValue create() {
					return pscope.float_type().make(RandomUtils.nextFloat());
				}
			},
			new TypeTester() {
				@Override public String name() { return "int16"; }
				@Override public DataValue create() {
					return pscope.int16().make((short) RandomUtils.nextInt(
							Short.MAX_VALUE));
				}
			},
			new TypeTester() {
				@Override public String name() { return "int32"; }
				@Override public DataValue create() {
					return pscope.int32().make(RandomUtils.nextInt());
				}
			},
			new TypeTester() {
				@Override public String name() { return "int64"; }
				@Override public DataValue create() {
					return pscope.int64().make(RandomUtils.nextLong());
				}
			},
			new TypeTester() {
				@Override public String name() { return "int8"; }
				@Override public DataValue create() {
					return pscope.int8().make((byte) RandomUtils.nextInt(
							Byte.MAX_VALUE));
				}
			},
			new TypeTester() {
				@Override public String name() { return "period"; }
				@Override public DataValue create() {
					return pscope.period().make(RandomUtils.nextLong());
				}
			},
			new TypeTester() {
				@Override public String name() { return "string"; }
				@Override public DataValue create() {
					return pscope.string().make(RandomStringUtils.random(
							STRING_SIZE));
				}
			},
			new TypeTester() {
				@Override public String name() { return "time"; }
				@Override public DataValue create() {
					return pscope.time().make(new Date().getTime());
				}
			}
		};
		
		try (	EventBus srv = new EventBus(p, pscope);
				BusConnection send_cli = new BusConnection("localhost",
						p, pscope);
				BusConnection recv_cli = new BusConnection("localhost",
						p, pscope)) {
			srv.start();
			send_cli.start();
			TestArraySaveQueue asq = new TestArraySaveQueue();
			recv_cli.queue_group().add(asq);
			recv_cli.start();
			/*
			 * Do a first round because we get bad values at startup due to
			 * transients.
			 */
			for (TypeTester t : testers) {
				DataValue values[] = new DataValue[VALUE_ARRAY_SIZE];
				for (int i = 0; i < values.length; i++) {
					values[i] = t.create();
				}
				
				check_performance(values, 1, send_cli, recv_cli, asq);
			}
			
			for (TypeTester t : testers) {
				DataValue values[] = new DataValue[VALUE_ARRAY_SIZE];
				for (int i = 0; i < values.length; i++) {
					values[i] = t.create();
				}
				
				long time = check_performance(values, ROUND_COUNT, send_cli,
						recv_cli, asq);
				double unit_time = time * 1.0 / (ROUND_COUNT * values.length);
				System.out.println(t.name() + " unit time: " + unit_time
						+ " ms");
				
				// 2 ms should be good enough.
				assertTrue(unit_time < 2.0);
			}
		}
		
		Thread.sleep(250);
	}
	
	private long check_performance(DataValue v[], int round_count,
			BusConnection send_cli, BusConnection recv_cli,
			TestArraySaveQueue asq) throws Exception {
		/*
		 * Wait for the connections to establish.
		 */
		Thread.sleep(100);
		
		long start_time = System.currentTimeMillis();
		
		for (int r = 0; r < round_count; r++) {
			for (int i = 0; i < v.length; i++) {
//				System.out.println("-> -> " + v[i]);
				send_cli.send(v[i]);
			}
		}
		
		/*
		 * Wait for data to arrive.
		 */
		int lr = asq.m_values.size();
		int last_recv = 0;
		long end_time = System.currentTimeMillis();
		while (true) {
			last_recv = lr;
			Thread.sleep(5);
			lr = asq.m_values.size();
			long now = System.currentTimeMillis();
			if (lr != last_recv) {
				end_time = now;
			} else if (now - end_time > 2000) {
				break;
			}
		}
		
		assertEquals(0, send_cli.collector().throwables().size());
		assertEquals(0, recv_cli.collector().throwables().size());
		
		assertEquals(round_count * v.length, asq.m_values.size());
		for (int r = 0; r < round_count; r++) {
			for (int i = 0; i < v.length; i++) {
//				System.out.println("== " + v[i] + " == " + asq.m_values.get(i));
				assertEquals(v[i], asq.m_values.get(r * v.length + i));
			}
		}
		
		asq.m_values.clear();
		
		return end_time - start_time;
	}
}
