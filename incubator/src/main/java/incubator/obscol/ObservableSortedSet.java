package incubator.obscol;

import java.util.SortedSet;

public interface ObservableSortedSet<E> extends SortedSet<E> {

	public abstract void addObservableSortedSetListener(
			ObservableSortedSetListener<E> l);

	public abstract void removeObservableSortedSetListener(
			ObservableSortedSetListener<E> l);
}