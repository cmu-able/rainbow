package edu.cmu.cs.able.eseb.rpc;

import incubator.Pair;
import incubator.dispatch.DispatchHelper;
import incubator.exh.ExhHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;
import auxtestlib.TestPropertiesDefinition;
import auxtestlib.ThreadCountTestHelper;
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
	 * Helper that ensures we stop all threads.
	 */
	@TestHelper
	public ThreadCountTestHelper m_thread_count_helper;
	
	/**
	 * Prints all exceptions to the stderr.
	 */
	@TestHelper
	public ExhHelper m_exh_helper;
	
	/**
	 * Makes sure we close all dispatcher threads.
	 */
	@TestHelper
	public DispatchHelper m_dispatcher_helper;
	
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
	private ServiceObjectRegistration m_sor;
	
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
		m_service_environment = new RpcEnvironment(m_service_client,
				m_service_participant.id());
		
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
		 * Provide an implementation for the server-side service.
		 */
		m_test_service = new TestService() {
			@Override
			public Pair<Integer, FailureInformation> execute(int x) {
				return new Pair<>(new Integer(x + 1), null);
			}
		};
		
		/*
		 * Create the object registration. The ID used, 3, must be known
		 * by both client and server.
		 */
		m_sor = ServiceObjectRegistration.make(executer, s_g, "3",
				m_service_environment);
		
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
		m_invoke_environment = new RpcEnvironment(m_invoke_client,
				m_invoke_participant.id());
		
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
				m_service_participant.id(), i_op, "3");
		
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
		
		if (m_sor != null) {
			m_sor.close();
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
		/*
		 * Provide an implementation for the server-side service.
		 */
		m_test_service = new TestService() {
			@Override
			public Pair<Integer, FailureInformation> execute(int x) {
				return new Pair<>(null, new FailureInformation("x", "y", "z"));
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
		assertFalse(result.successful());
		assertEquals("x", result.failure_information().type());
		assertEquals("y", result.failure_information().description());
		assertEquals("z", result.failure_information().data());
	}
	
	@Test(expected = TimeoutException.class)
	public void remote_rpc_timeout() throws Exception {
		final long timeout_ms = 250;
		
		/*
		 * Provide an implementation for the server-side service.
		 */
		m_test_service = new TestService() {
			@Override
			public Pair<Integer, FailureInformation> execute(int x) {
				try {
					Thread.sleep(2 * timeout_ms);
				} catch (InterruptedException e) {
					/*
					 * We don't care :)
					 */
				}
				
				return new Pair<>(new Integer(0), null);
			}
		};
		
		/*
		 * Create the arguments and invoke the execution.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecution re = m_execution.execute(args);
		
		/*
		 * We should get a timeout.
		 */
		re.get(timeout_ms, TimeUnit.MILLISECONDS);
	}
	
	@Test
	public void remote_rpc_not_exists_object() throws Exception {
		final boolean was_executed[] = new boolean[1];
		
		/*
		 * Provide an implementation for the server-side service.
		 */
		m_test_service = new TestService() {
			@Override
			public Pair<Integer, FailureInformation> execute(int x) {
				was_executed[0] = true;
				return new Pair<>(new Integer(x + 1), null);
			}
		};
		
		/*
		 * Create the service metadata on the invoker side. This is the same
		 * operation we use in the set up.
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "x",
				ParameterDirection.INPUT);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "y",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the stub that is used to invoke the operation that exists
		 * but in an object which does not exist -- object 4.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, "4");
		
		/*
		 * Perform the remote invocation. We should get an error.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecutionResult rer = ros.execute(args).get(1, TimeUnit.SECONDS);
		assertFalse(rer.successful());
		assertNotNull(rer.failure_information());
		assertFalse(was_executed[0]);
	}
	
	@Test
	public void remote_rpc_not_exists_operation() throws Exception {
		/*
		 * Create a reference on the client side to an operation that does
		 * not exist in the server side. 
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo2");
		invoke_oi.add_operation_to_group(i_g, i_op);
		
		/*
		 * Create the stub that is used to invoke the non-existent
		 * operation.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, "3");
		
		/*
		 * Perform the remote invocation. We should get an error.
		 */
		RemoteExecutionResult rer = ros.execute(
				new HashMap<String, DataValue>()).get(1, TimeUnit.SECONDS);
		assertFalse(rer.successful());
		assertNotNull(rer.failure_information());
	}
	
	@Test
	public void invoke_unpublished_service() throws Exception {
		/*
		 * Make the service unavailable.
		 */
		m_sor.close();
		
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
		assertFalse(result.successful());
		
		/*
		 * This should have no effect but should be allowed.
		 */
		m_sor.close();
	}
	
	@Test
	public void invoke_using_extra_input_arguments() throws Exception {
		/*
		 * Create the service metadata on the invoker side with an extra
		 * parameter.
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "x",
				ParameterDirection.INPUT);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "z",
				ParameterDirection.INPUT);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "y",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the stub that is used to invoke the operation.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, "3");
		
		/*
		 * Perform the remote invocation. We should get an error.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		args.put("z", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecutionResult rer = ros.execute(args).get(1, TimeUnit.SECONDS);
		assertFalse(rer.successful());
		assertNotNull(rer.failure_information());
	}
	
	@Test
	public void invoke_using_extra_output_argument() throws Exception {
		/*
		 * Create the service metadata on the invoker side with an extra
		 * parameter.
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "x",
				ParameterDirection.INPUT);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "y",
				ParameterDirection.OUTPUT);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "w",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the stub that is used to invoke the operation.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, "3");
		
		/*
		 * Perform the remote invocation. We should get an error.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecutionResult rer = ros.execute(args).get(1, TimeUnit.SECONDS);
		assertFalse(rer.successful());
		assertNotNull(rer.failure_information());
	}
	
	@Test
	public void invoke_using_missing_input_argument() throws Exception {
		/*
		 * Create the service metadata on the invoker side with an extra
		 * parameter.
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "y",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the stub that is used to invoke the operation.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, "3");
		
		/*
		 * Perform the remote invocation. We should get an error.
		 */
		Map<String, DataValue> args = new HashMap<>();
		RemoteExecutionResult rer = ros.execute(args).get(1, TimeUnit.SECONDS);
		assertFalse(rer.successful());
		assertNotNull(rer.failure_information());
	}
	
	@Test
	public void invoke_using_missing_output_argument() throws Exception {
		/*
		 * Create the service metadata on the invoker side with an extra
		 * parameter.
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "x",
				ParameterDirection.INPUT);
		
		/*
		 * Create the stub that is used to invoke the operation.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, "3");
		
		/*
		 * Perform the remote invocation. We should get an error.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecutionResult rer = ros.execute(args).get(1, TimeUnit.SECONDS);
		assertFalse(rer.successful());
		assertNotNull(rer.failure_information());
	}
	
	@Test
	public void invoke_using_wrong_input_argument_type() throws Exception {
		/*
		 * Create the service metadata on the invoker side with an extra
		 * parameter.
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int64(), "x",
				ParameterDirection.INPUT);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "y",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the stub that is used to invoke the operation.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, "3");
		
		/*
		 * Perform the remote invocation. We should get an error.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int64().make(4));
		RemoteExecutionResult rer = ros.execute(args).get(1, TimeUnit.SECONDS);
		assertFalse(rer.successful());
		assertNotNull(rer.failure_information());
	}
	
	@Test
	public void invoke_using_wrong_output_argument_type() throws Exception {
		/*
		 * Create the service metadata on the invoker side with an extra
		 * parameter.
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "x",
				ParameterDirection.INPUT);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int64(), "y",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the stub that is used to invoke the operation.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				m_service_participant.id(), i_op, "3");
		
		/*
		 * Perform the remote invocation. We should get an error.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecutionResult rer = ros.execute(args).get(1, TimeUnit.SECONDS);
		assertFalse(rer.successful());
		assertNotNull(rer.failure_information());
	}
	
	@Test
	public void remote_execution_support_of_future_methods() throws Exception {
		/*
		 * Create the arguments and invoke the execution.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecution re = m_execution.execute(args);
		
		assertFalse(re.cancel(true));
		assertFalse(re.cancel(false));
		assertFalse(re.isCancelled());
		assertFalse(re.isDone());
		
		/*
		 * Wait for the execution to finish and obtain the result.
		 */
		RemoteExecutionResult result = re.get(1, TimeUnit.SECONDS);
		assertTrue(re.isDone());
		assertTrue(result.successful());
		Map<String, DataValue> output = result.output_arguments();
		assertEquals(1, output.size());
		assertEquals(m_invoke_client.primitive_scope().int32().make(5),
				output.get("y"));
	}
	
	@Test
	public void remote_execution_unlimited_get() throws Exception {
		/*
		 * Create the arguments and invoke the execution.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecution re = m_execution.execute(args);
		
		/*
		 * Wait for the execution to finish and obtain the result. If
		 * something goes wrong, we'll interrupt the thread.
		 */
		Thread t = new Thread() {
			private Thread to_interrupt = Thread.currentThread();
			
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					/*
					 * We'll ignore it.
					 */
				}
				
				to_interrupt.interrupt();
			}
		};
		
		t.start();
		
		RemoteExecutionResult result = re.get(0, TimeUnit.SECONDS);
		assertTrue(result.successful());
		Map<String, DataValue> output = result.output_arguments();
		assertEquals(1, output.size());
		assertEquals(m_invoke_client.primitive_scope().int32().make(5),
				output.get("y"));
		
		try {
			t.join();
		} catch (InterruptedException e) {
			/*
			 * OK. We can get this because after sleeping, the thread will 
			 * interrupt us. Tricky but we don't really care.
			 */
		}
	}
	
	@Test
	public void parameter_direction_enum_auto_methods() throws Exception {
		cover_enumeration(ParameterDirection.class);
	}
	
	@Test
	public void double_close_rpc_environment() throws Exception {
		m_invoke_environment.close();
		m_invoke_environment.close();
	}
	
	@Test
	public void exception_thrown_during_service_invocation() throws Exception {
		/*
		 * Provide an implementation for the server-side service.
		 */
		m_test_service = new TestService() {
			@Override
			public Pair<Integer, FailureInformation> execute(int x) {
				throw new RuntimeException("FOO");
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
		assertFalse(result.successful());
		assertTrue(result.failure_information().type().contains(
				"RuntimeException"));
		assertEquals("FOO", result.failure_information().description());
		assertTrue(result.failure_information().data().contains("at "));
	}
	
	@Test(expected = TimeoutException.class)
	public void invoking_and_ignoring_executions() throws Exception {
		/*
		 * Create the service metadata on the invoker side. This is the same
		 * operation we use in the set up.
		 */
		OperationInformation invoke_oi =
				m_invoke_environment.operation_information();
		DataValue i_g = invoke_oi.create_group();
		DataValue i_op = invoke_oi.create_operation("foo");
		invoke_oi.add_operation_to_group(i_g, i_op);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "x",
				ParameterDirection.INPUT);
		invoke_oi.add_parameter(i_op,
				m_invoke_client.primitive_scope().int32(), "y",
				ParameterDirection.OUTPUT);
		
		/*
		 * Create the stub that is used to invoke the operation that exists
		 * but in a participant that does not exist. This means we'll never get
		 * a reply.
		 */
		RemoteOperationStub ros = new RemoteOperationStub(m_invoke_environment,
				0, i_op, "3");
		
		/*
		 * Create the arguments and create an invocation.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		RemoteExecution re = ros.execute(args);
		
		/*
		 * For some time, keep creating invocations.
		 */
		long finish = System.currentTimeMillis()
				+ 3 * ExecutionResultReadFilter.MINIMUM_CLEAR_TIME_MS;
		while (System.currentTimeMillis() < finish) {
			ros.execute(args);
			Thread.sleep(10);
			System.gc();
		}
		
		re.get(50, TimeUnit.MILLISECONDS);
	}
	
	@Test
	public void rpc_serial_performance() throws Exception {
		long sample_time_ms = 5000;
		
		/*
		 * Create the arguments and invoke the execution.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		
		long end = System.currentTimeMillis() + sample_time_ms;
		int count = 0;
		while (System.currentTimeMillis() < end) {
			RemoteExecution re = m_execution.execute(args);
			re.get(1, TimeUnit.SECONDS);
			count++;
		}
		
		double rtt_ms = ((double) sample_time_ms) / count;
		System.out.println("Average round-trip time: " + rtt_ms + " ms");
	}
	
	@Test
	public void rpc_parallel_performance() throws Exception {
		long sample_time_ms = 5000;
		int timeout_ms = 3000;
		
		/*
		 * Create the arguments and invoke the execution.
		 */
		Map<String, DataValue> args = new HashMap<>();
		args.put("x", m_invoke_client.primitive_scope().int32().make(4));
		
		int[] parallel_level = { 2, 5, 10, 20, 50 };
		List<Double> p_time = new ArrayList<>();
		
		for (int pl : parallel_level) {
			long end = System.currentTimeMillis() + sample_time_ms;
			int count = 0;
			
			RemoteExecution[] re = new RemoteExecution[pl];
			
			while (System.currentTimeMillis() < end) {
				for (int i = 0; i < pl; i++) {
					re[i] = m_execution.execute(args);
				}
				
				for (int i = 0; i < pl; i++) {
					re[i].get(timeout_ms, TimeUnit.MILLISECONDS);
				}
				
				count += pl;
			}
			
			double rtt_ms = ((double) sample_time_ms) / count;
			p_time.add(rtt_ms);
		}
		
		for (int i = 0; i < parallel_level.length; i++) {
			System.out.println("Average round-trip time (" + parallel_level[i]
				+ " parallel) : " + p_time.get(i) + " ms");
		}
	}
}
