package incubator.obscol;

import java.util.SortedSet;

public interface ObservableSortedSet<E> extends SortedSet<E> {

	void addObservableSortedSetListener (
			ObservableSortedSetListener<E> l);

	void removeObservableSortedSetListener (
			ObservableSortedSetListener<E> l);
}