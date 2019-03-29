package org.sa.rainbow.gui.arch;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;
import org.sa.rainbow.stitch.adaptation.AdaptationManager;
import org.sa.rainbow.stitch.adaptation.StitchStrategyAccessor;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.ActionKind;
import org.sa.rainbow.stitch.core.Strategy.ConditionKind;
import org.sa.rainbow.stitch.core.StrategyNode;
import java.awt.BorderLayout;

public class ArchStitchExecutorPanel extends JPanel implements IUIReporter {
	
	public static class StitchTreeTableModel extends AbstractTreeTableModel {

		private Strategy m_strategy;

		public StitchTreeTableModel(Strategy strategy) {
			m_strategy = strategy;
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
			if (arg0 instanceof String && index == 1) {
				return ((String )arg0);
			}
			if (arg0 instanceof StrategyNode) {
				StrategyNode node = (StrategyNode) arg0;
				switch(index) {
				case 0: 
					return node.label();
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

	public ArchStitchExecutorPanel() {
		setLayout(new BorderLayout(0, 0));
		m_treeTable = new JXTreeTable();
		
		JScrollPane p = new JScrollPane(m_treeTable);
		m_treeTable.setPreferredScrollableViewportSize(new Dimension(400,100));
		
		m_treeTable.setRootVisible(false);
		m_treeTable.setAutoResizeMode(JXTreeTable.AUTO_RESIZE_ALL_COLUMNS);
		m_treeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		DefaultTableColumnModel cm = new DefaultTableColumnModel();
		cm.addColumn(new TableColumn(0,70));
		cm.addColumn(new TableColumn(1,230));
		cm.addColumn(new TableColumn(100));
		m_treeTable.setColumnModel(cm);
		
		m_treeTable.setFont(new Font(m_treeTable.getFont().getFontName(), m_treeTable.getFont().getStyle(), 8));
		m_treeTable.getTableHeader().setFont(new Font(m_treeTable.getTableHeader().getFont().getFontName(), m_treeTable.getFont().getStyle(), 8));

		TableColumnAdjuster tca = new TableColumnAdjuster(m_treeTable);
		tca.setDynamicAdjustment(true);
		add(p);
	}

	private static Pattern S_START_STRATEGY = Pattern.compile("Executing Strategy (.*)");
	private static Pattern S_START_TACTIC = Pattern.compile("Executing node (.*):(.*)");
	private static Pattern S_END_TACTIC = Pattern.compile("Finished executing node (.*):(.*)->(.*)");
	private static Pattern S_SETTLE_TACTIC = Pattern.compile("Settling node (.*):(.*)@(.*)");
	
	@Override
	public void processReport(ReportType type, String message) {
		if ("Executing an adaptation".equals(message)) {
			m_treeTable.clearSelection();
		}
		else if (message.contains("Executing Strategy")) {
			Matcher m = S_START_STRATEGY.matcher(message);
			if (m.matches()) {
				getStrategyDetails(m.group(1));
			}
		}
		else if (message.contains("Executing node")) {
			Matcher m = S_START_TACTIC.matcher(message);
			if (m.matches()) {
				for (int row=0; row<m_treeTable.getRowCount(); row++) {
					if (m_treeTable.getValueAt(row, 0).equals(m.group(1))) {
						m_treeTable.setValueAt("EXECUTING", row, 2);
					}
				}
			}
		}
		else if (message.contains("Settling")) {
			Matcher m = S_SETTLE_TACTIC.matcher(message);
			if (m.matches()) {
				for (int row=0; row<m_treeTable.getRowCount(); row++) {
					if (m_treeTable.getValueAt(row, 0).equals(m.group(1))) {
						m_treeTable.setValueAt("SETTLING " + m.group(2), row, 2);
					}
				}
			}
		}
		else if (message.contains("Finished executing")) {
			Matcher m = S_END_TACTIC.matcher(message);
			if (m.matches()) {
				for (int row=0; row<m_treeTable.getRowCount(); row++) {
					if (m_treeTable.getValueAt(row, 0).equals(m.group(1))) {
						m_treeTable.setValueAt(m.group(2), row, 2);
					}
				}
			}
		}
	}
	
	protected void getStrategyDetails(String strategyName) {
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
