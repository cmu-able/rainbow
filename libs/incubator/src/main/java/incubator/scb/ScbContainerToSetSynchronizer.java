package incubator.scb;

import incubator.pval.Ensure;

import java.util.Set;

/**
 * Class that keeps a set updated with data from an {@link ScbContainer}.
 * @param <T> the SCB type
 */
public class ScbContainerToSetSynchronizer<T extends Scb<T>> {
	/**
	 * The set to keep updated.
	 */
	private Set<T> m_set;
	
	/**
	 * Creates a new synchronizer.
	 * @param container the container that will be observed for changes
	 * @param set the set that will receive all changes made to the container
	 */
	public ScbContainerToSetSynchronizer(ScbContainer<T> container,
			Set<T> set) {
		Ensure.not_null(container, "container == null");
		Ensure.not_null(set, "set == null");
		
		m_set = set;
		
		container.dispatcher().add(new ScbContainerListener<T>() {
			@Override
			public void scb_added(T t) {
				Ensure.not_null(t);
				m_set.add(t);
			}
			@Override
			public void scb_removed(T t) {
				Ensure.not_null(t);
				m_set.remove(t);
			}
			@Override
			public void scb_updated(T t) {
				/*
				 * We don't care.
				 */
			}
		});
		
		set.addAll(container.all_scbs());
	}
}
