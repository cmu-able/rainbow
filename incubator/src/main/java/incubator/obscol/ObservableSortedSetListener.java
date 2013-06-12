package incubator.obscol;

public interface ObservableSortedSetListener<E> {
	public void elementAdded(E e, int idx);
	public void elementRemoved(E e, int idx);
	public void setCleared();
}
