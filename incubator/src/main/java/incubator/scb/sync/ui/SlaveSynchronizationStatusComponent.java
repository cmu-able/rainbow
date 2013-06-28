package incubator.scb.sync.ui;

import incubator.pval.Ensure;
import incubator.scb.sync.SyncScbSlave;
import incubator.ui.IconResourceLoader;
import incubator.wt.WorkerThread;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * User interface component that shows the status of slave synchronization.
 */
public class SlaveSynchronizationStatusComponent extends JPanel {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Refresh interval in milliseconds.
	 */
	private static final long REFRESH_INTERVAL_MS = 250;
	
	/**
	 * Name of icon for waiting state.
	 */
	private static final String WAITING_ICON = "sync-waiting.png";
	
	/**
	 * Name of the prefix for the synchronization icon.
	 */
	private static final String SYNC_ICON_PREFIX = "sync-progress-";
	
	/**
	 * Name of the suffix for the synchronization icon.
	 */
	private static final String SYNC_ICON_SUFFIX = ".png";
	
	/**
	 * The slave.
	 */
	private SyncScbSlave m_slave;
	
	/**
	 * The worker thread.
	 */
	private WorkerThread m_worker;
	
	/**
	 * Synchronizing icons.
	 */
	private List<ImageIcon> m_sync_icons;
	
	/**
	 * Waiting icon (all is OK).
	 */
	private ImageIcon m_waiting_icon;
	
	/**
	 * Label with icon.
	 */
	private JLabel m_icon_label;
	
	/**
	 * Label with text.
	 */
	private JLabel m_text_label;
	
	/**
	 * Current sync icon shown, <code>-1</code> if showing the waiting icon.
	 */
	private int m_sync_idx;
	
	/**
	 * Creates a new component.
	 * @param slave the SCB synchronization slave we show information from
	 */
	public SlaveSynchronizationStatusComponent(SyncScbSlave slave) {
		Ensure.not_null(slave);
		
		m_slave = slave;
		m_worker = new WorkerThread("Sync status updater") {
			@Override
			protected void do_cycle_operation() throws Exception {
				update_status();
				synchronized (this) {
					wait(REFRESH_INTERVAL_MS);
				}
			}
		};
		
		m_waiting_icon = IconResourceLoader.loadIcon(
				SlaveSynchronizationStatusComponent.class, WAITING_ICON);
		Ensure.not_null(m_waiting_icon);
		
		m_sync_icons = new ArrayList<>();
		for (int i = 0; ; i++) {
			ImageIcon arrow = IconResourceLoader.loadIcon(
					SlaveSynchronizationStatusComponent.class,
					SYNC_ICON_PREFIX + i + SYNC_ICON_SUFFIX);
			if (arrow == null) {
				Ensure.greater(i, 0);
				break;
			}
			
			m_sync_icons.add(arrow);
		}
		
		m_sync_idx = -1;
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		m_icon_label = new JLabel();
		add(m_icon_label);
		m_text_label = new JLabel();
		add(m_text_label);
		
		update_status();
		
		m_worker.start();
	}
	
	/**
	 * Invoked from the worker thread to check slave status and update
	 * the component's UI accordingly.
	 */
	private void update_status() {
		final Icon icon;
		final String text;
		switch (m_slave.state()) {
		case SHUTDOWN:
		case WAITING:
			icon = m_waiting_icon;
			m_sync_idx = -1;
			
			Date lsd = m_slave.last_sync_date();
			if (lsd == null) {
				text = "Never synchronized";
			} else {
				text = "Synchronized at " + DateFormat.getDateTimeInstance(
						DateFormat.SHORT, DateFormat.MEDIUM).format(lsd);
			}
			
			break;
		case SYNCHRONIZING:
			if (m_sync_idx == -1 || m_sync_idx == m_sync_icons.size() - 1) {
				m_sync_idx = 0;
			} else {
				m_sync_idx++;
			}
			
			icon = m_sync_icons.get(m_sync_idx);
			text = "Synchronizing...";
			break;
		default:
			Ensure.unreachable();
			icon = null;
			text = null;
		}
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_icon_label.setIcon(icon);
				m_text_label.setText(text);
			}
		});
	}

	/**
	 * Shuts down the component.
	 */
	public synchronized void shutdown() {
		Ensure.not_null(m_worker);
		
		m_worker.stop();
		m_worker = null;
	}
}
