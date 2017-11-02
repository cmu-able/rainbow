package incubator.efw;

/**
 * Enumeration defining what are the transaction requirements of a method. This
 * enumeration is used with the {@link Transaction} annotation.
 */
public enum TransactionRequirement {
	/**
	 * The method requires a transaction which may be already opened or not. If
	 * a new transaction is opened, it will be closed when the method
	 * terminates.
	 */
	REQUIRE_ANY,
	
	/**
	 * The method requires a new transaction and should be executed in this new
	 * transaction irrespectively of the transactions that may already be
	 * opened.
	 */
	REQUIRE_NEW
}
