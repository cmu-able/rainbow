package incubator.obscol;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

public class ImmutableObservableList<E> extends DelegateObservableList<E> {
	public ImmutableObservableList(ObservableList<E> l) {
		super(l);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException("Cannot modify immutable list.");
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException("Cannot modify immutable list.");
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException("Cannot modify immutable list.");
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException("Cannot modify immutable list.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Cannot modify immutable list.");
	}

	@Override
	public Iterator<E> iterator() {
		return new ImmutableIterator<E>(super.iterator());
	}

	@Override
	public ListIterator<E> listIterator() {
		// TODO Auto-generated method stub
		return super.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		return super.listIterator(index);
	}

	@Override
	public E remove(int index) {
		// TODO Auto-generated method stub
		return super.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return super.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return super.removeAll(c);
	}

	@Override
	public void removeObservableListListener(ObservableListListener<? super E> l) {
		// TODO Auto-generated method stub
		super.removeObservableListListener(l);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return super.retainAll(c);
	}

	@Override
	public E set(int index, E element) {
		// TODO Auto-generated method stub
		return super.set(index, element);
	}
}
