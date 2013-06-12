package incubator.obscol;

import java.util.List;

public class SortedSetToListSynchronizer<E> {
	public SortedSetToListSynchronizer(ObservableSortedSet<E> os,
			final List<E> l) {
		os.addObservableSortedSetListener(new ObservableSortedSetListener<E>() {
			@Override
			public void elementAdded(E e, int idx) {
				l.add(idx, e);
			}

			@Override
			public void elementRemoved(E e, int idx) {
				l.remove(idx);
			}

			@Override
			public void setCleared() {
				l.clear();
			}
		});
		
		l.addAll(os);
	}
}
