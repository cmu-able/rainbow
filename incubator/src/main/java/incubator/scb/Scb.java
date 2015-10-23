package incubator.scb;

import incubator.dispatch.Dispatcher;

import java.util.List;

/**
 * Interface for a statically checked bean.
 * @param <T> the bean type
 */
public interface Scb<T extends Scb<T>> {
	/**
	 * Obtains the notification dispatcher.
	 * @return the dispatcher
	 */
	Dispatcher<ScbUpdateListener<T>> dispatcher ();
	
	/**
	 * Obtains all fields in the SCB. Normally, this method just delegates
	 * to a static method named <code>c_fields</code> by convention. Every
	 * field should also have a method to be obtained which should also
	 * delegate to a static method with the field name starting by
	 * <code>c_</code>.
	 * @return the list of all SCB fields
	 */
	List<ScbField<T, ?>> fields ();
}
