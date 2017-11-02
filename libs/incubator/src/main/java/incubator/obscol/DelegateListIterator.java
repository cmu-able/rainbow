package incubator.obscol;

import java.util.ListIterator;

public class DelegateListIterator<E> implements ListIterator<E> {
	private ListIterator<E> delegate;
	
	public DelegateListIterator(ListIterator<E> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void add(E e) {
		delegate.add(e);
	}

	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}

	@Override
	public boolean hasPrevious() {
		return delegate.hasPrevious();
	}

	@Override
	public E next() {
		return delegate.next();
	}

	@Override
	public int nextIndex() {
		return delegate.nextIndex();
	}

	@Override
	public E previous() {
		return delegate.previous();
	}

	@Override
	public int previousIndex() {
		return delegate.previousIndex();
	}

	@Override
	public void remove() {
		delegate.remove();
	}

	@Override
	public void set(E e) {
		delegate.set(e);
	}
}
