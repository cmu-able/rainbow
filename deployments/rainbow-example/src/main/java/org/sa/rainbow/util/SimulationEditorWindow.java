package org.sa.rainbow.util;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class SimulationEditorWindow {

	public JFrame m_frame;
	private JScrollPane m_scrollPane;
	private Map<Integer, List<Double>> m_arrivalRateMap;
	private JButton m_btnNewButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimulationEditorWindow window = new SimulationEditorWindow();
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
	public SimulationEditorWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		m_frame = new JFrame();
		m_frame.setBounds(100, 100, 450, 300);
		m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m_frame.getContentPane().setLayout(new BorderLayout(0, 0));

		m_scrollPane = new JScrollPane();
		m_frame.getContentPane().add(m_scrollPane, BorderLayout.CENTER);

		m_btnNewButton = new JButton("Cut");
		m_btnNewButton.setEnabled(false);
		m_frame.getContentPane().add(m_btnNewButton, BorderLayout.SOUTH);

		JMenuBar menuBar = new JMenuBar();
		m_frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSaveAs = new JMenuItem("Save as...");
		mnFile.add(mntmSaveAs);
		JFileChooser fc = new JFileChooser();
		mntmSaveAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int rv = fc.showSaveDialog(m_frame);
				if (rv == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					try (PrintWriter output = new PrintWriter(f)) {
						for (List<Double> b : m_arrivalRateMap.values()) {
							for (Double delta : b) {
								output.println(delta);
							}
						}
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}

				}
			}

		});
	}

	public void setData(List<Integer> arrivalRate, Map<Integer, List<Double>> arrivalRateMap) {
		ChartPanel panel = SimulationSelectionWindow.getArrivalRateAsChart(arrivalRate);
		m_scrollPane.setViewportView(panel);
		m_arrivalRateMap = arrivalRateMap;
		final ValueMarker leftMarker = new ValueMarker(Double.NaN);
		final ValueMarker rightMarker = new ValueMarker(Double.NaN);
		panel.getChart().getXYPlot().addDomainMarker(leftMarker);
		panel.getChart().getXYPlot().addDomainMarker(rightMarker);

		panel.addChartMouseListener(new ChartMouseListener() {

			int settingRightMarker = 0;

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				Rectangle2D dataArea = panel.getScreenDataArea();
				JFreeChart chart = event.getChart();
				XYPlot plot = (XYPlot) chart.getPlot();
				ValueAxis xAxis = plot.getDomainAxis();
				double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, RectangleEdge.BOTTOM);
				// make the crosshairs disappear if the mouse is out of range
				if (!xAxis.getRange().contains(x)) {
					x = Double.NaN;
				}

				if (settingRightMarker == 1) {
					if (x == Double.NaN)
						rightMarker.setValue(x);
					else if (Double.isNaN(leftMarker.getValue()) || leftMarker.getValue() < x)
						rightMarker.setValue(x);
				} else if (settingRightMarker == 0) {
					if (x == Double.NaN)
						leftMarker.setValue(x);
					else if (Double.isNaN(rightMarker.getValue()) || rightMarker.getValue() > x)
						leftMarker.setValue(x);
				}

			}

			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				settingRightMarker = (settingRightMarker + 1) % 3;
				if (settingRightMarker == 2)
					m_btnNewButton.setEnabled(true);
				else
					m_btnNewButton.setEnabled(false);
			}
		});

		Action action = new AbstractAction("Remove segment") {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Integer l = (int) Math.round(leftMarker.getValue() / 1000);
				Integer r = (int) Math.round(rightMarker.getValue() / 1000);

				for (int i = l; i < r; i++) {
					m_arrivalRateMap.remove(i);
				}
				leftMarker.setValue(Double.NaN);
				rightMarker.setValue(Double.NaN);
				arrivalRate.clear();
				arrivalRate.addAll(m_arrivalRateMap.values().stream().map(i -> i.size()).collect(Collectors.toList()));
				TimeSeries series = new TimeSeries("");
				int s = 0;
				for (Integer i : arrivalRate) {
					series.add(new Second(new Date(1000 * s++)), i, true);
				}
				panel.getChart().getXYPlot().setDataset(new TimeSeriesCollection(series));
			};
		};
		m_btnNewButton.setAction(action);
	}

}
