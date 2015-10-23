package incubator.obscol;

public interface ObservableSortedSetListener<E> {
	void elementAdded (E e, int idx);

	void elementRemoved (E e, int idx);

	void setCleared ();
}
