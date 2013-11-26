package incubator.ui;

import incubator.pval.Ensure;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Panel part that shows a progress task.
 */
@SuppressWarnings("serial")
public class ProgressTaskPanelPart extends PanelPart {
	/**
	 * The progress bar.
	 */
	private JProgressBar m_bar;
	
	/**
	 * Current text being shown;
	 */
	private JLabel m_text;
	
	/**
	 * Creates a new panel part.
	 * @param task the task to execute;
	 */
	public ProgressTaskPanelPart(ProgressTask task) {
		Ensure.not_null(task);
		
		m_bar = new JProgressBar();
		m_bar.setIndeterminate(true);
		Dimension p = m_bar.getPreferredSize();
		p.width = 100;
		m_bar.setPreferredSize(p);
		m_text = new JLabel();
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(m_bar);
		add(m_text);
		
		task.progress_disptacher().add(new ProgressListener() {
			@Override
			public void undefined() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_bar.setIndeterminate(true);
					}
				});
			}
			
			@Override
			public void text(final String text) {
				Ensure.not_null(text, "text == null");
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_text.setText(text);
					}
				});
			}
			
			@Override
			public void done() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						dismiss();
					}
				});
			}
			
			@Override
			public void defined(final int current, final int total) {
				Ensure.greater_equal(current, 0, "current < 0");
				Ensure.greater_equal(total, current, "total < current");
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_bar.setIndeterminate(false);
						m_bar.setMaximum(total);
						m_bar.setValue(current);
					}
				});
			}
		});
	}
}
