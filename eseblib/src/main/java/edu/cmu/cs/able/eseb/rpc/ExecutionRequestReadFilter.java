package edu.cmu.cs.able.eseb.rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import incubator.Pair;
import incubator.dispatch.LocalDispatcher;
import incubator.exh.ThrowableCollector;
import incubator.pval.Ensure;
import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.filter.EventFilter;
import edu.cmu.cs.able.eseb.filter.EventSink;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Filter that reads requests to execute operations and removes them from
 * the event chain.
 */
class ExecutionRequestReadFilter extends EventFilter {
	/**
	 * The operation information.
	 */
	private OperationInformation m_information;
	
	/**
	 * The ID of this participant.
	 */
	private long m_participant_id;
	
	/**
	 * Maps published service IDs to the services.
	 */
	private Map<Long, ServiceObjectRegistration> m_services;
	
	/**
	 * Dispatcher that executes operation requests.
	 */
	private LocalDispatcher<Runnable> m_dispatcher;
	
	/**
	 * Where to send the responses to requests.
	 */
	private EventSink m_response_sink;
	
	/**
	 * Collector that receives I/O exceptions when sending responses.
	 */
	private ThrowableCollector m_collector;
	
	/**
	 * Creates a new filter.
	 * @param information the operation information
	 * @param participant_id the ID of this participant
	 * @param response_sink where to send responses to detected requests
	 */
	ExecutionRequestReadFilter(OperationInformation information,
			long participant_id, EventSink response_sink) {
		Ensure.not_null(information);
		Ensure.not_null(response_sink);
		
		m_information = information;
		m_participant_id = participant_id;
		m_services = new HashMap<>();
		m_dispatcher = new LocalDispatcher<>();
		m_response_sink = response_sink;
		m_collector = new ThrowableCollector("Request read filter");
	}
	
	/**
	 * Publishes a service.
	 * @param sor the service to publish
	 */
	synchronized void publish(ServiceObjectRegistration sor) {
		Ensure.not_null(sor);
		Ensure.is_false(m_services.containsKey(sor.object_id()));
		
		m_services.put(sor.object_id(), sor);
	}
	
	/**
	 * Unpublishes a service.
	 * @param sor the service to unpublish
	 */
	synchronized void unpublish(ServiceObjectRegistration sor) {
		Ensure.not_null(sor);
		Ensure.is_true(m_services.containsKey(sor.object_id()));
		
		m_services.remove(sor.object_id());
	}
	
	@Override
	@SuppressWarnings("resource")
	public void sink(final BusData data) throws IOException {
		Ensure.not_null(data);
		
		if (!m_information.is_execution_request(data.value())) {
			forward(data);
			return;
		}
		
		if (m_information.execution_request_dst(data.value())
				!= m_participant_id) {
			/*
			 * Request for someone which isn't us.
			 */
			return;
		}
		
		/*
		 * Find the service object registration with the given ID.
		 */
		long obj_id = m_information.execution_request_obj_id(data.value());
		
		final ServiceObjectRegistration sor;
		synchronized (this) {
			sor = m_services.get(obj_id);
		}
		
		if (sor == null) {
			send_failure(data.value(), new FailureInformation(
					"Unknown service", "Service with ID " + obj_id
					+ " is not known to participant " + m_participant_id
					+ ".", ""));
			return;
		}
		
		/*
		 * Find which operation has been requested.
		 */
		DataValue group = sor.group();
		String op_name = m_information.execution_request_operation(
				data.value());
		Ensure.not_null(op_name);
		if (!m_information.group_has_operation(group, op_name)) {
			send_failure(data.value(), new FailureInformation(
					"Unknown operation", "Service with ID " + obj_id
					+ " in participant " + m_participant_id + " does not "
					+ "publish operation '" + op_name + "'.", ""));
			return;
		}
		
		final DataValue operation;
		operation = m_information.group_operation(group, op_name);
		Ensure.not_null(operation);
		
		/*
		 * Check that the operation's parameters are correct.
		 */
		Set<String> op_param_names = m_information.parameters(operation);
		for (Iterator<String> it = op_param_names.iterator(); it.hasNext(); ) {
			String n = it.next();
			if (m_information.parameter_direction(operation, n) ==
					ParameterDirection.OUTPUT) {
				it.remove();
			}
		}
		
		final Map<String, DataValue> args =
				m_information.execution_request_input_arguments(data.value());
		if (op_param_names.size() != args.size()) {
			send_failure(data.value(), new FailureInformation(
					"Invalid arguments", "Operation '"
					+ op_name + "' of service with ID " + obj_id + " in "
					+ "participant " + m_participant_id + " has "
					+ op_param_names.size() + " declared input parameters "
					+ "but " + args.size() + " were provided.",
					"Input parameters: " + op_param_names + "; provided "
					+ "inputs: " + args));
			return;
		}
		
		for (String p : op_param_names) {
			if (!args.containsKey(p)) {
				send_failure(data.value(), new FailureInformation(
						"Invalid arguments", "No parameter '" + p + "' was "
						+ "provided for operation '" + op_name + "' of "
						+ "service with ID " + obj_id + " in participant "
						+ m_participant_id + ".", ""));
				return;
			}
			
			DataType p_type = m_information.parameter_type(operation, p);
			if (!p_type.is_instance(args.get(p))) {
				send_failure(data.value(), new FailureInformation(
						"Invalid arguments", "Parameter '" + p + "' has "
						+ "type '" + p_type.name() + "' but argument is "
						+ "not an instance.", ""));
				return;
			}
		}
		
		m_dispatcher.dispatch(new Runnable() {
			@Override
			public void run() {
				Pair<Map<String, DataValue>, FailureInformation> result = null;
				if (sor != null) {
					try {
						ServiceOperationExecuter soe = sor.executer();
						Ensure.not_null(soe);
						
						result = soe.execute(operation, args);
					} catch (Exception e) {
						String msg = StringUtils.trimToEmpty(e.getMessage());
						StringWriter str_data = new StringWriter();
						e.printStackTrace(new PrintWriter(str_data));
						result = new Pair<>(null, new FailureInformation(
								e.getClass().getName(), msg,
								str_data.toString()));
					}
				}
				
				Ensure.not_null(result);
				if (result.first() != null) {
					Ensure.is_null(result.second());
					send_success(data.value(), operation, result.first());
				} else {
					Ensure.not_null(result.second());
					send_failure(data.value(), result.second());
				}
			}
		});
	}
	
	/**
	 * Sends a successful response to a request.
	 * @param request the request
	 * @param operation the operation executed
	 * @param output_arguments the output arguments
	 */
	private void send_success(DataValue request, DataValue operation,
			Map<String, DataValue> output_arguments) {
		Ensure.not_null(request);
		Ensure.not_null(operation);
		Ensure.not_null(output_arguments);
		
		DataValue response = m_information.create_execution_response(request,
				operation, output_arguments);
		
		try {
			m_response_sink.sink(new BusData(response));
		} catch (IOException e) {
			m_collector.collect(e, "Sending data to sink.");
		}
	}
	
	/**
	 * Sends an failure response to a request.
	 * @param request the request
	 * @param fi the failure information
	 */
	private void send_failure(DataValue request, FailureInformation fi) {
		Ensure.not_null(request);
		Ensure.not_null(fi);
		
		DataValue response = m_information.create_execution_failure(request,
				fi.type(), fi.description(), fi.data());
		
		try {
			m_response_sink.sink(new BusData(response));
		} catch (IOException e) {
			m_collector.collect(e, "Sending data to sink.");
		}
	}
}
