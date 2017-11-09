package org.sa.rainbow.stitch.test;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

public class NullTestReportingPort implements IRainbowReportingPort {
	@Override
	public void dispose() {
		
	}

	@Override
	public void warn(RainbowComponentT type, String msg) {
		
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Throwable e) {
		
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Logger logger) {
		
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		
	}

	@Override
	public void trace(RainbowComponentT type, String msg) {
		
	}

	@Override
	public void info(RainbowComponentT type, String msg) {
		
	}

	@Override
	public void info(RainbowComponentT type, String msg, Logger logger) {
		
	}

	@Override
	public void fatal(RainbowComponentT type, String msg) {
		
	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Throwable e) {
		
	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Logger logger) {
		
	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		
	}

	@Override
	public void error(RainbowComponentT type, String msg) {
		
	}

	@Override
	public void error(RainbowComponentT type, String msg, Throwable e) {
		
	}

	@Override
	public void error(RainbowComponentT type, String msg, Logger logger) {
		
	}

	@Override
	public void error(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		
	}
}