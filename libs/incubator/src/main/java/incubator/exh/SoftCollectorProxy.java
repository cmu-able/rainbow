package incubator.exh;

import incubator.pval.Ensure;
import incubator.scb.ScbContainerListener;

/**
 * In order to allow local collectors to register and unregister automatically
 * from the global collector upon garbage collection we use the soft collector
 * proxy. This class creates a throwable collector proxy for each
 * local collector. A proxy is essentially a collector in which throwables are
 * added and removed directly (by using the
 * {@link ThrowableCollector#collect(ThrowableContext)} and
 * {@link ThrowableCollector#uncollect(ThrowableContext)} methods) bypassing
 * list sizing controls. Since the soft collector proxy keeps the references
 * to both the proxy and the local collector. However, because the proxy
 * does not keep a reference to the proxy, the garbage collector is able
 * to collect the local collector and this soft collector proxy when the local
 * collector is garbage collected. When finalized, this method will unregister
 * the proxy from the global collector.
 */
class SoftCollectorProxy {
	/**
	 * The proxy collector.
	 */
	private ThrowableCollector m_proxy;
	
	/**
	 * Creates a new collector proxy.
	 * @param collector the local collector
	 */
	SoftCollectorProxy(ThrowableCollector collector) {
		Ensure.not_null(collector, "collector == null");
		
		m_proxy = new ThrowableCollector(collector.name());
		
		collector.dispatcher().add(
				new ScbContainerListener<ThrowableContext>() {
			@Override
			public void scb_updated(ThrowableContext t) {
				/*
				 * This never happens because ThrowableContext objects
				 * are immutable. 
				 */
			}
			
			@Override
			public void scb_removed(ThrowableContext t) {
				m_proxy.uncollect(t);
			}
			
			@Override
			public void scb_added(ThrowableContext t) {
				m_proxy.collect(t);
			}
		});
		
		GlobalCollector.instance().add_proxy_collector(m_proxy);
	}
	
	@Override
	public void finalize() throws Throwable {
		GlobalCollector.instance().remove_proxy_collector(m_proxy);
	}
}
