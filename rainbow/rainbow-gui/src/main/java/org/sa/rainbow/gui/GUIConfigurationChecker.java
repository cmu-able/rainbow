package org.sa.rainbow.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ho.yaml.Yaml;
import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.util.IRainbowConfigurationChecker;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.RainbowConfigurationChecker.ProblemT;
import org.sa.rainbow.util.Util;

public class GUIConfigurationChecker implements IRainbowConfigurationChecker {

	private IRainbowMaster m_master;
	private LinkedList<Problem> m_problems;

	private static final List<String> CATEGORIES = Arrays.asList("meter", "timeseries", "onoff");
	private static final List<String> COMPONENTS = Arrays.asList("analyzers", "managers", "executors", "models");

	private static final Collection<String> getValidFields(String category) {
		switch (category) {
		case "meter":
			return Arrays.asList("upper", "lower", "threshold");
		case "timeseries":
			return Arrays.asList("upper", "lower");
		case "onoff":
			return Collections.<String>emptySet();
		}
		return Collections.<String>emptySet();
	}

	public GUIConfigurationChecker() {
		m_problems = new LinkedList<Problem>();
	}

	@Override
	public void checkRainbowConfiguration() {
		File specs = Util.getRelativeToPath(Rainbow.instance().getTargetPath(),
				Rainbow.instance().getProperty("rainbow.gui.specs"));
		if (specs != null) {
			try {
				Problem p = new Problem(ProblemT.INFO, "Checking GUI configuration...");
				m_problems.add(p);
				Map<String, Object> uidb = (Map<String, Object>) Yaml.load(specs);
				checkGaugeGUI((Map<String, Object>) uidb.get("gauges"));
				checkComponents(uidb);
				checkDetails((Map<String, Object>) uidb.get("details"));
				if (m_problems.size()==1) {
					p.msg += "ok";
				}
				
			} catch (FileNotFoundException e) {
				m_problems.add(new Problem(ProblemT.ERROR, MessageFormat
						.format("The UI configuration file ''{0}'' does not exist or is not readable", specs)));
			}
		} else {
			m_problems.add(new Problem(ProblemT.WARNING,
					"The property 'rainbow.gui.specs' is not specfied in the rainbow properties file, even though the GUI is expecting it."));
		}
	}

	private void checkDetails(Map<String, Object> uidb) {
		for (String component : COMPONENTS) {
			String cls = (String) uidb.get(component);
			if (cls != null) {
				try {
					getClass().forName(cls);
				} catch (ClassNotFoundException e) {
					m_problems.add(new Problem(ProblemT.ERROR, 
							MessageFormat.format("The class ''{0}'' was not found.", cls)));
				}
			}
		}
	}

	private void checkComponents(Map<String, Object> uidb) {
		for (String component : COMPONENTS) {
			Map<String, Object> cust = (Map<String, Object>) uidb.get(component);
			if (cust != null) {
				for (Entry<String, Object> entry : cust.entrySet()) {
					try {
						Class.forName(entry.getKey());
					} catch (ClassNotFoundException e) {
						m_problems.add(new Problem(ProblemT.ERROR, 
								MessageFormat.format("The class ''{0}'' was not found.", entry.getKey())));
					}
					
					try {
						Class.forName((String )entry.getValue());
					} catch (ClassNotFoundException e) {
						m_problems.add(new Problem(ProblemT.ERROR, 
								MessageFormat.format("The class ''{0}'' was not found.", entry.getKey())));
					}
				}
			}
		}
	}

	private void checkGaugeGUI(Map<String, Object> map) {
		Collection<String> processed = Arrays.asList("category", "command", "value");
		for (Entry<String, Object> entry : map.entrySet()) {
			if (!m_master.gaugeDesc().typeSpec.containsKey(entry.getKey())) {
				m_problems.add(new Problem(ProblemT.ERROR,
						MessageFormat.format("The gauge type ''{0}'' is not valid", entry.getKey())));
			} else {
				Map<String, Object> fields = (Map<String, Object>) ((Map<String, Object>) entry.getValue())
						.get("builtin");
				if (fields == null) {
					m_problems.add(new Problem(ProblemT.ERROR, "Only builting gauge types are currently supported"));
				} else {
					String category = (String) fields.get("category");
					if (!CATEGORIES.contains(category)) {
						m_problems.add(new Problem(ProblemT.ERROR, "Only builtin gauge types are currently supported"));
					}
					String command = (String) fields.get("command");
					if (command == null) {
						m_problems.add(new Problem(ProblemT.ERROR,
								"Gauge UI widgets must specify the command that they are displaying."));
						continue;
					}
					try {
						IRainbowOperation op = OperationRepresentation.parseCommandSignature(command);
						boolean found = false;
						for (Pair<String, OperationRepresentation> gt : m_master.gaugeDesc().typeSpec
								.get(entry.getKey()).commandSignatures()) {
							if (gt.secondValue().getName().equals(op.getName())) {
								found = true;
							}
						}
						if (!found) {
							m_problems.add(new Problem(ProblemT.ERROR,
									MessageFormat.format("The specified command ''{0}'' is not a valid command for {1}",
											command, entry.getKey())));
						} else {
							String param = (String) fields.get("value");
							if (param == null) {
								m_problems.add(new Problem(ProblemT.ERROR, MessageFormat
										.format("{0} must specify a value to display", command, entry.getKey())));
							} else {
								if (!Arrays.asList(op.getParameters()).contains(param)) {
									m_problems.add(new Problem(ProblemT.ERROR, MessageFormat.format(
											"The specified command ''{0}'' does not have a parameter called ''{2}'' in {1}",
											command, entry.getKey(), param)));
								}
							}
						}
						Collection<String> validFields = getValidFields(category);
						for (String key : fields.keySet()) {
							if (!processed.contains(key)) {
								if (!validFields.contains(key)) {
									m_problems.add(new Problem(ProblemT.ERROR, MessageFormat
											.format("''{1}'' is not a valid parameter for {0}", entry.getKey(), key)));
								}
							}
						}
					} catch (Throwable e) {
						m_problems.add(new Problem(ProblemT.ERROR,
								MessageFormat.format("The specified command ''{0}'' is not a valid command", command)));
						continue;
					}

				}
			}
		}
	}

	@Override
	public void setRainbowMaster(IRainbowMaster master) {
		m_master = master;

	}

	@Override
	public Collection<Problem> getProblems() {
		return m_problems;
	}

	@Override
	public Collection<Class> getMustBeExecutedAfter() {
		return Collections.<Class>emptySet();
	}

}
