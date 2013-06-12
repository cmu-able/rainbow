package incubator.ctxaction;

import incubator.ui.AutoUpdateJComboBox;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Class which is able to keep an action context synchronized with the selected
 * item on a combo box. The class will keep a value on the context defined if
 * any item is selected. The key is defined in this class and the value is the
 * object provided by the model.
 */
public class ActionContextComboBoxSynchronizer {
	/**
	 * The combo box.
	 */
	private final AutoUpdateJComboBox<?> comboBox;

	/**
	 * The context to synchronize.
	 */
	private final ActionContext context;

	/**
	 * Context key to keep updated.
	 */
	private final String key;

	/**
	 * Creates a new synchronizer.
	 * 
	 * @param comboBox the combo box
	 * @param context the action context to keep synchronized
	 * @param key the context key to use
	 */
	public ActionContextComboBoxSynchronizer(AutoUpdateJComboBox<?> comboBox,
			ActionContext context, String key) {
		if (comboBox == null) {
			throw new IllegalArgumentException("comboBox == null");
		}

		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}

		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}

		this.comboBox = comboBox;
		this.context = context;
		this.key = key;

		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				synchronize();
			}
		});

		synchronize();
	}

	/**
	 * Synchronizes the context with the table selection.
	 */
	private void synchronize() {
		Object selected = comboBox.getSelected();
		context.set(key, selected);
	}

	/**
	 * This method does nothing but prevents checkstyle from complaining.
	 */
	public void dummy() {
		/*
		 * No code here. Dummy method.
		 */
	}
}
