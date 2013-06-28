package incubator.scb;

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
