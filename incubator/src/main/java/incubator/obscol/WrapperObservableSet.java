package incubator.obscol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WrapperObservableSet<E> implements ObservableSet<E> {
	private Set<E> set;
	private Set<ObservableSetListener<E>> listeners;
	
	public WrapperObservableSet(Set<E> set) {
		assert set != null;
		
		this.set = set;
		this.listeners = new HashSet<ObservableSetListener<E>>();
	}

	@Override
	public void addObservableSetListener(ObservableSetListener<E> listener) {
		assert listener != null;
		listeners.add(listener);
	}

	@Override
	public void removeObservableSetListener(ObservableSetListener<E> listener) {
		assert listener != null;
		listeners.remove(listener);
	}
	
	private void notifyAdded(E e) {
		for (ObservableSetListener<E> l
				: new HashSet<ObservableSetListener<E>>(listeners)) {
			l.elementAdded(e);
		}
	}
	
	private void notifyRemoved(E e) {
		for (ObservableSetListener<E> l
				: new HashSet<ObservableSetListener<E>>(listeners)) {
			l.elementRemoved(e);
		}
	}
	
	private void notifyCleared() {
		for (ObservableSetListener<E> l
				: new HashSet<ObservableSetListener<E>>(listeners)) {
			l.setCleared();
		}
	}

	@Override
	public boolean add(E e) {
		boolean added = set.add(e);
		if (added) {
			notifyAdded(e);
		}
		
		return added;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean added = false;
		for (E e : c) {
			if (add(e)) {
				added = true;
			}
		}
		
		return added;
	}

	@Override
	public void clear() {
		set.clear();
		notifyCleared();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return set.iterator();
	}

	@Override
	public boolean remove(Object o) {
		boolean removed = set.remove(o);
		
		@SuppressWarnings("unchecked")
		E e = (E) o;
		
		if (removed) {
			notifyRemoved(e);
		}
		
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean removed = false;
		
		for (Object o : c) {
			if (remove(o)) {
				removed = true;
			}
		}
		
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Set<E> copy = new HashSet<E>(set);
		copy.removeAll(c);
		return removeAll(c);
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}
}
