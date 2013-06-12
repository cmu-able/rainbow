package incubator.exh;

import incubator.scb.ScbContainer;
import incubator.scb.ui.ScbTableModel;

/**
 * Table model with throwable contexts.
 */
@SuppressWarnings("serial")
public class ThrowableContextScbTableModel
		extends ScbTableModel<ThrowableContext, ThrowableContextComparator> {
	/**
	 * Creates a new table model.
	 * @param container the container
	 */
	public ThrowableContextScbTableModel(
			ScbContainer<ThrowableContext> container) {
		super(container, new ThrowableContextComparator());
		
		add_field_auto(ThrowableContext.scbf_class());
		add_field_auto(ThrowableContext.scbf_message());
		add_field_auto(ThrowableContext.scbf_location());
		add_field_auto(ThrowableContext.scbf_when());
	}

}
