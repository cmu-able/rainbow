package org.sa.rainbow.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.gui.widgets.TimeSeriesPanel.ICommandProcessor;

public class BooleanPanel extends JPanel implements ICommandUpdate {

	private Color m_onColor;
	private Color m_offColor;
	private ICommandProcessor<Boolean> m_processor;
	private Boolean value;

	public BooleanPanel(Color onColor, Color offColor, ICommandProcessor<Boolean> processor) {
		m_onColor = onColor;
		m_offColor = offColor;
		m_processor = processor;
		setSize(25,25);
	}

	@Override
	public void newCommand(IRainbowOperation cmd) {
		value = m_processor.process(cmd);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Dimension size = getSize();
		if (value != null) {
			Color c = value?m_onColor:m_offColor;
			g2.setColor(c);
			g2.fillOval(0,0, size.width, size.height);
		}
		g2.setColor(Color.BLACK);
		g2.drawOval(0, 0, size.width, size.height);
		
	}

}
