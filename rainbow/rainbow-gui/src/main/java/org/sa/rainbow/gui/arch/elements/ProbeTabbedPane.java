package org.sa.rainbow.gui.arch.elements;

import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.sa.rainbow.gui.arch.model.RainbowArchProbeModel;
import org.jdesktop.beansbinding.AutoBinding;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

public class ProbeTabbedPane extends JTabbedPane {
	private AutoBinding<ProbeAttributes, ProbeAttributes, ProbeDetailPanel, ProbeAttributes> description;
	private JTextArea m_reportText;
	
	private RainbowArchProbeModel m_probeInfo;
	private ProbeDetailPanel m_probeDetailPanel;
	
	public RainbowArchProbeModel getProbeInfo() {
		return m_probeInfo;
	}

	public void setProbeInfo(RainbowArchProbeModel probeInfo) {
		m_probeInfo = probeInfo;
		if (description != null)
			description.unbind();
		if (probeInfo != null) {
			initDataBindings();
		}
	}

	public ProbeTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);
		
		JScrollPane scrollPane = new JScrollPane();
		addTab("Reports", null, scrollPane, null);
		
		m_reportText = new JTextArea();
		scrollPane.setViewportView(m_reportText);
		
		m_probeDetailPanel = new ProbeDetailPanel();
		addTab("Specification", null, m_probeDetailPanel, null);
	}

	protected void initDataBindings() {
		BeanProperty<ProbeDetailPanel, ProbeAttributes> probeDetailPanelBeanProperty = BeanProperty.create("probeAttributes");
		description = Bindings.createAutoBinding(UpdateStrategy.READ, m_probeInfo.getProbeDesc(), m_probeDetailPanel, probeDetailPanelBeanProperty);
		description.bind();
		
		updateReports(m_probeInfo.getProbeDesc().alias);
	}

	public void updateReports(String alias) {
		if (m_probeInfo == null) return;
		if (!m_probeInfo.getProbeDesc().alias.equals(alias)) return;
		m_reportText.setText("");
		int lower = Math.min(100, m_probeInfo.getReports().size());
		for (int i = 0; i < lower; i++) {
			m_reportText.append(m_probeInfo.getReports().get(i));
			m_reportText.append("\n");
		}
	}
	
	public void addToReports(String alias) {
		if (m_probeInfo == null) return;
		if (!m_probeInfo.getProbeDesc().alias.equals(alias)) return;
		m_reportText.setText(m_probeInfo.getReports().get(m_probeInfo.getReports().size()-1) + "\n" + m_reportText.getText());
	}
	
}
