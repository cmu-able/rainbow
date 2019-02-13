package org.sa.rainbow.gui;

import java.awt.EventQueue;
import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import java.awt.BorderLayout;

public class RainbowMonitor extends JInternalFrame {

	public static class Entry {
		RainbowComponentT componentType;
		List<Thread> children;
	}

	private final static String[] COLUMNNAMES = { "Id", "Status" };
	public  class NoRootTreeTableModel extends AbstractTreeTableModel {
		private List<Entry> members;

		public NoRootTreeTableModel(Map<RainbowComponentT, Map<String, Thread>> threads) {
			super(new Object());
			members = new ArrayList<>(threads.size());
			for (Map.Entry<RainbowComponentT, Map<String, Thread>> e : threads.entrySet()) {
				Entry nt = new Entry();
				nt.componentType = e.getKey();
				nt.children = new ArrayList<>(e.getValue().size());
				nt.children.addAll(e.getValue().values());
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
			return node instanceof Thread;
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
			Thread t = (Thread) child;
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
			} else if (node instanceof Thread) {
				Thread t = (Thread) node;
				switch (column) {
				case 0:
					return t.getName();
				case 1:
					if (t.getState() == State.TERMINATED) {
						StringBuffer ret = new StringBuffer("Failed: ");
						if (RainbowMonitor.this.m_uncaughtExceptions.containsKey(t)) {
							ret.append (m_uncaughtExceptions.get(t).getMessage());
						}
						else ret.append("Unknown failure");
						return ret;
					}
					return t.getState() == State.TERMINATED ? "Failed" : "OK";
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
