package org.sa.rainbow.brass.gauges.acme;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.acmestudio.acme.element.IAcmePortType;
import org.acmestudio.acme.element.IAcmeRoleType;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.acmestudio.acme.model.util.UMAttachment;
import org.acmestudio.acme.model.util.UMComponent;
import org.acmestudio.acme.model.util.UMConnector;
import org.acmestudio.acme.model.util.UMPort;
import org.acmestudio.acme.model.util.UMRole;
import org.acmestudio.acme.model.util.UMSystem;
import org.acmestudio.acme.model.util.core.UMStringValue;
import org.acmestudio.acme.model.util.property.UMProperty;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;
import net.sourceforge.argparse4j.inf.Namespace;

public class ROSToAcmeTranslator implements ROSAcmeStyle {

	public static class IncompleteCommandsException extends Exception {

	}

	public static final String NODE_REGEXP = "Node \\[(.*)\\].*Publications:(.*)Subscriptions(.*)Services(.*)contacting.*\n";
	public static final Pattern NODE_PATTERN = Pattern.compile(NODE_REGEXP, Pattern.DOTALL);
	public static final Pattern TOPIC_PATTERN = Pattern.compile(".*\\* (.*) \\[(.*)\\]", Pattern.DOTALL);
	public static final Pattern SERVICE_PATTERN = Pattern.compile(".*\\* (.*)$");

	private Map<String, IRainbowOperation> m_commands;

	private Set<String> m_nodes2ignore = new HashSet<>();
	private Set<Pattern> m_topics2ignore = new HashSet<>();
	private Set<Pattern> m_services2ignore = new HashSet<>();
	private Set<Pattern> m_actions2ignore = new HashSet<>();

	public ROSToAcmeTranslator(Map<String, IRainbowOperation> commands) throws IncompleteCommandsException {
		m_commands = commands;
		checkCommands();

	}

	public ROSToAcmeTranslator() {
		m_commands = new HashMap<>();
	}

	protected void checkCommands() throws IncompleteCommandsException {
		for (Operations o : Operations.values()) {
			if (!m_commands.containsKey(o.opname()))
				throw new IncompleteCommandsException();
		}
	}

	public void setIgnorance(Collection<String> nodes, Collection<String> topics, Collection<String> services,
			Collection<String> actions) {
		m_nodes2ignore.addAll(nodes);
		services.forEach(s->m_services2ignore.add(Pattern.compile(s)));
		topics.forEach(t->m_topics2ignore.add(Pattern.compile(t)));
		actions.forEach(t->m_actions2ignore.add(Pattern.compile(t)));
	}

	/**
	 * Takes the data received from the probe (in the format of 'rosnode info' on
	 * each node and returns a temporary system that contains the architectural
	 * information.
	 * 
	 * <p>
	 * "Temporary system" means it may not be a full system (e.g., it does not
	 * import the RosFam) and so it should not be used for typechecking, and there
	 * will be unbound names.
	 * 
	 * <p>
	 * The data is a string that is constructed from the output of this script:
	 * 
	 * <pre>
	nodes=$(rosnode list)
	for i in $nodes; do
	rosnode info $i
	done
	 * </pre>
	 * 
	 * @param data
	 *            The information from rosinfo
	 * @return the temporary system
	 */
	public UMSystem processROSDataToNewSystem(String data) {
		String[] nodes = data.split("[\\-]+\r?\n");
		UMSystem system = new UMSystem("CurrentSystem");
		system.addDeclaredType(ROSFam);
		system.addInstantiatedType(ROSFam);

		Map<String, UMConnector> topicConnectors = new HashMap<>();
		Map<String, UMConnector> actionConnectors = new HashMap<>();
		Set<UMAttachment> attachments = new HashSet<>();
		Map<String, UMPort> servicePorts = new HashMap<>();

		// Ignore the first one because it is just boiler plate
		for (int i = 1; i < nodes.length; i++) {
			Matcher match = NODE_PATTERN.matcher(nodes[i]);
			match.matches();
			String nodeName = match.group(1);
			String publications = match.group(2);
			String subscriptions = match.group(3);
			String services = match.group(4);

			if (!m_nodes2ignore.contains(nodeName)) {

				UMComponent node = createNodeComponent(system, nodeName);
				attachments.addAll(processTopics(node, publications, subscriptions, topicConnectors, actionConnectors));

				processServices(node, services, servicePorts);
			}

		}

		for (UMAttachment att : attachments) {
			system.createUnifiedAttachment(att.getPort(), att.getRole());
		}

		return system;

	}

	/**
	 * 
	 * Takes information about the nodes in a ROS system and an Acme system
	 * representing old information about the system, and changes the system to
	 * represent the new data.
	 * 
	 * @param data
	 * @param system
	 */
	public void processRosDataToUpdateSystem(String data, UMSystem system) {
		List<IRainbowOperation> o = new LinkedList<>();
		List<Map<String, String>> p = new LinkedList<>();
		try {
			processROSDataToUpdateSystem(data, system, o, p, false);
		} catch (Exception e) {
		}
	}

	private UMComponent createNodeComponent(UMSystem system, String nodeName) {
		UMComponent node = system.createUnifiedComponent(makeAcmeName(nodeName));
		if (nodeName.endsWith("nodelet_manager")) {
			node.addDeclaredType(ROSNodeManagerCompT);
			node.addInstantiatedType(ROSNodeManagerCompT);
		} else {
			node.addDeclaredType(ROSNodeCompT);
			node.addInstantiatedType(ROSNodeCompT);
		}

		UMProperty nameProp = node.createUnifiedProperty("name", DefaultAcmeModel.defaultStringType(),
				new UMStringValue(nodeName));
		node.addProperty(nameProp);
		return node;
	}

	private void processServices(UMComponent node, String servicesStr, Map<String, UMPort> servicePorts) {
		Set<String> services = new HashSet<>(Arrays.asList(servicesStr.split("\r?\n")));
		for (String s : services) {
			Matcher matcher = SERVICE_PATTERN.matcher(s);
			if (matcher.matches()) {
				String serviceName = matcher.group(1);
				if (matchesRegex(m_services2ignore, serviceName)) continue;
				String portName = makeAcmeName(serviceName) + "_s";
				UMPort servicePort = createServiceResponderPort(node, portName);
				servicePorts.put(serviceName, servicePort);
			}
		}
	}

	private boolean matchesRegex(Set<Pattern> patterns, String item) {
		for (Pattern p : patterns) {
			if (p.matcher(item).matches()) return true;
		}
		return false;
	}

	UMPort createServiceResponderPort(UMComponent node, String portName) {
		UMPort servicePort;
		servicePort = node.createUnifiedPort(portName);
		servicePort.addDeclaredType(ServiceProviderPortT);
		servicePort.addInstantiatedType(ServiceProviderPortT);
		return servicePort;
	}

	private Set<UMAttachment> processTopics(UMComponent node, String publications, String subscriptions,
			Map<String, UMConnector> topicConnectors, Map<String, UMConnector> actionConnectors) {
		Set<String> advertisers = new HashSet<>(Arrays.asList(publications.split("\r?\n")));
		Set<String> subscribers = new HashSet<>(Arrays.asList(subscriptions.split("\r?\n")));

		Set<String> actions = new HashSet<>();
		filterTopicsForActions(actions, advertisers, subscribers);

		Set<UMAttachment> atts = new HashSet<>();

		atts.addAll(
				processTopicPorts(node, advertisers, topicConnectors, TopicAdvertisePortT, ROSTopicAdvertiserRoleT));
		atts.addAll(
				processTopicPorts(node, subscribers, topicConnectors, TopicSubscribePortT, ROSTopicSubscriberRoleT));
		atts.addAll(processActionPorts(node, actions, actionConnectors));
		return atts;
	}

	private Collection<? extends UMAttachment> processActionPorts(UMComponent node, Set<String> actions,
			Map<String, UMConnector> actionConnectors) {
		Set<UMAttachment> atts = new HashSet<>();
		for (String a : actions) {
			if (m_actions2ignore.contains(a)) continue;
			IAcmePortType portType = ActionServerPortT;
			IAcmeRoleType roleType = ROSActionResponderRoleT;
			String actionRoleName = "responder";

			if (a.endsWith("_c")) {
				portType = ActionClientPortT;
				roleType = ROSActionCallerRoleT;
				actionRoleName = "caller";
			}
			UMPort port = createActionPort(node, a, portType);

			String actionName = a.substring(0, a.length() - 2);
			UMConnector actionConn = actionConnectors.get(actionName);
			if (actionConn == null) {
				actionConn = ((UMSystem) node.getParent()).createUnifiedConnector(actionName + "_action_conn");
				actionConn.addDeclaredType(ActionServerConnT);
				actionConn.addInstantiatedType(ActionServerConnT);
				actionConnectors.put(actionName, actionConn);
			}
			UMRole ar = actionConn.createUnifiedRole(actionRoleName);
			ar.addDeclaredType(roleType);
			ar.addInstantiatedType(roleType);
			atts.add(new UMAttachment(port, ar));
		}
		return atts;
	}

	UMPort createActionPort(UMComponent node, String a, IAcmePortType portType) {
		UMPort port = node.createUnifiedPort(a);
		port.addDeclaredType(portType);
		port.addInstantiatedType(portType);
		return port;
	}

	private void filterTopicsForActions(Set<String> actions, Set<String> advertisers, Set<String> subscribers) {
		Map<String, Set<String>> actionCandidates = new HashMap<>();
		Set<String> topics = new HashSet<>(advertisers);
		topics.addAll(subscribers);
		boolean isServer = false;
		for (String topic : topics) {
			Matcher matcher = TOPIC_PATTERN.matcher(topic);
			if (matcher.matches()) {
				String topicName = matcher.group(1);
				if (topicName.endsWith("/cancel") || topicName.endsWith("/feedback") || topicName.endsWith("/goal")
						|| topicName.endsWith("/status") || topicName.endsWith("/result")) {
					String topicPrefix = topicName.substring(0, topicName.lastIndexOf("/"));
					Set<String> candidateSet = actionCandidates.get(topicPrefix);
					if (candidateSet == null) {
						candidateSet = new HashSet<>();
						actionCandidates.put(topicPrefix, candidateSet);
					}
					candidateSet.add(topic);
					if (topicName.endsWith("/cancel") && subscribers.contains(topic)) {
						isServer = true;
					}
				}
			}
		}

		for (Map.Entry<String, Set<String>> candidates : actionCandidates.entrySet()) {
			if (candidates.getValue().size() == 5) {
				String name = candidates.getKey();
				actions.add(makeAcmeName(name) + (isServer ? "_s" : "_c"));
				advertisers.removeAll(candidates.getValue());
				subscribers.removeAll(candidates.getValue());
			}
		}

	}

	String makeAcmeName(String name) {
		return name.replaceFirst("/", "").replaceAll("/", "_");
	}

	private Collection<? extends UMAttachment> processTopicPortsIncremental(UMComponent node, Set<String> topics,
			Map<String, UMConnector> topicConnectors, IAcmePortType topicType, IAcmeRoleType roleType,
			List<IRainbowOperation> ops, List<Map<String, String>> params, Set<String> accountedForPorts) {
		Set<UMAttachment> atts = new HashSet<>();
		for (String topic : topics) {
			Matcher match = TOPIC_PATTERN.matcher(topic);
			if (match.matches()) { 
				String topicName = match.group(1);
				if (matchesRegex(m_topics2ignore,topicName)) continue;
				String portName = topicName.replaceAll("/", "_")
						+ (topicType == TopicAdvertisePortT ? "_a" : "_s");
				UMPort port = node.getPort(portName);
				accountedForPorts.add(portName);
				if (port != null) {
					continue;
				}
				port = createTopicPort(node, topicType, match, portName);
				IRainbowOperation op = m_commands.get(Operations.CREATE_TOPIC_PORT.opname());
				Map<String, String> p = new HashMap<>();
				if (op != null) {
					p.put(op.getTarget(), node.getName());
					p.put(op.getParameters()[0], portName);
					p.put(op.getParameters()[1], topicType.getName());
					ops.add(op);
					params.add(p);
				}
				UMConnector topicConnector = getAppropriateTopicConnIncremental(node,
						topicName + "_" + match.group(2), topicConnectors, topicName, match.group(2), ops,
						params);
				UMRole r = createTopicRole(topicConnector, portName + "_" + node.getName(), roleType);
				op = m_commands.get(Operations.CREATE_TOPIC_ROLE.opname());
				if (op != null) {
					p = new HashMap<>();
					p.put(op.getTarget(), topicConnector.getName());
					p.put(op.getParameters()[0], r.getName());
					p.put(op.getParameters()[1], roleType.getName());
					ops.add(op);
					params.add(p);
				}
				atts.add(new UMAttachment(port, r));
			}
		}
		return atts;
	}

	private Set<UMAttachment> processTopicPorts(UMComponent node, Set<String> topics,
			Map<String, UMConnector> topicConnectors, IAcmePortType topicType, IAcmeRoleType roleType) {
		Set<UMAttachment> atts = new HashSet<>();
		for (String topic : topics) {
			Matcher match = TOPIC_PATTERN.matcher(topic);
			if (match.matches()) {
				String topicName = match.group(1);
				if (matchesRegex(m_topics2ignore,topicName)) continue;
				String portName = topicName.replaceAll("/", "_")
						+ (topicType == TopicAdvertisePortT ? "_a" : "_s");
				UMPort port = createTopicPort(node, topicType, match, portName);

				UMConnector topicConnector = getAppropriateTopicConn(node, topicName + "_" + match.group(2),
						topicConnectors, topicName, match.group(2));
				UMRole r = createTopicRole(topicConnector, portName, roleType);

				atts.add(new UMAttachment(port, r));
			}
		}

		return atts;
	}

	public void processROSDataToUpdateSystem(String data, UMSystem currentSystem, List<IRainbowOperation> ops,
			List<Map<String, String>> params) throws IncompleteCommandsException {
		processROSDataToUpdateSystem(data, currentSystem, ops, params, true);
	}

	/**
	 * Takes information about the nodes in a ROS system and an Acme system
	 * representing old information about the system, and (a) changes the system to
	 * represent the new data and (b) places operations and parameters in the other
	 * parameters that will turn a system into the new system.
	 * 
	 * @param data
	 *            The probe data @see processProbeToNewSystem
	 * @param currentSystem
	 *            The system that the data will be applied to
	 * @param ops
	 *            Will be filled with operations that would change the old current
	 *            system into the new one
	 * @param params
	 *            Contains the params for the operations (they are matched by their
	 *            order inn the list)
	 * @throws IncompleteCommandsException
	 */
	public void processROSDataToUpdateSystem(String data, UMSystem currentSystem, List<IRainbowOperation> ops,
			List<Map<String, String>> params, boolean check) throws IncompleteCommandsException {
		if (check) {
			checkCommands();
		}
		String[] nodes = data.split("[\\-]+\r?\n");

		Map<String, UMConnector> topicConnectors = new HashMap<>();
		Map<String, UMConnector> actionConnectors = new HashMap<>();
		Set<UMAttachment> attachments = new HashSet<>();
		Map<String, UMPort> servicePorts = new HashMap<>();
		Set<String> accountedForNodes = new HashSet<>();
		// Ignore the first one because it is just boiler plate
		for (int i = 1; i < nodes.length; i++) {
			Set<String> accountedForPorts = new HashSet<>();
			Matcher match = NODE_PATTERN.matcher(nodes[i]);
			match.matches();
			String nodeName = match.group(1);
			String publications = match.group(2);
			String subscriptions = match.group(3);
			String services = match.group(4);
			if (m_nodes2ignore.contains(nodeName)) continue; // ignore this node
			String name = makeAcmeName(nodeName);
			accountedForNodes.add(name);
			UMComponent node = currentSystem.getComponent(name);
			if (node == null) {
				node = createNodeComponent(currentSystem, nodeName);
				IRainbowOperation op = m_commands.get(Operations.NEW_ROS_NODE.opname());
				if (node.declaresType(ROSNodeManagerCompT)) {
					op = m_commands.get(Operations.NEW_ROS_NODE_MANAGER.opname());
				}
				if (op != null) {
					Map<String, String> p = new HashMap<>();
					p.put(op.getTarget(), currentSystem.getName());
					p.put(op.getParameters()[0], name);
					ops.add(op);
					params.add(p);
				}
			}
			attachments.addAll(processTopicsIncremental(node, publications, subscriptions, topicConnectors,
					actionConnectors, ops, params, accountedForPorts));
			processServicesIncremental(node, services, servicePorts, ops, params, accountedForPorts);

			Set<String> portsToRemove = new HashSet<String>();
			for (UMPort p : node.getPorts()) {
				portsToRemove.add(p.getName());
			}
			portsToRemove.removeAll(accountedForPorts);
			for (String port : portsToRemove) {
				node.removeUnifiedPort(port);
				IRainbowOperation op = m_commands.get(Operations.DELETE_PORT.opname());
				if (op != null) {
					Map<String, String> p = new HashMap<>();
					p.put(op.getParameters()[0], port);
					p.put(op.getTarget(), node.getName());
					ops.add(op);
					params.add(p);
				}
			}

		}

		for (UMAttachment att : attachments) {
			currentSystem.createUnifiedAttachment(att.getPort(), att.getRole());
			IRainbowOperation op = m_commands.get(Operations.CREATE_ATTACHMENT.opname());
			if (op != null) {
				Map<String, String> p = new HashMap<String, String>();
				p.put(op.getTarget(), currentSystem.getName());
				p.put(op.getParameters()[0], att.getPort().getQualifiedName());
				p.put(op.getParameters()[1], att.getRole().getQualifiedName());
				ops.add(op);
				params.add(p);
			}
		}

		Set<String> nodesToRemove = new HashSet<>();
		for (UMComponent node : currentSystem.getComponents()) {
			nodesToRemove.add(node.getName());
		}

		nodesToRemove.removeAll(accountedForNodes);
		for (String n : nodesToRemove) {
			currentSystem.removeUnifiedComponent(n);
			IRainbowOperation op = m_commands.get(Operations.DELETE_COMPONENT.opname());
			if (op != null) {
				Map<String, String> p = new HashMap<>();
				p.put(op.getTarget(), currentSystem.getName());
				p.put(op.getParameters()[0], n);
				ops.add(op);
				params.add(p);
			}
		}

		// Clean up connectors
		for (UMConnector conn : currentSystem.getConnectors()) {
			Set<UMRole> disconnectedRoles = new HashSet<>();
			for (UMRole role : conn.getRoles()) {
				if (currentSystem.getAttachments(role).isEmpty()) {
					disconnectedRoles.add(role);
				}
			}
			if (disconnectedRoles.size() == conn.getRoles().size()) {
				currentSystem.removeUnifiedConnector(conn.getName());
				IRainbowOperation op = m_commands.get(Operations.DELETE_CONNECTOR.opname());
				if (op != null) {
					Map<String, String> p = new HashMap<>();
					p.put(op.getTarget(), conn.getName());
					ops.add(op);
					params.add(p);
				}
			} else {
				for (UMRole r : disconnectedRoles) {
					conn.removeUnifiedRole(r.getName());
					IRainbowOperation op = m_commands.get(Operations.DELETE_ROLE.opname());
					if (op != null) {
						Map<String, String> p = new HashMap<>();
						p.put(op.getTarget(), conn.getName());
						p.put(op.getParameters()[0], r.getName());
						ops.add(op);
						params.add(p);
					}
				}
			}
		}
	}

	private void processServicesIncremental(UMComponent node, String servicesStr, Map<String, UMPort> servicePorts,
			List<IRainbowOperation> ops, List<Map<String, String>> params, Set<String> accountedForPorts) {
		Set<String> services = new HashSet<>(Arrays.asList(servicesStr.split("\r?\n")));
		for (String s : services) {
			Matcher matcher = SERVICE_PATTERN.matcher(s);
			if (matcher.matches()) {
				String serviceName = matcher.group(1);
				if (matchesRegex(m_services2ignore,serviceName)) continue;
				String portName = makeAcmeName(serviceName) + "_s";
				accountedForPorts.add(portName);
				UMPort servicePort = node.getPort(portName);
				if (servicePort == null) {
					servicePort = createServiceResponderPort(node, portName);
					servicePorts.put(serviceName, servicePort);
					IRainbowOperation op = m_commands.get(Operations.CREATE_SERVICE_RESPONDER_PORT.opname());
					if (op != null) {
						Map<String, String> p = new HashMap<>();
						p.put(op.getTarget(), node.getName());
						p.put(op.getParameters()[0], servicePort.getName());
						p.put(op.getParameters()[1], ServiceProviderPortT.getName());
						ops.add(op);
						params.add(p);
					}
				}
			}
		}
	}

	private Collection<? extends UMAttachment> processTopicsIncremental(UMComponent node, String publications,
			String subscriptions, Map<String, UMConnector> topicConnectors, Map<String, UMConnector> actionConnectors,
			List<IRainbowOperation> ops, List<Map<String, String>> params, Set<String> accountedForPorts) {
		Set<String> advertisers = new HashSet<>(Arrays.asList(publications.split("\r?\n")));
		Set<String> subscribers = new HashSet<>(Arrays.asList(subscriptions.split("\r?\n")));

		Set<String> actions = new HashSet<>();
		filterTopicsForActions(actions, advertisers, subscribers);

		Set<UMAttachment> atts = new HashSet<>();
		atts.addAll(processTopicPortsIncremental(node, advertisers, topicConnectors, TopicAdvertisePortT,
				ROSTopicAdvertiserRoleT, ops, params, accountedForPorts));
		atts.addAll(processTopicPortsIncremental(node, subscribers, topicConnectors, TopicSubscribePortT,
				ROSTopicSubscriberRoleT, ops, params, accountedForPorts));
		atts.addAll(processActionPortsIncremental(node, actions, actionConnectors, ops, params));
		accountedForPorts.addAll(actions);

		return atts;
	}

	private Collection<? extends UMAttachment> processActionPortsIncremental(UMComponent node, Set<String> actions,
			Map<String, UMConnector> actionConnectors, List<IRainbowOperation> ops, List<Map<String, String>> params) {
		Set<UMAttachment> atts = new HashSet<>();

		for (String a : actions) {
			if (matchesRegex(m_actions2ignore, a)) continue;
			IAcmePortType portType = ActionServerPortT;
			IAcmeRoleType roleType = ROSActionResponderRoleT;
			String actionRoleName = "responder";

			if (a.endsWith("_c")) {
				portType = ActionClientPortT;
				roleType = ROSActionCallerRoleT;
				actionRoleName = "caller";
			}
			UMPort port = node.getPort(a);
			if (port == null) {
				port = createActionPort(node, a, portType);
				IRainbowOperation op = m_commands.get(Operations.CREATE_ACTION_PORT.opname());
				Map<String, String> p = new HashMap<>();
				if (op != null) {
					ops.add(op);
					p.put(op.getTarget(), node.getName());
					p.put(op.getParameters()[0], port.getName());
					p.put(op.getParameters()[1], portType.getName());
					params.add(p);
				}
				String actionName = a.substring(0, a.length() - 2);
				UMConnector actionConn = actionConnectors.get(actionName);
				if (actionConn == null) {
					actionConn = ((UMSystem) node.getParent()).createUnifiedConnector(actionName + "_action_conn");
					actionConn.addDeclaredType(ActionServerConnT);
					actionConn.addInstantiatedType(ActionServerConnT);
					actionConnectors.put(actionName, actionConn);
					op = m_commands.get(Operations.CREATE_ACTION_CONNECTOR.opname());
					if (op != null) {
						ops.add(op);
						p = new HashMap<>();
						p.put(op.getParameters()[0], actionName + "_action_conn");
						params.add(p);
					}
				}

				String arName = actionRoleName + "_" + node.getName();
				UMRole ar = actionConn.createUnifiedRole(arName);
				ar.addDeclaredType(roleType);
				ar.addInstantiatedType(roleType);
				atts.add(new UMAttachment(port, ar));
				actionConnectors.put(actionName, actionConn);
				op = m_commands.get(Operations.CREATE_ACTION_ROLE.opname());
				if (op != null) {
					p = new HashMap<>();
					p.put(op.getTarget(), actionName + "_action_conn");
					p.put(op.getParameters()[0], arName);
					p.put(op.getParameters()[1], roleType.getName());
				}
			}
		}
		return atts;
	}

	UMRole createTopicRole(UMConnector topicConnector, String portName, IAcmeRoleType roleType) {
		UMRole r = topicConnector.createUnifiedRole(portName);
		r.addDeclaredType(roleType);
		r.addInstantiatedType(roleType);
		return r;
	}

	UMPort createTopicPort(UMComponent node, IAcmePortType topicType, Matcher match, String portName) {
		UMPort port = node.createUnifiedPort(portName);
		port.addDeclaredType(topicType);
		port.addInstantiatedType(topicType);
		UMProperty topicProp = port.createUnifiedProperty("topic", null, new UMStringValue(match.group(1)));

		UMProperty msgTypeProp = port.createUnifiedProperty("msg_type", null, new UMStringValue(match.group(2)));
		return port;
	}

	private UMConnector getAppropriateTopicConn(UMComponent node, String topic,
			Map<String, UMConnector> topicConnectors, String topicProp, String msgType) {
		UMConnector conn = topicConnectors.get(topic);
		if (conn == null) {
			conn = createTopicConnector(node, topic, topicProp, msgType);
			topicConnectors.put(topic, conn);
		}
		return conn;
	}

	UMConnector createTopicConnector(UMComponent node, String topic, String topicProp, String msgType) {
		UMConnector conn;
		conn = ((UMSystem) node.getParent()).createUnifiedConnector(topic.replaceAll("/", "_").replaceAll(" ", "_"));
		conn.addDeclaredType(TopicConnectorT);
		conn.addInstantiatedType(TopicConnectorT);
		conn.createUnifiedProperty("topic", DefaultAcmeModel.defaultStringType(), new UMStringValue(topicProp));
		conn.createUnifiedProperty("msg_type", DefaultAcmeModel.defaultStringType(), new UMStringValue(msgType));
		return conn;
	}

	private UMConnector getAppropriateTopicConnIncremental(UMComponent node, String topic,
			Map<String, UMConnector> topicConnectors, String topicProp, String msgType, List<IRainbowOperation> ops,
			List<Map<String, String>> params) {
		UMConnector conn = topicConnectors.get(topic);
		if (conn == null) {
			conn = ((UMSystem) node.getParent()).getConnector(topic.replaceAll("/", "_").replaceAll(" ", "_"));
			if (conn == null) {
				conn = createTopicConnector(node, topic, topicProp, msgType);
				topicConnectors.put(topic, conn);
				IRainbowOperation op = m_commands.get(Operations.CREATE_TOPIC_CONNECTOR.opname());
				if (op != null) {
					Map<String, String> p = new HashMap<>();
					p.put(op.getTarget(), node.getParent().getName());
					p.put(op.getParameters()[0], conn.getName());
					p.put(op.getParameters()[1], topicProp);
					p.put(op.getParameters()[2], msgType);
					ops.add(op);
					params.add(p);
				}
			}
		}
		return conn;
	}

}