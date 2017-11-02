package incubator.obscol;

import java.util.Set;

public class SetToSetSynchronizer<E> {
	 public SetToSetSynchronizer(ObservableSet<E> s, final Set<E> copy) {
		 if (s == null) {
			 throw new IllegalArgumentException("s == null");
		 }
		 
		 if (copy == null) {
			 throw new IllegalArgumentException("copy == null");
		 }
		 
		 s.addObservableSetListener(new ObservableSetListener<E>() {
			@Override
			public void elementAdded(E e) {
				copy.add(e);
			}

			@Override
			public void elementRemoved(E e) {
				copy.remove(e);
			}

			@Override
			public void setCleared() {
				copy.clear();
			}
		 });
		 
		 copy.addAll(s);
	 }
}
