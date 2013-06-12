package incubator.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

/**
 * <p>
 * Class implementing support for vetoable list selection. Since list
 * selection models do not support vetoing selection changes, this class
 * adds veto support. It works by registering itself as a listener on the
 * list and informing its own listeners when a list selection change has
 * occurred. If a veto listener vetoes the selection change, the selection
 * is moved back to the old value (and it doesn't inform anyone). After all
 * veto listeners have been informed and if none has vetoed the change, all
 * normal listeners are then informed of the list selection change.
 * </p>
 * 
 * <p>
 * Please note that, technically, this class is a hack and may not work
 * correctly with other listeners directly placed on the list selection
 * model. Also note that this class expects the selection to be contiguous
 * and totally ignores everything concerning lead and anchor selections: it
 * assumes a simple plain selection model.
 * </p>
 */
public class VetoableListSelectionSupport {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(VetoableListSelectionSupport.class);

	/**
	 * The list selection model we're watching.
	 */
	private final ListSelectionModel model;

	/**
	 * Vetoable listeners.
	 */
	private final List<VetoableListSelectionListener> vetoables;

	/**
	 * Normal listeners.
	 */
	private final List<ListSelectionListener> listeners;

	/**
	 * This queue is used to inform veto listeners and, simultaneously, to
	 * correctly support reentrancy. More details on
	 * {@link VetoableListSelectionSupport#processChanges(ListSelectionEvent)}
	 * method.
	 */
	private final List<VetoableListSelectionListener> vetoQueue;

	/**
	 * This queue is used to inform listeners and, simultaneously, to
	 * correctly support reentrancy. More details on
	 * {@link VetoableListSelectionSupport#processChanges(ListSelectionEvent)}
	 * method.
	 */
	private final List<ListSelectionListener> queue;

	/**
	 * Are we currently vetoing a selection change?
	 */
	private boolean vetoing;

	/**
	 * Last known first selection index.
	 */
	private int first;

	/**
	 * Last known last selection index.
	 */
	private int last;

	/**
	 * Creates a new object.
	 * 
	 * @param lsm the list selection model we're observing
	 */
	public VetoableListSelectionSupport(ListSelectionModel lsm) {
		if (lsm == null) {
			throw new IllegalArgumentException("lsm == null");
		}

		model = lsm;
		vetoables = new ArrayList<>();
		listeners = new ArrayList<>();
		vetoing = false;
		queue = new ArrayList<>();
		vetoQueue = new ArrayList<>();

		lsm.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				assert e != null;

				LOGGER.debug("valueChanged(first=" + e.getFirstIndex()
						+ ",last=" + e.getLastIndex() + ",adjusting="
						+ e.getValueIsAdjusting() + ")");

				if (e.getValueIsAdjusting()) {
					return;
				}

				int mfirst = model.getMaxSelectionIndex();
				int mlast = model.getMaxSelectionIndex();
				LOGGER.trace("valueChanged: mfirst=" + mfirst + ",mlast="
						+ mlast);

				if (vetoing) {
					LOGGER.trace("valueChanged: vetoing==true");
					return;
				}

				if (mfirst == first && mlast == last) {
					LOGGER.trace("valueChanged: first & last unchanged");
					return;
				}

				processChanges(e);
			}
		});

		first = lsm.getMinSelectionIndex();
		last = lsm.getMaxSelectionIndex();
	}

	/**
	 * Adds a new vetoable listener.
	 * 
	 * @param vlsl the vetoable listener
	 */
	public void addVetoableListSelectionListener(
			VetoableListSelectionListener vlsl) {
		if (vlsl == null) {
			throw new IllegalArgumentException("vlsl == null");
		}

		vetoables.add(vlsl);
	}

	/**
	 * Removes a vetoable listener.
	 * 
	 * @param vlsl the vetoable listener
	 */
	public void removeVetoableListSelectionListener(
			VetoableListSelectionListener vlsl) {
		if (vlsl == null) {
			throw new IllegalArgumentException("vlsl == null");
		}

		vetoables.remove(vlsl);
	}

	/**
	 * Adds a listener (which will only be informed <em>if</em> none of the
	 * vetoable listeners vetoes the change).
	 * 
	 * @param lsl the listener
	 */
	public void addListSelectionListener(ListSelectionListener lsl) {
		if (lsl == null) {
			throw new IllegalArgumentException("lsl == null");
		}

		listeners.add(lsl);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param lsl the listener
	 */
	public void removeListSelectionListener(ListSelectionListener lsl) {
		if (lsl == null) {
			throw new IllegalArgumentException("lsl == null");
		}

		listeners.remove(lsl);
	}

	/**
	 * Processes the changes of a list selection event. This method will
	 * firstly inform all veto listeners and performs the veto canceling if
	 * any of them requires it. If not it canceled, all listeners to be
	 * informed are placed in queue and we iterate one by one to inform
	 * them. If we reenter the method, we clear the whole queue and inform
	 * all listeners. This guarantees that no listener will received
	 * outdated events.
	 * 
	 * @param e the event
	 */
	private void processChanges(ListSelectionEvent e) {
		LOGGER.debug("processChanges(e={" + e + "})");
		/*
		 * Clear the queues (for reentrancy).
		 */
		vetoQueue.clear();
		queue.clear();

		/*
		 * Check if the selection change is vetoed.
		 */
		vetoQueue.addAll(vetoables);
		while (vetoQueue.size() > 0) {
			VetoableListSelectionListener vlsl = vetoQueue.remove(vetoQueue
					.size() - 1);
			if (!vlsl.canChangeSelection(e)) {
				vetoChange();
				return;
			}
		}

		first = model.getMaxSelectionIndex();
		last = model.getMaxSelectionIndex();
		LOGGER.trace("processChanges: first=" + first + ",last=" + last);

		/*
		 * Inform everyone else.
		 */
		queue.clear();
		queue.addAll(listeners);
		while (queue.size() > 0) {
			ListSelectionListener lsl = queue.remove(queue.size() - 1);
			lsl.valueChanged(e);
		}
	}

	/**
	 * Vetoes the last change.
	 */
	private void vetoChange() {
		LOGGER.debug("vetoChange()");
		assert !vetoing;

		vetoing = true;
		try {
			model.setValueIsAdjusting(false);
			model.setSelectionInterval(first, last);
		} finally {
			vetoing = false;
		}
	}
}
