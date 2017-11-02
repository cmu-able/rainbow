package incubator.obscol;

import java.util.ListIterator;

public class ImmutableListIterator<E> extends DelegateListIterator<E> {
	public ImmutableListIterator(ListIterator<E> iterator) {
		super(iterator);
	}

	@Override
	public void add(E e) {
		throw new UnsupportedOperationException("Cannot change an immutable "
				+ "collection.");
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot change an immutable "
				+ "collection.");
	}

	@Override
	public void set(E e) {
		throw new UnsupportedOperationException("Cannot change an immutable "
				+ "collection.");
	}
}
