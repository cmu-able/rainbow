package incubator.qxt;

import javax.swing.JPopupMenu;

/**
 * Interface implemented by classes that provide popup menus for
 * <code>qxt</code>s.
 * 
 * @param <T> the bean type
 */
public interface PopupProvider<T> {
	/**
	 * Indicates whether this provider requires the line to be selected in
	 * order to trigger the popup. This is important because the popup
	 * trigger (usually the right-mouse click) might not trigger a selection
	 * change on the table and, consequently, the popup may appear on a
	 * non-selected row. By returning <code>true</code>, the table will try
	 * to switch selection before showing the popup menu and will not show
	 * the menu if selection cannot be changed.
	 * 
	 * @return should be popup be shown only on selected rows?
	 */
	boolean popupRequiresSelection();

	/**
	 * Obtains the menu to show if the user activates the popup trigger but
	 * the cursor is not over a row.
	 * 
	 * @return the menu or <code>null</code> if no menu should be shown
	 */
	JPopupMenu getNonRowMenu();

	/**
	 * Obtains the menu to show if the user activates the popup trigger over
	 * a table row.
	 * 
	 * @param t the bean associated with the row
	 * 
	 * @return the menu or <code>null</code> if no menu should be shown
	 */
	JPopupMenu getRowMenu(T t);
}
