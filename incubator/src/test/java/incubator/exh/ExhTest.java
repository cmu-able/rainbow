package incubator.exh;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Test program that shows a window with the EXH UI.
 */
public class ExhTest {
	/**
	 * Number of collectors created so far.
	 */
	static int m_collector_count;
	
	/**
	 * Starts the program.
	 * @param args not used
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("Test");
		f.setLayout(new BorderLayout());
		
		ExhUi exh = new ExhUi();
		new ExhGlobalUiSynchronizer(exh);
		
		f.add(exh, BorderLayout.CENTER);
		
		JMenuBar bar = new JMenuBar();
		f.setJMenuBar(bar);
		
		final JMenu collectors = new JMenu("Collectors");
		bar.add(collectors);
		
		JMenu system_menu = new JMenu("System");
		bar.add(system_menu);
		
		JMenuItem gc = new JMenuItem("Run Garbage Collector");
		system_menu.add(gc);
		gc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.gc();
			}
		});
		
		JMenuItem add_collector = new JMenuItem("Add New Collector");
		collectors.add(add_collector);
		add_collector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_collector_count++;
				final ThrowableCollector nc = new LocalCollector(
						"Collector " + m_collector_count);
//				exh.add_collector(nc);
				
				final JMenu collector_menu = new JMenu("Collector "
						+ m_collector_count);
				collectors.add(collector_menu);
				
				JMenuItem add_exception = new JMenuItem("Add Exception");
				collector_menu.add(add_exception);
				add_exception.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						nc.collect(new Exception(
								RandomStringUtils.randomAlphabetic(20)),
								RandomStringUtils.randomNumeric(5));
					}
				});
				
				JMenuItem remove_collector = new JMenuItem("Remove "
						+ "Collector " + m_collector_count);
				collector_menu.add(remove_collector);
				remove_collector.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						collectors.remove(collector_menu);
//						exh.remove_collector(nc);
					}
				});
			}
		});
		
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		f.pack();
		f.setVisible(true);
	}
}
