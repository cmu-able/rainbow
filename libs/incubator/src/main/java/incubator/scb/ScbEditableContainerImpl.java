package incubator.scb;

import incubator.obscol.ObservableSet;
import incubator.obscol.ObservableSetListener;
import incubator.pval.Ensure;

/**
 * Implementation of an editable SCB container.
 * @param <T> the SCB type.
 */
public class ScbEditableContainerImpl<T extends Scb<T>>
		extends ScbContainerImpl<T> implements ScbEditableContainer<T> {
	/**
	 * Creates a new container.
	 */
	public ScbEditableContainerImpl() {
	}
	
	/**
	 * Creates a new container whose data is copied from an observable set.
	 * @param src the observable set which has the source data for this set;
	 * all changes to the set are reflected in this container
	 */
	public ScbEditableContainerImpl(ObservableSet<T> src) {
		Ensure.not_null(src);
		
		src.addObservableSetListener(new ObservableSetListener<T>() {
			@Override
			public void elementAdded(T e) {
				add_scb(e);
			}

			@Override
			public void elementRemoved(T e) {
				remove_scb(e);
			}

			@Override
			public void setCleared() {
				while (inner_set().size() > 0) {
					T t = inner_set().iterator().next();
					remove_scb(t);
				}
			}
		});
	}

	@Override
	public void add_scb(T t) {
		Ensure.not_null(t);
		inner_set().add(t);
	}

	@Override
	public void remove_scb(T t) {
		Ensure.not_null(t);
		inner_set().remove(t);
	}
}
