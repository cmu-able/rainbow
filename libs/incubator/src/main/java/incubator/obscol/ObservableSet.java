package incubator.obscol;

import java.util.Set;

public interface ObservableSet<E> extends Set<E> {
	void addObservableSetListener (ObservableSetListener<E> listener);

	void removeObservableSetListener (ObservableSetListener<E> listener);
}
