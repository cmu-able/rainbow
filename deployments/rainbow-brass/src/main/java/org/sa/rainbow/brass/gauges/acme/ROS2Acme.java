package org.sa.rainbow.brass.gauges.acme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acmestudio.acme.model.util.UMSystem;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.apache.commons.lang.NotImplementedException;
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

	public static void main(String[] a) {
		ArgumentParser parser = ArgumentParsers.newFor("ROS2Acme").build()
				.description("Process ROS data into Acme instance");
		parser.addArgument("-n", "--ignore-nodes").type(String.class).nargs("+").help("Ignore the following ROS nodes");
		parser.addArgument("-t", "--ignore-topics").type(String.class).nargs("+").help("Ignore these topics");
		parser.addArgument("-s", "--ignore-services").type(String.class).nargs("+").help("Ignore these services");
		parser.addArgument("-a", "--ignore-actions").type(String.class).nargs("+").help("Ignore these actions");
		parser.addArgument("-i", "--ignore").type(String.class)
				.help("The YAML file containing sections of things to ignore");
		parser.addArgument("-r", "--rate").type(Integer.class).setDefault(10).help("The loop rate (in seconds)");
		parser.addArgument("-1", "--once").action(Arguments.storeTrue()).help("Only do this once");
		parser.addArgument("-d", "--data").type(String.class).help("Take the ROS information from this file");
		parser.addArgument("-k", "--keep").type(Integer.class).setDefault(10)
				.help("The number of versions of the Acme file to keep");
		parser.addArgument("output").help(
				"The Acme file to produce (this file will hold the most recent description; if -1 is not specified then the files will be numbered 0..keep, where 0 is the oldest)");

		try {
			Namespace args = parser.parseArgs(a);

			ROS2Acme r2a = new ROS2Acme();
			r2a.processSeperateIgnores(args);
			r2a.processIgnore(args.getString("ignore"));

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
		while (!stop) {
			long startTime = new Date().getTime();
			if (m_dataFileName != null) {
				StringBuffer data = new StringBuffer();
				Files.readAllLines(Paths.get(m_dataFileName)).forEach(l -> {
					data.append(l).append("\n");
				});
				UMSystem system = r2a.processROSDataToNewSystem(data.toString());
				String acme = StandaloneLanguagePackHelper.defaultLanguageHelper().elementToString(system, null);
				Files.write(Paths.get(m_outputFileName), acme.getBytes());
				stop = true;
			} else {
				throw new NotImplementedException("These options aren't implemented yet");
			}

			long endTime = new Date().getTime();
			long sleep = m_rate * 1000 - (endTime - startTime);
			if (sleep > 0 && !stop)
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
				}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processIgnore(String filename) throws FileNotFoundException {
		File ignoreFile = new File(filename);
		Map ignores = (Map) Yaml.load(ignoreFile);
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
