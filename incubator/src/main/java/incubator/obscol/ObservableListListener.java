package incubator.obscol;

public interface ObservableListListener<E> {
	public void elementAdded(E e, int idx);
	public void elementRemoved(E e, int idx);
	public void elementChanged(E oldE, E newE, int idx);
	public void listCleared();
}
