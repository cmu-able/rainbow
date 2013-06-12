package incubator.obscol;

import java.util.Comparator;
import java.util.TreeSet;

public class SyncedSortedObservableSet<E> extends DelegateObservableSortedSet<E> {
	private ObservableSet<E> set;
	private ObservableSetListener<E> listener;
	
	public SyncedSortedObservableSet(ObservableSet<E> set,
			Comparator<E> comparator) {
		super(new WrapperObservableSortedSet<E>(new TreeSet<E>(comparator)));
		
		this.set = set;
		addAll(set);
		
		listener = new ObservableSetListener<E>() {
			@Override
			public void elementAdded(E e) {
				add(e);
			}

			@Override
			public void elementRemoved(E e) {
				remove (e);
			}

			@Override
			public void setCleared() {
				clear();
			}
		};
		set.addObservableSetListener(listener);
	}
	
	public void detachFromSet() {
		assert listener != null;
		set.removeObservableSetListener(listener);
		listener = null;
	}
}
