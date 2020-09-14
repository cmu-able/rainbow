package org.sa.rainbow.core.ports.guava;

import java.util.Collection;
import java.util.List;

import org.sa.rainbow.core.ports.IGaugeConfigurationPort;
import org.sa.rainbow.core.ports.IGaugeQueryPort;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public class GuavaGaugePort implements IGaugeConfigurationPort, IGaugeQueryPort {

	IGauge m_gauge;
	boolean disposed = false;

	public GuavaGaugePort() {
	}

	void setGauge(IGauge g) {
		m_gauge = g;
	}

	@Override
	public void dispose() {
		if (!disposed) {
			disposed = true;
			if (m_gauge == null)
				throw new NullPointerException("The gauge for this connector is null");
			m_gauge.dispose();
		}
	}

	@Override
	public boolean configureGauge(List<TypedAttributeWithValue> configParams) {
		if (m_gauge == null)
			throw new NullPointerException("The gauge for this connector is null");

		return m_gauge.configureGauge(configParams);
	}

	@Override
	public boolean reconfigureGauge() {
		if (m_gauge == null)
			throw new NullPointerException("The gauge for this connector is null");

		return m_gauge.reconfigureGauge();
	}

	@Override
	public IGaugeState queryGaugeState() {
		if (m_gauge == null)
			throw new NullPointerException("The gauge for this connector is null");

		return m_gauge.queryGaugeState();
	}

	@Override
	public IRainbowOperation queryCommand(String commandName) {
		if (m_gauge == null)
			throw new NullPointerException("The gauge for this connector is null");

		return m_gauge.queryCommand(commandName);
	}

	@Override
	public Collection<IRainbowOperation> queryAllCommands() {
		if (m_gauge == null)
			throw new NullPointerException("The gauge for this connector is null");

		return m_gauge.queryAllCommands();
	}

}
