package org.sa.rainbow.stitch.gui.executor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.StitchScript;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.Outcome;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionStateT;
import org.sa.rainbow.stitch.visitor.Stitch;

public class StrategyExecutionPanel extends JPanel implements IRainbowModelChangeCallback {
	static public class StrategyInstanceDataRenderer extends JLabel implements ListCellRenderer<StrategyInstanceData> {

		private Font italicFont;
		private static final Color ERROR_COLOR = Color.RED;
		private static final Color DARK_ERROR_COLOR = new Color(139, 0, 0);

		@Override
		public Component getListCellRendererComponent(JList<? extends StrategyInstanceData> list,
				StrategyInstanceData value, int index, boolean isSelected, boolean cellHasFocus) {
			setOpaque(true);
			setText(value.strategyData.name);
			if (value.currentState != ExecutionStateT.STRATEGY_DONE) {
				if (italicFont == null) {
					italicFont = new Font(this.getFont().getName(), Font.ITALIC, this.getFont().getSize());
				}
				setFont(italicFont);
			}
			if (value.outcome != Outcome.SUCCESS) {
				this.setBackground(isSelected ? DARK_ERROR_COLOR : ERROR_COLOR);
			} else if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}

	}

	/*
	 * Note, this class only works when there is one strategy executor, which is the
	 * normal case in Rainbow.
	 */
	class StrategyData {
		StitchScript script;
		int numberOfRuns = 1;
		int numberOfSuccesses = 0;
		int numberOfFailures = 0;
		String name;

		List<StrategyInstanceData> runs = new LinkedList<StrategyInstanceData>();
		public Strategy strategy;

		public StrategyInstanceData addNewRun() {
			StrategyInstanceData sid = new StrategyInstanceData();
			sid.strategyData = this;
			runs.add(0, sid);
			return sid;
		}

		public StrategyInstanceData getCurrentRun() {
			if (!runs.isEmpty()) {
				return runs.get(0);
			}
			return null;
		}
	}

	class TraceData {
		String label;
		ExecutionStateT state;
	}

	class StrategyInstanceData {
		StrategyData strategyData;
		ExecutionStateT currentState = ExecutionStateT.NOT_EXECUTING;
		List<TraceData> traces = new LinkedList<TraceData>();
		Outcome outcome;

		public void setStatus(ExecutionStateT eventType) {
			currentState = eventType;
		}

		public void addTraceElement(String nodeLabel, ExecutionStateT eventType) {
			TraceData td = new TraceData();
			td.label = nodeLabel;
			td.state = eventType;
			traces.add(0, td);
		}

		public void setTraceStatus(String target, ExecutionStateT eventType) {
			for (TraceData i : traces) {
				if (i.label.equals(target)) {
					i.state = eventType;
					break;
				}
			}
		}

	}

	private JTextField m_numberOfRunsFields;
	private JTextField m_successesField;
	private JTextField m_failuresField;
	private Map<String, StrategyData> m_strategyData = new HashMap<>();
	private JList m_strategiesExecuted;

	private final IRainbowChangeBusSubscription m_strategyExecutionSubscriber = new IRainbowChangeBusSubscription() {

		@Override
		public boolean matches(IRainbowMessage message) {
			String type = (String) message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP);
			if (type != null && ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE.equals(type)) {
				return true;
			}
			return false;
		}

	};
	private IModelChangeBusSubscriberPort m_modelChangePort;
	private DefaultListModel m_listModel;
	private Map<String, String> m_tacticToStrategy = new HashMap<>();
	private RSyntaxTextArea m_strategyText;
	protected Map<String, String> m_pathToText = new HashMap<>();

	/**
	 * Create the panel.
	 */
	public StrategyExecutionPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);

		JLabel lblNewLabel = new JLabel("Number of Runs:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);

		m_numberOfRunsFields = new JTextField();
		GridBagConstraints gbc_numberOfRunsFields = new GridBagConstraints();
		gbc_numberOfRunsFields.insets = new Insets(0, 0, 5, 5);
		gbc_numberOfRunsFields.fill = GridBagConstraints.HORIZONTAL;
		gbc_numberOfRunsFields.gridx = 2;
		gbc_numberOfRunsFields.gridy = 0;
		add(m_numberOfRunsFields, gbc_numberOfRunsFields);
		m_numberOfRunsFields.setColumns(10);

		m_strategyText = new RSyntaxTextArea();
		RTextScrollPane sp = new RTextScrollPane(m_strategyText);
		m_strategyText.setCodeFoldingEnabled(true);
		m_strategyText.setSyntaxEditingStyle("text/stitch");
		m_strategyText.setEditable(false);
		
		GridBagConstraints gbc_m_strategyText = new GridBagConstraints();
		gbc_m_strategyText.gridheight = 4;
		gbc_m_strategyText.insets = new Insets(0, 0, 5, 0);
		gbc_m_strategyText.fill = GridBagConstraints.BOTH;
		gbc_m_strategyText.gridx = 3;
		gbc_m_strategyText.gridy = 0;
		add(sp, gbc_m_strategyText);

		JLabel lblSuccess = new JLabel("Success:");
		GridBagConstraints gbc_lblSuccess = new GridBagConstraints();
		gbc_lblSuccess.anchor = GridBagConstraints.EAST;
		gbc_lblSuccess.insets = new Insets(0, 0, 5, 5);
		gbc_lblSuccess.gridx = 1;
		gbc_lblSuccess.gridy = 1;
		add(lblSuccess, gbc_lblSuccess);

		m_successesField = new JTextField();
		GridBagConstraints gbc_successesField = new GridBagConstraints();
		gbc_successesField.insets = new Insets(0, 0, 5, 5);
		gbc_successesField.fill = GridBagConstraints.HORIZONTAL;
		gbc_successesField.gridx = 2;
		gbc_successesField.gridy = 1;
		add(m_successesField, gbc_successesField);
		m_successesField.setColumns(10);

		JLabel lblFailures = new JLabel("Failures:");
		GridBagConstraints gbc_lblFailures = new GridBagConstraints();
		gbc_lblFailures.anchor = GridBagConstraints.EAST;
		gbc_lblFailures.insets = new Insets(0, 0, 5, 5);
		gbc_lblFailures.gridx = 1;
		gbc_lblFailures.gridy = 2;
		add(lblFailures, gbc_lblFailures);

		m_failuresField = new JTextField();
		GridBagConstraints gbc_failuresField = new GridBagConstraints();
		gbc_failuresField.insets = new Insets(0, 0, 5, 5);
		gbc_failuresField.fill = GridBagConstraints.HORIZONTAL;
		gbc_failuresField.gridx = 2;
		gbc_failuresField.gridy = 2;
		add(m_failuresField, gbc_failuresField);
		m_failuresField.setColumns(10);
		
		m_numberOfRunsFields.setEnabled(false);
    	m_failuresField.setEnabled(false);
    	m_successesField.setEnabled(false);

		m_strategiesExecuted = new JList();
		m_listModel = new DefaultListModel();
		m_strategiesExecuted.setModel(m_listModel);
		scrollPane.setViewportView(m_strategiesExecuted);

		try {
			m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
			m_modelChangePort.subscribe(m_strategyExecutionSubscriber, this);
		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_strategiesExecuted.setCellRenderer(new StrategyInstanceDataRenderer());
		m_strategiesExecuted.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_strategiesExecuted.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();

		        int firstIndex = e.getFirstIndex();
		        if (!lsm.isSelectionEmpty()) {
		        	StrategyInstanceData sid = (StrategyInstanceData) m_listModel.get(firstIndex);
		        	m_numberOfRunsFields.setText(Integer.toString(sid.strategyData.numberOfRuns));
		        	m_failuresField.setText(Integer.toString(sid.strategyData.numberOfFailures));
		        	m_successesField.setText(Integer.toString(sid.strategyData.numberOfSuccesses));
		        	String path = sid.strategyData.strategy.m_stitch.path;
		        	String stitchText = m_pathToText.get(path);
					if (stitchText == null) {
		        		try {
							stitchText = new String(Files.readAllBytes(new File(path).toPath()));
							m_pathToText.put(path, stitchText);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		        	}
					m_strategyText.setText(stitchText);
					Pattern p = Pattern.compile("strategy.*" + sid.strategyData.name);
					Matcher m = p.matcher(stitchText);
					if (m.find()) {
						int location = m.start();
						m_strategyText.setCaretPosition(location);
						try {
							m_strategyText.addLineHighlight(m_strategyText.getLineOfOffset(location), Color.BLUE);
							for (TraceData trace : sid.traces) {
								p = Pattern.compile(trace.label + "\\s*:");
								m = p.matcher(stitchText);
								if (m.find(location)) {
									m_strategyText.addLineHighlight(m_strategyText.getLineOfOffset(m.start()), Color.BLUE);

								}
							}
						} catch (BadLocationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
		        }
		        else {
		        	m_numberOfRunsFields.setText("");
		        	m_failuresField.setText("");
		        	m_successesField.setText("");
		        	m_strategyText.setText("");
		        }
			}
		});

	}

	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
		ExecutionStateT eventType = ExecutionStateT
				.valueOf((String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + "2"));
		String target = (String) message.getProperty(IModelChangeBusPort.TARGET_PROP);
		String[] targetConstituents = target.split("\\.");
		switch (eventType) {
		case STRATEGY_EXECUTING: {
			String strategyName = targetConstituents[targetConstituents.length - 1];

			StrategyData sd = getStrategyData(strategyName);
			sd.numberOfRuns++;
			StrategyInstanceData sid = sd.addNewRun();
			sid.setStatus(eventType);
			m_listModel.addElement(sid);
			m_strategiesExecuted.setModel(m_listModel);
			break;
		}
		case STRATEGY_SETTLING: {
			String nodeLabel = targetConstituents[targetConstituents.length - 1];
			String strategyName = targetConstituents[targetConstituents.length - 2];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setTraceStatus(nodeLabel, eventType);
			m_strategiesExecuted.repaint();
			break;
		}
		case STRATEGY_DONE: {
			String strategyName = targetConstituents[targetConstituents.length - 1];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setStatus(eventType);
			Outcome outcome = Outcome
					.valueOf((String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + "3"));
			sd.getCurrentRun().outcome = outcome;
			switch (outcome) {
			case SUCCESS:
				sd.numberOfSuccesses++;
				break;
			case FAILURE:
				sd.numberOfFailures++;
				break;
			}

			m_strategiesExecuted.repaint();
			break;
		}
		case NODE_EXECUTING: {
			String nodeLabel = targetConstituents[targetConstituents.length - 1];
			String strategyName = targetConstituents[targetConstituents.length - 2];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().addTraceElement(nodeLabel, eventType);
			m_strategiesExecuted.repaint();
			break;
		}
		case TACTIC_EXECUTING: {
			targetConstituents = ((String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + "3")).split("\\.");
			String strategyName = targetConstituents[targetConstituents.length - 1];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().addTraceElement(target, eventType);
			m_tacticToStrategy.put(target, strategyName);
			break;
		}
		case TACTIC_SETTLING: {
			String strategyName = m_tacticToStrategy.get(target);
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setTraceStatus(target, eventType);
			break;
		}
		case TACTIC_DONE: {
			String strategyName = m_tacticToStrategy.get(target);
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setTraceStatus(target, eventType);
			m_tacticToStrategy.remove(target);
			break;
		}
		case NODE_DONE: {
			String strategyName = targetConstituents[targetConstituents.length - 2];
			String nodeLabel = targetConstituents[targetConstituents.length - 1];
			StrategyData sd = getStrategyData(strategyName);
			sd.getCurrentRun().setTraceStatus(nodeLabel, eventType);
			m_strategiesExecuted.repaint();

			break;
		}
		}
	}

	private StrategyData getStrategyData(String strategyName) {
		StrategyData strategyData = m_strategyData.get(strategyName);
		if (strategyData == null) {
			strategyData = new StrategyData();
			m_strategyData.put(strategyName, strategyData);
			List<Stitch> stitches = Ohana.instance().listStitches();
			OUTER: for (Stitch stitch : stitches) {
				List<Strategy> strategies = stitch.script.strategies;
				for (Strategy s : strategies) {
					if (s.getName().equals(strategyName)) {
						strategyData.script = stitch.script;
						strategyData.strategy = s;
						break OUTER;
					}
				}
			}
		}

		return strategyData;

	}

}
