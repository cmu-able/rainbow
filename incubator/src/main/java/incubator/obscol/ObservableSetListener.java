package incubator.obscol;

public interface ObservableSetListener<E> {
	void elementAdded (E e);

	void elementRemoved (E e);

	void setCleared ();
}
