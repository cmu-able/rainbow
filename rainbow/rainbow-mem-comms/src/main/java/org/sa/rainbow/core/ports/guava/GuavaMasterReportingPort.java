package org.sa.rainbow.core.ports.guava;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.util.Util;

public class GuavaMasterReportingPort extends AbstractGuavaReportingPort implements IRainbowReportingPort {
	protected static final Logger LOGGER = Logger.getLogger(GuavaMasterReportingPort.class);

	public GuavaMasterReportingPort() {
		super(ChannelT.UIREPORT);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void report(ReportType type, RainbowComponentT compType, String msg, Throwable t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		t.printStackTrace(ps);
		ps.close();
		report(type, compType, MessageFormat.format("{0}. Exception: {1}.", msg, t.getMessage()), baos.toString());
	}

	private void report(ReportType type, RainbowComponentT compT, String msg, String additionalInfo) {
		LOGGER.log(Util.reportTypeToPriority(type), compT.name() + ": " + msg);
		LOGGER.info(additionalInfo);
		if (getReportEventBus() == null)
			return;
		GuavaRainbowMessage esebMsg = new GuavaRainbowMessage();
		esebMsg.setProperty(ESEBConstants.COMPONENT_TYPE_KEY, compT.name());
		esebMsg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UI_REPORT);
		esebMsg.setProperty(ESEBConstants.REPORT_TYPE_KEY, type.name());
		esebMsg.setProperty(ESEBConstants.REPORT_MSG_KEY, msg);
		esebMsg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, "master");
		if (additionalInfo != null) {
			esebMsg.setProperty(ESEBConstants.REPORT_MSG_ADDITIONAL_INFO, additionalInfo);
		}
		getReportEventBus().publish(esebMsg);

	}

	@Override
	protected void report(ReportType type, RainbowComponentT compType, String msg) {
		report(type, compType, msg, (String )null);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
