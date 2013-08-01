package edu.cmu.cs.able.eseb.rpc;

import incubator.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;
import auxtestlib.TestPropertiesDefinition;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.conn.BusConnectionState;
import edu.cmu.cs.able.eseb.participant.ParticipantIdentifier;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;


/**
 * Test suite that tests RPC execution directly using metadata.
 */
@SuppressWarnings("javadoc")
public class MetadataRpcExecutionTest extends DefaultTCase {
	/**
	 * The event bus.
	 */
	private EventBus m_bus;
	
	/**
	 * Client providing the service.
	 */
	private BusConnection m_service_client;
	
	/**
	 * Participant of the client providing the service.
	 */
	private ParticipantIdentifier m_service_participant;
	
	/**
	 * Client invoking the service.
	 */
	private BusConnection m_invoke_client;
	
	/**
	 * Participant of the client invoking the service.
	 */
	private ParticipantIdentifier m_invoke_participant;
	
	/**
	 * Test service to be invoked remotely.
	 */
	private TestService m_test_service;
	
	/**
	 * Operation registry.
	 */
	private OperationRegistry m_registry;
	
	/**
	 * Operation execution, the remote stub.
	 */
	private RemoteOperationStub m_execution;
	
	/**
	 * RPC environment used in the service.
	 */
	private RpcEnvironment m_service_environment;
	
	/**
	 * RPC environment used in the invoker.
	 */
	private RpcEnvironment m_invoke_environment;
	
	@Before
	public void set_up() throws Exception {
		
		short port = (short) TestPropertiesDefinition.getInt(
				"free-port-zone-start");
		
		/*
		 * Create and start the event bus.
		 */
		PrimitiveScope bus_scope = new PrimitiveScope();
		m_bus = new EventBus(port, bus_scope);
		m_bus.start();
		
		/*
		 * Create the connection used by the service publisher to connect to
		 * the event bus.
		 */
		final PrimitiveScope service_scope = new PrimitiveScope();
		m_service_client = new BusConnection("localhost", port, service_scope);
		m_service_client.start();
		
		/*
		 * Create the service participant so that the service connection has
		 * an ID which is used to invoke this service.
		 */
		m_service_participant = new ParticipantIdentifier(m_service_client);
		
		/*
		 * Create the service RPC environment.
		 */
		m_service_environment = new RpcEnvironment(m_service_client);
		
		/*
		 * Create the service metadata on the service side.
		 */
		final OperationInformation service_oi =
				m_service_environment.operation_information();
		DataValue s_g = service_oi.create_group();
		DataValue s_op = service_oi.create_operation("foo");
		service_oi.add_operation_to_group(s_g, s_op);
		service_oi.add_parameter(s_op, service_scope.int32(), "x",
				ParameterDirection.INPUT);
		service_oi.add_parameter(s_op, service_scope.int32(), "y",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the object that will execute the service.
		 */
		ServiceOperationExecuter executer = new ServiceOperationExecuter() {
			@Override
			public Pair<Map<String, DataValue>, FailureInformation> execute(
					DataValue operation,
					Map<String, DataValue> input_arguments) throws Exception {
				int x = ((Int32Value) input_arguments.get("x")).value();
				
				Pair<Integer, FailureInformation> p =
						m_test_service.execute(x);
				if (p.first() != null) {
					Map<String, DataValue> output = new HashMap<>();
					output.put("y", service_scope.int32().make(p.first()));
					return new Pair<>(output, null);
				} else {
					return new Pair<>(null, p.second());
				}
			}
		};
		
		/*
		 * Create the object registration. The ID used, 3, must be known
		 * by both client and server.
		 */
		ServiceObjectRegistration sor = new ServiceObjectRegistration(
				service_oi, s_g, executer, 3);
		
		/*
		 * Publish the service.
		 */
		m_registry = new OperationRegistry(m_service_client, service_oi, s_g,
				executer, 3);
		m_registry.install();
		sor.publish(m_registry);
		
		/*
		 * Create the connection that will be used by the object used to
		 * invoke the service.
		 */
		PrimitiveScope invoke_scope = new PrimitiveScope();
		m_invoke_client = new BusConnection("localhost", port, invoke_scope);
		m_invoke_client.start();
		
		/*
		 * Create the participant so that the invoker has an ID.
		 */
		m_invoke_participant = new ParticipantIdentifier(m_invoke_client);
		
		/*
		 * Create the invoker RPC environment.
		 */
		m_invoke_environment = new RpcEnvironment(m_invoke_client);
		
		/*
		 * Create the service metadata on the invoker side.
		 */
		final OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op, invoke_scope.int32(), "x",
				ParameterDirection.INPUT);
		invoke_oi.add_parameter(i_op, invoke_scope.int32(), "y",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the stub that is used to invoke the service. Note that the
		 * object ID, 3, has to be known by the remote invoker.
		 */
		m_execution = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, 3);
		
		/*
		 * Wait for all connections to establish.
		 */
		wait_for_true(new BooleanEvaluation() {
			@Override
			public boolean evaluate() throws Exception {
				return m_service_client.state() == BusConnectionState.CONNECTED
						&& m_invoke_client.state()
						== BusConnectionState.CONNECTED;
			}
		});
	}
	
	@After
	public void tear_down() throws Exception {
		if (m_bus != null) {
			m_bus.close();
		}
		
		if (m_bus != null) {
			m_invoke_client.close();
		}
		
		if (m_service_client != null) {
			m_service_client.close();
		}
		
		if (m_invoke_participant != null) {
			m_invoke_participant.close();
		}
		
		if (m_service_participant != null) {
			m_service_participant.close();
		}
		
		if (m_service_environment != null) {
			m_service_environment.close();
		}
		
		if (m_invoke_environment != null) {
			m_invoke_environment.close();
		}
	}
	
	@Test
	public void remote_rpc_success() throws Exception {
		/*
		 * Provide an implementation for the server-side service.
		 */
		m_test_service = new TestService() {
			@Override
			public Pair<Integer, FailureInformation> execute(int x) {
				return new Pair<>(new Integer(x + 1), null);
			}
		};
		
		/*
		 * Create the arguments and invoke the execution.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecution re = m_execution.execute(args);
		
		/*
		 * Wait for the execution to finish and obtain the result.
		 */
		RemoteExecutionResult result = re.get(1, TimeUnit.SECONDS);
		assertTrue(result.successful());
		Map<String, DataValue> output = result.output_arguments();
		assertEquals(1, output.size());
		assertEquals(m_invoke_client.primitive_scope().int32().make(5),
				output.get("y"));
	}
	
	@Test
	public void remote_rpc_failure() throws Exception {
		fail("NYI");
	}
	
	@Test
	public void remote_rpc_timeout() throws Exception {
		fail("NYI");
	}
	
	@Test
	public void remote_rpc_not_exists_object() throws Exception {
		fail("NYI");
	}
	
	@Test
	public void remote_rpc_not_exists_operation() throws Exception {
		fail("NYI");
	}
}
