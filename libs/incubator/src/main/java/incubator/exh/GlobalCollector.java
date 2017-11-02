package incubator.exh;

import incubator.obscol.ImmutableObservableSet;
import incubator.obscol.ObservableSet;
import incubator.obscol.WrapperObservableSet;
import incubator.pval.Ensure;

import java.util.HashSet;

/**
 * The global collector keeps track of all local collectors. There is only
 * a single global collector. Keeping track of local collectors in a way that
 * doesn't keep references to them is somewhat tricky. What we do is use the
 * {@link SoftCollectorProxy} class to handle proxying.
 */
public class GlobalCollector {
	/**
	 * The singleton instance.
	 */
	private static GlobalCollector m_instance;
	
	/**
	 * The set of all proxy collectors.
	 */
	private ObservableSet<ThrowableCollector> m_proxy_collectors;
	
	/**
	 * Private constructor.
	 */
	private GlobalCollector() {
		m_proxy_collectors = new WrapperObservableSet<>(
				new HashSet<ThrowableCollector>());
	}
	
	/**
	 * Obtains the global collector.
	 * @return the global collector
	 */
	public static synchronized GlobalCollector instance() {
		if (m_instance == null) {
			m_instance = new GlobalCollector();
		}
		
		return m_instance;
	}
	
	/**
	 * Adds a proxy collector.
	 * @param collector the collector to add
	 */
	synchronized void add_proxy_collector(ThrowableCollector collector) {
		Ensure.not_null(collector, "collector == null");
		Ensure.is_false(m_proxy_collectors.contains(collector), "Collector "
				+ "already added");
		m_proxy_collectors.add(collector);
	}
	
	/**
	 * Removes a proxy collector.
	 * @param collector the collector to remove
	 */
	synchronized void remove_proxy_collector(ThrowableCollector collector) {
		Ensure.not_null(collector, "collector == null");
		Ensure.is_true(m_proxy_collectors.remove(collector), "Collector not "
				+ "found");
	}
	
	/**
	 * Obtains the set of all collectors.
	 * @return all collectors
	 */
	public ImmutableObservableSet<ThrowableCollector> collectors() {
		return new ImmutableObservableSet<>(m_proxy_collectors);
	}
}
