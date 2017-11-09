package org.sa.rainbow.stitch.test;

import java.util.HashMap;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.IRainbowRunnable.State;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.stitch.adaptation.IStitchExecutor;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;

public class TestStitchExecutor implements IStitchExecutor {
	private IModelUSBusPort m_testHMPort;
	private IModelDSBusPublisherPort m_operationPublishingPort;
	private IRainbowReportingPort m_reportingPort;
	
	public TestStitchExecutor(IModelUSBusPort mub, IModelDSBusPublisherPort mdp, IRainbowReportingPort rrp) {
		m_testHMPort = mub;
		m_operationPublishingPort = mdp;
		m_reportingPort = rrp;
	}

	@Override
	public IModelUSBusPort getHistoryModelUSPort() {
		if (m_testHMPort == null) {
			m_testHMPort = new NullTestModelUSPort();
		}
		return m_testHMPort;
	}

	@Override
	public void setModelToManage(ModelReference modelRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(IRainbowReportingPort reportingPort) throws RainbowConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IModelDSBusPublisherPort getOperationPublishingPort() {
		if (m_operationPublishingPort == null) {
			m_operationPublishingPort = new NullTestModelDSPublisherPort();
		}
		return m_operationPublishingPort;
	}

	@Override
	public IRainbowReportingPort getReportingPort() {
		if (m_reportingPort == null) {
			m_reportingPort = new NullTestReportingPort();
		}
		return m_reportingPort;
	}

	@Override
	public String id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public State state() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDisposed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ExecutionHistoryModelInstance getExecutionHistoryModel() {
		return new ExecutionHistoryModelInstance(new HashMap<String,ExecutionHistoryData> (), "testHistory", "test");
	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.EXECUTOR;
	}
}