package incubator.obscol;

import java.util.Collection;
import java.util.Iterator;

public class DelegateObservableSet<E> implements ObservableSet<E> {
	private ObservableSet<E> delegate;
	
	public DelegateObservableSet(ObservableSet<E> delegate) {
		assert delegate != null;
		
		this.delegate = delegate;
	}

	@Override
	public void addObservableSetListener(ObservableSetListener<E> listener) {
		delegate.addObservableSetListener(listener);
	}

	@Override
	public void removeObservableSetListener(ObservableSetListener<E> listener) {
		delegate.removeObservableSetListener(listener);
	}

	@Override
	public boolean add(E e) {
		return delegate.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return delegate.addAll(c);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return delegate.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return delegate.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return delegate.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return delegate.retainAll(c);
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}
}
