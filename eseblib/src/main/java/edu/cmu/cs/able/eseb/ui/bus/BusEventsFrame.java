package edu.cmu.cs.able.eseb.ui.bus;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;
import incubator.pval.Ensure;
import incubator.ui.DataRefresher;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.apache.commons.lang.RandomStringUtils;

import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterface;
import edu.cmu.cs.able.eseb.bus.rci.LimitedDistributionQueue;
import edu.cmu.cs.able.eseb.bus.rci.LimitedDistributionQueueElement;
import edu.cmu.cs.able.eseb.ui.EventListViewComponent;
import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Frame that shows the events of an event bus.
 */
@SuppressWarnings("serial")
public class BusEventsFrame extends JInternalFrame {
	/**
	 * Refresh rate interval (milliseconds).
	 */
	private static final long REFRESH_RATE_MS = 1000;
	
	/**
	 * Remote control interface.
	 */
	private EventBusRemoteControlInterface m_remote;
	
	/**
	 * Component used to view the events in the bus.
	 */
	private EventListViewComponent m_view_component;
	
	/**
	 * The primitive scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * Data value encoding.
	 */
	private DataValueEncoding m_encoding;
	
	/**
	 * Key to use for the event queue in the server.
	 */
	private String m_queue_key;
	
	/**
	 * Total number of events lost.
	 */
	private int m_total_lost;
	
	/**
	 * Text field with total events lost.
	 */
	private JTextField m_total_lost_text;
	
	/**
	 * Creates a new frame.
	 * @param host the host
	 * @param port the port used to connect to the event bus
	 * @param remote the remote event bus control
	 */
	public BusEventsFrame(String host, short port,
			EventBusRemoteControlInterface remote) {
		super("Events @" + Ensure.not_null(host) + ":" + port, true, true,
				true, true);
		Ensure.not_null(remote);
		
		m_remote = remote;
		m_pscope = new PrimitiveScope();
		m_encoding = new DefaultTextEncoding(m_pscope);
		m_queue_key = RandomStringUtils.randomAlphanumeric(10);
		m_total_lost = 0;
		
		setup_ui();
		
		pack();
		setVisible(true);
	}
	
	/**
	 * Sets up the user interface.
	 */
	private void setup_ui() {
		setLayout(new BorderLayout());
		m_view_component = new EventListViewComponent();
		add(m_view_component, BorderLayout.CENTER);
		
		ActionContext refresher_context = new ActionContext();
		
		@SuppressWarnings("unused")
		DataRefresher refresher = new DataRefresher(REFRESH_RATE_MS, true,
				refresher_context) {
			@Override
			public void refresh() {
				refresh_ui();
			}
		};
		
		JToolBar tb = new JToolBar();
		add(tb, BorderLayout.NORTH);
		
		ContextualAction pause = new DataRefresher.PauseRefreshAction();
		pause.bind(refresher_context);
		tb.add(pause);
		ContextualAction resume = new DataRefresher.ResumeRefreshAction();
		resume.bind(refresher_context);
		tb.add(resume);
		tb.addSeparator();
		tb.add(new JLabel("Lost events: "));
		m_total_lost_text = new JTextField(5);
		m_total_lost_text.setEditable(false);
		tb.add(m_total_lost_text);
	}
	
	/**
	 * Refreshes the events reading new events from the server.
	 */
	private void refresh_ui() {
		LimitedDistributionQueue q = m_remote.distribution_queue(m_queue_key);
		for (LimitedDistributionQueueElement ldqe : q.all()) {
			DataValue v = null;
			try {
				v = m_encoding.decode(new DataInputStream(
						new ByteArrayInputStream(ldqe.contents())), m_pscope);
			} catch (IOException | InvalidEncodingException e) {
				v = m_pscope.string().make(e.getClass().getCanonicalName()
						+ ": " + e.toString());
			}
			
			m_view_component.add(ldqe.date(), v);
		}
		
		m_total_lost += q.lost();
		m_total_lost_text.setText("" + m_total_lost);
	}
}
