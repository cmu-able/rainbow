package incubator.obscol;

import java.util.Set;

public interface ObservableSet<E> extends Set<E> {
	public void addObservableSetListener(ObservableSetListener<E> listener);
	public void removeObservableSetListener(ObservableSetListener<E> listener);
}
