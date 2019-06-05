package org.sa.rainbow.util;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class SimulationSelectionWindow {

	public JFrame m_frame;
	private List<ChartPanel> m_seriesImages = new ArrayList<>();
	private DefaultListModel<Object> m_listModel;
	private JButton m_btnRunSwimSimulation;
	private JList m_list;
	private JLabel m_label;
	private JScrollPane m_scrollPane;
	private List<List<Integer>> m_arrivalRates = new ArrayList<>();

	private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimulationSelectionWindow window = new SimulationSelectionWindow();
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
	public SimulationSelectionWindow() {
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
		m_listModel = new DefaultListModel<>();

		JPanel panel = new JPanel();
		m_frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		m_btnRunSwimSimulation = new JButton("Run SWIM Simulation");
		panel.add(m_btnRunSwimSimulation);
		m_btnRunSwimSimulation.setEnabled(false);

		m_label = new JLabel("Select the simulation to run:");
		m_frame.getContentPane().add(m_label, BorderLayout.NORTH);

		m_scrollPane = new JScrollPane();
		m_frame.getContentPane().add(m_scrollPane, BorderLayout.CENTER);

		m_list = new JList();
		m_scrollPane.setViewportView(m_list);
		m_list.setFixedCellHeight(120);
		m_list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				label.setPreferredSize(new Dimension(500,20));
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
				ChartPanel comp = m_seriesImages.get(index);
				comp.setSize(500, 100);
				comp.setMaximumSize(new Dimension(500, 120));
				comp.setPreferredSize(new Dimension(500,100));
				panel.add(comp);
				panel.add(label);
				panel.setSize(500, 120);
				if (isSelected) {
					panel.setBackground(m_list.getSelectionBackground());
					comp.setBackground(m_list.getSelectionBackground());
				} else {
					panel.setBackground(m_list.getBackground());
					comp.setBackground(m_list.getBackground()); 
				}
				return panel;
			}
		});
		m_list.setModel(m_listModel);
		m_list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getFirstIndex() != -1) {
					m_btnRunSwimSimulation.setEnabled(true);
				}
			}
		});

		m_btnRunSwimSimulation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = m_list.getSelectedIndex();
				if (index != -1) {
					String[] cmds = new String[] { "xterm", "-e",
							MessageFormat.format("./run.sh sim {0} | tee swim-console.out", index) };
					ProcessBuilder pb = new ProcessBuilder(cmds);
					File workDir = new File("/headless/seams-swim/swim/simulations/swim/");
					pb.directory(workDir);
					try {
						Process p = pb.start();
						Container parent = m_btnRunSwimSimulation.getParent();
						parent.remove(m_btnRunSwimSimulation);
						final JLabel l = new JLabel("Simulation Running...");
						parent.add(l);
						parent.repaint();
						ChartPanel cp = m_seriesImages.get(index);
						final XYPlot xyplot = cp.getChart().getXYPlot();
						
						
						final List<Integer> ar = m_arrivalRates.get(index);
						new Thread(() -> {
							int sec = 0;
							ValueMarker tm = new ValueMarker(1000*sec); 
							tm.setPaint(Color.BLACK);
							tm.setStroke(new BasicStroke(2));
							xyplot.addDomainMarker(tm);

							while (sec < ar.size()) {
//								if (tm != null) {
//									xyplot.removeDomainMarker(tm);
//								}
								
								l.setText("Simulation Running..." + DurationFormatUtils.formatDuration(sec*1000, "HH:mm:ss", true));
								tm.setValue(1000*sec++);
								xyplot.setDataset(xyplot.getDataset());
								m_list.repaint();
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
							}
						}).start();
						
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			}
		});
	}

	private ChartPanel getArrivalRateAsChart(List<Integer> arrivalRate) {
		TimeSeries series = new TimeSeries("");
		TimeSeriesCollection tsc = new TimeSeriesCollection(series);
		int s = 0;
		for (Integer i : arrivalRate) {
			series.add(new Second(new Date(1000 * s++)), i, true);
		}
		JFreeChart chart = ChartFactory.createTimeSeriesChart(null, null, null, tsc, false, false, false);
		chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(1));
		chart.getXYPlot().getRangeAxis().setVisible(false);
		chart.getXYPlot().getDomainAxis().setVisible(false);
		chart.removeLegend();
		((XYPlot) chart.getPlot()).clearAnnotations();
		ChartPanel cp = new ChartPanel(chart);
		cp.setSize(400, 100);
		cp.setMaximumSize(new Dimension(400,100));
		return cp;
//		ImageIcon image = new ImageIcon(chart.createBufferedImage(400, 100));
//		return image;
	}

	public void addSimulationSeries(List<Integer> series, String description) {
		ChartPanel i = getArrivalRateAsChart(series);
		m_seriesImages.add(i);
		m_listModel.addElement(description);
		m_arrivalRates.add(series);
	}
}
