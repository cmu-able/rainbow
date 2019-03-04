package org.sa.rainbow.core.ports.guava;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;

import com.google.common.eventbus.Subscribe;

public class GuavaMasterSideManagementPort extends AbstractGuavaReportingPort
		implements IDelegateManagementPort, GuavaManagementPortConstants {

	private static final Logger LOGGER = Logger.getLogger(GuavaMasterSideManagementPort.class);

	private RainbowMaster m_rainbowMaster;
	private String m_delegateID;
	private Properties m_connectionProperties;

	public GuavaMasterSideManagementPort(RainbowMaster rainbowMaster, String delegateID,
			Properties connectionProperties) {
		super(ChannelT.HEALTH);
		m_rainbowMaster = rainbowMaster;
		m_delegateID = delegateID;
		m_connectionProperties = connectionProperties;
		getEventBus().addListener(new IGuavaMessageListener() {

			@Override
			public void receive(GuavaRainbowMessage msg) {
				String msgType = (String) msg.getProperty(ESEBConstants.MSG_TYPE_KEY);
				switch (msgType) {
				case REQUEST_CONFIG_INFORMATION: {
					if (msg.getProperty(ESEBConstants.MSG_DELEGATE_ID_KEY).equals(getDelegateId())) {
						requestConfigurationInformation();
					}

				}
					break;
				case RECEIVE_HEARTBEAT: {
					if (msg.getProperty(ESEBConstants.MSG_DELEGATE_ID_KEY).equals(getDelegateId())) {
						heartbeat();
					}
				}
				}
			}
		});
	}

	@Override
	public String getDelegateId() {
		return m_delegateID;
	}

	@Override
	public void sendConfigurationInformation(Properties configuration) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.fillProperties(configuration);
		// No response is expected from the client, so don't do any waiting, just send
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, SEND_CONFIGURATION_INFORMATION);
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId());
		getEventBus().publish(msg);
	}

	class BooleanReply implements IGuavaMessageListener {
		boolean m_reply = false;

		@Override
		@Subscribe public void receive(GuavaRainbowMessage msg) {
			m_reply = (Boolean) msg.getProperty(ESEBConstants.MSG_REPLY_VALUE);
		}
	}

	@Override
	public void heartbeat() {
		m_rainbowMaster.processHeartbeat(m_delegateID);
	}

	@Override
	public void requestConfigurationInformation() {
		m_rainbowMaster.requestDelegateConfiguration(m_delegateID);
	}

	@Override
	public boolean startDelegate() throws IllegalStateException {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, START_DELEGATE);
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId());

		try {
			BooleanReply reply = new BooleanReply();
			getEventBus().blockingSendAndReceive(msg, reply,
					Rainbow.instance().getProperty(IRainbowEnvironment.PROPKEY_PORT_TIMEOUT, 10000));
			return reply.m_reply;
		} catch (RainbowConnectionException e) {
			LOGGER.error(MessageFormat.format("startDelegate did not return for delegate {0}", getDelegateId()));
			return false;
		}
	}

	@Override
	public boolean pauseDelegate() throws IllegalStateException {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, PAUSE_DELEGATE);
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId());

		try {
			BooleanReply reply = new BooleanReply();
			getEventBus().blockingSendAndReceive(msg, reply,
					Rainbow.instance().getProperty(IRainbowEnvironment.PROPKEY_PORT_TIMEOUT, 10000));
			return reply.m_reply;
		} catch (RainbowConnectionException e) {
			LOGGER.error(MessageFormat.format("pauseDelegate did not return for delegate {0}", getDelegateId()));
			return false;
		}
	}

	@Override
	public boolean terminateDelegate() throws IllegalStateException {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, TERMINATE_DELEGATE);
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId());

		BooleanReply reply = new BooleanReply();
		getEventBus().sendAndReceive(msg, reply);
		return reply.m_reply;
	}

	@Override
	public void startProbes() throws IllegalStateException {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, START_PROBES);
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId());
		getEventBus().publish(msg);
	}

	@Override
	public void killProbes() throws IllegalStateException {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, KILL_PROBES);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        getEventBus().publish (msg);
	}

	public void report(ReportType type, RainbowComponentT compT, String msg) {
		GuavaRainbowMessage gMsg = new GuavaRainbowMessage();
		gMsg.setProperty(ESEBConstants.MSG_CHANNEL_KEY, ChannelT.UIREPORT.name());
		gMsg.setProperty(ESEBConstants.COMPONENT_TYPE_KEY, compT.name());
		gMsg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UI_REPORT);
		gMsg.setProperty(ESEBConstants.REPORT_TYPE_KEY, type.name());
		gMsg.setProperty(ESEBConstants.REPORT_MSG_KEY, msg);
		getReportEventBus().publish(gMsg);
	}

	

	protected void report(ReportType type, RainbowComponentT compType, String msg, Throwable t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		t.printStackTrace(ps);
		ps.close();
		report(type, compType,
				MessageFormat.format("{0}.\nException: {1}\n{2}", msg, t.getMessage(), baos.toString()));
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
