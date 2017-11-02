package incubator.obscol;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

public class WrapperObservableSortedSet<E> implements ObservableSortedSet<E> {
	private final SortedSet<E> sortedSet;
	private final Set<ObservableSortedSetListener<E>> listeners;

	public WrapperObservableSortedSet(SortedSet<E> set) {
		assert set != null;
		sortedSet = set;
		listeners = new HashSet<ObservableSortedSetListener<E>>();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#addObservableSortedSetListener(ptm.dow.ObservableSortedSetListener)
	 */
	public void addObservableSortedSetListener(
			ObservableSortedSetListener<E> l) {
		assert l != null;

		listeners.add(l);
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#removeObservableSortedSetListener(ptm.dow.ObservableSortedSetListener)
	 */
	public void removeObservableSortedSetListener(
			ObservableSortedSetListener<E> l) {
		assert l != null;

		listeners.remove(l);
	}

	private void notifyElementAdded(E e, int idx) {
		assert e != null;
		assert idx >= 0;
		assert idx < size();

		for (ObservableSortedSetListener<E> l : new HashSet<ObservableSortedSetListener<E>>(
				listeners)) {
			l.elementAdded(e, idx);
		}
	}

	private void notifyElementRemoved(E e, int idx) {
		assert e != null;
		assert idx >= 0;
		assert idx <= size();

		for (ObservableSortedSetListener<E> l : new HashSet<ObservableSortedSetListener<E>>(
				listeners)) {
			l.elementRemoved(e, idx);
		}
	}

	private void notifyCleared() {
		assert size() == 0;

		for (ObservableSortedSetListener<E> l : new HashSet<ObservableSortedSetListener<E>>(
				listeners)) {
			l.setCleared();
		}
	}

	private int indexOf(E e) {
		int idx = 0;
		for (E ee : sortedSet) {
			if (ee != e) {
				idx++;
			} else {
				return idx;
			}
		}

		return -1;
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#comparator()
	 */
	@Override
	public Comparator<? super E> comparator() {
		return sortedSet.comparator();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#first()
	 */
	@Override
	public E first() {
		return sortedSet.first();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#headSet(E)
	 */
	@Override
	public SortedSet<E> headSet(E toElement) {
		return sortedSet.headSet(toElement);
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#last()
	 */
	@Override
	public E last() {
		return sortedSet.last();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#subSet(E, E)
	 */
	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		return sortedSet.subSet(fromElement, toElement);
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#tailSet(E)
	 */
	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return sortedSet.tailSet(fromElement);
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#add(E)
	 */
	@Override
	public boolean add(E e) {
		boolean ret;
		ret = sortedSet.add(e);

		int idx = indexOf(e);

		notifyElementAdded(e, idx);

		return ret;
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends E> collection) {
		boolean any = false;

		for (E e : collection) {
			if (add(e)) {
				any = true;
			}
		}

		return any;
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#clear()
	 */
	@Override
	public void clear() {
		sortedSet.clear();

		notifyCleared();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object e) {
		return sortedSet.contains(e);
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> collection) {
		return sortedSet.containsAll(collection);
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return sortedSet.isEmpty();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return sortedSet.iterator();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object e) {
		@SuppressWarnings("unchecked")
		E ee = (E) e;

		int idx = indexOf(ee);
		boolean ret = sortedSet.remove(e);
		if (idx != -1) {
			notifyElementRemoved(ee, idx);
		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean changed = false;

		for (Object o : collection) {
			if (remove(o)) {
				changed = true;
			}
		}

		return changed;
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> collection) {
		Set<E> copy = new HashSet<E>(sortedSet);
		copy.removeAll(collection);

		boolean changed = removeAll(copy);
		return changed;
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#size()
	 */
	@Override
	public int size() {
		return sortedSet.size();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#toArray()
	 */
	@Override
	public Object[] toArray() {
		return sortedSet.toArray();
	}

	/* (non-Javadoc)
	 * @see ptm.dow.ObservableSortedSet#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] ts) {
		return sortedSet.toArray(ts);
	}

}
