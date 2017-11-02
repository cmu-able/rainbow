package incubator.ui;

import javax.swing.event.ListSelectionEvent;

/**
 * Interface implemented by objects that want to be able to veto list
 * selection changes. This interface is used together with
 * {@link VetoableListSelectionSupport}.
 */
public interface VetoableListSelectionListener {
	/**
	 * The list selection has changed.
	 * 
	 * @param e the change event (<code>getValueIsAdjusting</code> is
	 * guaranteed to return <code>false</code>)
	 * 
	 * @return change is ok?
	 */
	boolean canChangeSelection(ListSelectionEvent e);
}
