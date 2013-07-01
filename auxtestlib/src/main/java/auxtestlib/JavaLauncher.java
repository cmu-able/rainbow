package auxtestlib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.SystemUtils;

import auxtestlib.CommandRunner.CommandOutput;
import auxtestlib.CommandRunner.ProcessInterface;

/**
 * Class that is capable of launching new Java virtual machines. It provides an
 * easier interface to {@link CommandRunner}.
 */
public class JavaLauncher {
    /**
     * The object used to run commands.
     */
    private final CommandRunner m_command_runner;

    /**
     * Creates a new java launcher.
     */
    public JavaLauncher() {
        m_command_runner = new CommandRunner();
    }

    /**
     * @param class_name deprecated
     * @param limit deprecated
     * @param arguments deprecated
     * @return deprecated
     * @throws IOException 
     * @deprecated use {@link #launch_java(String, int, String...)}
     */
    @Deprecated
    public CommandOutput launchJava(String class_name, int limit,
            String... arguments) throws IOException {
        return launchJava(class_name, Arrays.asList(arguments), limit);
    }

    /**
     * Runs a java program and waits for its completion. This is equivalent to
     * invoke {@link #launch_java(String, List, List, int)} using the current
     * class path.
     * @param class_name the Java class name
     * @param limit the time limit (in seconds) for the program to execute
     * @param arguments the program arguments
     * @return the program's output
     * @throws IOException failed to launch the program
     */
    public CommandOutput launch_java(String class_name, int limit,
            String... arguments) throws IOException {
        return launch_java(class_name, Arrays.asList(arguments), limit);
    }

    /**
     * Runs a java program but doesn't wait for its completion. This is
     * equivalent to invoke {@link #launch_java_async(String, List, List, int)}
     * using the current JVM's class path.
     * @param class_name the Java class name
     * @param arguments the program arguments
     * @param limit the time limit (in seconds) for the program to execute
     * @return an interface to the running program
     * @throws IOException failed to launch the program
     */
    public ProcessInterface launch_java_async(String class_name, int limit,
            String... arguments) throws IOException {
        return launch_java_async(class_name, Arrays.asList(arguments), limit);
    }

    /**
     * @param className deprecated
     * @param limit deprecated
     * @param arguments deprecated
     * @return deprecated
     * @throws IOException deprecated
     * @deprecated use {@link #launch_java_async(String, int, String...)}
     */
    @Deprecated
    public ProcessInterface launchJavaAsync(String className, int limit,
            String... arguments) throws IOException {
        return launchJavaAsync(className, Arrays.asList(arguments), limit);
    }

    /**
     * Runs a java program and waits for its completion. This is equivalent to
     * invoke {@link #launch_java(String, List, List, int)} using the current
     * class path.
     * @param class_name the Java class name
     * @param arguments the program arguments
     * @param limit the time limit (in seconds) for the program to execute
     * @return the program's output
     * @throws IOException failed to launch the program
     */
    public CommandOutput launch_java(String class_name, List<String> arguments,
            int limit) throws IOException {
        return launchJava(class_name, arguments, find_current_classpath(), limit);
    }

    /**
     * @param className deprecated
     * @param arguments deprecated
     * @param limit deprecated
     * @return deprecated
     * @throws IOException deprecated
     * @deprecated use {@link #launch_java(String, List, int)}
     */
    @Deprecated
    public CommandOutput launchJava(String className, List<String> arguments,
            int limit) throws IOException {
        return launchJava(className, arguments, find_current_classpath(), limit);
    }

    /**
     * Runs a java program but doesn't wait for its completion. This is
     * equivalent to invoke {@link #launch_java_async(String, List, List, int)}
     * using the current JVM's class path.
     * @param class_name the Java class name
     * @param arguments the program arguments
     * @param limit the time limit (in seconds) for the program to execute
     * @return an interface to the running program
     * @throws IOException failed to launch the program
     */
    public ProcessInterface launch_java_async(String class_name,
            List<String> arguments, int limit) throws IOException {
        return launch_java_async(class_name, arguments, find_current_classpath(),
                limit);
    }

    /**
     * @param className deprecated
     * @param arguments deprecated
     * @param limit deprecated
     * @return deprecated
     * @throws IOException deprecated
     * @deprecated use {@link #launch_java_async(String, List, int)}
     */
    @Deprecated
    public ProcessInterface launchJavaAsync(String className,
            List<String> arguments, int limit) throws IOException {
        return launchJavaAsync(className, arguments, find_current_classpath(),
                limit);
    }

    /**
     * Runs a java program but doesn't wait for completion.
     * @param class_name the Java class name
     * @param arguments the program arguments
     * @param class_path the program's class path
     * @param limit the time limit (in seconds) for the program to execute
     * @return an interface to the running program
     * @throws IOException failed to launch the program
     */
    public ProcessInterface launch_java_async(String class_name,
            List<String> arguments, List<URL> class_path, int limit)
                    throws IOException {
        return launch_java_async(class_name, arguments, class_path, limit,
                null);
    }

    /**
     * @param class_name deprecated
     * @param arguments deprecated
     * @param class_path deprecated
     * @param limit deprecated
     * @return deprecated
     * @throws IOException deprecated
     * @deprecated use {@link #launch_java_async(String, List, List, int)}
     */
    @Deprecated
    public ProcessInterface launchJavaAsync(String class_name,
            List<String> arguments, List<URL> class_path, int limit)
                    throws IOException {
        return launchJavaAsync(class_name, arguments, class_path, limit, null);
    }

    /**
     * Runs a java program but doesn't wait for completion.
     * @param class_name the Java class name
     * @param arguments the program arguments
     * @param class_path the program's class path
     * @param limit the time limit (in seconds) for the program to execute
     * @param debug_port if not <code>null</code>, a java debug port will
     * be open in this port
     * @return an interface to the running program
     * @throws IOException failed to launch the program
     */
    public ProcessInterface launch_java_async(String class_name,
            List<String> arguments, List<URL> class_path, int limit,
            Short debug_port) throws IOException {
        return launch_java_async (class_name, null, arguments, class_path, limit, debug_port);
    }

    /**
     * Runs a java program but doesn't wait for completion
     * 
     * @param class_name
     *            the Java class name
     * @param user_dir
     *            the directory to start the program in
     * @param arguments
     *            the program arguments
     * @param limit
     *            the time limit (in seconds) for the program to execute
     * @return an interface to the running program
     * @throws IOException
     *             failed to launch the program
     */
    public ProcessInterface launch_java_async (String class_name, File user_dir, List<String> arguments, int limit)
            throws IOException {
        return launch_java_async (class_name, user_dir, arguments, find_current_classpath (), limit, null);
    }

    /**
     * Runs a java program but doesn't wait for completion
     * 
     * @param class_name
     *            the Java class name
     * @param user_dir
     *            the directory to start the program in
     * @param arguments
     *            the program arguments
     * @param class_path
     *            the program's class path
     * @param limit
     *            the time limit (in seconds) for the program to execute
     * @param debug_port
     *            if not <code>null</code>, a java debug port will be open in this port
     * @return an interface to the running program
     * @throws IOException
     *             failed to launch the program
     */
    public ProcessInterface launch_java_async (String class_name,
            File user_dir,
            List<String> arguments,
            List<URL> class_path,
            int limit,
            Short debug_port) throws IOException {
        if (class_name == null) throw new IllegalArgumentException("className == null");

        if (arguments == null) throw new IllegalArgumentException("arguments == null");

        if (class_path == null) throw new IllegalArgumentException("classPath == null");

        if (limit <= 0) throw new IllegalArgumentException("limit <= 0");

        if (user_dir == null) {
            user_dir = new File(SystemUtils.USER_DIR);
        }

        String[] cmds = build_commands (class_name, arguments, class_path, debug_port);

        ProcessInterface pi = m_command_runner.run_command_async (cmds, user_dir, limit);
        return pi;
    }

    /**
     * @param class_name deprecated
     * @param arguments deprecated
     * @param class_path deprecated
     * @param limit deprecated
     * @param debug_port deprecated
     * @return deprecated
     * @throws IOException deprecated
     * @deprecated use
     * 		{@link #launch_java_async(String, List, List, int, Short)}
     */
    @Deprecated
    public ProcessInterface launchJavaAsync(String class_name,
            List<String> arguments, List<URL> class_path, int limit,
            Short debug_port) throws IOException {
        return launch_java_async(class_name, arguments, class_path, limit,
                debug_port);
    }

    /**
     * Runs a java program and waits for completion.
     * @param class_name the Java class name.
     * @param arguments the program arguments
     * @param class_path the program's class path
     * @param limit the time limit (in seconds) for the program to execute
     * @return the program's output
     * @throws IOException failed to launch the program
     */
    public CommandOutput launch_java(String class_name, List<String> arguments,
            List<URL> class_path, int limit) throws IOException {
        return launch_java(class_name, arguments, class_path, limit, null);
    }

    /**
     * @param class_name deprecated
     * @param arguments deprecated
     * @param class_path deprecated
     * @param limit deprecated
     * @return deprecated
     * @throws IOException deprecated
     * @deprecated use {@link #launch_java(String, List, List, int)}
     */
    @Deprecated
    public CommandOutput launchJava(String class_name, List<String> arguments,
            List<URL> class_path, int limit) throws IOException {
        return launchJava(class_name, arguments, class_path, limit, null);
    }

    /**
     * Runs a java program and waits for completion.
     * @param class_name the Java class name.
     * @param arguments the program arguments
     * @param class_path the program's class path
     * @param limit the time limit (in seconds) for the program to execute
     * @param debug_port if not <code>null</code>, the jvm will open a debug
     * port in this address
     * @return the program's output
     * @throws IOException failed to launch the program
     */
    public CommandOutput launch_java(String class_name, List<String> arguments,
            List<URL> class_path, int limit, Short debug_port)
                    throws IOException {
        if (class_name == null) throw new IllegalArgumentException("className == null");

        if (arguments == null) throw new IllegalArgumentException("arguments == null");

        if (class_path == null) throw new IllegalArgumentException("classPath == null");

        if (limit <= 0) throw new IllegalArgumentException("limit <= 0");

        File userDir = new File(SystemUtils.USER_DIR);

        String[] cmds = build_commands(class_name, arguments, class_path,
                debug_port);

        CommandOutput output = m_command_runner.run_command(cmds, userDir,
                limit);
        return output;
    }

    /**
     * @param class_name deprecated
     * @param arguments deprecated
     * @param class_path deprecated
     * @param limit deprecated
     * @param debug_port deprecated
     * @return deprecated
     * @throws IOException deprecated
     * @deprecated use {@link #launch_java(String, List, List, int, Short)}
     */
    @Deprecated
    public CommandOutput launchJava(String class_name, List<String> arguments,
            List<URL> class_path, int limit, Short debug_port)
                    throws IOException {
        return launch_java(class_name, arguments, class_path, limit,
                debug_port);
    }

    /**
     * Builds the command line to run a JVM with a set of arguments and class
     * path.
     * @param class_name the name of the class
     * @param arguments the arguments to run the class
     * @param class_path the class path
     * @param debug_port if not <code>null</code>, will be used as port
     * number to open the remote debugger
     * @return the commands to run
     */
    private String[] build_commands(String class_name, List<String> arguments,
            List<URL> class_path, Short debug_port) {
        assert class_name != null;
        assert arguments != null;
        assert class_path != null;

        List<String> cmds = new ArrayList<>();
        cmds.add("java");
        if (debug_port != null) {
            cmds.add("-agentlib:jdwp=transport=dt_socket,server=y,"
                    + "suspend=y,address=" + debug_port);
        }

        if (!class_path.isEmpty()) {
            cmds.add("-cp");
            StringBuffer cp = new StringBuffer();
            for (int i = 0; i < class_path.size(); i++) {
                if (i > 0) {
                    cp.append(SystemUtils.PATH_SEPARATOR);
                }

                cp.append(class_path.get(i).getPath());
            }

            cmds.add(cp.toString());
        }

        cmds.add(class_name);
        cmds.addAll(arguments);

        return cmds.toArray(new String[cmds.size()]);
    }

    /**
     * Obtains the URLs in the current class path.
     * @return a list with all URLS
     */
    public List<URL> find_current_classpath() {
        List<URL> cp = new ArrayList<>();
        ClassLoader cl = JavaLauncher.class.getClassLoader();
        for (; cl != null; cl = cl.getParent()) {
            if (cl instanceof URLClassLoader) {
                @SuppressWarnings("resource")
                URLClassLoader ucl = (URLClassLoader) cl;
                cp.addAll(Arrays.asList(ucl.getURLs()));
            }
        }

        return cp;
    }


}
