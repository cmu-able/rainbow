package org.sa.rainbow.gui.arch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.DesktopIconUI;
import javax.swing.table.DefaultTableModel;

import org.beryx.awt.color.ColorFactory;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.gui.GaugePanel;
import org.sa.rainbow.gui.arch.model.RainbowArchGaugeModel;
import org.sa.rainbow.gui.widgets.BooleanPanel;
import org.sa.rainbow.gui.widgets.ICommandUpdate;
import org.sa.rainbow.gui.widgets.MeterPanel;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;
import org.sa.rainbow.gui.widgets.TimeSeriesPanel;
import org.sa.rainbow.gui.widgets.TimeSeriesPanel.ICommandProcessor;


public class ArchGuagePanel extends GaugePanel {

	interface IConverter<T> {
		T convert(String s);
	}

	private RainbowArchGaugeModel m_gaugeInfo;
	private HashMap<String, Integer> m_op2row = new HashMap<>();
	private JInternalFrame m_frame;

	public ArchGuagePanel(String gaugeId, RainbowArchGaugeModel gaugeInfo, JInternalFrame frame) {
		super(gaugeId);
		m_gaugeInfo = gaugeInfo;
		m_frame = frame;
	}

	@Override
	public void createContent() {
		setLayout(new BorderLayout(0, 0));
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		Object[][] data = {};
		Object[] colNames = { "Operation", "Target", "Parameters" };
		DefaultTableModel tableModel = new DefaultTableModel(data, colNames);
		m_table = new JTable(tableModel);
//		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollPane.setViewportView(m_table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		m_table.setAutoscrolls(true);
		TableColumnAdjuster tca = new TableColumnAdjuster(m_table);
		tca.setDynamicAdjustment(true);
		List<Pair<String, OperationRepresentation>> signatures = m_gaugeInfo.getGaugeDesc().commandSignatures();
		int row = 0;
		for (Pair<String, OperationRepresentation> pair : signatures) {
			String name = pair.secondValue().getName();
			tableModel.addRow(new String[] { name, "", "" });
			m_op2row.put(name, row++);
		}
		m_table.setPreferredScrollableViewportSize(new Dimension(250, 50 * m_op2row.size()));

	}

	@Override
	public void requestModelUpdate(IRainbowOperation command) throws IllegalStateException, RainbowException {
		if (!m_gaugeId.equals(command.getOrigin()))
			return;
		boolean update = true;
		m_table.clearSelection();
		processOperation(command, update, false);
	}

	@Override
	public void requestModelUpdate(List<IRainbowOperation> commands, boolean transaction)
			throws IllegalStateException, RainbowException {
		m_table.clearSelection();

		for (IRainbowOperation op : commands) {
			processOperation(op, false, true);
		}
		for (Runnable runnable : updaters) {
			runnable.run();
		}
		
	}

	protected void processOperation(IRainbowOperation command, boolean update, boolean extend) throws RainbowException {

		int row = updateOperation(command);
		JDesktopIcon desktopIcon = m_frame.getDesktopIcon();
		Component c = ((JPanel )((JLayeredPane )desktopIcon.getComponent(0)).getComponent(1)).getComponent(0);
		if (c instanceof ICommandUpdate) {
			((ICommandUpdate) c).newCommand(command);
		}
		if (update) {
			for (Runnable runnable : updaters) {
				runnable.run();
			}
		}
		for (IGaugeReportUpdate r : reporters) {
			r.update(command);
		}
		m_table.changeSelection(row, 0, false, extend);
	}

	private int updateOperation(IRainbowOperation op) throws RainbowException {
		DefaultTableModel tableModel = (DefaultTableModel) m_table.getModel();
		String[] data = getTableData(op);
		Integer row = m_op2row.get(data[0]);
		if (row == null)
			throw new RainbowException(data[0] + " is not a known command");
		tableModel.setValueAt(data[1], row, 1);
		tableModel.setValueAt(data[2], row, 2);
		m_gaugeInfo.getOperations().get(op.getName()).add(new Pair<>(new Date(),op));
//		m_table.firePropertyChange("model", 0, 1);
		return row;
	}

	public DesktopIconUI createIcon(JInternalFrame frame, Map<String, Object> uidb) {

		if (m_gaugeInfo != null && m_gaugeInfo.getGaugeDesc() != null) {
			GaugeInstanceDescription desc = m_gaugeInfo.getGaugeDesc();
			Map<String, Object> ui = (Map<String, Object>) uidb.get(desc.gaugeName());
			if (ui == null) {
				ui = (Map<String, Object>) uidb.get(desc.gaugeType());
			}
			if (ui != null) {
				Map<String, Object> builtin;
				if ((builtin = (Map<String, Object>) ui.get("builtin")) != null) {
					String category = (String) builtin.get("category");
					if ("timeseries".equals(category)) {
						String xLabel = (String) builtin.get("xlabel");
						String yLabel = (String) builtin.get("ylabel");
						String command = (String) builtin.get("command");
						Double upper = (Double) builtin.get("upper");
						Double lower = (Double) builtin.get("lower");
						final String value = (String) builtin.get("value");
						if (command != null && value != null) {
							ICommandProcessor<Double> processor = createOperationProcessor(command, value, s -> {
								return Double.parseDouble(s);
							});
							TimeSeriesPanel ts = new TimeSeriesPanel(null, null, upper, lower, processor);
							ts.setSampleWindow(10);
							return new DynamicDesktopIconUI(ts);
						}
					} else if ("meter".equals(category)) {
						String command = (String) builtin.get("command");
						final String value = (String) builtin.get("value");
						Double upper = (Double) builtin.get("upper");
						Double lower = (Double) builtin.get("lower");
						Double threshold = (Double) builtin.get("threshold");
						if (command != null && value != null) {
							ICommandProcessor<Double> processor = createOperationProcessor(command, value, s -> {
								return Double.parseDouble(s);
							});
							MeterPanel meter = new MeterPanel(lower, upper, threshold, processor);
							return new DynamicDesktopIconUI(meter);
						}
					} else if ("onoff".equals(category)) {
						String command = (String) builtin.get("command");
						final String value = (String) builtin.get("value");
						final String onColor = (String) builtin.get("oncolor");
						final String offColor = (String) builtin.get("offcolor");
						if (command != null && value != null) {
							ICommandProcessor<Boolean> processor = createOperationProcessor(command, value, s -> {
								return Boolean.parseBoolean(s);
							});
							BooleanPanel onoff = new BooleanPanel(
									onColor == null ? Color.GREEN : ColorFactory.valueOf(onColor),
									offColor == null ? Color.RED : ColorFactory.valueOf(offColor), processor);
							return new DynamicDesktopIconUI(onoff);

						}
					}
				}
			}

		}

		return new RainbowDesktopIconUI(frame.getFrameIcon());
	}

	protected <T> ICommandProcessor<T> createOperationProcessor(String command, final String value, IConverter<T> cvt) {
		final OperationRepresentation rep = OperationRepresentation.parseCommandSignature(command);
		int param = 0;
		try {
			param = Integer.parseInt(value)-1;
		}
		catch (NumberFormatException e) {
			String[] parameters = rep.getParameters();
			for (String p : parameters) {
				if (value.equals(p))
					break;
				param++;
			}
		}
		final int theParam = param;
		ICommandProcessor<T> processor = (op) -> {
			return cvt.convert(op.getParameters()[theParam]);
		};
		return processor;
	}

}
