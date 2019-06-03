package org.sa.rainbow.core.ports.guava;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.DisconnectedRainbowManagementPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IDelegateMasterConnectionPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;

public class GuavaDelegateConnectionPort extends AbstractGuavaReportingPort implements IDelegateMasterConnectionPort {
	

	private static final Logger LOGGER = Logger.getLogger(GuavaDelegateConnectionPort.class);
	
	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
	
	private IDelegateManagementPort m_deploymentPort;

	private RainbowDelegate m_delegate;
	
	public  GuavaDelegateConnectionPort(RainbowDelegate delegate) {
		super (org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT.HEALTH);
		m_delegate = delegate;
		
		getEventBus().addListener(new IGuavaMessageListener() {
			
			@Override
			public void receive(GuavaRainbowMessage msg) {
				String type = (String) msg.getProperty(ESEBConstants.MSG_TYPE_KEY);
				switch (type) {
				case ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE: {
					if (msg.hasProperty(ESEBConstants.TARGET)
							&& m_delegate.getId().equals(msg.getProperty(ESEBConstants.TARGET))) {
						m_delegate.disconnectFromMaster();
					}
				}
				}
			}
		});
	}

	
	@Override
	public IDelegateManagementPort connectDelegate(String delegateID, Properties connectionProperties)
			throws RainbowConnectionException {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.fillProperties(connectionProperties);
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, delegateID);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_CONNECT_DELEGATE);

		m_deploymentPort = null;
		try {
			getEventBus().blockingSendAndReceive(msg, new IGuavaMessageListener() {
				
				@Override
				public void receive(GuavaRainbowMessage msgRcvd) {
					String reply = (String) msgRcvd.getProperty(ESEBConstants.MSG_CONNECT_REPLY);
					if (!ESEBConstants.MSG_REPLY_OK.equals(reply)) {
						LOGGER.error(
								MessageFormat.format("Delegate {0}: connectDelegate received the following reply: {1}",
										m_delegate.getId(), reply));
					} else {
						try {
							m_deploymentPort = RainbowPortFactory.createDelegateDeploymentPort(m_delegate,
									m_delegate.getId());
						} catch (RainbowConnectionException e) {

						}
					}
				}
			}, Rainbow.instance().getProperty(IRainbowEnvironment.PROPKEY_PORT_TIMEOUT, 10000));
		}catch (RainbowConnectionException e) {
			if (m_deploymentPort == null)
				throw e;
		}
		if (m_deploymentPort == null) {
			LOGGER.error("The call to connectDelegate timed out without returning a deployment port...");
			// REVIEW: Throw an exception instead
			m_deploymentPort = DisconnectedRainbowManagementPort.instance();
		}
		return m_deploymentPort;
	}

	@Override
	public void disconnectDelegate(String delegateId) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE);
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, delegateId);
		getEventBus().publish(msg);
	}


	public void report(String delegateID, ReportType type, RainbowComponentT compT, String msg) {
		GuavaRainbowMessage gMsg = new GuavaRainbowMessage();
		gMsg.setProperty(ESEBConstants.MSG_CHANNEL_KEY, ChannelT.UIREPORT.name());
		gMsg.setProperty(ESEBConstants.COMPONENT_TYPE_KEY, compT.name());
		gMsg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UI_REPORT);
		gMsg.setProperty(ESEBConstants.REPORT_TYPE_KEY, type.name());
		gMsg.setProperty(ESEBConstants.REPORT_MSG_KEY, msg);
		gMsg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, delegateID);
		getReportEventBus().publish(gMsg);
	}

	

	protected void report(ReportType type, RainbowComponentT compType, String msg, Throwable t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		t.printStackTrace(ps);
		ps.close();
		report(m_delegate.getId(), type, compType,
				MessageFormat.format("{0}.\nException: {1}\n{2}", msg, t.getMessage(), baos.toString()));
	}

	@Override
	protected void report(ReportType type, RainbowComponentT compType, String msg) {
		report(m_delegate.getId(),type, compType, msg);
		
	}

}
