package incubator.exh;

/**
 * Collector that registers itself from the global collector
 * ({@link GlobalCollector}) and unregisters automatically upon garbage
 * collection. See {@link SoftCollectorProxy} for information on how this is
 * done.
 */
public class LocalCollector extends ThrowableCollector {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new local collector.
	 * @param name the collector name
	 */
	public LocalCollector(String name) {
		super(name);
		@SuppressWarnings("unused")
		SoftCollectorProxy scp = new SoftCollectorProxy(this);
	}
}
