package incubator;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of a set backed by a list which does not require
 * <code>hashCode</code>.
 * @param <T> the type of object.
 */
public class ListSet<T> implements Set<T> {
	/**
	 * Tee list.
	 */
	private List<T> m_list;
	
	/**
	 * Creates a new, empty set.
	 */
	public ListSet() {
		m_list = new LinkedList<>();
	}
	
	/**
	 * Creates a new, empty set from a collection.
	 * @param col the collection
	 */
	public ListSet(Collection<T> col) {
		m_list = new LinkedList<>();
		for (T t : col) {
			m_list.add(t);
		}
	}
	
	@Override
	public boolean add(T t) {
		if (m_list.contains(t)) {
			return false;
		} else {
			m_list.add(t);
			return true;
		}
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean any = false;
		for (T t : c) {
			if (add(t)) {
				any = true;
			}
		}
		
		return any;
	}
	
	@Override
	public void clear() {
		m_list.clear();
	}
	
	@Override
	public boolean contains(Object obj) {
		return m_list.contains(obj);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return m_list.containsAll(c);
	}
	
	@Override
	public boolean equals(Object c) {
		if (c == null) {
			return false;
		}
		
		if (c == this) {
			return true;
		}
		
		if (!(c instanceof ListSet)) {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		ListSet<T> cc = (ListSet<T>) c;
		
		return m_list.equals(cc.m_list);
	}
	
	@Override
	public int hashCode() {
		return m_list.hashCode();
	}
	
	@Override
	public boolean isEmpty() {
		return m_list.isEmpty();
	}
	
	@Override
	public Iterator<T> iterator() {
		return m_list.iterator();
	}
	
	@Override
	public boolean remove(Object obj) {
		return m_list.remove(obj);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return m_list.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return m_list.retainAll(c);
	}
	
	@Override
	public int size() {
		return m_list.size();
	}
	
	@Override
	public Object[] toArray() {
		return m_list.toArray();
	}
	
	@Override
	public <U extends Object> U[] toArray(U[] array) {
		return m_list.toArray(array);
	}
}
