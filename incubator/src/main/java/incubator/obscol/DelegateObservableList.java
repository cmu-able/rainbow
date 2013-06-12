package incubator.obscol;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DelegateObservableList<E> implements ObservableList<E> {
	private ObservableList<E> delegate;

	public DelegateObservableList(ObservableList<E> delegate) {
		assert delegate != null;

		this.delegate = delegate;
	}

	@Override
	public void addObservableListListener(ObservableListListener<? super E> l) {
		delegate.addObservableListListener(l);
	}

	@Override
	public void removeObservableListListener(ObservableListListener<? super E> l) {
		delegate.removeObservableListListener(l);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		delegate.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		delegate.removePropertyChangeListener(pcl);
	}

	@Override
	public boolean add(E e) {
		return delegate.add(e);
	}

	@Override
	public void add(int index, E element) {
		delegate.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return delegate.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return delegate.addAll(index, c);
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
	public E get(int index) {
		return delegate.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return delegate.indexOf(o);
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
	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return delegate.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return delegate.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return delegate.remove(o);
	}

	@Override
	public E remove(int index) {
		return delegate.remove(index);
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
	public E set(int index, E element) {
		return delegate.set(index, element);
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return delegate.subList(fromIndex, toIndex);
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
