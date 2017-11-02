package incubator.qxt;

import javax.swing.*;

/**
 * Interface implemented by classes that are used to choose an icon
 * corresponding to the status of a row.
 * 
 * @param <T> the row type
 */
public interface StatusIconChooser<T> {
	/**
	 * Choose an icon corresponding to the status of the row.
	 * 
	 * @param t the row value (maybe <code>null</code> if the row is an
	 * empty row)
	 * @param type the status type
	 * 
	 * @return the icon to show or <code>null</code> if none
	 */
	Icon chooseIcon(T t, StatusType type);

	/**
	 * Types of row status.
	 */
	enum StatusType {
		/**
		 * The row is empty.
		 */
		EMPTY,

		/**
		 * The row corresponds to a new, uncommitted row.
		 */
		NEW,

		/**
		 * The row is being edited.
		 */
		EDITING,

		/**
		 * The row is a committed, unchanged row.
		 */
		NORMAL,

		/**
		 * The row has been changed and an installed validator states that
		 * the row is not valid.
		 */
		INVALID
	}
}
