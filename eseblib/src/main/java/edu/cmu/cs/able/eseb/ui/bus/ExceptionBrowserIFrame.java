package edu.cmu.cs.able.eseb.ui.bus;

import incubator.exh.ExhGlobalUiSynchronizer;
import incubator.exh.ExhUi;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

/**
 * Frame that shows the exception browser.
 */
@SuppressWarnings("serial")
public class ExceptionBrowserIFrame extends JInternalFrame {
	/**
	 * Creates a new frame.
	 */
	public ExceptionBrowserIFrame() {
		super("Exception Browser", true, true, true, true);
		
		setLayout(new BorderLayout());
		ExhUi ui = new ExhUi();
		@SuppressWarnings("unused")
		ExhGlobalUiSynchronizer gsync = new ExhGlobalUiSynchronizer(ui);
		add(ui, BorderLayout.CENTER);
		setVisible(true);
		pack();
	}
}
