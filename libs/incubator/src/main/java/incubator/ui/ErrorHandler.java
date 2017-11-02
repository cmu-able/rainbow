package incubator.ui;

import java.awt.Component;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

/**
 * Class that shows an error dialog.
 */
public class ErrorHandler {
	/**
	 * Utility class: no constructor.
	 */
	private ErrorHandler() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Shows an error dialog.
	 * 
	 * @param owner the owner of the dialog to show (can be <code>null</code>)
	 * @param throwable the error to show
	 */
	public static void showErrorDialog(Component owner, Throwable throwable) {
		String msg = throwable.getMessage();
		if (msg == null) {
			msg = throwable.getClass().getName();
		}

		ErrorInfo ei = new ErrorInfo("Error", msg, null, null, throwable, null,
				null);
		JXErrorPane.showDialog(owner, ei);
	}

	/**
	 * Shows an error dialog.
	 * 
	 * @param throwable the error to show
	 */
	public static void showErrorDialog(Throwable throwable) {
		showErrorDialog(null, throwable);
	}
}
