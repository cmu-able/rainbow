package incubator.ctxaction;

/**
 * Interface implemented by table models that provide a mapping between a
 * line and an object. It is allowed to establish the context defined by a
 * table.
 */
public interface RowContextTableModel {
	/**
	 * Obtains the object associated with a row.
	 * 
	 * @param row the row number
	 * 
	 * @return the associated object (may be <code>null</code>)
	 */
	Object getRowContextObject(int row);
}
