package edu.cmu.cs.able.eseb.rpc;

import incubator.Pair;
import incubator.pval.Ensure;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import edu.cmu.cs.able.typelib.parser.DataTypeNameParser;
import edu.cmu.cs.able.typelib.parser.ParseException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;


/**
 * Class that provides support for a remote RPC interface. This class provides
 * a way to make a Java interface available remotely and to create a stub to
 * invoke it remotely.
 */
public class JavaRpcFactory {
    /**
     * Prefix to use as input parameter names.
     */
    private static final String INPUT_PARAMETER_NAME_PREFIX = "i";

    /**
     * Name of return parameter.
     */
    private static final String OUTPUT_PARAMETER_NAME = "output";

    /**
     * Creates a stub to execute remote operations.
     * @param t_class the interface used to execute the remote operations;
     * this interface must match the one used to create the registry wrapper
     * with the
     * {@link #create_registry_wrapper(Class, Object, RpcEnvironment, long)}
     * method; all methods in this interface must throw
     * {@link OperationException} or some super class
     * @param env the RPC execution environment
     * @param dst_id the ID of the participant that will be invoked
     * @param time_out_ms timeout, in milliseconds, for operation executions;
     * <code>0</code> means no timeout
     * @param obj_id the ID of the remote object to invoke the operation on
     * @return the remote execution stub; the returned object also implements
     * <code>Closeable</code> regardless of whether <code>T</code> extends
     * it or not; this can be used to close the service
     */
    public static <T> T create_remote_stub(Class<T> t_class,
            final RpcEnvironment env, final long dst_id,
            final long time_out_ms, final long obj_id) {
        Ensure.not_null(t_class);
        Ensure.not_null(env);
        Ensure.greater_equal(time_out_ms, 0);

        final DataValue g = create_meta_data_for_service(t_class, env);
        final Map<Method, DataValue> method_op_map = new HashMap<>();
        for (Method m : t_class.getMethods()) {
            if (env.operation_information().group_has_operation(g,
                    m.getName())) {
                DataValue op = env.operation_information().group_operation(g,
                        m.getName());
                method_op_map.put(m, op);
            }
        }

        return t_class.cast(Proxy.newProxyInstance(t_class.getClassLoader(),
                new Class[] { t_class, Closeable.class },
                new InvocationHandler() {
            /**
             * The remote operation stubs.
             */
            private Map<Method, RemoteOperationStub> m_stubs = new HashMap<>();

            @Override
            public Object invoke(Object arg0, Method m, Object[] args)
                    throws Throwable {
                if (m.equals(Closeable.class.getMethod("close"))) {
                    do_close();
                    return null;
                }

                /*
                 * Find the operation for this method.
                 */
                DataValue op = method_op_map.get(m);
                Ensure.not_null(op);

                /*
                 * Check if we need a new stub.
                 */
                RemoteOperationStub stub;
                synchronized (this) {
                    Ensure.not_null(m_stubs);
                    stub = m_stubs.get(m);
                    if (stub == null) {
                        stub = new RemoteOperationStub(env, dst_id, op,
                                obj_id);
                        m_stubs.put(m, stub);
                    }
                }

                /*
                 * Convert the input values.
                 */
                Map<String, DataValue> input = new HashMap<>();
                        if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        String pname = INPUT_PARAMETER_NAME_PREFIX + i;
                        DataType ptype =
                                env.operation_information().parameter_type(
                                        op, pname);
                        DataValue v = env.converter().from_java(args[i], ptype);
                        input.put(pname, v);
                    }
                }

                /*
                 * Invoke the object.
                 */
                RemoteExecution exec = stub.execute(input);
                RemoteExecutionResult r = null;
                try {
                    r = exec.get(time_out_ms, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    throw new OperationTimedOutException(e.getMessage());
                } catch (Exception e) {
                    throw new OperationFailureException("Failed to execute "
                            + "operation.", e);
                }

                if (r.successful()) {
                    /*
                     * Check if we have an output parameter.
                     */
                    if (r.output_arguments().size() == 0)
                        return null;
                    else {
                        Ensure.equals(r.output_arguments().size(), 1);
                        DataValue rv = r.output_arguments().get(
                                OUTPUT_PARAMETER_NAME);
                        Ensure.not_null(rv);

                        Class<?> cls = m.getReturnType();
                        cls = ClassUtils.primitiveToWrapper(cls);
                        return env.converter().to_java(rv, cls);
                    }
                } else {
                    /*
                     * We have an execution failure.
                     */
                    FailureInformation fi = r.failure_information();
                    Ensure.not_null(fi);
                    throw new OperationFailureException("Operation failure: "
                            + fi.type() + ": " + fi.description());
                }
            }

            private void do_close() throws IOException {
                Ensure.not_null(m_stubs);
                m_stubs = null;
            }
        }));
    }

    /**
     * Registers an interface as a registry, effectively allowing it to be
     * invoked remotely.
     * @param t_class the type of the interface
     * @param t the interface itself which will be invoked
     * @param env the RPC execution environment
     * @param obj_id the ID of the object to register
     * @return an object which should be closed to dispose of the service
     */
    public static <T> Closeable create_registry_wrapper(final Class<T> t_class,
            final T t, final RpcEnvironment env, long obj_id) {
        Ensure.not_null(t_class);
        Ensure.not_null(t);
        Ensure.not_null(env);

        ServiceOperationExecuter executer = new ServiceOperationExecuter() {
            @Override
            public Pair<Map<String, DataValue>, FailureInformation> execute(
                    DataValue operation,
                    Map<String, DataValue> input_arguments)
                            throws Exception {
                Ensure.not_null(operation);
                Ensure.not_null(input_arguments);
                Ensure.is_true(env.operation_information().is_operation(
                        operation));

                /*
                 * Find the method that is going to be executed.
                 */
                Method[] ms = t_class.getMethods();
                Method m = null;
                String op_name = env.operation_information().operation_name(
                        operation);
                for (Method mm : ms) {
                    if (mm.getName().equals(op_name)) {
                        m = mm;
                        break;
                    }
                }

                Ensure.not_null(m);

                /*
                 * Map all input arguments provided to the methods' input
                 * arguments.
                 */
                int arg_count = 0;
                List<DataValue> args_tl = new ArrayList<>();
                DataValue f;
                do {
                    f = input_arguments.get(INPUT_PARAMETER_NAME_PREFIX
                            + arg_count);
                    if (f != null) {
                        arg_count++;
                        args_tl.add(f);
                    }
                } while (f != null);

                @SuppressWarnings("null")
                int len = m.getParameterTypes().length;

                Ensure.equals(arg_count, input_arguments.size());
                Ensure.equals(arg_count, len);

                Object[] args_j = new Object[arg_count];
                for (int i = 0; i < arg_count; i++) {
                    Class<?> cls = m.getParameterTypes()[i];
                    cls = ClassUtils.primitiveToWrapper(cls);
                    args_j[i] = env.converter().to_java(args_tl.get(i), cls);
                }

                /*
                 * Find out if there is an output type and, if so, what it is.
                 */
                Set<String> ps = env.operation_information().parameters(
                        operation);
                DataType output_type = null;
                for (String p : ps) {
                    if (env.operation_information().parameter_direction(
                            operation, p) == ParameterDirection.OUTPUT) {
                        output_type =
                                env.operation_information().parameter_type(
                                        operation, p);
                        Ensure.not_null(output_type);
                    }
                }

                /*
                 * Invoke the object.
                 */
                Map<String, DataValue> outs = new HashMap<>();
                FailureInformation fi = null;
                try {
                    Object r = m.invoke(t, args_j);
                    if (m.getReturnType() != void.class){
                        Ensure.not_null(output_type);
                        DataValue dv = env.converter().from_java(r,
                                output_type);
                        outs.put(OUTPUT_PARAMETER_NAME, dv);
                    }
                } catch (Exception e) {
                    Throwable t = e;

                    if (e instanceof InvocationTargetException) {
                        t = ((InvocationTargetException) e).getCause();
                    }

                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));

                    fi = new FailureInformation(
                            t.getClass().getCanonicalName(),
                            StringUtils.trimToEmpty(t.getMessage()),
                            sw.toString());
                    outs = null;
                }

                return new Pair<>(outs, fi);
            }
        };

        DataValue g = create_meta_data_for_service(t_class, env);
        ServiceObjectRegistration sor = ServiceObjectRegistration.make(
                executer, g, obj_id, env);
        return sor;
    }

    /**
     * Creates the service meta data for a given service.
     * @param t_class the service interface
     * @param env the RPC execution environment
     * @return the meta data describing the service
     */
    private static <T> DataValue create_meta_data_for_service(
            Class<T> t_class, RpcEnvironment env) {
        Ensure.not_null(t_class);
        Ensure.not_null(env);

        OperationInformation oi = env.operation_information();
        DataValue g = oi.create_group();

        Set<String> found_names = new HashSet<>();

        for (Method m : t_class.getMethods()) {
            Ensure.not_null(m);
            if (found_names.contains(m.getName()))
                throw new IllegalServiceDefinitionException("Interface \""
                        + t_class.getCanonicalName() + "\" has more than "
                        + "one method named '" + m.getName() + "'.");
            else {
                found_names.add(m.getName());
            }

            int method_parameters = m.getParameterTypes().length;

            ParametersTypeMapping ptm = m.getAnnotation(
                    ParametersTypeMapping.class);
            int parameter_maps = 0;
            if (ptm != null) {
                parameter_maps = ptm.value().length;
            }

            if (method_parameters != parameter_maps) throw new IllegalServiceDefinitionException("Interface \""
                    + t_class.getCanonicalName() + "\"'s method \""
                    + m.getName() + "\" has " + method_parameters
                    + " parameters but " + parameter_maps + " parameters "
                    + "declared in the @ParametersTypeMapping "
                    + "annotation.");

            ReturnTypeMapping rtm = m.getAnnotation(ReturnTypeMapping.class);
            Class<?> return_type = m.getReturnType();

            if (rtm == null && return_type != void.class) throw new IllegalServiceDefinitionException("Interface \""
                    + t_class.getCanonicalName() + "\"'s method \""
                    + m.getName() + "\" doesn't have a @ReturnTypeMapping "
                    + "annotation but has non-void return type.");

            if (rtm != null && return_type == void.class) throw new IllegalServiceDefinitionException("Interface \""
                    + t_class.getCanonicalName() + "\"'s method \""
                    + m.getName() + "\" has a @ReturnTypeMapping "
                    + "annotation but has void return type.");

            DataValue op = oi.create_operation(m.getName());
            PrimitiveScope ps = env.connection().primitive_scope();
            DataTypeNameParser dtnp = new DataTypeNameParser();
            for (int i = 0; i < method_parameters; i++) {
                @SuppressWarnings("null")
                String type_name = ptm.value()[i];
                Ensure.not_null(type_name);

                try {
                    DataType dt = dtnp.parse(type_name, ps, ps);
                    oi.add_parameter(op, dt, INPUT_PARAMETER_NAME_PREFIX + i,
                            ParameterDirection.INPUT);
                } catch (ParseException e) {
                    throw new IllegalServiceDefinitionException("Failed "
                            + "to parse data type name of parameter index "
                            + i + ": '" + type_name + "'.", e);
                }
            }

            if (rtm != null) {
                try {
                    DataType dt = dtnp.parse(rtm.value(), ps, ps);
                    oi.add_parameter(op, dt, OUTPUT_PARAMETER_NAME,
                            ParameterDirection.OUTPUT);
                } catch (ParseException e) {
                    throw new IllegalServiceDefinitionException("Failed "
                            + "to parse return type data type name: '"
                            + rtm.value() + "'.", e);
                }
            }

            oi.add_operation_to_group(g, op);
        }

        return g;
    }
}
