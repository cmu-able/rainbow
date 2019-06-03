package org.sa.rainbow.gui.arch.elements;

import javax.swing.JTabbedPane;

import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;

public class DefaultThreadInfoPane extends JTabbedPane {

	private ThreadInformationPane m_ti;

	public DefaultThreadInfoPane() {
		m_ti = new ThreadInformationPane();
		addTab("Details", m_ti);
		setTabPlacement(JTabbedPane.BOTTOM);
	}
	
	public void initBindings(RainbowArchModelElement el) {
		m_ti.initBindings(el);
	}
	
}
