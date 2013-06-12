package incubator.obscol;

import java.util.Collection;
import java.util.Iterator;

public class ImmutableObservableSet<E> extends DelegateObservableSet<E> {
	public ImmutableObservableSet(ObservableSet<E> set) {
		super(set);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException("Cannot modify immutable set.");
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException("Cannot modify immutable set.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Cannot modify immutable set.");
	}

	@Override
	public Iterator<E> iterator() {
		return new ImmutableIterator<E>(super.iterator());
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Cannot modify immutable set.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Cannot modify immutable set.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Cannot modify immutable set.");
	}
}
