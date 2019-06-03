package org.sa.rainbow.gui.widgets;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

public abstract class ChartPanelContainer extends JPanel {

	protected ChartPanel m_chartPanel;

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		m_chartPanel.addMouseListener(l);
	}

	@Override
	public synchronized void addMouseMotionListener(MouseMotionListener l) {
		super.addMouseMotionListener(l);
		m_chartPanel.addMouseMotionListener(l);
	}

	@Override
	public synchronized void removeMouseListener(MouseListener l) {
		m_chartPanel.removeMouseListener(l);
		super.removeMouseListener(l);
	}

	@Override
	public synchronized void removeMouseMotionListener(MouseMotionListener l) {
		m_chartPanel.removeMouseMotionListener(l);
		super.removeMouseMotionListener(l);
	}

	
	
}
