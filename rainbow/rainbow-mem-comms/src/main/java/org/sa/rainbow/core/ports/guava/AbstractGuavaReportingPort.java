package org.sa.rainbow.core.ports.guava;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;

public abstract class AbstractGuavaReportingPort implements IRainbowReportingPort {

	private GuavaEventConnector m_eventBus;
	private GuavaEventConnector m_uiEventBus;

	public AbstractGuavaReportingPort(ChannelT channel) {
		m_eventBus = new GuavaEventConnector(channel);
		m_uiEventBus = new GuavaEventConnector(ChannelT.UIREPORT);
	}

	public GuavaEventConnector getEventBus() {
		return m_eventBus;
	}

	protected GuavaEventConnector getReportEventBus() {
		return m_uiEventBus;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	protected abstract void report(ReportType type, RainbowComponentT compType, String msg, Throwable t);
	protected abstract void report(ReportType type, RainbowComponentT compType, String msg);

	@Override
	public void info(RainbowComponentT type, String msg) {
		report(ReportType.INFO, type, msg);
	}

	@Override
	public void info(RainbowComponentT type, String msg, Logger logger) {
		logger.info(msg);
		report(ReportType.INFO, type, msg);
	}

	@Override
	public void warn(RainbowComponentT type, String msg) {
		report(ReportType.WARNING, type, msg);
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Throwable e) {
		report(ReportType.WARNING, type, msg, e);
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Logger logger) {
		logger.warn(msg);
		report(ReportType.WARNING, type, msg);
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		logger.warn(msg, e);
		report(ReportType.WARNING, type, msg, e);
	}

	@Override
	public void error(RainbowComponentT type, String msg) {
		report(ReportType.ERROR, type, msg);
	}

	@Override
	public void error(RainbowComponentT type, String msg, Throwable e) {
		report(ReportType.ERROR, type, msg, e);
	}

	@Override
	public void error(RainbowComponentT type, String msg, Logger logger) {
		logger.error(msg);
		report(ReportType.ERROR, type, msg);
	}

	@Override
	public void error(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		logger.error(msg, e);
		report(ReportType.ERROR, type, msg, e);
	}

	@Override
	public void fatal(RainbowComponentT type, String msg) {
		report(ReportType.FATAL, type, msg);
	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Throwable e) {
		report(ReportType.FATAL, type, msg, e);
	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Logger logger) {
		logger.fatal(msg);
		report(ReportType.FATAL, type, msg);
	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		logger.fatal(msg, e);
		report(ReportType.FATAL, type, msg, e);
	}
	
	@Override
	public void trace(RainbowComponentT type, String msg) {
		getLogger ().trace(msg);
	}

	protected abstract Logger getLogger();

}
