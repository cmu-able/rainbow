package incubator.obscol;

public interface ObservableListListener<E> {
	void elementAdded (E e, int idx);

	void elementRemoved (E e, int idx);

	void elementChanged (E oldE, E newE, int idx);

	void listCleared ();
}
