package incubator.ui;

import incubator.pval.Ensure;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Panel that shows several different parts.
 */
@SuppressWarnings("serial")
public class MultipartStatusPanel extends JPanel {
	/**
	 * The panel part.
	 */
	private List<PanelPart> m_parts;
	
	/**
	 * Part separators. Each item in {@link #m_parts} has one separator with
	 * the same index in this list that visually comes after it in the panel.
	 * The last part does not have a separator.
	 */
	private List<JSeparator> m_separators;
	
	/**
	 * Creates a new panel.
	 */
	public MultipartStatusPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		m_parts = new ArrayList<>();
		m_separators = new ArrayList<>();
	}
	
	/**
	 * Adds a panel part.
	 * @param pp the panel part
	 */
	public void add_part(final PanelPart pp) {
		Ensure.not_null(pp, "pp == null");
		m_parts.add(pp);
		pp.dispatcher().add(new PanelPartListener() {
			@Override
			public void part_dismissed() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						dismiss(pp);
					}
				});
			}
		});
		
		JSeparator sep = null;
		if (m_parts.size() > 1) {
			sep = new JSeparator();
			m_separators.add(sep);
			add(sep);
		}
		
		add(pp);
	}
	
	/**
	 * Dismisses a panel part.
	 * @param pp the panel part
	 */
	private void dismiss(PanelPart pp) {
		Ensure.not_null(pp, "pp == null");
		Ensure.is_true(m_parts.contains(pp), "!m_parts.contains(pp)");
		
		int idx = m_parts.indexOf(pp);
		JSeparator rsep = null;
		if (idx < m_separators.size()) {
			rsep = m_separators.get(idx);
		} else if (m_parts.size() > 1) {
			Ensure.equals(idx, m_separators.size(), "The part must be the "
					+ "last one. Part index is " + idx + " and number of "
					+ "separators is " + m_separators.size());
			rsep = m_separators.get(idx - 1);
		}
		
		remove(pp);
		m_parts.remove(pp);
		if (rsep != null) {
			remove(rsep);
			m_separators.remove(rsep);
		}
	}
}
