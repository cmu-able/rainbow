package org.sa.rainbow.core.ports.guava;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;

public class GuavaDelegateManagementPort extends AbstractGuavaReportingPort
		implements IDelegateManagementPort, GuavaManagementPortConstants {

	private static final Logger LOGGER = Logger.getLogger(GuavaDelegateManagementPort.class);
	private RainbowDelegate m_delegate;

	public GuavaDelegateManagementPort(RainbowDelegate delegate) {
		super(ChannelT.HEALTH);
		m_delegate = delegate;

		getEventBus().addListener(new IGuavaMessageListener() {

			@Override
			public void receive(GuavaRainbowMessage msg) {
				String msgType = (String) msg.getProperty(ESEBConstants.MSG_TYPE_KEY);
				String did = (String) msg.getProperty(ESEBConstants.MSG_DELEGATE_ID_KEY);
				if (getDelegateId().equals(did)) {
					if (msgType != null) {
						boolean result;
						switch (msgType) {
						/*
						 * case SEND_CONFIGURATION_INFORMATION: sendConfigurationInformation
						 * (msg.pulloutProperties ()); break;
						 */
						case START_DELEGATE:
							result = startDelegate();
							getEventBus().replyToMessage(msg, result);
							break;
						case TERMINATE_DELEGATE:
							result = terminateDelegate();
							getEventBus().replyToMessage(msg, result);
							break;
						case PAUSE_DELEGATE:
							result = pauseDelegate();
							getEventBus().replyToMessage(msg, result);
						case START_PROBES:
							startProbes();
							break;
						case KILL_PROBES:
							killProbes();
						}
					}
				}
			}
		});
	}

	@Override
	public String getDelegateId() {
		return m_delegate.getId();
	}

	@Override
	public void sendConfigurationInformation(Properties configuration) {
		m_delegate.receiveConfigurationInformation(configuration, Collections.<ProbeAttributes>emptyList(),
				Collections.<EffectorAttributes>emptyList(), Collections.<GaugeInstanceDescription>emptyList());
	}

	@Override
	public void heartbeat() {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId());
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, RECEIVE_HEARTBEAT);
		LOGGER.debug(MessageFormat.format("Delegate {0} sending heartbeat.", getDelegateId()));
		getEventBus().publish(msg);
	}

	@Override
	public void requestConfigurationInformation() {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId());
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, REQUEST_CONFIG_INFORMATION);
		LOGGER.debug(MessageFormat.format("Delegate {0} requesting configuration information.", getDelegateId()));
		getEventBus().publish(msg);
	}

	@Override
	public boolean startDelegate() throws IllegalStateException {
		m_delegate.start();
		return true;
	}

	@Override
	public boolean pauseDelegate() throws IllegalStateException {
		m_delegate.stop();
		return true;
	}

	@Override
	public boolean terminateDelegate() throws IllegalStateException {
		m_delegate.terminate();
		return true;
	}

	@Override
	public void startProbes() throws IllegalStateException {
		m_delegate.startProbes();
	}

	@Override
	public void killProbes() throws IllegalStateException {
		m_delegate.killProbes();
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
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected void report(ReportType type, RainbowComponentT compType, String msg) {
		report(m_delegate.getId(), type, compType, msg);
	}

}
