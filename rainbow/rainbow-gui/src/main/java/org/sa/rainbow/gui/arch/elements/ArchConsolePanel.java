package org.sa.rainbow.gui.arch.elements;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.RainbowWindow;


public class ArchConsolePanel extends JPanel implements IUIUpdater, IUIReporter {
	private JTextArea m_textArea;
	private List<Runnable> m_updaters = new LinkedList<>();
	public ArchConsolePanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);
		m_textArea = new JTextArea();
		scrollPane.setViewportView(m_textArea);
		
		m_textArea.setFont(m_textArea.getFont().deriveFont(RainbowWindow.TEXT_FONT_SIZE));
		m_textArea.setEditable(false);
		m_textArea.setLineWrap(true);
		m_textArea.setWrapStyleWord(true);
		m_textArea.setAutoscrolls(false);
		scrollPane.setAutoscrolls(true);
		
		
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(m_textArea.getFont());
		m_textArea.setPreferredSize(new Dimension(fm.charWidth('A')*70, fm.getHeight()*10));
	}
	
	@Override
	public void processReport(ReportType type, String message) {
		for (Runnable r : m_updaters) 
			r.run();
		m_textArea.append("\n" + message);
		m_textArea.setCaretPosition(m_textArea.getText().length());
	}

	@Override
	public void addUpdateListener(Runnable listener) {
		m_updaters .add(listener);
	}

}
