package edu.cmu.cs.able.eseb.rpc;

import incubator.Pair;
import incubator.dispatch.LocalDispatcher;
import incubator.exh.ThrowableCollector;
import incubator.pval.Ensure;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.filter.EventSink;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Class that reads execution requests for an execution environment and
 * processes them. This class is used to detect RPC invocations by an
 * environment and is used by the {@link ExecutionRequestReadFilter} class.
 */
class ExecutionRequestReader {
    /**
     * The operation information.
     */
    private OperationInformation m_information;

    /**
     * The ID of this participant.
     */
    private String m_participant_id;

    /**
     * Maps published service IDs to the services.
     */
    private Map<String, ServiceObjectRegistration> m_services;

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
     */
    ExecutionRequestReader(OperationInformation information,
            String participant_id) {
        Ensure.not_null(information, "information == null");
        Ensure.not_null(participant_id, "participant_id == null");

        m_information = information;
        m_participant_id = participant_id;
        m_services = new HashMap<>();
        m_dispatcher = new LocalDispatcher<>();
        m_response_sink = null;
        m_collector = new ThrowableCollector("Request read filter");
    }

    /**
     * Defines the sink where responses are written.
     * @param sink the sink
     */
    synchronized void response_sink(EventSink sink) {
        Ensure.not_null(sink, "sink == null");
        Ensure.is_null(m_response_sink, "Sink already defined");

        m_response_sink = sink;
    }

    /**
     * Handles bus data which is known to be an execution request (although
     * not necessarily for any specific participant).
     * @param data the data
     * @return was this request handled by this reader?
     */
    @SuppressWarnings("resource")
    boolean handles(final BusData data) {
        Ensure.not_null(data, "data == null");
        Ensure.is_true(m_information.is_execution_request(data.value()),
                "data is not an execution request");

        final EventSink sink;
        synchronized (this) {
            Ensure.not_null(m_response_sink, "Cannot handle data is response "
                    + "sink is not defined.");
            sink = m_response_sink;
        }

        if (!m_information.execution_request_dst(data.value()).equals(
                m_participant_id)) /*
                 * Request for someone which isn't us.
                 */
            return false;

        /*
         * Find the service object registration with the given ID.
         */
        String obj_id = m_information.execution_request_obj_id(data.value());
        Ensure.not_null(obj_id);

        final ServiceObjectRegistration sor;
        synchronized (this) {
            sor = m_services.get(obj_id);
        }

        if (sor == null) {
            if (!m_services.isEmpty ()) {
                // if services is not empty, then there are operations,
                // just not for this
                send_failure(data.value(), new FailureInformation(
                        "Unknown service", "Service with ID " + obj_id
                        + " is not known to participant " + m_participant_id
                        + ".", ""), sink);
            } // Otherwise, this might just be created as part of a stub

            return true;

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
                    + "publish operation '" + op_name + "'.", ""), sink);
            return true;
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
                                    + "inputs: " + args), sink);
            return true;
        }

        for (String p : op_param_names) {
            if (!args.containsKey(p)) {
                send_failure(data.value(), new FailureInformation(
                        "Invalid arguments", "No parameter '" + p + "' was "
                                + "provided for operation '" + op_name + "' of "
                                + "service with ID " + obj_id + " in participant "
                                + m_participant_id + ".", ""), sink);
                return true;
            }

            DataType p_type = m_information.parameter_type(operation, p);
            if (!p_type.is_instance(args.get(p))) {
                send_failure(data.value(), new FailureInformation(
                        "Invalid arguments", "Parameter '" + p + "' has "
                                + "type '" + p_type.name() + "' but argument is "
                                + "not an instance.", ""), sink);
                return true;
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
                    send_success(data.value(), operation, result.first(),
                            sink);
                } else {
                    Ensure.not_null(result.second());
                    send_failure(data.value(), result.second(), sink);
                }
            }
        });

        return true;
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

    /**
     * Sends a successful response to a request.
     * @param request the request
     * @param operation the operation executed
     * @param output_arguments the output arguments
     * @param sink the sink to use
     */
    private void send_success(DataValue request, DataValue operation,
            Map<String, DataValue> output_arguments, EventSink sink) {
        Ensure.not_null(request, "request == null");
        Ensure.not_null(operation, "operation == null");
        Ensure.not_null(output_arguments, "output_arguments == null");
        Ensure.not_null(sink, "sink == null");

        DataValue response = m_information.create_execution_response(request,
                operation, output_arguments);

        try {
            sink.sink(new BusData(response));
        } catch (IOException e) {
            m_collector.collect(e, "Sending data to sink.");
        }
    }

    /**
     * Sends an failure response to a request.
     * @param request the request
     * @param fi the failure information
     * @param sink the sink to use
     */
    private void send_failure(DataValue request, FailureInformation fi,
            EventSink sink) {
        Ensure.not_null(request, "request == null");
        Ensure.not_null(fi, "fi == null");
        Ensure.not_null(sink, "sink == null");

        DataValue response = m_information.create_execution_failure(request,
                fi.type(), fi.description(), fi.data());

        try {
            sink.sink(new BusData(response));
        } catch (IOException e) {
            m_collector.collect(e, "Sending data to sink.");
        }
    }
}
