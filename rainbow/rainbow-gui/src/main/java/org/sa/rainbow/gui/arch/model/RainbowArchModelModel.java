package org.sa.rainbow.gui.arch.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowEnvironmentDelegate;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.gui.arch.controller.RainbowModelController;

public class RainbowArchModelModel extends RainbowArchModelElement implements IRainbowModelChangeCallback {

	public static class RainbowModelOperationRepresentation extends OperationRepresentation {

		private String m_warning = "";

		public RainbowModelOperationRepresentation(String name, ModelReference modelRef, String target,
				String[] parameters) {
			super(name, modelRef, target, parameters);
		}

		public void setEntryWarning(String warning) {
			m_warning = warning;
		}

		public String getWarning() {
			return m_warning;
		}

	}

	public static final String OPERATION_PROP = "operation";
	public static final String OPERATION__ERROR_PROP = "operrror";
	private ModelReference m_modelRef;
	private Set<String> m_gauges = new HashSet<>();
	private List<Pair<Date, IRainbowOperation>> m_reports = new LinkedList<>();
	private List<Pair<Date, IRainbowOperation>> m_errors = new LinkedList<>();
	private Map<String, RainbowModelOperationRepresentation> m_operationsAccepted = new HashMap<>();

	private IModelChangeBusSubscriberPort m_modelChangePort;
	
	protected static IRainbowEnvironment m_rainbowEnvironment = new RainbowEnvironmentDelegate();


	public RainbowArchModelModel(ModelReference m) {
		super();
		m_modelRef = m;

		try {
			m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();

			m_modelChangePort.subscribe(new IRainbowChangeBusSubscription() {

				@Override
				public boolean matches(IRainbowMessage message) {
					return m_modelRef.getModelName().equals(message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP))
							&& m_modelRef.getModelType()
									.equals(message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP));
				}
			}, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addOperations();
	}

	private void addOperations() {
		IModelInstance<Object> rm = m_rainbowEnvironment.getRainbowMaster().modelsManager().getModelInstance(m_modelRef);
		if (rm == null)
			return;
		Map<String, Class<? extends AbstractRainbowModelOperation<?, Object>>> commands;
		Map<String, Method> commandMethods;
		Set<String> unhandledCommands;
		Set<Method> unhandledMethods;
		try {
			Method[] methods = rm.getCommandFactory().getClass().getMethods();
			commands = rm.getCommandFactory().getCommands();
			commandMethods = new HashMap<>();
			unhandledCommands = new HashSet<>(commands.keySet());
			unhandledMethods = new HashSet<>();
			for (Method method : methods) {
				if (IRainbowOperation.class.isAssignableFrom(method.getReturnType())) {
					String name = method.getName().toLowerCase();
					if (commands.containsKey(name)) {
						commandMethods.put(name, method);
						unhandledCommands.remove(name);
					} else {
						if (name.endsWith("cmd")) {
							name = name.substring(0, name.length() - 3);
							if (commands.containsKey(name)) {
								commandMethods.put(name, method);
								unhandledCommands.remove(name);
							}
						} else {
							name = name + "cmd";
							if (commands.containsKey(name)) {
								commandMethods.put(name, method);
								unhandledCommands.remove(name);
							} else {
								if (!method.getName().equals("generateCommand")
										&& !AbstractSaveModelCmd.class.isAssignableFrom(method.getReturnType())
										&& !AbstractLoadModelCmd.class.isAssignableFrom(method.getReturnType()))
									unhandledMethods.add(method);
							}
						}
					}

				}
			}

			for (Entry<String, Method> e : commandMethods.entrySet()) {
				String commandName = e.getKey();
//			Object[] row = new Object[] { commandName, "target", "args", "" };
				Parameter[] parameters = e.getValue().getParameters();
				String[] params = new String[0];
				String target = "target";
				if (parameters.length > 0) {
					String paramname = parameters[0].getName();
					target = !paramname.startsWith("arg") ? (paramname + " : ")
							: "" + parameters[0].getType().getSimpleName();
					params = fillParameters(parameters, 1);
				}
				RainbowModelOperationRepresentation op = new RainbowModelOperationRepresentation(commandName,
						m_modelRef, target, params);
				m_operationsAccepted.put(op.getName(), op);
//			((DefaultTableModel) m_table.getModel()).addRow(row);
			}
			for (String command : unhandledCommands) {
				Class<? extends AbstractRainbowModelOperation<?, Object>> class1 = commands.get(command);
				Constructor<?> constructor = class1.getConstructors()[0];
				String[] params = fillParameters(constructor.getParameters(), 2);
				RainbowModelOperationRepresentation op = new RainbowModelOperationRepresentation(command, m_modelRef,
						"target : String", params);
				op.setEntryWarning("No corresponding method factory entry.");
				m_operationsAccepted.put(op.getName(), op);
//			((DefaultTableModel) m_table.getModel()).addRow(row);
			}
			for (Method m : unhandledMethods) {

				Parameter[] parameters = m.getParameters();
				String target = "target : String";
				String[] params = new String[0];
				if (parameters.length > 0) {
					String paramname = parameters[0].getName();
					target = !paramname.startsWith("arg") ? (paramname + " : ")
							: "" + parameters[0].getType().getName();
					params = fillParameters(parameters, 1);
				}
				RainbowModelOperationRepresentation op = new RainbowModelOperationRepresentation(m.getName(),
						m_modelRef, "target : String", params);
				op.setEntryWarning("No corresponding entry in factory table.");
				m_operationsAccepted.put(op.getName(), op);
//			((DefaultTableModel) m_table.getModel()).addRow(row);
			}
		} catch (SecurityException | RainbowException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	protected String[] fillParameters(Parameter[] parameters, int start) {
		StringBuffer b = new StringBuffer();
		ArrayList<String> params = new ArrayList<>();
		for (int i = start; i < parameters.length; i++) {
			String name = parameters[i].getName();
			if (!name.startsWith("arg")) {
				b.append(name);
				b.append(" : ");
			}
			b.append(parameters[i].getType().getSimpleName());
			params.add(b.toString());
			b = new StringBuffer();
//			if (i + 1 < parameters.length)
//				b.append(", ");
		}
		return params.toArray(new String[0]);
	}

	@Override
	public String getId() {
		return m_modelRef.toString();
	}

	public ModelReference getModelRef() {
		return m_modelRef;
	}

	public void addGaugeReference(String gid) {
		m_gauges.add(gid);
	}

	public Collection<String> getGaugeReferences() {
		return m_gauges;
	}

	@Override
	public RainbowModelController getController() {
		return (RainbowModelController) super.getController();
	}

	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
		if (message.getProperty(IModelChangeBusPort.PARENT_ID_PROP) != null)
			return;
		IRainbowOperation op = msgToOperation(message);
		m_reports.add(new Pair<>(new Date(), op));
		pcs.firePropertyChange(OPERATION_PROP, null, op);
	}

	public IRainbowOperation msgToOperation(IRainbowMessage message) {
		String modelName = (String) message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP);
		if (modelName == null)
			throw new IllegalArgumentException("The message does not represent an operation");
		String commandName = (String) message.getProperty(IModelChangeBusPort.COMMAND_PROP);
		String target = (String) message.getProperty(IModelChangeBusPort.TARGET_PROP);
		List<String> params = new LinkedList<>();
		int i = 0;
		String numParams = (String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + i);
		while (numParams != null) {
			params.add(numParams);
			numParams = (String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + (++i));
		}
		OperationRepresentation rep = new OperationRepresentation(commandName, this.m_modelRef, target,
				params.toArray(new String[0]));
		return rep;
	}

	public void reportErrorForOperation(IRainbowOperation op, String error) {
		m_errors.add(new Pair<>(new Date(), op));
		pcs.firePropertyChange(OPERATION__ERROR_PROP, null, op);

	}

	public List<Pair<Date, IRainbowOperation>> getReports() {
		return m_reports;
	}

	public List<Pair<Date, IRainbowOperation>> getErrors() {
		return m_errors;
	}

	public Map<String, RainbowModelOperationRepresentation> getOperationsAccepted() {
		return m_operationsAccepted;
	}

	@Override
	public AbstractRainbowRunnable getRunnable() {
		return null;
	}

}
