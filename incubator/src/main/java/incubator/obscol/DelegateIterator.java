package incubator.obscol;

import java.util.Iterator;

public class DelegateIterator<E> implements Iterator<E> {
	private Iterator<E> delegate;
	
	public DelegateIterator(Iterator<E> delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}

	@Override
	public E next() {
		return delegate.next();
	}

	@Override
	public void remove() {
		delegate.remove();
	}
}
