package auxtestlib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import auxtestlib.CommandRunner.CommandOutput;

/**
 * Class that runs javac and compiles source code. Javac is run as an external
 * process.
 */
public final class JavacRunner {
	/**
	 * Utility class: no constructor.
	 */
	private JavacRunner() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Runs javac.
	 * 
	 * @param dir the directory where javac should run
	 * @param source file with source code
	 * 
	 * @throws Exception failed to compile
	 */
	public static void javac(File dir, String source) throws Exception {
		javac(dir, source, null);
	}

	/**
	 * Runs javac.
	 * 
	 * @param dir the directory where javac should run
	 * @param source file with source code
	 * @param cp additional classpath (<code>null</code> not to add any
	 * additional classpath). If this parameter is defined, any position in the
	 * array which is <code>null</code> is replaced by the application's current
	 * classpath. Inside this array both strings or files can be placed. Files
	 * will be added to the classpath as absolute paths.
	 * 
	 * @throws Exception compilation failed
	 */
	public static void javac(File dir, String source, Object cp[])
			throws Exception {
		javac(dir, new String[] { source }, cp);
	}

	/**
	 * Creates the additional classpath to run javac.
	 * 
	 * @param classpath additional classpath (<code>null</code> not to add any
	 * additional classpath). If this parameter is defined, any position in the
	 * array which is <code>null</code> is replaced by the application's current
	 * classpath. Inside this array both strings or files can be placed. Files
	 * will be added to the classpath as absolute paths.
	 * 
	 * @return the classpath
	 * 
	 * @throws Exception failed to add the classpath
	 */
	private static String buildAdditionalClasspath(Object[] classpath)
			throws Exception {
		Object[] cp = classpath;

		if (cp == null || cp.length == 0) {
			cp = new String[] { null };
		}

		StringBuffer cpb = new StringBuffer();
		for (int i = 0; i < cp.length; i++) {
			if (i != 0) {
				cpb.append(File.pathSeparator);
			}

			if (cp[i] == null) {
				cpb.append(System.getProperty("java.class.path"));
			} else {
				if (cp[i] instanceof String) {
					cpb.append(cp[i]);
				} else if (cp[i] instanceof File) {
					cpb.append(((File) cp[i]).getAbsolutePath());
				} else {
					throw new IllegalArgumentException("Path must "
							+ "contain only strings or files.");
				}
			}
		}

		return cpb.toString();
	}

	/**
	 * Runs javac.
	 * 
	 * @param dir directory where javac should be run
	 * @param sources files with source code. It can be <code>null</code> if
	 * there are no files to compile
	 * @param cp additional classpath (<code>null</code> not to add any
	 * additional classpath). If this parameter is defined, any position in the
	 * array which is <code>null</code> is replaced by the application's current
	 * classpath. Inside this array both strings or files can be placed. Files
	 * will be added to the classpath as absolute paths.
	 * 
	 * @throws Exception failed to compile
	 */
	public static void javac(File dir, String[] sources, Object[] cp)
			throws Exception {

		// Leave if there is nothing to compile.
		if (sources == null || sources.length == 0) {
			return;
		}

		String cpb = buildAdditionalClasspath(cp);
		CommandRunner cr = new CommandRunner();

		List<String> cmdList = new ArrayList<>();
		cmdList.add("javac");
		cmdList.add("-d");
		cmdList.add(".");
		cmdList.add("-cp");
		cmdList.add(cpb);
		for (int i = 0; i < sources.length; i++) {
			cmdList.add(sources[i]);
		}

		String[] cmds = cmdList.toArray(new String[cmdList.size()]);
		CommandOutput co = cr.run_command(cmds, dir, 60);
		if (co == null || co.exitCode != 0) {
			StringBuffer cmdsStr = new StringBuffer();
			cmdsStr.append('{');
			for (int i = 0; i < cmds.length; i++) {
				if (i != 0) {
					cmdsStr.append(';');
				}

				cmdsStr.append('\'');
				cmdsStr.append(cmds[i]);
				cmdsStr.append('\'');
			}

			cmdsStr.append('}');

			StringBuffer extra = new StringBuffer();
			if (co != null) {
				extra.append("; stdout:\n");
				extra.append(co.output);
				extra.append("\nstderr:\n");
				extra.append(co.error);
			}

			throw new CommandExecutionException("Failed running " + cmdsStr
					+ ", @ " + dir.getAbsolutePath() + extra);
		}
	}

	/**
	 * Compiles a file which exists as a resource.
	 * 
	 * @param dir directory where javac should be run
	 * @param resource the resource to compile
	 * @param cls the name of the class
	 * @param cp additional classpath (<code>null</code> not to add any
	 * additional classpath). If this parameter is defined, any position in the
	 * array which is <code>null</code> is replaced by the application's current
	 * classpath. Inside this array both strings or files can be placed. Files
	 * will be added to the classpath as absolute paths.
	 * 
	 * @return the directory with the first "package".
	 * 
	 * @throws Exception failed to compile
	 */
	public static File javacResource(File dir, String resource, String cls,
			Object[] cp) throws Exception {
		return javacResources(dir, new String[] { resource },
				new String[] { cls }, cp);
	}

	/**
	 * Compiles files which exists as resources.
	 * 
	 * @param dir directory where javac should be run
	 * @param resources the resources to compile
	 * @param cls the name of the classes
	 * @param cp additional classpath (<code>null</code> not to add any
	 * additional classpath). If this parameter is defined, any position in the
	 * array which is <code>null</code> is replaced by the application's current
	 * classpath. Inside this array both strings or files can be placed. Files
	 * will be added to the classpath as absolute paths.
	 * 
	 * @return the directory with the first "package".
	 * 
	 * @throws Exception failed to compile
	 */
	public static File javacResources(File dir, String[] resources,
			String[] cls, Object[] cp) throws Exception {
		String classes[] = new String[cls.length];
		File subdir = null;

		// LEts break the name by "." because we want the directories.
		for (int j = 0; j < cls.length; j++) {
			String parts[] = cls[j].split("\\.");

			if (parts.length < 2) {
				throw new IllegalArgumentException("The class to compile "
						+ "must have at least one package.");
			}

			// We create all directories.
			File files[] = new File[parts.length];
			for (int i = 0; i < files.length - 1; i++) {
				files[i] = new File(i == 0 ? dir : files[i - 1], parts[i]);
				boolean res = files[i].mkdir();
				assert res || !res; // Otherwise findbugs complains.
			}

			subdir = files[0];

			// Create a file that represents .java.
			int pos = parts.length - 1;
			files[pos] = new File(files[pos - 1], parts[pos] + ".java");

			// We define the file contents.
			FileContentWorker.setContentsBin(files[pos], FileContentWorker
					.readResourceContentsBin(resources[j]));
			classes[j] = cls[j].replace('.', '/') + ".java";
		}

		// Compile.
		JavacRunner.javac(dir, classes, cp);

		return subdir;
	}
}
