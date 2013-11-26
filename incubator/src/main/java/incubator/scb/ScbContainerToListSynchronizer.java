package incubator.scb;

import incubator.pval.Ensure;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class that keeps a list updated with data from an {@link ScbContainer}.
 * @param <T> the SCB type
 */
public class ScbContainerToListSynchronizer<T extends Scb<T>> {
	/**
	 * The list to keep updated.
	 */
	private List<T> m_list;
	
	/**
	 * The comparator to sort list elements.
	 */
	private Comparator<T> m_comparator;
	
	/**
	 * Creates a new synchronizer.
	 * @param container the container that will be observed for changes
	 * @param list the list that will receive all changes made to the
	 * container
	 * @param comparator a comparator to keep the list sorted
	 */
	public ScbContainerToListSynchronizer(ScbContainer<T> container,
			List<T> list, Comparator<T> comparator) {
		Ensure.not_null(container, "container == null");
		Ensure.not_null(list, "list == null");
		Ensure.not_null(comparator, "comparator == null");
		
		m_list = list;
		m_comparator = comparator;
		
		container.dispatcher().add(new ScbContainerListener<T>() {
			@Override
			public void scb_added(T t) {
				Ensure.not_null(t, "t == null");
				synchronized (m_list) {
					int idx;
					for (idx = 0; idx < m_list.size(); idx++) {
						if (m_comparator.compare(m_list.get(idx), t) > 0) {
							break;
						}
					}
					
					m_list.add(idx, t);
				}
			}
			@Override
			public void scb_removed(T t) {
				Ensure.not_null(t);
				m_list.remove(t);
			}
			@Override
			public void scb_updated(T t) {
				/*
				 * Nothing to do.
				 */
			}
		});
		
		m_list.addAll(container.all_scbs());
		Collections.sort(m_list, comparator);
	}
}
