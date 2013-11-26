package incubator.scb;

import incubator.obscol.ObservableSet;

/**
 * Container implementation that gives public access to the supporting inner
 * set.
 * @param <T> the SCB type
 */
public class ScbOpenContainerImpl<T extends Scb<T>>
		extends ScbContainerImpl<T> {
	/**
	 * Creates a new container implementation.
	 */
	public ScbOpenContainerImpl() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public synchronized ObservableSet<T> inner_set() {
		return super.inner_set();
	}
}
