package incubator.obscol;

import java.util.Iterator;

public class ImmutableIterator<E> extends DelegateIterator<E> {
	public ImmutableIterator(Iterator<E> iterator) {
		super(iterator);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot change an immutable "
				+ "collection.");
	}
}
