package incubator.ui;

/**
 * Interface implemented by objects that should be "pinged" regularly.
 */
public interface Pinged {
	/**
	 * A ping has been received.
	 */
	void ping ();
}
