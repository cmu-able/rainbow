package org.sa.rainbow.gui.arch.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelUpdater;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.Pair;

public class RainbowArchGaugeModel extends RainbowArchModelElement implements IModelUpdater{

	public static final String GAUGEREPORT = "gaugereport";
	public static final String GAUGEREPORTS = "gaugereports";
	private GaugeInstanceDescription m_gaugeDesc;
	private String m_id;
	
	private Map<String, List<Pair<Date, IRainbowOperation>>> m_operations;
	private List<String> m_probes = null;

	private IModelUSBusPort m_usPort;
	
	public RainbowArchGaugeModel(GaugeInstanceDescription gaugeDesc) {
		super();
		m_gaugeDesc = gaugeDesc;
		
		m_operations = new HashMap<>();
		for (Pair<String, OperationRepresentation> key : gaugeDesc.commandSignatures()) {
			m_operations.put(key.secondValue().getName(), new LinkedList<>());
		}
		try {
			m_usPort = RainbowPortFactory.createModelsManagerUSPort(this);
		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public String getId() {
		if (m_id == null) {
			m_id = GaugeInstanceDescription.genID(m_gaugeDesc);
		}
		return m_id;
//		return Util.genID(m_gaugeDesc.gaugeName(), m_gaugeDesc.)
	}

	public GaugeInstanceDescription getGaugeDesc() {
		return m_gaugeDesc;
	}
	
	public Map<String,List<Pair<Date,IRainbowOperation>>> getOperations() {
		return m_operations;
	}
	
    private void addOperation(IRainbowOperation op) {
		m_operations.get(op.getName()).add(new Pair<Date,IRainbowOperation>(new Date(), op));
		pcs.firePropertyChange(GAUGEREPORT, null, op);
	}
	
	public void addProbe(String probeId) {
		if (m_probes == null) {
			m_probes = new LinkedList<>();
		}
		m_probes.add(probeId);
	}
	
	

	@Override
	public void requestModelUpdate(IRainbowOperation command) throws IllegalStateException, RainbowException {
		if (!getId().equals(command.getOrigin())) return;

		addOperation(command);
	}

	@Override
	public void requestModelUpdate(List<IRainbowOperation> commands, boolean transaction)
			throws IllegalStateException, RainbowException {
		Date d = new Date();
		for (IRainbowOperation op : commands) {
			m_operations.get(op.getName()).add(new Pair<Date,IRainbowOperation>(d,op));
		}
		pcs.firePropertyChange(GAUGEREPORTS, null, commands);
	}

	@Override
	public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getProbes() {
		return m_probes;
	}

	@Override
	public AbstractRainbowRunnable getRunnable() {
		return null;
	}
}
