package incubator.obscol;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public class DelegateObservableSortedSet<E> implements ObservableSortedSet<E> {
	private ObservableSortedSet<E> delegate;
	
	public DelegateObservableSortedSet(ObservableSortedSet<E> delegate) {
		assert delegate != null;
		
		this.delegate = delegate;
	}

	@Override
	public void addObservableSortedSetListener(
			ObservableSortedSetListener<E> l) {
		delegate.addObservableSortedSetListener(l);
	}

	@Override
	public void removeObservableSortedSetListener(
			ObservableSortedSetListener<E> l) {
		delegate.removeObservableSortedSetListener(l);
	}

	@Override
	public Comparator<? super E> comparator() {
		return delegate.comparator();
	}

	@Override
	public E first() {
		return delegate.first();
	}

	@Override
	public SortedSet<E> headSet(E toElement) {
		return delegate.headSet(toElement);
	}

	@Override
	public E last() {
		return delegate.last();
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		return delegate.subSet(fromElement, toElement);
	}

	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return delegate.tailSet(fromElement);
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
