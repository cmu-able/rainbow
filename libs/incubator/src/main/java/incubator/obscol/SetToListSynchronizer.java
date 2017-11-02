package incubator.obscol;

import java.util.Comparator;
import java.util.List;

public class SetToListSynchronizer<T> {
	private List<T> list;
	private Comparator<T> comparator;
	
	public SetToListSynchronizer(ObservableSet<T> set, List<T> list,
			Comparator<T> comparator) {
		if (set == null) {
			throw new IllegalArgumentException("set == null");
		}
		
		if (list == null) {
			throw new IllegalArgumentException("list == null");
		}
		
		if (comparator == null) {
			throw new IllegalArgumentException("comparator == null");
		}
		
		this.list = list;
		this.comparator = comparator;
		
		for (T t : set) {
			addItem(t);
		}
		
		set.addObservableSetListener(new ObservableSetListener<T>() {
			@Override
			public void elementAdded(T e) {
				addItem(e);
			}

			@Override
			public void elementRemoved(T e) {
				SetToListSynchronizer.this.list.remove(e);
			}

			@Override
			public void setCleared() {
				SetToListSynchronizer.this.list.clear();
			}
		});
	}
	
	private void addItem(T t) {
		assert t != null;
		
		for (int i = 0; i < list.size(); i++) {
			int r = comparator.compare(t, list.get(i));
			if (r < 0) {
				list.add(i, t);
				return;
			}
		}
		
		list.add(t);
	}

}
