package org.sa.rainbow.brass.gauges.acme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acmestudio.acme.model.util.UMSystem;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.ho.yaml.Yaml;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class ROS2Acme {

	private Integer m_rate;
	private Integer m_keep;
	private String m_outputFileName;
	private Boolean m_once;
	private String m_dataFileName;
	private Set<String> m_ignoreNodes;
	private Set<String> m_ignoreTopics;
	private Set<String> m_ignoreServices;
	private Set<String> m_ignoreActions;
	private Path m_shellScript;

	public static void main(String[] a) {
		ArgumentParser parser = ArgumentParsers.newFor("ROS2Acme").build()
				.description("Process ROS data into Acme instance");
		parser.addArgument("-n", "--ignore-nodes").type(String.class).nargs("+").help("Ignore the following ROS nodes");
		parser.addArgument("-t", "--ignore-topics").type(String.class).nargs("+").help("Ignore these topics");
		parser.addArgument("-s", "--ignore-services").type(String.class).nargs("+").help("Ignore these services");
		parser.addArgument("-a", "--ignore-actions").type(String.class).nargs("+").help("Ignore these actions");
		parser.addArgument("-i", "--ignore").type(String.class)
				.help("The YAML file containing sections of things to ignore");
		parser.addArgument("-r", "--rate").type(Integer.class).setDefault(10)
				.help("NOT IMPLEMENTED - The loop rate (in seconds)");
		parser.addArgument("-1", "--once").action(Arguments.storeTrue()).help("Only do this once");
		parser.addArgument("-d", "--data").type(String.class).help("Take the ROS information from this file");
		parser.addArgument("-k", "--keep").type(Integer.class).setDefault(10)
				.help("NOT IMPLEMENTED - The number of versions of the Acme file to keep");
		parser.addArgument("output").help(
				"The Acme file to produce (this file will hold the most recent description; if -1 is not specified then the files will be numbered 0..keep, where 0 is the oldest)");

		try {
			Namespace args = parser.parseArgs(a);

			ROS2Acme r2a = new ROS2Acme();
			r2a.processSeperateIgnores(args);
			String ini = args.getString("ignore");
			if (ini != null) {
				File ignoreFile = new File(ini);
				Map configData = (Map) Yaml.load(ignoreFile);
				Map ignores = (Map) configData.get("ignorance");
				r2a.processIgnores(ignores);
			}
			r2a.setRate(args.getInt("rate"));
			r2a.setKeep(args.getInt("keep"));
			r2a.setOutputFileName(args.getString("output"));
			r2a.setOnlyOnce(args.getBoolean("once"));
			r2a.setInputFileName(args.getString("data"));

			r2a.produceArch();

		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		} catch (FileNotFoundException e) {
			System.err.println("The specified ignore file does not exist");
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("There was an error reading the data file");
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private void produceArch() throws IOException {
		ROSToAcmeTranslator r2a = new ROSToAcmeTranslator();
		r2a.setIgnorance(m_ignoreNodes, m_ignoreTopics, m_ignoreServices, m_ignoreActions);
		boolean stop = false;

		if (m_once) {
			generateToAcmeFile(r2a, m_outputFileName);
		} else {
			long startTime = new Date().getTime();

			generateToAcmeFile(r2a, m_outputFileName);
			sleepForRate(stop, startTime);

			Path path = Paths.get(m_outputFileName);
			int dot = path.getFileName().toString().lastIndexOf(".");
			String stem = path.getFileName().toString();
			String ext = "";
			if (dot > 0) {
				stem = stem.substring(0, dot - 1);
				ext = stem.substring(dot, stem.length());
			}
			while (!stop) {
				startTime = new Date().getTime();
				for (int i = m_keep - 1; i > 0; i--) {
					Path kp = Paths.get(Paths.get(m_outputFileName).getParent().toString(), stem + i + ext);
					if (Files.exists(kp)) {
						Files.move(kp,
								Paths.get(Paths.get(m_outputFileName).getParent().toString(), stem + (i + 1) + ext));
					}
				}
				generateToAcmeFile(r2a, m_outputFileName);
				sleepForRate(stop, startTime);
			}
		}
	}

	private void sleepForRate(boolean stop, long startTime) {
		long endTime = new Date().getTime();
		long sleep = m_rate * 1000 - (endTime - startTime);
		if (sleep > 0 && !stop)
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
			}
	}

	private void generateToAcmeFile(ROSToAcmeTranslator r2a, String outputFileName) throws IOException {
		if (m_dataFileName != null) {
			StringBuffer data = new StringBuffer();
			Files.readAllLines(Paths.get(m_dataFileName)).forEach(l -> {
				data.append(l).append("\n");
			});
			UMSystem system = r2a.processROSDataToNewSystem(data.toString());
			String acme = StandaloneLanguagePackHelper.defaultLanguageHelper().elementToString(system, null);
			Files.write(Paths.get(outputFileName), acme.getBytes());
		} else {
			if (m_shellScript == null || !Files.exists(m_shellScript.toAbsolutePath()))
				createROSShellScript();
			try {
				ProcessBuilder pb = new ProcessBuilder("bash", m_shellScript.toString());
				pb.inheritIO();
				Process process = pb.start();
				process.waitFor();
				StringBuffer b = new StringBuffer();
				BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String s;
				while ((s = r.readLine()) != null) {
					b.append(s);
					b.append("\n");
				}
				UMSystem system = r2a.processROSDataToNewSystem(b.toString());
				String acme = StandaloneLanguagePackHelper.defaultLanguageHelper().elementToString(system, null);
				Files.write(Paths.get(outputFileName), acme.getBytes());
			} catch (InterruptedException e) {
			}

		}
	}

	private void createROSShellScript() throws IOException {
		m_shellScript = Files.createTempFile("script", ".sh");
		Writer streamWriter = new OutputStreamWriter(new FileOutputStream(m_shellScript.toFile()));
		PrintWriter printWriter = new PrintWriter(streamWriter);

		printWriter.println("#!/bin/bash");
		printWriter.println("nodes=$(rosnode list)");
		printWriter.println("for n in $nodes; do rosnode info $n; done");
		printWriter.close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processIgnores(Map ignores) {
		List<String> nodes = (List<String>) ignores.get("nodes");
		if (nodes != null) {
			this.m_ignoreNodes.addAll(nodes);
			nodes.forEach(a -> m_ignoreNodes.add("/" + a));
		}

		List<String> topics = (List<String>) ignores.get("topics");
		if (topics != null) {
			this.m_ignoreTopics.addAll(topics);
		}

		List<String> services = (List<String>) ignores.get("services");
		if (services != null)
			this.m_ignoreServices.addAll(services);

		List<String> actions = (List<String>) ignores.get("actions");
		if (actions != null)
			this.m_ignoreActions.addAll(actions);
	}

	private void processSeperateIgnores(Namespace args) {
		m_ignoreNodes = new HashSet<String>();
		m_ignoreTopics = new HashSet<String>();
		m_ignoreServices = new HashSet<String>();
		m_ignoreActions = new HashSet<String>();

		List<String> n = args.<String>getList("ignore-nodes");
		if (n != null)
			m_ignoreNodes.addAll(n);
		List<String> t = args.<String>getList("ignore-topics");
		if (t != null)
			m_ignoreTopics.addAll(t);
		List<String> s = args.<String>getList("ignore-services");
		if (s != null)
			m_ignoreServices.addAll(s);
		List<String> a = args.<String>getList("ignore-actions");
		if (a != null)
			m_ignoreActions.addAll(a);
	}

	private void setInputFileName(String dataFileName) {
		m_dataFileName = dataFileName;
	}

	private void setOnlyOnce(Boolean once) {
		m_once = once;
	}

	private void setOutputFileName(String outputFileName) {
		m_outputFileName = outputFileName;
	}

	private void setKeep(Integer keep) {
		m_keep = keep;
	}

	private void setRate(Integer rate) {
		this.m_rate = rate;
	}
}
