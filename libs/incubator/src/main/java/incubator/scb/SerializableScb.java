package incubator.scb;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Serializable;

/**
 * Abstract implementation of an SCB that is serializable.
 * @param <T> the bean type
 */
public abstract class SerializableScb<T extends SerializableScb<T>>
		implements Scb<T>, Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Notification dispatcher.
	 */
	private transient LocalDispatcher<ScbUpdateListener<T>> m_dispatcher;
	
	/**
	 * Creates a new bean.
	 */
	public SerializableScb() {
		m_dispatcher = new LocalDispatcher<>();
	}
	
	@Override
	public Dispatcher<ScbUpdateListener<T>> dispatcher() {
		return m_dispatcher;
	}
	
	@SuppressWarnings("javadoc")
	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		Ensure.not_null(in);
		in.defaultReadObject();
		m_dispatcher = new LocalDispatcher<>();
	}
	
	/**
	 * Notifies all listeners that the SCB has been updated.
	 */
	protected void fire_update() {
		m_dispatcher.dispatch(new DispatcherOp<ScbUpdateListener<T>>() {
			@Override
			public void dispatch(ScbUpdateListener<T> l) {
				l.updated(this_as_t());
			}
		});
	}
	
	/**
	 * Obtains <code>this</code> cast as an object of type <code>T</code>. 
	 * @return <code>this</code>
	 */
	private T this_as_t() {
		return my_class().cast(this);
	}
	
	/**
	 * Obtains the class of this bean.
	 * @return the class of the bean
	 */
	protected abstract Class<T> my_class();
}
