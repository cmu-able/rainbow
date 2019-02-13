package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;

public class RainbowMonitor extends JInternalFrame {
	
//	private ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
//	private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
//	private OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

//	public static class ThreadData {
//		Thread thread;
//		long uptime=0;
//		long initialCPU;
//		float cpuUsage = 0;
//		long lastUpdate = 0;
//	}
	
	public static class Entry {
		RainbowComponentT componentType;
		List<Thread> children;
//		List<ThreadData> children;
	}
	
//	protected void calculateThreadStats(NoRootTreeTableModel ttm) {
//		Map<Long,ThreadInfo> tis = new HashMap<>();
//		ThreadInfo[] ti = threadMxBean.dumpAllThreads(false, false);
//		for (int i = 0; i < ti.length; i++) {
//			tis.put(ti[i].getThreadId(), ti[i]);
//		}
//		
//		List<Entry> members = ttm.members;
//		for (Entry entry : members) {
//			for (ThreadData d : entry.children) {
//				ThreadInfo info = tis.get(d.thread.getId());
//				if (info != null) {
//					d.uptime = runtimeMXBean.getUptime();
//					//http://marjavamitjava.com/calculate-cpu-usage-java-thread-not-just-whole-process/
////					if (la)
//				}
//			}
//		}
//		
//	}

	private final static String[] COLUMNNAMES = { "Id", "Status", "Uptime", "% CPU" };
	public  class NoRootTreeTableModel extends AbstractTreeTableModel {
		private List<Entry> members;

		public NoRootTreeTableModel(Map<RainbowComponentT, Map<String, Thread>> threads) {
			super(new Object());
			members = new ArrayList<>(threads.size());
			for (Map.Entry<RainbowComponentT, Map<String, Thread>> e : threads.entrySet()) {
				Entry nt = new Entry();
				nt.componentType = e.getKey();
				nt.children = new ArrayList<>(e.getValue().size());
			
				Collection<Thread> values = e.getValue().values();
//				for (Iterator iterator = values.iterator(); iterator.hasNext();) {
//					Thread thread = (Thread) iterator.next();
//					ThreadData data = new ThreadData();
//					data.thread = thread;
//					nt.children.add(data);
//				}
				nt.children.addAll(values);
				members.add(nt);
			}
		}

		@Override
		public int getColumnCount() {
			return COLUMNNAMES.length;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMNNAMES[column];
		}

		@Override
		public boolean isCellEditable(Object node, int column) {
			return false;
		}

		@Override
		public boolean isLeaf(Object node) {
			return node instanceof Thread/*Data*/;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent instanceof Entry) {
				return ((Entry) parent).children.size();
			}
			return members.size();
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof Entry) {
				return ((Entry) parent).children.get(index);
			}
			return members.get(index);
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			Entry nt = (Entry) parent;
			Thread/*Data*/ t = (Thread/*Data*/) child;
			return nt.children.indexOf(t);
		}

		@Override
		public Object getValueAt(Object node, int column) {
			if (node instanceof Entry) {
				Entry nt = (Entry) node;
				switch (column) {
				case 0:
					return nt.componentType.name();
				}
			} else if (node instanceof Thread/*Data*/) {
				Thread/*Data*/ t = (Thread/*Data*/) node;
				switch (column) {
				case 0:
					return t/*.thread*/.getName();
				case 1:
					if (t/*.thread*/.getState() == State.TERMINATED) {
						StringBuffer ret = new StringBuffer("Failed: ");
						if (RainbowMonitor.this.m_uncaughtExceptions.containsKey(t)) {
							ret.append (m_uncaughtExceptions.get(t).getMessage());
						}
						else ret.append("Unknown failure");
						return ret;
					}
					return t/*.thread*/.getState() == State.TERMINATED ? "Failed" : "OK";
				case 2: 
					return "???";// t.uptime==0?"???":Long.toString(t.uptime);
				case 3:
					return "???";//t.cpuUsage==0?"???":String.format("$.2f",t.cpuUsage);
				}
			}
			return null;
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RainbowMonitor frame = new RainbowMonitor();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private JXTreeTable m_treeTable;
	private boolean m_refreshEnabled;
	private UncaughtExceptionHandler m_exceptionHandler;
	private Map<Thread, Throwable> m_uncaughtExceptions = new HashMap<>();

	/**
	 * Create the frame.
	 */
	public RainbowMonitor() {
		setResizable(true);
		setIconifiable(true);
		setMaximizable(true);
		setClosable(true);
		setSize(350, 390);
		m_exceptionHandler = new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				synchronized (m_uncaughtExceptions) {
					m_uncaughtExceptions.put(t, e);
				}
			}

		};

		Map<RainbowComponentT, Map<String, Thread>> registeredThreads = Rainbow.instance().getRegisteredThreads();
		for (Map.Entry<RainbowComponentT, Map<String, Thread>> e : registeredThreads.entrySet()) {
			for (Map.Entry<String, Thread> e2 : e.getValue().entrySet()) {
				e2.getValue().setUncaughtExceptionHandler(m_exceptionHandler);
			}
		}
		NoRootTreeTableModel ttm = new NoRootTreeTableModel(registeredThreads);
		getContentPane().setLayout(new BorderLayout(0, 0));
		m_treeTable = new JXTreeTable(ttm);
//		m_treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		m_treeTable.setRootVisible(false);
		getContentPane().add(new JScrollPane(m_treeTable));
		setTitle("Rainbow Component Status");
		pack();
		m_refreshEnabled = true;

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (RainbowMonitor.this.m_refreshEnabled) {
					Map<RainbowComponentT, Map<String, Thread>> registeredThreads = Rainbow.instance().getRegisteredThreads();
					
					
					NoRootTreeTableModel ttm = new NoRootTreeTableModel(registeredThreads);
//					calculateThreadStats(ttm);
					m_treeTable.setTreeTableModel(ttm);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
	}



	@Override
	public void dispose() {
		m_refreshEnabled = false;
		super.dispose();
	}

}
