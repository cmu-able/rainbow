package edu.cmu.cs.able.eseb;

import incubator.exh.ExhHelper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;

import org.junit.Test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.TestPropertiesDefinition;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.conn.BusConnectionState;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.DelegateTextEncoding;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case that shows how to send and receive a custom data type.
 */
public class SendReceiveCustomDataType extends DefaultTCase {
	/**
	 * Exception helper.
	 */
	@TestHelper
	private ExhHelper m_exh_helper;
	
	/**
	 * Port for the bus server to use.
	 */
	private short m_bus_server_port;
	
	/**
	 * The test case.
	 * @throws Exception test failed
	 */
	@Test
	public void send_receive_custom_data_type() throws Exception {
		setup_base();
		try (EventBus bus = setup_bus();
				BusConnection receiver = setup_connection();
				BusConnection sender = setup_connection()) {
			/*
			 * Get the data type to send the data to and create a value of
			 * that type. The type has to be the data type in the sender scope.
			 */
			CustomType sender_type =
					(CustomType) sender.primitive_scope().find("custom");
			assertNotNull(sender_type);
			CustomValue sender_value = new CustomValue(sender_type);
			
			/*
			 * Register a queue with the receiver to make sure we can get
			 * the value.
			 */
			final TestArraySaveQueue q = new TestArraySaveQueue();
			receiver.queue_group().add(q);
			
			/*
			 * Send the value and wait to be received.
			 */
			sender.send(sender_value);
			wait_for_true(new BooleanEvaluation() {
				@Override
				public boolean evaluate() throws Exception {
					return q.m_values.size() > 0;
				}
			});
			
			/*
			 * Make sure the received value has the right type.
			 */
			DataValue received_value = q.m_values.get(0);
			assertTrue(received_value instanceof CustomValue);
			assertFalse(sender_type.is_instance(received_value));
			CustomType receiver_type = 
					(CustomType) receiver.primitive_scope().find("custom");
			assertTrue(receiver_type.is_instance(received_value));
		}
	}
	
	/**
	 * Sets up common stuff for the test case.
	 * @throws Exception setup failed
	 */
	private void setup_base() throws Exception {
		m_bus_server_port = (short) TestPropertiesDefinition.getInt(
				"free-port-zone-start");
	}
	
	/**
	 * Creates and starts the event bus.
	 * @return the event bus, which has already been started
	 * @throws Exception failed to start the event bus
	 */
	private EventBus setup_bus() throws Exception {
		PrimitiveScope bus_scope = new PrimitiveScope();
		CustomType ct = new CustomType();
		bus_scope.add(ct);
		
		/*
		 * Create the encoding to use.
		 */
		DefaultTextEncoding encoding = new DefaultTextEncoding(bus_scope);
		encoding.add(new CustomEncoder());
		
		/*
		 * Creat the bus.
		 */
		EventBus bus = new EventBus(m_bus_server_port, bus_scope, encoding);
		bus.start();
		return bus;
	}
	
	/**
	 * Creates and starts a bus connection.
	 * @return the bus connection, which has already been started
	 * @throws Exception failed to create or start the connection
	 */
	private BusConnection setup_connection() throws Exception {
		/*
		 * Create the data type structure.
		 */
		PrimitiveScope conn_scope = new PrimitiveScope();
		CustomType ct = new CustomType();
		conn_scope.add(ct);
		
		/*
		 * Create the encoding to use.
		 */
		DefaultTextEncoding encoding = new DefaultTextEncoding(conn_scope);
		encoding.add(new CustomEncoder());
		
		final BusConnection sender = new BusConnection("localhost",
				m_bus_server_port, conn_scope, encoding);
		sender.start();
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return sender.state() == BusConnectionState.CONNECTED;
			}
		});
		
		return sender;
	}
	
	/**
	 * Custom data type to send through the bus.
	 */
	private static class CustomType extends DataType {
		/**
		 * Creates a custom data type.
		 */
		private CustomType() {
			super("custom", new HashSet<DataType>());
		}

		@Override
		public boolean is_abstract() {
			return false;
		}
	}
	
	/**
	 * Custom data value to send through the bus.
	 */
	private static class CustomValue extends DataValue {
		/**
		 * Creates a custom value.
		 * @param type the data type
		 */
		protected CustomValue(DataType type) {
			super(type);
		}

		@Override
		public DataValue clone() throws CloneNotSupportedException {
			return new CustomValue(type());
		}
	}
	
	/**
	 * Encoding used to send/receive the custom values.
	 */
	private static class CustomEncoder implements DelegateTextEncoding {
		@Override
		public boolean supports(DataType t) {
			return t instanceof CustomType;
		}

		@Override
		public void encode(DataValue v, Writer w, TextEncoding enc)
				throws IOException {
			w.write('x');
		}

		@Override
		public DataValue decode(Reader r, DataType type, DataTypeScope dts,
				TextEncoding enc) throws IOException, InvalidEncodingException {
			char ch = (char) r.read();
			assertEquals('x', ch);
			return new CustomValue(type);
		}
		
	}
}
