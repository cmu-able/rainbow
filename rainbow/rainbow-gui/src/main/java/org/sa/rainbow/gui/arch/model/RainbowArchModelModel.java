package org.sa.rainbow.gui.arch.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.gui.arch.controller.RainbowModelController;

public class RainbowArchModelModel extends RainbowArchModelElement implements IRainbowModelChangeCallback {

	public static final String OPERATION_PROP = "operation";
	public static final String OPERATION__ERROR_PROP = "operrror";
	private ModelReference m_modelRef;
	private Set<String> m_gauges = new HashSet<>();
	private List<Pair<Date,IRainbowOperation>> m_reports = new LinkedList<>();
	private List<Pair<Date,IRainbowOperation>> m_errors = new LinkedList<>();
	private IModelChangeBusSubscriberPort m_modelChangePort;

	public RainbowArchModelModel(ModelReference m) {
		super();
		m_modelRef = m;
		
		try {
			m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();

			m_modelChangePort.subscribe(new IRainbowChangeBusSubscription() {

				@Override
				public boolean matches(IRainbowMessage message) {
					return m_modelRef.getModelName().equals(message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP))
							&& m_modelRef.getModelType().equals(message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP));
				}
			}, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		m_reports.add(new Pair<> (new Date(), op));
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
		m_errors.add(new Pair<> (new Date(), op));
		pcs.firePropertyChange(OPERATION__ERROR_PROP, null, op);

	}

	public List<Pair<Date, IRainbowOperation>> getReports() {
		return m_reports;
	}


	public List<Pair<Date, IRainbowOperation>> getErrors() {
		return m_errors;
	}


	

}
