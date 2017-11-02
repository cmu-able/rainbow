package incubator.ui;

/**
 * Listener that is informed when there are changes in panel parts.
 */
public interface PanelPartListener {
	/**
	 * Part is no longer necessary.
	 */
	void part_dismissed();
}
