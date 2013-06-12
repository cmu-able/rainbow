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
	private final CommandRunner commandRunner;

	/**
	 * Creates a new java launcher.
	 */
	public JavaLauncher() {
		commandRunner = new CommandRunner();
	}

	/**
	 * Runs a java program and waits for its completion. This is equivalent to
	 * invoke {@link #launchJava(String, List, List, int)} using the current
	 * classpath.
	 * 
	 * @param className the Java class name
	 * @param limit the time limit (in seconds) for the program to execute
	 * @param arguments the program arguments
	 * 
	 * @return the program's output
	 * 
	 * @throws IOException failed to launch the program
	 */
	public CommandOutput launchJava(String className, int limit,
			String... arguments) throws IOException {
		return launchJava(className, Arrays.asList(arguments), limit);
	}

	/**
	 * Runs a java program but doesn't wait for its completion. This is
	 * equivalent to invoke {@link #launchJavaAsync(String, List, List, int)}
	 * using the current JVM's classpath.
	 * 
	 * @param className the Java class name
	 * @param arguments the program arguments
	 * @param limit the time limit (in seconds) for the program to execute
	 * 
	 * @return an interface to the running program
	 * 
	 * @throws IOException failed to launch the program
	 */
	public ProcessInterface launchJavaAsync(String className, int limit,
			String... arguments) throws IOException {
		return launchJavaAsync(className, Arrays.asList(arguments), limit);
	}

	/**
	 * Runs a java program and waits for its completion. This is equivalent to
	 * invoke {@link #launchJava(String, List, List, int)} using the current
	 * classpath.
	 * 
	 * @param className the Java class name
	 * @param arguments the program arguments
	 * @param limit the time limit (in seconds) for the program to execute
	 * 
	 * @return the program's output
	 * 
	 * @throws IOException failed to launch the program
	 */
	public CommandOutput launchJava(String className, List<String> arguments,
			int limit) throws IOException {
		return launchJava(className, arguments, findCurrentClasspath(), limit);
	}

	/**
	 * Runs a java program but doesn't wait for its completion. This is
	 * equivalent to invoke {@link #launchJavaAsync(String, List, List, int)}
	 * using the current JVM's classpath.
	 * 
	 * @param className the Java class name
	 * @param arguments the program arguments
	 * @param limit the time limit (in seconds) for the program to execute
	 * 
	 * @return an interface to the running program
	 * 
	 * @throws IOException failed to launch the program
	 */
	public ProcessInterface launchJavaAsync(String className,
			List<String> arguments, int limit) throws IOException {
		return launchJavaAsync(className, arguments, findCurrentClasspath(),
				limit);
	}

	/**
	 * Runs a java program but doesn't wait for completion.
	 * 
	 * @param className the Java class name
	 * @param arguments the program arguments
	 * @param classPath the program's class path
	 * @param limit the time limit (in seconds) for the program to execute
	 * 
	 * @return an interface to the running program
	 * 
	 * @throws IOException failed to launch the program
	 */
	public ProcessInterface launchJavaAsync(String className,
			List<String> arguments, List<URL> classPath, int limit)
			throws IOException {
		if (className == null) {
			throw new IllegalArgumentException("className == null");
		}

		if (arguments == null) {
			throw new IllegalArgumentException("arguments == null");
		}

		if (classPath == null) {
			throw new IllegalArgumentException("classPath == null");
		}

		if (limit <= 0) {
			throw new IllegalArgumentException("limit <= 0");
		}

		File userDir = new File(SystemUtils.USER_DIR);

		String[] cmds = buildCommands(className, arguments, classPath);

		ProcessInterface pi = commandRunner.runCommandAsync(cmds, userDir,
				limit);
		return pi;
	}

	/**
	 * Runs a java program and waits for completion.
	 * 
	 * @param className the Java class name.
	 * @param arguments the program arguments
	 * @param classPath the program's class path
	 * @param limit the time limit (in seconds) for the program to execute
	 * 
	 * @return the program's output
	 * 
	 * @throws IOException failed to launch the program
	 */
	public CommandOutput launchJava(String className, List<String> arguments,
			List<URL> classPath, int limit) throws IOException {
		if (className == null) {
			throw new IllegalArgumentException("className == null");
		}

		if (arguments == null) {
			throw new IllegalArgumentException("arguments == null");
		}

		if (classPath == null) {
			throw new IllegalArgumentException("classPath == null");
		}

		if (limit <= 0) {
			throw new IllegalArgumentException("limit <= 0");
		}

		File userDir = new File(SystemUtils.USER_DIR);

		String[] cmds = buildCommands(className, arguments, classPath);

		CommandOutput output = commandRunner.runCommand(cmds, userDir, limit);
		return output;
	}

	/**
	 * Builds the command line to run a JVM with a set of arguments and class
	 * path.
	 * 
	 * @param className the name of the class
	 * @param arguments the arguments to run the class
	 * @param classPath the class path
	 * 
	 * @return the commands to run
	 */
	private String[] buildCommands(String className, List<String> arguments,
			List<URL> classPath) {
		assert className != null;
		assert arguments != null;
		assert classPath != null;

		List<String> cmds = new ArrayList<>();
		cmds.add("java");

		if (!classPath.isEmpty()) {
			cmds.add("-cp");
			StringBuffer cp = new StringBuffer();
			for (int i = 0; i < classPath.size(); i++) {
				if (i > 0) {
					cp.append(SystemUtils.PATH_SEPARATOR);
				}

				cp.append(classPath.get(i).getPath());
			}

			cmds.add(cp.toString());
		}

		cmds.add(className);
		cmds.addAll(arguments);

		return cmds.toArray(new String[cmds.size()]);
	}

	/**
	 * Obtains the URLs in the current class path.
	 * 
	 * @return a list with all URLS
	 */
	public List<URL> findCurrentClasspath() {
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
