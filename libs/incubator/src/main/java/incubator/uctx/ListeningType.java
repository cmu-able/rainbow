package incubator.uctx;

/**
 * <p>
 * When a listener is registered for listening to a context, irrespectively of
 * listening to just a key or the whole context, it has a listening type. This
 * type defines how deep changes can occur and the listener still be informed.
 * </p>
 * <p>
 * Changes to object's contents are only supported on the following object
 * types:
 * <ul>
 * <li>Beans that support bound properties (beans that support property
 * listeners).</li>
 * <li>Observable collectons from the <code>obscol</code> package.</li>
 * </ul>
 * </p>
 */
public enum ListeningType {
	/**
	 * Only inform the listener if the object under the key changes (not the
	 * object's contents, just the reference).
	 */
	SIMPLE,
	
	/**
	 * Inform the listener if the object under the key changes or its contents
	 * changes.
	 */
	CONTENTS
}
