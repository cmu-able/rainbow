package incubator.obscol;

public interface ObservableSetListener<E> {
	public void elementAdded(E e);
	public void elementRemoved(E e);
	public void setCleared();
}
