package edu.cmu.cs.able.eseb.rpc;

import incubator.dispatch.DispatchHelper;
import incubator.exh.ExhHelper;

import java.io.Closeable;

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
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;

/**
 * Tests RPC execution using Java classes.
 */
@SuppressWarnings("javadoc")
public class JavaRpcExecutionTest extends DefaultTCase {
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
	 * RPC environment used in the service.
	 */
	private RpcEnvironment m_service_environment;
	
	/**
	 * RPC environment used in the invoker.
	 */
	private RpcEnvironment m_invoke_environment;
	
	/**
	 * The java service implementation that is invoked.
	 */
	private RemoteJavaRpcTestService m_java_service;
	
	/**
	 * The stub of the java service.
	 */
	private RemoteJavaRpcTestService m_stub;
	
	/**
	 * The closeable used to close the service.
	 */
	private Closeable m_service_closeable;
	
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
		 * Publish the service on the service side.
		 */
		m_java_service = new RemoteJavaRpcTestService() {
			@Override
			public int returns_number_plus_one(int value) {
				return value + 1;
			}

			@Override
			public void no_return(int value1, int value2) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void no_arguments() {
				/*
				 * Nothing to do.
				 */
			}
		};
		m_service_closeable = JavaRpcFactory.create_registry_wrapper(
				RemoteJavaRpcTestService.class,
				new RemoteJavaRpcTestService() {
					@Override
					public int returns_number_plus_one(int value) {
						return m_java_service.returns_number_plus_one(value);
					}

					@Override
					public void no_return(int value1, int value2) {
						m_java_service.no_return(value1, value2);
					}

					@Override
					public void no_arguments() {
						m_java_service.no_arguments();
					}
			}, m_service_environment, "3");
		
		
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
		 * Create the stub that is used to invoke the service. Note that the
		 * object ID, 3, has to be known by the remote invoker.
		 */
		m_stub = JavaRpcFactory.create_remote_stub(
				RemoteJavaRpcTestService.class, m_invoke_environment,
				m_service_participant.id(), 500, "3");
		
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
		
		if (m_service_closeable != null) {
			m_service_closeable.close();
		}
		
		if (m_service_environment != null) {
			m_service_environment.close();
		}
		
		if (m_stub != null) {
			((Closeable) m_stub).close();
		}
		
		if (m_invoke_environment != null) {
			m_invoke_environment.close();
		}
	}
	
	@Test
	public void remote_rpc_success() throws Exception {
		int r = m_stub.returns_number_plus_one(7);
		assertEquals(8, r);
	}
	
	@Test
	public void remote_rpc_failure() throws Exception {
		m_java_service = new RemoteJavaRpcTestService() {
			@Override
			public int returns_number_plus_one(int value) {
				throw new RuntimeException("foobar");
			}

			@Override
			public void no_return(int value1, int value2) {
				/* */
			}

			@Override
			public void no_arguments() {
				/* */
			}
		};
		
		try {
			m_stub.returns_number_plus_one(7);
			fail();
		} catch (OperationFailureException e) {
			assertTrue(e.getMessage().contains("RuntimeException"));
			assertTrue(e.getMessage().contains("foobar"));
		}
	}
	
	@Test(expected = OperationTimedOutException.class)
	public void remote_rpc_timeout() throws Exception {
		m_java_service = new RemoteJavaRpcTestService() {
			@Override
			public int returns_number_plus_one(int value) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					/*
					 * We don't care.
					 */
				}
				
				return 0;
			}

			@Override
			public void no_return(int value1, int value2) {
				/* */
			}

			@Override
			public void no_arguments() {
				/* */
			}
		};
		
		m_stub.returns_number_plus_one(7);
	}
	
	@Test
	public void execute_method_twice() throws Exception {
		int r = m_stub.returns_number_plus_one(10);
		assertEquals(11, r);
		r = m_stub.returns_number_plus_one(-11);
		assertEquals(-10, r);
	}
	
	@Test
	public void execute_method_no_return() throws Exception {
		final int[] x = new int[2];
		
		m_java_service = new RemoteJavaRpcTestService() {
			@Override
			public void no_return(int value1, int value2) {
				x[0] = value1;
				x[1] = value2;
			}

			@Override
			public int returns_number_plus_one(int value) {
				return 0;
			}

			@Override
			public void no_arguments() {
				/* */
			}
		};
		
		m_stub.no_return(-3, 3);
		assertEquals(-3, x[0]);
		assertEquals(3, x[1]);
	}
	
	@Test
	public void execute_method_no_arguments() throws Exception {
		final int[] x = new int[1];
		x[0] = 0;
		
		m_java_service = new RemoteJavaRpcTestService() {
			@Override
			public int returns_number_plus_one(int value) {
				return 0;
			}
			
			@Override
			public void no_return(int value1, int value2) {
				/* */
			}
			
			@Override
			public void no_arguments() {
				x[0]++;
			}
		};
		
		m_stub.no_arguments();
		assertEquals(1, x[0]);
	}
	
	interface i_more_than_one_method_with_the_same_name {
		@ParametersTypeMapping({"int32"})
		void x(int i);
		
		@ParametersTypeMapping({"int32", "int32"})
		void x(int i, int j);
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void more_than_one_method_with_the_same_name() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_more_than_one_method_with_the_same_name.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_too_many_parameters_in_declaration {
		@ParametersTypeMapping({"int32", "int32"})
		void x(int i);
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void too_many_parameters_in_declaration() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_too_many_parameters_in_declaration.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_too_few_parameters_in_declaration {
		@ParametersTypeMapping({})
		void x(int i);
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void too_few_parameters_in_declaration() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_too_few_parameters_in_declaration.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_no_parameter_declaration_with_parameters_in_method {
		void x(int i);
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void no_parameter_declaration_with_parameters_in_method()
			throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_no_parameter_declaration_with_parameters_in_method.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_no_parameter_declaration_without_parameters_in_method{
		void x();
	}
	
	@Test
	public void no_parameter_declaration_without_parameters_in_method()
			throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_no_parameter_declaration_without_parameters_in_method.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_no_return_type_with_return_type_declaration {
		@ReturnTypeMapping("")
		void x();
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void no_return_type_with_return_type_declaration()
			throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_no_return_type_with_return_type_declaration.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_return_type_with_no_return_type_declaration {
		int x();
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void return_type_with_no_return_type_declaration()
			throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_return_type_with_no_return_type_declaration.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_invalid_data_type_name_in_parameter {
		@ParametersTypeMapping({"int10"})
		void x(int i);
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void invalid_data_type_name_in_parameter() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_invalid_data_type_name_in_parameter.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_unparseble_data_type_name_in_parameter {
		@ParametersTypeMapping({"x x"})
		void x(int i);
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void unparseble_data_type_name_in_parameter() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_unparseble_data_type_name_in_parameter.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_unparseble2_data_type_name_in_parameter {
		@ParametersTypeMapping({"@@"})
		void x(int i);
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void unparseble2_data_type_name_in_parameter() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_unparseble2_data_type_name_in_parameter.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_invalid_data_type_name_in_return_type {
		@ReturnTypeMapping("int10")
		int x();
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void invalid_data_type_name_in_return_type() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_invalid_data_type_name_in_return_type.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_unparseable_data_type_name_in_return_type {
		@ReturnTypeMapping("x x")
		int x();
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void unparseable_data_type_name_in_return_type() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_unparseable_data_type_name_in_return_type.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	interface i_unparseable2_data_type_name_in_return_type {
		@ReturnTypeMapping("@@")
		int x();
	}
	
	@Test(expected = IllegalServiceDefinitionException.class)
	public void unparseable2_data_type_name_in_return_type() throws Exception {
		JavaRpcFactory.create_remote_stub(
				i_unparseable2_data_type_name_in_return_type.class,
				m_invoke_environment, m_service_participant.id(), 100, "8");
	}
	
	@Test
	public void multiple_services_invoked_same_environment() throws Exception {
		invoke_multiple_services(m_service_environment, m_invoke_environment);
	}
	
	@Test
	public void multiple_services_invoked_different_environment()
			throws Exception {
		try (RpcEnvironment senv2 = new RpcEnvironment(m_service_client,
				"sfoo");
			RpcEnvironment ienv2 = new RpcEnvironment(m_invoke_client,
				"ifoo")) {
			invoke_multiple_services(senv2, ienv2);
		}
	}
	
	/**
	 * Invokes multiple services.
	 * @param senv the environment of the second service
	 * @param renv the environment of the second invoker
	 * @throws Exception
	 */
	private void invoke_multiple_services(RpcEnvironment senv, 
			RpcEnvironment renv) throws Exception {
		final int[] called = new int[2];
		
		try (Closeable s2 = JavaRpcFactory.create_registry_wrapper(
				RemoteJavaRpcTestService.class, new RemoteJavaRpcTestService() {
			@Override
			public int returns_number_plus_one(int value) {
				return 0;
			}
			
			@Override
			public void no_return(int value1, int value2) {
				/*
				 * Nothing to do.
				 */
			}
			
			@Override
			public void no_arguments() {
				called[0]++;
			}
		}, senv, "7")) {
			m_java_service = new RemoteJavaRpcTestService() {
				@Override
				public int returns_number_plus_one(int value) {
					return 0;
				}
				
				@Override
				public void no_return(int value1, int value2) {
					/*
					 * Nothing to do.
					 */
				}
				
				@Override
				public void no_arguments() {
					called[1]++;
				}
			};
			
			RemoteJavaRpcTestService r2 = JavaRpcFactory.create_remote_stub(
					RemoteJavaRpcTestService.class, renv,
					senv.participant_id(), 10000, "7");
			
			assertEquals(0, called[0]);
			assertEquals(0, called[1]);
			
			r2.no_arguments();
			
			assertEquals(1, called[0]);
			assertEquals(0, called[1]);
			
			m_stub.no_arguments();
			
			assertEquals(1, called[0]);
			assertEquals(1, called[1]);
		
			((Closeable) r2).close(); 
		}
	}
}
