package edu.cmu.cs.able.eseb.participant;

import incubator.dispatch.DispatchHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.TestPropertiesDefinition;
import auxtestlib.ThreadCountTestHelper;
import edu.cmu.cs.able.eseb.TestArraySaveQueue;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.participant.Participant;
import edu.cmu.cs.able.eseb.participant.ParticipantIdentifier;
import edu.cmu.cs.able.eseb.participant.ParticipantModelFilter;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.struct.Field;
import edu.cmu.cs.able.typelib.struct.FieldDescription;
import edu.cmu.cs.able.typelib.struct.StructureDataType;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Tests that participants publish their information and are informed of
 * each other.
 */
@SuppressWarnings("javadoc")
public class ParticipantIdentificationTest extends DefaultTCase {
	@TestHelper
	public ThreadCountTestHelper m_thread_count_helper;
	@TestHelper
	public DispatchHelper m_dispatcher_helper;
	
	/**
	 * Primitive scope.
	 */
	private PrimitiveScope m_scope;
	
	/**
	 * A free port.
	 */
	private short m_port;
	
	/**
	 * Renew interval in milliseconds.
	 */
	private long m_renew_ms;
	
	/**
	 * Encoding to use.
	 */
	private TextEncoding m_enc;
	
	@Before
	public void set_up() throws Exception {
		m_scope = new PrimitiveScope();
		m_port = (short) TestPropertiesDefinition.getInt(
				"free-port-zone-start");
		m_renew_ms = 100;
		m_enc = new DefaultTextEncoding(m_scope);
	}
	
	@Test
	public void two_participants_are_informed_of_each_other()
			throws Exception {
		try (EventBus bs = new EventBus(m_port, m_scope);
				BusConnection bc1 = new BusConnection("localhost", m_port,
				m_scope);
				BusConnection bc2 = new BusConnection("localhost", m_port,
				m_scope);
				ParticipantIdentifier pi1 = new ParticipantIdentifier(bc1,
						m_renew_ms);
				ParticipantIdentifier pi2 = new ParticipantIdentifier(bc2,
						m_renew_ms)) {
			bs.start();
			bc1.start();
			bc2.start();
			
			ParticipantModelFilter f1 = new ParticipantModelFilter(m_renew_ms,
					0, m_scope, m_enc);
			bc1.incoming_chain().add_filter(f1);
			
			ParticipantModelFilter f2 = new ParticipantModelFilter(m_renew_ms,
					0, m_scope, m_enc);
			bc2.incoming_chain().add_filter(f2);
			
			Thread.sleep(m_renew_ms * 2);
			
			Collection<Participant> pl1 = f1.model().all_scbs();
			assertEquals(2, pl1.size());
			Iterator<Participant> i1 = pl1.iterator();
			Participant pl1_1 = i1.next();
			Participant pl1_2 = i1.next();
			
			Collection<Participant> pl2 = f2.model().all_scbs();
			assertEquals(2, pl2.size());
			Iterator<Participant> i2 = pl1.iterator();
			Participant pl2_1 = i2.next();
			Participant pl2_2 = i2.next();
			
			assertTrue(pl1_1.id().equals(pi1.id())
					|| pl1_1.id().equals(pi2.id()));
			assertTrue(pl1_2.id().equals(pi1.id())
					|| pl1_2.id().equals(pi2.id()));
			assertTrue(pl1_1.id().equals(pi1.id())
					|| pl1_2.id().equals(pi1.id()));
			assertTrue(pl1_1.id().equals(pi2.id())
					|| pl1_2.id().equals(pi2.id()));
			
			assertTrue(pl2_1.id().equals(pi1.id())
					|| pl2_1.id().equals(pi2.id()));
			assertTrue(pl2_2.id().equals(pi1.id())
					|| pl2_2.id().equals(pi2.id()));
			assertTrue(pl2_1.id().equals(pi1.id())
					|| pl2_2.id().equals(pi1.id()));
			assertTrue(pl2_1.id().equals(pi2.id())
					|| pl2_2.id().equals(pi2.id()));
			
			f1.shutdown();
			f2.shutdown();
		}
	}
	
	@Test
	public void participant_not_renewing_is_removed() throws Exception {
		try (EventBus bs = new EventBus(m_port, m_scope);
				BusConnection bc1 = new BusConnection("localhost", m_port,
				m_scope);
				BusConnection bc2 = new BusConnection("localhost", m_port,
				m_scope);
				ParticipantIdentifier pi1 = new ParticipantIdentifier(bc1,
						m_renew_ms);
				ParticipantIdentifier pi2 = new ParticipantIdentifier(bc2,
						m_renew_ms)) {
			bs.start();
			bc1.start();
			bc2.start();
			
			ParticipantModelFilter f1 = new ParticipantModelFilter(m_renew_ms,
					0, m_scope, m_enc);
			bc1.incoming_chain().add_filter(f1);
			
			ParticipantModelFilter f2 = new ParticipantModelFilter(m_renew_ms,
					0, m_scope, m_enc);
			bc2.incoming_chain().add_filter(f2);
			
			Thread.sleep(m_renew_ms * 2);
			
			Collection<Participant> pl1 = f1.model().all_scbs();
			assertEquals(2, pl1.size());
			Collection<Participant> pl2 = f2.model().all_scbs();
			assertEquals(2, pl2.size());
			
			bc2.close();
			
			Thread.sleep(m_renew_ms * 2);
			
			pl1 = f1.model().all_scbs();
			assertEquals(1, pl1.size());
			pl2 = f2.model().all_scbs();
			assertEquals(0, pl2.size());
			
			assertEquals(pi1.id(), pl1.iterator().next().id());
			
			f1.shutdown();
			f2.shutdown();
		}
	}
	
	@Test
	public void other_events_pass_through() throws Exception {
		try (EventBus bs = new EventBus(m_port, m_scope);
				BusConnection bc1 = new BusConnection("localhost", m_port,
				m_scope);
				BusConnection bc2 = new BusConnection("localhost", m_port,
				m_scope);
				ParticipantIdentifier pi1 = new ParticipantIdentifier(bc1,
						m_renew_ms);
				ParticipantIdentifier pi2 = new ParticipantIdentifier(bc2,
						m_renew_ms)) {
			bs.start();
			bc1.start();
			bc2.start();
			
			final TestArraySaveQueue q2 = new TestArraySaveQueue();
			bc2.queue_group().add(q2);
			
			ParticipantModelFilter f1 = new ParticipantModelFilter(m_renew_ms,
					0, m_scope, m_enc);
			bc1.incoming_chain().add_filter(f1);
			
			ParticipantModelFilter f2 = new ParticipantModelFilter(m_renew_ms,
					0, m_scope, m_enc);
			bc2.incoming_chain().add_filter(f2);
			
			bc1.send(m_scope.int32().make(5));
			
			Thread.sleep(2 * m_renew_ms);
			
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return q2.m_values.size() > 0;
				}
			});
			
			assertEquals(1, q2.m_values.size());
			assertEquals(m_scope.int32().make(5), q2.m_values.get(0));
			
			f1.shutdown();
			f2.shutdown();
		}
	}
	
	@Test
	public void meta_data_is_sent() throws Exception {
		try (EventBus bs = new EventBus(m_port, m_scope);
				BusConnection bc1 = new BusConnection("localhost", m_port,
				m_scope);
				BusConnection bc2 = new BusConnection("localhost", m_port,
				m_scope);
				ParticipantIdentifier pi1 = new ParticipantIdentifier(bc1,
						m_renew_ms)) {
			bs.start();
			bc1.start();
			bc2.start();
			
			pi1.meta_data("X", m_scope.ascii().make("foo"));
			
			ParticipantModelFilter f2 = new ParticipantModelFilter(m_renew_ms,
					0, m_scope, m_enc);
			bc2.incoming_chain().add_filter(f2);
			
			Thread.sleep(2 * m_renew_ms);
			
			assertEquals(1, f2.model().all_scbs().size());
			Participant p = f2.model().all_scbs().iterator().next();
			assertEquals(1, p.meta_data_keys().size());
			assertEquals("X", p.meta_data_keys().iterator().next());
			assertEquals(m_scope.ascii().make("foo"), p.meta_data("X"));
			
			f2.shutdown();
		}
	}
	
	@Test
	public void meta_data_changes_are_updated() throws Exception {
		try (EventBus bs = new EventBus(m_port, m_scope);
				BusConnection bc1 = new BusConnection("localhost", m_port,
				m_scope);
				BusConnection bc2 = new BusConnection("localhost", m_port,
				m_scope);
				ParticipantIdentifier pi1 = new ParticipantIdentifier(bc1,
						m_renew_ms)) {
			bs.start();
			bc1.start();
			bc2.start();
			
			pi1.meta_data("X", m_scope.ascii().make("foo"));
			
			ParticipantModelFilter f2 = new ParticipantModelFilter(m_renew_ms,
					0, m_scope, m_enc);
			bc2.incoming_chain().add_filter(f2);
			
			Thread.sleep(2 * m_renew_ms);
			
			assertEquals(1, f2.model().all_scbs().size());
			Participant p = f2.model().all_scbs().iterator().next();
			assertEquals(1, p.meta_data_keys().size());
			assertEquals("X", p.meta_data_keys().iterator().next());
			assertEquals(m_scope.ascii().make("foo"), p.meta_data("X"));
			
			pi1.meta_data("X", m_scope.ascii().make("bar"));
			Thread.sleep(2 * m_renew_ms);
			assertEquals(m_scope.ascii().make("bar"), p.meta_data("X"));
			
			f2.shutdown();
		}
	}
	
	@Test
	public void invalid_meta_data_types() throws Exception {
		PrimitiveScope pscope2 = new PrimitiveScope();
		try (EventBus bs = new EventBus(m_port, m_scope);
				BusConnection bc1 = new BusConnection("localhost", m_port,
				m_scope);
				BusConnection bc2 = new BusConnection("localhost", m_port,
				pscope2);
				ParticipantIdentifier pi1 = new ParticipantIdentifier(bc1,
						m_renew_ms)) {
			bs.start();
			bc1.start();
			bc2.start();
			
			StructureDataType new_type = new StructureDataType("fu", false,
					new HashSet<FieldDescription>(), m_scope.any());
			m_scope.add(new_type);
			
			pi1.meta_data("X", new_type.make(new HashMap<Field, DataValue>()));
			
			ParticipantModelFilter f2 = new ParticipantModelFilter(m_renew_ms,
					0, pscope2, m_enc);
			bc2.incoming_chain().add_filter(f2);
			
			Thread.sleep(2 * m_renew_ms);
			
			assertEquals(1, f2.model().all_scbs().size());
			Participant p = f2.model().all_scbs().iterator().next();
			assertEquals(1, p.meta_data_keys().size());
			assertEquals("X", p.meta_data_keys().iterator().next());
			assertNull(p.meta_data("X"));
			
			f2.shutdown();
		}
	}
}
