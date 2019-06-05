package org.sa.rainbow.util;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class SimulationSelectionWindow {

	public JFrame m_frame;
	private List<ImageIcon> m_seriesImages = new ArrayList<>();
	private DefaultListModel<Object> m_listModel;
	private JButton m_btnRunSwimSimulation;
	private JList m_list;

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

		m_list = new JList();
		m_frame.getContentPane().add(m_list, BorderLayout.CENTER);

		m_list.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				label.setIcon(m_seriesImages.get(index));
				return label;
			}
		});
		m_listModel = new DefaultListModel<>();
		m_list.setModel(m_listModel);
		m_list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getFirstIndex() != -1) {
					m_btnRunSwimSimulation.setEnabled(true);
				}
			}
		});

		JPanel panel = new JPanel();
		m_frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		m_btnRunSwimSimulation = new JButton("Run SWIM Simulation");
		panel.add(m_btnRunSwimSimulation);
		m_btnRunSwimSimulation.setEnabled(false);

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
						JLabel l = new JLabel ("Simulation Running...");
						parent.add(l);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			}
		});
	}

	private ImageIcon getArrivalRateAsChart(List<Integer> arrivalRate) {
		TimeSeries series = new TimeSeries("");
		TimeSeriesCollection tsc = new TimeSeriesCollection(series);
		arrivalRate.forEach(i -> series.add(new Second(), i, true));
		JFreeChart chart = ChartFactory.createTimeSeriesChart(null, null, null, tsc, false, false, false);
		chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(3));
		chart.getXYPlot().getRangeAxis().setVisible(false);
		chart.getXYPlot().getDomainAxis().setVisible(false);
		chart.removeLegend();
		((XYPlot) chart.getPlot()).clearAnnotations();
		ImageIcon image = new ImageIcon(chart.createBufferedImage(400, 100));
		return image;
	}

	public void addSimulationSeries(List<Integer> series, String description) {
		ImageIcon i = getArrivalRateAsChart(series);
		m_seriesImages.add(i);
		m_listModel.addElement(description);
	}
}
