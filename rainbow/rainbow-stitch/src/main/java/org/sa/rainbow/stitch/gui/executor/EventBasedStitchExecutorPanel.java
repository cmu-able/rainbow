package org.sa.rainbow.stitch.gui.executor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;
import org.sa.rainbow.stitch.adaptation.AdaptationManager;
import org.sa.rainbow.stitch.adaptation.StitchStrategyAccessor;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.ActionKind;
import org.sa.rainbow.stitch.core.Strategy.ConditionKind;
import org.sa.rainbow.stitch.core.StrategyNode;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionStateT;

public class EventBasedStitchExecutorPanel extends JPanel implements IRainbowModelChangeCallback {
	public static class StitchTreeTableModel extends AbstractTreeTableModel {

		private Strategy m_strategy;
		Map<StrategyNode, String> m_statusMap = new HashMap<>();

		public StitchTreeTableModel(Strategy strategy) {
			m_strategy = strategy;
		}
		
		public void setStatusForNode(StrategyNode node, String status) {
			m_statusMap.put(node, status);
//			this.modelSupport.fireNewRoot();
		}
		
		@Override
		public Object getRoot() {
			return m_strategy!=null?m_strategy.getRootNode():"Nothing to execute";
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: return "Label";
			case 1: return "Step";
			case 2: return "Status";
			}
			return null;
		}

		@Override
		public Object getValueAt(Object arg0, int index) {
			if (arg0 instanceof String && index == 0) {
				return ((String )arg0);
			}
			if (arg0 instanceof StrategyNode) {
				StrategyNode node = (StrategyNode) arg0;
				switch(index) {
				case 0: 
					return node.label();
				case 2: return m_statusMap.get(arg0);
				case 1:
					StringBuffer nodeRep = new StringBuffer();
					ConditionKind condFlag = node.getCondFlag();
					switch (condFlag) {
					case DEFAULT: 
						nodeRep.append("default");
						break;
					case FAILURE:
						nodeRep.append("failure");
						break;
					case SUCCESS:
						nodeRep.append("success");
						break;
					case EXPRESSION:
						Expression expr = node.getCondExpr();
						nodeRep.append(expr.tree().getText());
						break;
					}
					nodeRep.append(" -> ");
					ActionKind actionFlag = node.getActionFlag();
					switch (actionFlag) {
					case NULL:
						nodeRep.append("TNULL");
						break;
					case DONE:
						nodeRep.append("done");
						break;
					case DOLOOP:
						nodeRep.append("do[ ");
						nodeRep.append(node.getNumDoTrials());
						nodeRep.append("] ");
						nodeRep.append(node.getDoTarget());
					case TACTIC:
						nodeRep.append(node.getTactic());
						nodeRep.append("(");
						List<Expression> tacticArgExprs = node.getTacticArgExprs();
						if (tacticArgExprs != null && !tacticArgExprs.isEmpty()) {
							nodeRep.append(tacticArgExprs.get(0).tree().getText());
							for (int i = 1; i < tacticArgExprs.size();i++) {
								nodeRep.append(",");
								nodeRep.append(tacticArgExprs.get(i).tree().getText());
							}
						}
						nodeRep.append(")");
					}
					return nodeRep.toString();
					
				}
				
			}
			return null;
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof StrategyNode) {
				StrategyNode node = (StrategyNode) parent;
				String label = node.getChildren().get(index);
				return m_strategy.nodes.get(label);
			}
			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent instanceof StrategyNode) {
				StrategyNode node = (StrategyNode) parent;
				return node.getChildren().size();
			}
			else return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent instanceof StrategyNode) {
				StrategyNode pNode = (StrategyNode) parent;
				if (child instanceof StrategyNode) {
					StrategyNode cNode = (StrategyNode) child;
					return ((StrategyNode) parent).getChildren().indexOf(cNode.label());
					
				}
			}
			return -1;
		}
		
	}
	
	
	private JXTreeTable m_treeTable;
	private ScheduledExecutorService m_executionScheduler;
	
	protected Collection<ExecutionHistoryModelInstance> m_listeningTo = new HashSet<>();
	
//	private Runnable m_checkForNewExecutionHistoryModel = new Runnable() {
//
//		@Override
//		public void run() {
//			Collection<? extends IModelInstance<?>> modelsOfType = m_mm.getModelsOfType(ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE);
//			modelsOfType.removeAll(m_listeningTo);
//			
//			if (!modelsOfType.isEmpty()) {
//				for (IModelInstance<?> m : modelsOfType) {
//					if (m instanceof ExecutionHistoryModelInstance) {
//						ExecutionHistoryModelInstance hm = (ExecutionHistoryModelInstance) m;
//						
//					}
//				}
//			}
//		}
//		
//	};
//	private ModelsManager m_mm;
	
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
	
	public EventBasedStitchExecutorPanel() {
		setLayout(new BorderLayout(0, 0));
		m_treeTable = new JXTreeTable();
		
		JScrollPane p = new JScrollPane(m_treeTable);
		m_treeTable.setPreferredScrollableViewportSize(new Dimension(400,100));
		
		m_treeTable.setRootVisible(false);
		m_treeTable.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		m_treeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		DefaultTableColumnModel cm = new DefaultTableColumnModel();
		cm.addColumn(new TableColumn(0,50));
		cm.addColumn(new TableColumn(1,230));
		cm.addColumn(new TableColumn(2,120));
		m_treeTable.setColumnModel(cm);
		
		m_treeTable.setFont(new Font(m_treeTable.getFont().getFontName(), m_treeTable.getFont().getStyle(), 8));
		m_treeTable.getTableHeader().setFont(new Font(m_treeTable.getTableHeader().getFont().getFontName(), m_treeTable.getFont().getStyle(), 8));
		m_treeTable.setTreeTableModel(new StitchTreeTableModel(null));
		TableColumnAdjuster tca = new TableColumnAdjuster(m_treeTable);
		tca.setDynamicAdjustment(true);
		add(p);
//		m_executionScheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
//		m_executionScheduler.scheduleAtFixedRate(m_checkForNewExecutionHistoryModel, 10, 5*60, TimeUnit.SECONDS);
//		
//		m_mm = Rainbow.instance().getRainbowMaster().modelsManager();
		try {
			m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
			m_modelChangePort.subscribe(m_strategyExecutionSubscriber, this);
		} catch (RainbowConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	Map<String, String> m_strategy2currentLabel = new HashMap<> ();
	Map<String, String> m_tacticToStrategy = new HashMap<>();

	@Override
	public synchronized void onEvent(ModelReference reference, IRainbowMessage message) {
		ExecutionStateT eventType = ExecutionStateT.valueOf((String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP+"2"));
		String target = (String) message.getProperty(IModelChangeBusPort.TARGET_PROP);
		String[] targetConstituents = target.split("\\.");
		switch (eventType) {
		case STRATEGY_EXECUTING:
			getStrategyDetails(targetConstituents[targetConstituents.length-1]);
			break;
		case STRATEGY_SETTLING:
			updateStatus(targetConstituents[targetConstituents.length-1], "SETTLING");
			break;
		case STRATEGY_DONE:
			updateStatus(targetConstituents[targetConstituents.length-1], "DONE");
			m_strategy2currentLabel.remove(targetConstituents[targetConstituents.length-1]);
			break;
		case NODE_EXECUTING:
			m_strategy2currentLabel.put(targetConstituents[targetConstituents.length-2], targetConstituents[targetConstituents.length-1]);
			updateStatus(targetConstituents[targetConstituents.length-1], "EXECUTING");
			break;
		case TACTIC_EXECUTING:
			String[] strategy = ((String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP+"3")).split("\\.");
			m_tacticToStrategy.put(target, strategy[strategy.length-1]);
			break;
		case TACTIC_SETTLING:
			updateStatus(m_strategy2currentLabel.get(m_tacticToStrategy.get(target)), "TACTIC SETTLING");
			break;
		case TACTIC_DONE:
			m_tacticToStrategy.remove(target);
			break;
		case NODE_DONE:
			updateStatus(targetConstituents[targetConstituents.length-1], (String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP+"3"));
			break;
		}
	}

	private void updateStatus(String label, String status) {
		StitchTreeTableModel model = (StitchTreeTableModel )m_treeTable.getTreeTableModel();
		StrategyNode n = model.m_strategy.nodes.get(label);
		if (n != null) {
			model.setStatusForNode(n, status);
		}
		m_treeTable.repaint();
		for (int row=0; row<m_treeTable.getRowCount(); row++) {
			if (m_treeTable.getValueAt(row, 0).equals(label)) {
				m_treeTable.addRowSelectionInterval(row, row);
//				m_treeTable.changeSelection(row, 1, true, true);
			}
		}		
	}

	private void getStrategyDetails(String strategyName) {
		Collection<IAdaptationManager<?>> ams = Rainbow.instance().getRainbowMaster().adaptationManagers().values();
		Strategy s = null;
		for (Iterator<IAdaptationManager<?>> iterator = ams.iterator(); iterator.hasNext() && s == null;) {
			IAdaptationManager<?> m = iterator.next();
			if (m instanceof AdaptationManager) {
				s = StitchStrategyAccessor.retrieveStrategy((AdaptationManager) m, strategyName);
			}
		}
		if (s != null) {
			StitchTreeTableModel tm = new StitchTreeTableModel(s);
		
			m_treeTable.setTreeTableModel(tm);
			
			m_treeTable.expandAll();
		}		
	}
}
