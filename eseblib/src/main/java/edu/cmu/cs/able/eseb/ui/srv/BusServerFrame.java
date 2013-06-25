package edu.cmu.cs.able.eseb.ui.srv;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import incubator.ctxaction.ActionContext;
import incubator.pval.Ensure;
import incubator.ui.MainApplicationFrame;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import edu.cmu.cs.able.eseb.BusServerRemoteInterface;

/**
 * Frame that shows information from a bus server.
 */
@SuppressWarnings("serial")
public class BusServerFrame extends JInternalFrame {
	/**
	 * The remote interface.
	 */
	private BusServerRemoteInterface m_ri;
	
	/**
	 * Creates a new frame.
	 * @param host the host name
	 * @param port the port number
	 * @param r the server remote interface
	 * @param maf the main application frame
	 */
	public BusServerFrame(String host, short port, BusServerRemoteInterface r,
			MainApplicationFrame maf) {
		super("Bus server @" + Ensure.not_null(host) + ":" + port,
				true, true, true, true);
		
		Ensure.not_null(host);
		Ensure.greater(port, 0);
		Ensure.not_null(r);
		Ensure.not_null(maf);
		
		m_ri = r;
		
		setup_ui();
		setup_actions(host, port, maf);
		pack();
		setVisible(true);
	}
	
	/**
	 * Sets up the user interface.
	 */
	private void setup_ui() {
		setLayout(new BorderLayout());
		JPanel port_panel = new JPanel();
		add(port_panel, BorderLayout.CENTER);
		
		port_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		port_panel.add(new JLabel("Client port: " + m_ri.port()));
	}
	
	/**
	 * Sets up actions.
	 * @param host the host we're connected to
	 * @param port the port we're connected to
	 * @param maf the main application frame
	 */
	private void setup_actions(String host, short port,
			MainApplicationFrame maf) {
		ShowBusEventsAction seba = new ShowBusEventsAction(host, port,
				m_ri, maf);
		seba.bind(new ActionContext());
		
		JToolBar toolbar = new JToolBar();
		add(toolbar, BorderLayout.NORTH);
		toolbar.add(seba.createJButton(false));
	}
}
