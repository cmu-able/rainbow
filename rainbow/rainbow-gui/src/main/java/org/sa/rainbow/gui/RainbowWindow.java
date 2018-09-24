package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.internal.jaxb.mapping.orm.JaxbSecondaryTable;
import org.sa.rainbow.core.IDisposable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.GaugeManager;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import org.sa.rainbow.util.Util;

public class RainbowWindow implements IRainbowGUI, IDisposable, IRainbowReportingSubscriberCallback {
	public static final int MAX_TEXT_LENGTH = 100000;
	/** Convenience constant: size of text field to set to when Max is exceeded. */
	public static final int TEXT_HALF_LENGTH = 50000;
	public static final float TEXT_FONT_SIZE = 9.0f;

	Map<RainbowComponentT, JInternalFrame> m_internalFrames = new HashMap<>();
	Map<RainbowComponentT, JTabbedPane> m_tabs = new HashMap<>();
	Map<RainbowComponentT, JTextArea> m_allTabs = new HashMap<>();
	private JFrame m_frame;
	private IMasterCommandPort m_master;
	private OracleStatusPanel m_oracleMessagePane;
	private GUIGaugeLifecycleListener m_gaugeListener;
	private IGaugeLifecycleBusPort m_gaugeLifecyclePort;
	private GUIEffectorLifecycleListener m_effectorListener;
	private IEffectorLifecycleBusPort m_effectorLifecyclePort;
	private IModelUSBusPort m_usPort;
	private IModelDSBusPublisherPort m_dsPort;
	private Map<String, ModelPanel> m_modelSections = new HashMap<>();
	private Map<String, GaugePanel> m_gaugeSections = new HashMap<>();
	private Map<String, JTextArea> m_probeSections = new HashMap<>();
	
	JDesktopPane desktopPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RainbowWindow window = new RainbowWindow(null);
					window.addModelPanel("TestModel", "TestModel");
					OperationRepresentation op = new OperationRepresentation("test",
							new ModelReference("TestModel", "TestModel"), "operation", "1", "2", "3");
					op.setOrigin("TestGauge");
					window.m_modelSections.get("TestModel").addOperation(op, false);
					window.addGaugePanel("TestGauge");
					window.m_gaugeSections.get("TestGauge").addOperation(op);
					window.m_frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public RainbowWindow(IMasterCommandPort master) {
		m_master = master;
		initialize();
	}

	public RainbowWindow() {
		m_master = null;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		m_frame = new JFrame();
		m_frame.setBounds(100, 100, 1260, 900);
		m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		m_frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		desktopPane = new JDesktopPane();
		desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		m_frame.getContentPane().add(desktopPane, BorderLayout.CENTER);
		List<String> expectedDelegateLocations;
		Throwable error = null;
		try {
			expectedDelegateLocations = m_master.getExpectedDelegateLocations();
		} catch (Throwable e) {
			expectedDelegateLocations = Arrays.asList("Error");
			error = e;
		}

		JInternalFrame masterFrame = new JInternalFrame("Rainbow Master");
		masterFrame.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		masterFrame.setMaximizable(true);
		masterFrame.setIconifiable(true);
		masterFrame.setBounds(0, 0, 420, 250);
		desktopPane.add(masterFrame);
		m_oracleMessagePane = new OracleStatusPanel(Color.white, expectedDelegateLocations);
		masterFrame.getContentPane().add(m_oracleMessagePane, BorderLayout.CENTER);
		m_internalFrames.put(RainbowComponentT.MASTER, masterFrame);

		JInternalFrame adaptationManagerFrame = new JInternalFrame("Adaptation Managers");
		adaptationManagerFrame.setMaximizable(true);
		adaptationManagerFrame.setIconifiable(true);
		adaptationManagerFrame.setBounds(420, 0, 420, 250);
		desktopPane.add(adaptationManagerFrame);
		adaptationManagerFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		m_internalFrames.put(RainbowComponentT.ADAPTATION_MANAGER, adaptationManagerFrame);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.WHITE);
		tabbedPane.setBorder(new LineBorder(Color.RED, 2));
		adaptationManagerFrame.getContentPane().add(tabbedPane);
		m_tabs.put(RainbowComponentT.ADAPTATION_MANAGER, tabbedPane);

		JTextArea adaptationMgrTextArea = createTextAreaInTab(tabbedPane, "All");
		m_allTabs.put(RainbowComponentT.ADAPTATION_MANAGER, adaptationMgrTextArea);

		JInternalFrame analyzersFrame = new JInternalFrame("Analyzers");
		analyzersFrame.setMaximizable(true);
		analyzersFrame.setIconifiable(true);
		analyzersFrame.setBounds(840, 0, 420, 500);
		desktopPane.add(analyzersFrame);

		JTabbedPane analyzerTabs = new JTabbedPane(JTabbedPane.TOP);
		;
		analyzerTabs.setBorder(new LineBorder(Color.PINK, 2));
		analyzersFrame.getContentPane().add(analyzerTabs, BorderLayout.CENTER);

		JTextArea analyzersTextArea = createTextAreaInTab(analyzerTabs, "All");
		m_internalFrames.put(RainbowComponentT.ANALYSIS, analyzersFrame);
		m_allTabs.put(RainbowComponentT.ANALYSIS, analyzersTextArea);
		m_tabs.put(RainbowComponentT.ANALYSIS, analyzerTabs);

		// analyzerTabs.addTab("New tab", null, textArea, null);

		JInternalFrame executorsFrame = new JInternalFrame("Executors");
		executorsFrame.setMaximizable(true);
		executorsFrame.setIconifiable(true);
		executorsFrame.setBounds(0, 250, 420, 250);
		desktopPane.add(executorsFrame);

		JTabbedPane executorsTabs = new JTabbedPane(JTabbedPane.TOP);
		executorsTabs.setBorder(new LineBorder(Color.GREEN, 2));
		executorsFrame.getContentPane().add(executorsTabs, BorderLayout.CENTER);

		JTextArea executorsTextArea = createTextAreaInTab(executorsTabs, "All");
		m_internalFrames.put(RainbowComponentT.EXECUTOR, executorsFrame);
		m_allTabs.put(RainbowComponentT.EXECUTOR, executorsTextArea);
		m_tabs.put(RainbowComponentT.EXECUTOR, executorsTabs);

		JInternalFrame modelsManagerFrame = new JInternalFrame("Models Manager");
		modelsManagerFrame.setMaximizable(true);
		modelsManagerFrame.setIconifiable(true);
		modelsManagerFrame.setBounds(420, 250, 420, 250);
		desktopPane.add(modelsManagerFrame);

		JTabbedPane modelsManagerTabs = new JTabbedPane(JTabbedPane.TOP);
		modelsManagerTabs.setBorder(new LineBorder(Color.MAGENTA, 2));
		modelsManagerFrame.getContentPane().add(modelsManagerTabs, BorderLayout.CENTER);

		JTextArea modelsManagerTextArea = createTextAreaInTab(modelsManagerTabs, "All");
		m_internalFrames.put(RainbowComponentT.MODEL, modelsManagerFrame);
		m_allTabs.put(RainbowComponentT.MODEL, modelsManagerTextArea);
		m_tabs.put(RainbowComponentT.MODEL, modelsManagerTabs);

		JInternalFrame effectorsFrame = new JInternalFrame("Effectors");
		effectorsFrame.setMaximizable(true);
		effectorsFrame.setIconifiable(true);
		effectorsFrame.setBounds(0, 500, 420, 250);
		desktopPane.add(effectorsFrame);

		JTabbedPane effectorsTabs = new JTabbedPane(JTabbedPane.TOP);
		effectorsTabs.setBorder(new LineBorder(Color.ORANGE, 2));
		effectorsFrame.getContentPane().add(effectorsTabs, BorderLayout.CENTER);

		JTextArea effectorsTextArea = createTextAreaInTab(effectorsTabs, "All");
		m_internalFrames.put(RainbowComponentT.EFFECTOR, effectorsFrame);
		m_allTabs.put(RainbowComponentT.EFFECTOR, effectorsTextArea);
		m_tabs.put(RainbowComponentT.EFFECTOR, effectorsTabs);

		JInternalFrame gaugesFrame = new JInternalFrame("Gauges");
		gaugesFrame.setMaximizable(true);
		gaugesFrame.setIconifiable(true);
		gaugesFrame.setBounds(420, 500, 420, 250);
		desktopPane.add(gaugesFrame);

		JTabbedPane gaugesTabs = new JTabbedPane(JTabbedPane.TOP);
		gaugesTabs.setBorder(new LineBorder(Color.BLUE, 2));
		gaugesFrame.getContentPane().add(gaugesTabs, BorderLayout.CENTER);

		JTextArea gaugesTextArea = createTextAreaInTab(gaugesTabs, "All");
		m_internalFrames.put(RainbowComponentT.GAUGE, gaugesFrame);
		m_allTabs.put(RainbowComponentT.GAUGE, gaugesTextArea);
		m_tabs.put(RainbowComponentT.GAUGE, gaugesTabs);

		JInternalFrame probesFrame = new JInternalFrame("Probes");
		probesFrame.setMaximizable(true);
		probesFrame.setIconifiable(true);
		probesFrame.setBounds(840, 500, 420, 250);
		desktopPane.add(probesFrame);

		JTabbedPane probesTabs = new JTabbedPane(JTabbedPane.TOP);
		probesTabs.setBorder(new LineBorder(Color.ORANGE, 2));
		probesFrame.getContentPane().add(probesTabs, BorderLayout.CENTER);

		JTextArea probesTextArea = createTextAreaInTab(probesTabs, "All");
		m_internalFrames.put(RainbowComponentT.PROBE, probesFrame);
		m_allTabs.put(RainbowComponentT.PROBE, probesTextArea);
		m_tabs.put(RainbowComponentT.PROBE, probesTabs);
		probesFrame.setVisible(true);
		gaugesFrame.setVisible(true);
		effectorsFrame.setVisible(true);
		modelsManagerFrame.setVisible(true);
		executorsFrame.setVisible(true);
		analyzersFrame.setVisible(true);
		adaptationManagerFrame.setVisible(true);
		masterFrame.setVisible(true);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setBackground(Color.LIGHT_GRAY);

		JMenu menu = new JMenu("Rainbow");
		menu.setMnemonic(KeyEvent.VK_R);
		createRainbowMenu(menu);
		menuBar.add(menu);

		menu = new JMenu("Delegates");
		menu.setMnemonic(KeyEvent.VK_D);
		createDelegateMenu(menu);
		menuBar.add(menu);

		menu = new JMenu("Info");
		menu.setMnemonic(KeyEvent.VK_I);
		createInformationMenu(menu);
		menuBar.add(menu);

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		createHelpMenu(menu);
		menuBar.add(menu);

		m_frame.setJMenuBar(menuBar);
	}

	protected void quit() {
		Rainbow.instance().signalTerminate();
	}

	private JTextArea createTextAreaInTab(JTabbedPane tabbedPane, String title) {
		JTextArea ta = new JTextArea();
		ta.setFont(ta.getFont().deriveFont(TEXT_FONT_SIZE));
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setAutoscrolls(false);
		JScrollPane p = new JScrollPane(ta);
		p.setAutoscrolls(true);
		tabbedPane.add(p, title);
		return ta;

	}

	@Override
	public void dispose() {
		m_frame.setVisible(false);
		m_frame.dispose();
		m_frame = null;
	}

	@Override
	public boolean isDisposed() {
		return m_frame == null;
	}

	@Override
	public void display() {
		if (m_frame != null) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					show();
				}
			});
		}
	}

	protected void show() {
		if (m_frame == null) {
			initialize();
			initializeTabs();
		}
		m_frame.setVisible(true);
	}

	private void initializeTabs() {
		ModelsManager modelsManager = Rainbow.instance().getRainbowMaster().modelsManager();
		Collection<? extends String> types = modelsManager.getRegisteredModelTypes();
		for (String t : types) {
			Collection<? extends IModelInstance<?>> models = modelsManager.getModelsOfType(t);
			for (IModelInstance<?> m : models) {
				if (m_modelSections.get(m.getModelName()) == null) {
					try {
						String modelName = m.getModelName();
						String modelType = m.getModelType();
						addModelPanel(modelName, modelType);
					} catch (RainbowConnectionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		GaugeManager gaugeManager = Rainbow.instance().getRainbowMaster().gaugeManager();
		for (String g : gaugeManager.getCreatedGauges()) {
			if (m_gaugeSections.get(g) == null) {
				addGaugePanel(g);
			}
		}
		
		ProbeDescription probes = Rainbow.instance().getRainbowMaster().probeDesc();
		for (ProbeAttributes p : probes.probes) {
			String probeId = p.alias + "@" + p.getLocation();
			if (m_probeSections.get(probeId) == null) {
				addProbePanel(probeId);
			}
		}
		
		try {
			RainbowPortFactory.createProbeReportingPortSubscriber(new IProbeReportPort() {
				
				@Override
				public void dispose() {
					
				}
				
				@Override
				public void reportData(IProbeIdentifier probe, String data) {
					JTextArea ta = m_probeSections.get(probe.id());
					if (ta != null) {
						ta.append(data);
						ta.setCaretPosition(ta.getText().length());
						if (ta.getText().length() > MAX_TEXT_LENGTH) {
							ta.setText(ta.getText().substring(TEXT_HALF_LENGTH));
						}
					}
				}
			});
		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addGaugePanel(String gaugeID) {
		GaugePanel gp = new GaugePanel(gaugeID);
		JTabbedPane tp = m_tabs.get(RainbowComponentT.GAUGE);
		tp.add(gaugeID, gp);
		m_gaugeSections.put(gaugeID, gp);
	}

	private void addModelPanel(String modelName, String modelType) throws RainbowConnectionException {
		ModelPanel mp = new ModelPanel(new ModelReference(modelName, modelType));
		JTabbedPane tp = m_tabs.get(RainbowComponentT.MODEL);
		tp.add(modelName, mp);
		m_modelSections.put(modelName, mp);
	}
	
	private void addProbePanel(String probeId) {
		JTextArea p = new JTextArea ();
		JScrollPane s = new JScrollPane();
		s.setViewportView(p);
		s.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		s.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JTabbedPane tp = m_tabs.get(RainbowComponentT.PROBE);
		tp.add(probeId, p);
		m_probeSections.put(probeId, p);

	}

	@Override
	public void setMaster(IMasterCommandPort master) {
		m_master = master;
		try {
			if (m_master == null) {
				// RainbowPortFactory.createDelegateMasterConnectionPort (null);
				m_master = RainbowPortFactory.createMasterCommandPort();
			}
			IRainbowReportingSubscriberPort reportingSubscriberPort = RainbowPortFactory
					.createReportingSubscriberPort(this);
			reportingSubscriberPort.subscribe(EnumSet.allOf(RainbowComponentT.class), EnumSet.allOf(ReportType.class));

		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Creates a series of Oracle-specific menu items.
	 * 
	 * @param menu
	 *            the menu on which to create items.
	 */
	private void createRainbowMenu(JMenu menu) {
		JMenuItem item;

		item = new JMenuItem("Populate panels");
		item.setMnemonic(KeyEvent.VK_P);
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				initializeTabs();
			}
		});
		menu.add(item);

		// Management menu item
		item = new JMenuItem("Toggle adaptation switch");
		item.setMnemonic(KeyEvent.VK_A);
		item.setToolTipText("Toggles whether self-adaptation is enabled, default is ON");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean b = Rainbow.instance().getRainbowMaster().isAdaptationEnabled();
				Rainbow.instance().getRainbowMaster().enableAdaptation(!b);

				// boolean b = !((AdaptationManager )Oracle.instance().adaptationManager())
				// .adaptationEnabled();
				// ((AdaptationManager
				// )Oracle.instance().adaptationManager()).setAdaptationEnabled(b);
				writeText(RainbowComponentT.MASTER, "Adaptation switched " + (b ? "ON" : "OFF"));
			}
		});
		menu.add(item);

		item = new JMenuItem("Identify consoles");
		item.setMnemonic(KeyEvent.VK_I);
		item.setToolTipText("Prints a message to each console to identify it");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				writeText(RainbowComponentT.MODEL, "MODELS MANAGER");
				writeText(RainbowComponentT.ANALYSIS, "ANALYSIS");
				writeText(RainbowComponentT.ADAPTATION_MANAGER, "Adaptation Manager");
				writeText(RainbowComponentT.EXECUTOR, "Executor");
				writeText(RainbowComponentT.PROBE, "Probes");
				writeText(RainbowComponentT.EFFECTOR, "Effectors");
				writeText(RainbowComponentT.MASTER, "Management");

			}
		});
		menu.add(item);

		item = new JMenuItem("Clear consoles");
		item.setMnemonic(KeyEvent.VK_C);
		item.setToolTipText("Clears all the GUI consoles");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (JTextArea textArea : m_allTabs.values()) {
					textArea.setText("");
				}
			}
		});
		menu.add(item);
		menu.add(new JSeparator());
		// Termination menu item
		item = new JMenuItem("Sleep Master+Delegate");
		item.setMnemonic(KeyEvent.VK_S);
		item.setToolTipText("Signals Master and all Delegates to terminate, then sleep");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		item.setEnabled(false);
		menu.add(item);
		
		item = new JMenuItem("Monitor Rainbow Threads");
		item.setMnemonic(KeyEvent.VK_M);
		item.setToolTipText("Opens a window for monitoring threads in Rainbow");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				monitorRainbow();
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Restart Master+Delegate");
		item.setMnemonic(KeyEvent.VK_R);
		item.setToolTipText("Signals Master and all Delegates to terminate, then restart");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Rainbow.instance().signalTerminate(ExitState.RESTART);
			}
		});
		item.setEnabled(false);

		menu.add(item);
		item = new JMenuItem("Destroy Master+Delegate");
		item.setMnemonic(KeyEvent.VK_D);
		item.setToolTipText("Signals Master and all Delegates to terminate, then self-destruct");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_master.destroyDelegates();
				Rainbow.instance().signalTerminate(ExitState.DESTRUCT);
			}
		});
		menu.add(item);
		menu.add(new JSeparator());
		// Quit menu item
		item = new JMenuItem("Force Quit");
		item.setMnemonic(KeyEvent.VK_Q);
		item.setToolTipText("Forces the Master component to quit immediately");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				forceQuit();
			}
		});
		menu.add(item);
	}

	protected void monitorRainbow() {
		RainbowMonitor adaptationManagerFrame = new RainbowMonitor();
		adaptationManagerFrame.setMaximizable(true);
		adaptationManagerFrame.setIconifiable(true);
		desktopPane.add(adaptationManagerFrame);		
	}

	protected void forceQuit() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				m_master.destroyDelegates();
				Rainbow.instance().signalTerminate();
				Util.pause(IRainbowRunnable.LONG_SLEEP_TIME);
				while (Rainbow.instance().getThreadGroup().activeCount() > 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.exit(RainbowConstants.EXIT_VALUE_DESTRUCT);
			}
		}).start();
		int ret = JOptionPane.showOptionDialog(m_frame, "Waiting for Rainbow to shutdown. Continue to wait?",
				"Quitting", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

		if (ret == JOptionPane.NO_OPTION) {
			System.exit(RainbowConstants.EXIT_VALUE_ABORT);
		}
	}

	/**
	 * Creates a series of Delegate-specific menu items.
	 * 
	 * @param menu
	 *            the menu on which to create items.
	 */
	private void createDelegateMenu(JMenu menu) {
		JMenuItem item;

		// Probe start menu item
		item = new JMenuItem("Start Probes");
		item.setMnemonic(KeyEvent.VK_P);
		item.setToolTipText("Signals all Delegates to start the probes (key: rainbow.delegate.startProbesOnInit)");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_master.startProbes();
				// if (! Rainbow.inSimulation()) {
				// ((SystemDelegate )Oracle.instance().targetSystem()).signalStartProbes();
				// }
			}
		});
		menu.add(item);
		// Probe kill menu item
		item = new JMenuItem("Kill Probes");
		item.setMnemonic(KeyEvent.VK_K);
		item.setToolTipText("Signals all Delegates to kill the probes");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_master.killProbes();
				// if (! Rainbow.inSimulation()) {
				// ((SystemDelegate )Oracle.instance().targetSystem()).signalKillProbes();
				// }
			}
		});
		item.setEnabled(false);

		menu.add(item);
		menu.add(new JSeparator());

		// Test Effector 1 menu item
		item = new JMenuItem("T1 KillDelegate Effector");
		item.setMnemonic(KeyEvent.VK_1);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String hostname = JOptionPane.showInputDialog(m_frame, "Please provide hostname of Delegate to kill");
				if (hostname != null && hostname.length() > 0) {
					m_master.killDelegate(hostname);
				}
			}
		});
		menu.add(item);
		// Test Effector 2 menu item
		item = new JMenuItem("T2 Test An Effector");
		item.setMnemonic(KeyEvent.VK_2);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String effID = JOptionPane.showInputDialog(m_frame,
						"Please identify Effector to test: " + "'name@location' (or just 'name' for localhost)");
				if (effID == null || effID.length() == 0) {
					writeText(RainbowComponentT.MASTER, "Sorry, Master needs to know what effector to invoke!");
				}
				Pair<String, String> namePair = Util.decomposeID(effID);
				if (namePair.secondValue() == null) { // default to localhost
					namePair.setSecondValue("localhost");
				}
				String argStr = JOptionPane.showInputDialog(m_frame,
						"Please provide String arguments, separated by " + "'|'");
				String[] args;
				if (argStr == null || argStr.length() == 0) {
					args = new String[0];
				} else {
					args = argStr.split("\\s*\\|\\s*");
				}
				// run the test
				testEffector(namePair.secondValue(), namePair.firstValue(), args);
			}
		});
		menu.add(item);
		// Test an operation menu item
		item = new JMenuItem("T3 Test An Operation");
		item.setMnemonic(KeyEvent.VK_3);
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String operationName = JOptionPane.showInputDialog(m_frame, "Please identify the Operation to test:");
				if (operationName == null || operationName.isEmpty()) {
					writeText(RainbowComponentT.MASTER, "Sorry, Rainbow Master needs to know what operation to invoke.");
				}

				String argStr = JOptionPane.showInputDialog(m_frame,
						"Please provide string arguments, separated by ','");
				String[] args;
				if (argStr == null || argStr.isEmpty()) {
					args = new String[0];
				} else {
					args = argStr.split("\\s*,\\s*");
				}
				String modelRef = JOptionPane.showInputDialog(m_frame,
						"Please identify the model to run the operation on: modelName:modelType (or"
								+ " just 'modelName' for Acme)");
				ModelReference model = Util.decomposeModelReference(modelRef);
				if (model.getModelType() == null || model.getModelType().isEmpty()) {
					model = new ModelReference(model.getModelName(), "Acme");
				}

				// Publish the operation
				testOperation(model, operationName, args);
			}
		});
		menu.add(item);

		item = new JMenuItem("T4 Change the model");
		item.setMnemonic(KeyEvent.VK_4);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mr = JOptionPane.showInputDialog(m_frame, "Please identify the model to run the operation on: "
						+ "modelName:modelType (or" + " just 'modelName' for Acme)");
				ModelReference model = Util.decomposeModelReference(mr);
				if (model.getModelType() == null || model.getModelType().isEmpty()) {
					model = new ModelReference(model.getModelName(), "Acme");
				} else {
					writeText(RainbowComponentT.MASTER, "Sorry, we need to know the model that will be changed");
					return;
				}

				String operation = JOptionPane.showInputDialog(m_frame, "Please identify a model operation to test");
				if (operation == null || operation.isEmpty()) {
					writeText(RainbowComponentT.MASTER, "Sorry, we need to know what model operation to conduct");
					return;
				}

				String argStr = JOptionPane.showInputDialog(m_frame,
						"Please provide string arguments, separated by ','");
				String[] args;
				if (argStr == null || argStr.isEmpty()) {
					args = new String[0];
				} else {
					args = argStr.split("\\s*,\\s*");
				}

				testModelOperation(model, operation, args);
			}
		});

		menu.add(new JSeparator());
		// Delegate control menu item
		item = new JMenuItem("Restart Delegates");
		item.setMnemonic(KeyEvent.VK_R);
		item.setToolTipText("Signals all the Delegates to terminate and restart");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				throw new NotImplementedException();
				// // issues restart to all delegates
				// signalDelegates(ServiceConstants.SVC_CMD_RESTART);
			}
		});
		item.setEnabled(false);

		menu.add(item);
		item = new JMenuItem("Sleep Delegates");
		item.setMnemonic(KeyEvent.VK_S);
		item.setToolTipText("Signals all the Delegates to terminate and sleep");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				throw new NotImplementedException();
				// // issues sleep to all delegates
				// signalDelegates(ServiceConstants.SVC_CMD_SLEEP);
			}
		});
		item.setEnabled(false);

		menu.add(item);
		item = new JMenuItem("Destroy Delegates");
		item.setMnemonic(KeyEvent.VK_D);
		item.setToolTipText("Signals all the Delegates to terminate and the self-destruct");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_master.destroyDelegates();
				// // issues destroy to all delegates
				// signalDelegates(ServiceConstants.SVC_CMD_STOP);
			}
		});
		menu.add(item);
		menu.add(new JSeparator());

		// RainDropD control
		item = new JMenuItem("Awaken RainDropD...");
		item.setMnemonic(KeyEvent.VK_A);
		item.setToolTipText("Given a hostname, awakens the Delegate RainDrop Daemon on that host");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				throw new NotImplementedException();
				// String hostname = JOptionPane.showInputDialog(m_frame, "Please provide a
				// hostname with a sleeping
				// RainDropD");
				// if (hostname != null && hostname.length() > 0) {
				// RemoteControl.waker(hostname, RemoteControl.WAKER_RESTART);
				// }
			}
		});
		item.setEnabled(false);

		menu.add(item);
		item = new JMenuItem("Kill RainDropD...");
		item.setMnemonic(KeyEvent.VK_L);
		item.setToolTipText("Given a hostname, kills the Delegate RainDropD on that host");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				throw new NotImplementedException();
				// String hostname = JOptionPane.showInputDialog(m_frame, "Please provide a
				// hostname with a sleeping
				// RainDropD");
				// if (hostname != null && hostname.length() > 0) {
				// RemoteControl.waker(hostname, RemoteControl.WAKER_KILL);
				// }
			}
		});
		item.setEnabled(false);

		menu.add(item);

	}

	/**
	 * Creates the help menu items.
	 * 
	 * @param menu
	 *            the menu on which to create items.
	 */
	private void createHelpMenu(JMenu menu) {
		JMenuItem item;

		item = new JMenuItem("Software Update...");
		item.setMnemonic(KeyEvent.VK_U);
		item.setToolTipText("Allows the update of the Master and RainbowDelegate software components");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// String hostname = JOptionPane.showInputDialog(m_frame,
				// "Please provide a hostname to send update software.\n'*' for all delegate
				// hosts.");
				// if (hostname != null && hostname.length() > 0) {
				// final JFileChooser fc = new JFileChooser(Rainbow.instance().getBasePath());
				// int rv = fc.showDialog(m_frame, "Select File");
				// if (rv == JFileChooser.APPROVE_OPTION) {
				// File file = fc.getSelectedFile();
				// writeText(ID_ORACLE_MESSAGE, "Attempting remote update on " + hostname + "
				// with " + file.getAbsolutePath());
				// RemoteControl.updater(hostname, file.getParentFile(), file.getName());
				// }
				// }
				throw new NotImplementedException();
			}
		});
		item.setEnabled(false);

		menu.add(item);
		menu.add(new JSeparator());
		item = new JMenuItem("About");
		item.setMnemonic(KeyEvent.VK_A);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(m_frame, "Will be available soon...", "No Help",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu.add(item);
	}

	private void createInformationMenu(final JMenu menu) {

		JMenu gauges = new JMenu("Gauges");
		gauges.setMnemonic(KeyEvent.VK_G);
		menu.add(gauges);

		// Set up listener for gauge creation and deletion, and set create a port to the
		// lifecycle port
		m_gaugeListener = new GUIGaugeLifecycleListener(gauges);
		try {
			m_gaugeLifecyclePort = RainbowPortFactory.createManagerLifecylePort(m_gaugeListener);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JMenu effectors = new JMenu("Effectors");
		gauges.setMnemonic(KeyEvent.VK_E);
		menu.add(effectors);
		m_effectorListener = new GUIEffectorLifecycleListener(effectors);
		try {
			m_effectorLifecyclePort = RainbowPortFactory.createClientSideEffectorLifecyclePort(m_effectorListener);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void signalDelegates(String cmd) {
		// if (! Rainbow.inSimulation()) {
		// String[] locs = ((SystemDelegate
		// )Oracle.instance().targetSystem()).delegateLocations();
		// for (String loc : locs) {
		// if (loc.equals(Rainbow.property(Rainbow.PROPKEY_MASTER_LOCATION))) {
		// continue;
		// }
		// writeText(ID_ORACLE_MESSAGE, "Signalling RainbowDelegate@" + loc + " to " +
		// cmd);
		// RemoteControl.restarter(loc, cmd);
		// }
		// }
	}

	// GUI invoked test methods
	private void testModelOperation(ModelReference model, String operation, String[] args) {
		OperationRepresentation or = new OperationRepresentation(operation, model, args[0],
				Arrays.copyOfRange(args, 1, args.length));
		or.setOrigin("GUI");
		if (m_usPort == null) {
			try {
				m_usPort = RainbowPortFactory.createModelsManagerClientUSPort(new Identifiable() {
					@Override
					public String id() {
						return "GUI";
					}
				});
				m_usPort.updateModel(or);
			} catch (RainbowConnectionException e) {
				writeText(RainbowComponentT.MASTER, "Failed to publish the operation to the model");
			}
		}
	}

	private void testEffector(String target, String effName, String[] args) {
		String message = "Testing Effector " + effName + "@" + target + Arrays.toString(args);
		writeText(RainbowComponentT.EFFECTOR, message);
		Outcome outcome = m_master.testEffector(target, effName, Arrays.asList(args));
		JOptionPane.showMessageDialog(m_frame, message + " - outcome: " + outcome);
		writeText(RainbowComponentT.EFFECTOR, message + " - outcome: " + outcome);
		// writeText(ID_EXECUTOR, "Testing Effector " + effName +
		// Arrays.toString(args));
		// IEffector.Outcome outcome =
		// Rainbow.instance().sysOpProvider().execute(effName, target, args);
		// writeText(ID_EXECUTOR, " - outcome: " + outcome);
	}

	private void testOperation(ModelReference modelRef, String opName, String[] args) {
		OperationRepresentation or = new OperationRepresentation(opName, modelRef, args[0],
				Arrays.copyOfRange(args, 1, args.length));
		if (m_dsPort == null) {
			try {
				m_dsPort = RainbowPortFactory.createModelDSPublishPort(new Identifiable() {

					@Override
					public String id() {
						return "UI";
					}
				});

			} catch (RainbowConnectionException e) {
				writeText(RainbowComponentT.MASTER, "Failed to publish the operation.");
			}
		}
		OperationResult result = m_dsPort.publishOperation(or);
		String msg = modelRef.toString() + "." + opName + Arrays.toString(args) + " - returned " + result.result.name()
				+ ": " + result.reply;
		writeText(RainbowComponentT.MASTER, msg);
		JOptionPane.showMessageDialog(m_frame, msg);

	}

	/**
	 * Writes text to the panel without a newline.
	 * 
	 * @param panelID
	 * @param text
	 */
	public void writeTextSL(RainbowComponentT panelID, String text) {
		if (panelID == RainbowComponentT.MASTER) {
			if (m_oracleMessagePane == null)
				return;
			m_oracleMessagePane.report(text, false);
			return;
		}
		JTextArea ta = m_allTabs.get(panelID);
		if (ta == null)
			return;
		ta.append(text);
		ta.setCaretPosition(ta.getText().length());
	}

	public void writeText(RainbowComponentT panelID, String text) {
		if (panelID == RainbowComponentT.MASTER) {
			if (m_oracleMessagePane == null)
				return;
			m_oracleMessagePane.report(text, true);
			return;
		}
		JTextArea ta = m_allTabs.get(panelID);

		if (ta == null)
			return;
		ta.append(text + "\n");
		ta.setCaretPosition(ta.getText().length());
		if (ta.getText().length() > MAX_TEXT_LENGTH) {
			ta.setText(ta.getText().substring(TEXT_HALF_LENGTH));
		}
	}

	@Override
	public void report(RainbowComponentT component, ReportType type, String message) {
		String msg = MessageFormat.format("[{0}]: {1}", type.toString(), message);

		Util.dataLogger().info(msg);

		RainbowComponentT panel = component;
		if (component == RainbowComponentT.DELEGATE)
			component = RainbowComponentT.MASTER;
		if (component == RainbowComponentT.EFFECTOR_MANAGER)
			component = RainbowComponentT.EFFECTOR;
		if (component == RainbowComponentT.PROBE_MANAGER)
			component = RainbowComponentT.PROBE;
		if (component == RainbowComponentT.SELECTOR)
			component = RainbowComponentT.MASTER;
		if (component == RainbowComponentT.GAUGE_MANAGER)
			component = RainbowComponentT.GAUGE;
		
		if (component == RainbowComponentT.GAUGE) {
			for (GaugePanel gp : m_gaugeSections.values()) {
				gp.processReport(type, message);
			}
			
		}
		else if (component == RainbowComponentT.MODEL) {
			for (ModelPanel mp : m_modelSections.values()) {
				mp.processReport(type, message);
			}
		}
		
		writeText(component, msg);
	}

}
