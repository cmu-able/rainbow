package org.sa.rainbow.gui.arch;

import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;

import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public class GaugeInfo {
	private JInternalFrame frame;
	private GaugeInstanceDescription description;
	private Map<String, List<IRainbowOperation>> operations;
	private List<String> probes = null;
	public List<String> getProbes() {
		return probes;
	}
	public void setProbes(List<String> probes) {
		this.probes = probes;
	}
	public JInternalFrame getFrame() {
		return frame;
	}
	public void setFrame(JInternalFrame frame) {
		this.frame = frame;
	}
	public GaugeInstanceDescription getDescription() {
		return description;
	}
	public void setDescription(GaugeInstanceDescription description) {
		this.description = description;
	}
	public Map<String, List<IRainbowOperation>> getOperations() {
		return operations;
	}
	public void setOperations(Map<String, List<IRainbowOperation>> operations) {
		this.operations = operations;
	}
}