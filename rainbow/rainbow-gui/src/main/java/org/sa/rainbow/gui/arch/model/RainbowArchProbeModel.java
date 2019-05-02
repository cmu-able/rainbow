package org.sa.rainbow.gui.arch.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.gui.arch.controller.AbstractRainbowController;
import org.sa.rainbow.util.Util;

public class RainbowArchProbeModel extends RainbowArchModelElement {

	public static final String PROBE_REPORT_PROPERTY = "report";
	private ProbeAttributes m_probeDesc;
	private List<String> m_reports = new LinkedList<>();
	private Set<String> m_listeningGauges = new HashSet<>();

	public RainbowArchProbeModel(ProbeAttributes probeDesc) {
		super();
		setProbeDesc(probeDesc);
	}

	@Override
	public AbstractRainbowController getController() {
		return (AbstractRainbowController) super.getController();
	}

	@Override
	public String getId() {
		return Util.genID(m_probeDesc.alias, m_probeDesc.location);
	}

	public ProbeAttributes getProbeDesc() {
		return m_probeDesc;
	}

	protected void setProbeDesc(ProbeAttributes probeDesc) {
		m_probeDesc = probeDesc;
	}

	public List<String> getReports() {
		synchronized (m_reports) {
			return m_reports;
		}
	}

	public void addReport(String report) {
		synchronized (m_reports) {
			m_reports.add(report);
		}
		pcs.firePropertyChange(PROBE_REPORT_PROPERTY, null, report);
		
	}

	public void addListeningGauge(String gaugeKey) {
		m_listeningGauges.add(gaugeKey);
	}

	@Override
	public AbstractRainbowRunnable getRunnable() {
		return null;
	}

}
