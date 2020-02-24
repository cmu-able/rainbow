package org.sa.rainbow.core;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.globals.Environment;
import org.sa.rainbow.core.globals.ExitState;

import com.google.inject.Inject;

public class RainbowEnvironmentDelegate implements IRainbowEnvironment {

	@Inject
	static IRainbowEnvironment m_env;

	public String getProperty(String key, String defaultProperty) {
		synchronized (m_env) {
			return m_env.getProperty(key, defaultProperty);
		}
	}

	public String getProperty(String key) {
		synchronized (m_env) {

			return m_env.getProperty(key);
		}
	}

	public boolean getProperty(String key, boolean b) {
		synchronized (m_env) {

			return m_env.getProperty(key, b);
		}
	}

	public long getProperty(String key, long default_) {
		synchronized (m_env) {

			return m_env.getProperty(key, default_);
		}
	}

	public short getProperty(String key, short default_) {
		synchronized (m_env) {
			return m_env.getProperty(key, default_);
		}
	}

	public int getProperty(String key, int default_) {
		synchronized (m_env) {
			return m_env.getProperty(key, default_);
		}
	}

	public double getProperty(String key, double default_) {

		synchronized (m_env) {
			return m_env.getProperty(key, default_);
		}
	}

	public void setProperty(String key, short val) {
		synchronized (m_env) {
			m_env.setProperty(key, val);
		}
	}

	public void setProperty(String key, long val) {
		synchronized (m_env) {
			m_env.setProperty(key, val);
		}
	}

	public boolean shouldTerminate() {
		synchronized (m_env) {
			return m_env.shouldTerminate();
		}
	}

	public void signalTerminate() {
		synchronized (m_env) {
			m_env.signalTerminate();
		}
	}

	public void setProperty(String key, boolean val) {
		synchronized (m_env) {
			m_env.setProperty(key, val);
		}
	}

	public void signalTerminate(ExitState exitState) {
		synchronized (m_env) {
			m_env.signalTerminate(exitState);
		}
	}

	public void setProperty(String key, String val) {
		synchronized (m_env) {
			m_env.setProperty(key, val);
		}
	}

	public int exitValue() {
		synchronized (m_env) {
			return m_env.exitValue();
		}
	}

	public void setExitState(ExitState state) {
		synchronized (m_env) {
			m_env.setExitState(state);
		}
	}

	public void setProperty(String key, double val) {
		synchronized (m_env) {
			m_env.setProperty(key, val);
		}
	}

	public boolean isMaster() {
		synchronized (m_env) {
			return m_env.isMaster();
		}
	}

	public void setProperty(String key, int val) {
		synchronized (m_env) {
			m_env.setProperty(key, val);
		}
	}

	public ThreadGroup getThreadGroup() {
		synchronized (m_env) {
			return m_env.getThreadGroup();
		}
	}

	public Properties allProperties() {
		synchronized (m_env) {
			return m_env.allProperties();
		}
	}

	public File getTargetPath() {
		synchronized (m_env) {
			return m_env.getTargetPath();
		}
	}

	public void setMaster(IRainbowMaster rainbowMaster) {
		synchronized (m_env) {
			m_env.setMaster(rainbowMaster);
		}
	}

	public IRainbowMaster getRainbowMaster() {
		synchronized (m_env) {
			return m_env.getRainbowMaster();
		}
	}

	public void registerGauge(IGauge gauge) {
		synchronized (m_env) {
			m_env.registerGauge(gauge);
		}
	}

	public IGauge lookupGauge(String id) {
		synchronized (m_env) {
			return m_env.lookupGauge(id);
		}
	}

	public Environment environment() {
		synchronized (m_env) {
			return m_env.environment();
		}
	}

	public void registerRainbowThread(Thread thread, RainbowComponentT componentType) {
		synchronized (m_env) {
			m_env.registerRainbowThread(thread, componentType);
		}
	}

	public Map<RainbowComponentT, Map<String, Thread>> getRegisteredThreads() {
		synchronized (m_env) {
			return m_env.getRegisteredThreads();
		}
	}

}
