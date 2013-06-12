package incubator.qxt;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Class responsible to keep track of which bean is currently being changed
 * and notify all interested listeners of changes to the bean.
 * </p>
 * <p>
 * Changes to the bean are informed to listeners which use a sort of
 * two-phase commit protocol. Listeners are informed to try to commit and
 * any listener may inform that it failed to commit. If all listeners commit
 * everything ok, all listeners are informed that all listeners have
 * committed (and failure is not accepted at this point). Otherwise all
 * listeners are informed that commit failed.
 * </p>
 * 
 * @param <T> the bean type
 */
class ChangeController<T> {
	/**
	 * The bean currently being changed (<code>null</code> if none).
	 */
	private T inChange;

	/**
	 * All listeners interested in being informed of changes.
	 */
	private final List<Listener<T>> listeners;

	/**
	 * Creates a new controller.
	 */
	ChangeController() {
		inChange = null;
		listeners = new ArrayList<>();
	}

	/**
	 * Adds a listener to the controller.
	 * 
	 * @param l the listener to add
	 */
	void addListener(Listener<T> l) {
		assert l != null;

		listeners.add(l);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param l the listener to remove
	 */
	void removeListener(Listener<T> l) {
		assert l != null;

		listeners.remove(l);
	}

	/**
	 * Determines if we're currently changing a bean.
	 * 
	 * @return is a bean being changed?
	 */
	boolean isChanging() {
		return inChange != null;
	}

	/**
	 * Obtains the bean currently being changed.
	 * 
	 * @return the bean being changed or <code>null</code> if none
	 */
	T getInChange() {
		return inChange;
	}

	/**
	 * Starts changing a bean. We must not be currently changing a bean.
	 * 
	 * @param t the bean to change, cannot be <code>null</code>
	 */
	void startChanging(T t) {
		assert inChange == null;
		assert t != null;

		inChange = t;

		for (Listener<T> l : new ArrayList<>(listeners)) {
			l.changeStarting(t);
		}
	}

	/**
	 * Rolls back changes to the bean (it actually only informs that changes
	 * should be rolled back).
	 */
	void rollback() {
		assert inChange != null;

		T chobj = inChange;
		inChange = null;

		for (Listener<T> l : new ArrayList<>(listeners)) {
			l.changeRolledBack(chobj);
		}
	}

	/**
	 * Commits changes to the bean (it actually only informs that changes
	 * must be committed).
	 * 
	 * @return did commit succeed?
	 */
	boolean commit() {
		assert inChange != null;

		List<Listener<T>> cp = new ArrayList<>(listeners);

		/*
		 * Two-phase commit phase 1: inform all listeners to try to commit.
		 */
		boolean ok = true;
		for (Listener<T> l : cp) {
			ok &= l.tryCommit(inChange);
		}

		if (!ok) {
			/*
			 * Some listener failed to cancel everything.
			 */
			for (Listener<T> l : cp) {
				l.changeNotCommitted(inChange);
			}

			return false;
		}

		T chobj = inChange;
		inChange = null;

		/*
		 * Two-phase commit phase 2: inform all listeners that commit was
		 * successful.
		 */

		for (Listener<T> l : cp) {
			l.changeCommitted(chobj);
		}

		return true;
	}

	/**
	 * Interface implemented by classes that listen to the controller.
	 * 
	 * @param <T> bean type
	 */
	static interface Listener<T> {
		/**
		 * Tries to commit changes in the bean.
		 * 
		 * @param t the bean
		 * 
		 * @return could changes be committed successfully?
		 */
		boolean tryCommit(T t);

		/**
		 * Changes have been committed on a bean.
		 * 
		 * @param t the bean
		 */
		void changeCommitted(T t);

		/**
		 * Commits of changes on the bean were attempted but failed and the
		 * changes were not committed.
		 * 
		 * @param t the bean
		 */
		void changeNotCommitted(T t);

		/**
		 * Changes have been rolled back on a bean.
		 * 
		 * @param t the bean
		 */
		void changeRolledBack(T t);

		/**
		 * A bean is starting to change.
		 * 
		 * @param t the bean
		 */
		void changeStarting(T t);
	}
}
