package incubator.obscol;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;

public class ImmutableObservableSortedSet<E>
		extends DelegateObservableSortedSet<E> {
	public ImmutableObservableSortedSet(ObservableSortedSet<E> delegate) {
		super(delegate);
	}

	@Override
	public SortedSet<E> headSet(E toElement) {
		return Collections.unmodifiableSortedSet(super.headSet(toElement));
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		return Collections.unmodifiableSortedSet(super.subSet(fromElement,
				toElement));
	}

	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return Collections.unmodifiableSortedSet(super.tailSet(fromElement));
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException("Cannot modify immutable "
				+ "object.");
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException("Cannot modify immutable "
				+ "object.");
	}
	
	@Override
	public Iterator<E> iterator() {
		return new ImmutableIterator<E>(super.iterator());
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Cannot modify immutable "
				+ "object.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Cannot modify immutable "
				+ "object.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Cannot modify immutable "
				+ "object.");
	}
}
