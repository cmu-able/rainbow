package org.sa.rainbow.core.ports.guava;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;

import com.google.common.eventbus.Subscribe;

public class GuavaMasterConnectionPort extends AbstractGuavaReportingPort implements IMasterConnectionPort {

	private static final Logger LOGGER = Logger.getLogger(GuavaMasterConnectionPort.class);
	protected RainbowMaster m_master;
	
	public GuavaMasterConnectionPort(RainbowMaster master) {
		super(ChannelT.HEALTH);
		m_master = master;
		
		getEventBus().addListener(new IGuavaMessageListener() {
			
			@Override
			@Subscribe
			public void receive(GuavaRainbowMessage msg) {
				String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                switch (type) {
                case ESEBConstants.MSG_TYPE_CONNECT_DELEGATE: {
//                    if (msg.hasProperty (ESEBConstants.TARGET)) {
                    String delegateId = (String )msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY);
                    Properties connectionProperties = msg.pulloutProperties ();
                    String replyMsg = ESEBConstants.MSG_REPLY_OK;
                    try {
                        IDelegateManagementPort port = connectDelegate (delegateId, connectionProperties);
                        if (port == null) {
                            replyMsg = "Could not create a deployment port on the master.";
                        }
                    }
                    catch (Throwable t) {
                        replyMsg = MessageFormat.format ("Failed to connect with the following exception: {0}",
                                t.getMessage ());
                    }
                    GuavaRainbowMessage reply = new GuavaRainbowMessage();
                    reply.setProperty (ESEBConstants.MSG_REPLY_KEY,
                            (String )msg.getProperty (ESEBConstants.MSG_REPLY_KEY));
                    reply.setProperty (ESEBConstants.MSG_CONNECT_REPLY, replyMsg);
                    reply.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_REPLY);
                    getEventBus().publish (reply);
//                    }
                }
                break;
                case ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE: {
                    String delegateId = (String )msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY);
                    m_master.disconnectDelegate (delegateId);
                }
                }
			}
		});
		
		getReportEventBus().addListener(new IGuavaMessageListener() {
			
			@Override
			public void receive(GuavaRainbowMessage msg) {
				String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                switch (type) {
                case ESEBConstants.MSG_TYPE_UI_REPORT: {
                    try {
                        String delegateId = (String )msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY);
                        String message = (String )msg.getProperty (ESEBConstants.REPORT_MSG_KEY);
                        RainbowComponentT compT = RainbowComponentT.valueOf ((String )msg
                                .getProperty (ESEBConstants.COMPONENT_TYPE_KEY));
                        ReportType reportType = ReportType.valueOf ((String )msg.getProperty (ESEBConstants.REPORT_TYPE_KEY));
                        m_master.report (delegateId, reportType, compT, message);
                    }
                    catch (Exception e) {
                        LOGGER.error ("Failed to process message: " + msg.toString ());

                    }
                }
                break;
                }
            }
		});
		
	}
	
	@Override
	public IDelegateManagementPort connectDelegate(String delegateID, Properties connectionProperties) {
		return m_master.connectDelegate(delegateID, connectionProperties);
	}

	@Override
	public void disconnectDelegate(String delegateId) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, delegateId);
        msg.setProperty (ESEBConstants.TARGET, delegateId);
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
		report(type, compType,
				MessageFormat.format("{0}.\nException: {1}\n{2}", msg, t.getMessage(), baos.toString()));
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	protected void report(ReportType type, RainbowComponentT compType, String msg) {
		GuavaRainbowMessage gMsg = new GuavaRainbowMessage();
		gMsg.setProperty(ESEBConstants.MSG_CHANNEL_KEY, ChannelT.UIREPORT.name());
		gMsg.setProperty(ESEBConstants.COMPONENT_TYPE_KEY, compType.name());
		gMsg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UI_REPORT);
		gMsg.setProperty(ESEBConstants.REPORT_TYPE_KEY, type.name());
		gMsg.setProperty(ESEBConstants.REPORT_MSG_KEY, msg);
		getReportEventBus().publish(gMsg);
	}

}
