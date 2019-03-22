package org.sa.rainbow.gui.arch.elements;

import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.sa.rainbow.gui.RainbowWindoe.ProbeInfo;
import org.jdesktop.beansbinding.AutoBinding;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

public class ProbeTabbedPane extends JTabbedPane {
	private AutoBinding<ProbeAttributes, ProbeAttributes, ProbeDetailPanel, ProbeAttributes> description;
	private JTextArea m_reportText;
	
	private ProbeInfo m_probeInfo;
	private ProbeDetailPanel m_probeDetailPanel;
	
	public ProbeInfo getProbeInfo() {
		return m_probeInfo;
	}

	public void setProbeInfo(ProbeInfo probeInfo) {
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
		addTab("Spec", null, m_probeDetailPanel, null);
	}

	protected void initDataBindings() {
		BeanProperty<ProbeDetailPanel, ProbeAttributes> probeDetailPanelBeanProperty = BeanProperty.create("probeAttributes");
		description = Bindings.createAutoBinding(UpdateStrategy.READ, m_probeInfo.description, m_probeDetailPanel, probeDetailPanelBeanProperty);
		description.bind();
	}
}
