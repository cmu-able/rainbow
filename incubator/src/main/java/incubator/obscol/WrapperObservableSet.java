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
		this.listeners = new HashSet<>();
	}

	@Override
	public synchronized void addObservableSetListener(
			ObservableSetListener<E> listener) {
		assert listener != null;
		listeners.add(listener);
	}

	@Override
	public synchronized void removeObservableSetListener(
			ObservableSetListener<E> listener) {
		assert listener != null;
		listeners.remove(listener);
	}
	
	private synchronized void notifyAdded(E e) {
		for (ObservableSetListener<E> l : new HashSet<>(listeners)) {
			l.elementAdded(e);
		}
	}
	
	private synchronized void notifyRemoved(E e) {
		for (ObservableSetListener<E> l : new HashSet<>(listeners)) {
			l.elementRemoved(e);
		}
	}
	
	private synchronized void notifyCleared() {
		for (ObservableSetListener<E> l : new HashSet<>(listeners)) {
			l.setCleared();
		}
	}

	@Override
	public synchronized boolean add(E e) {
		boolean added = set.add(e);
		if (added) {
			notifyAdded(e);
		}
		
		return added;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends E> c) {
		boolean added = false;
		for (E e : c) {
			if (add(e)) {
				added = true;
			}
		}
		
		return added;
	}

	@Override
	public synchronized void clear() {
		set.clear();
		notifyCleared();
	}

	@Override
	public synchronized boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public synchronized boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public synchronized Iterator<E> iterator() {
		return set.iterator();
	}

	@Override
	public synchronized boolean remove(Object o) {
		boolean removed = set.remove(o);
		
		@SuppressWarnings("unchecked")
		E e = (E) o;
		
		if (removed) {
			notifyRemoved(e);
		}
		
		return removed;
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c) {
		boolean removed = false;
		
		for (Object o : c) {
			if (remove(o)) {
				removed = true;
			}
		}
		
		return removed;
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c) {
		Set<E> copy = new HashSet<>(set);
		copy.removeAll(c);
		return removeAll(c);
	}

	@Override
	public synchronized int size() {
		return set.size();
	}

	@Override
	public synchronized Object[] toArray() {
		return set.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}
}
