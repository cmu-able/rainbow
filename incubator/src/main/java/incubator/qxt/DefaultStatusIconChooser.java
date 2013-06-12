package incubator.qxt;

import java.io.Serializable;

import javax.swing.Icon;

import incubator.ui.IconResourceLoader;

/**
 * Default implementation of the class responsible for choosing an icon to
 * show in the status column.
 * 
 * @param <T> the bean type
 */
class DefaultStatusIconChooser<T> implements StatusIconChooser<T>,
		Serializable {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The "editing" icon.
	 */
	private final Icon editingIcon;

	/**
	 * The "new" icon.
	 */
	private final Icon newIcon;

	/**
	 * The "invalid" icon.
	 */
	private final Icon invalidIcon;

	/**
	 * Creates a new chooser.
	 */
	DefaultStatusIconChooser() {
		editingIcon = IconResourceLoader.loadIcon(getClass(),
				"page_white_edit.png");
		newIcon = IconResourceLoader.loadIcon(getClass(),
				"page_white_put.png");
		invalidIcon = IconResourceLoader.loadIcon(getClass(), "cross.png");
	}

	@Override
	public Icon chooseIcon(T t, StatusType type) {
		switch (type) {
		case EDITING:
			return editingIcon;
		case NEW:
			return newIcon;
		case INVALID:
			return invalidIcon;
		default:
			break;
		}

		return null;
	}

}
