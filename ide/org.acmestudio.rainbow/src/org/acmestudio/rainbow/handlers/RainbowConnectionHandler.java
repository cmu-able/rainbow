package org.acmestudio.rainbow.handlers;

import java.io.IOException;

import org.acmestudio.rainbow.Rainbow;
import org.acmestudio.rainbow.model.events.RainbowModelEventListener;
import org.acmestudio.rainbow.ui.RainbowConnectionDialog;
import org.acmestudio.ui.EclipseHelper;
import org.acmestudio.ui.editor.diagram.DiagramEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class RainbowConnectionHandler extends AbstractHandler {
    /**
     * The constructor.
     */
    public RainbowConnectionHandler() {
    }

    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        RainbowModelEventListener listener = Rainbow.getRainbowListener();
        if (listener == null) {
            RainbowConnectionDialog connD = new RainbowConnectionDialog(Display.getDefault().getActiveShell());
            int result = connD.open();
            if (result == IDialogConstants.OK_ID) {
                Rainbow.s_rainbowListener = new RainbowModelEventListener(connD.getHost(), connD.getPort());
                DiagramEditor de = EclipseHelper.getCurrentDiagramEditor();
                if (de != null) {
                    try {
                        Rainbow.getRainbowListener().attachToSystem(de.getSystem().getName(), de.getSystem());
                    }
                    catch (IOException e) {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), "Connection error",
                                "Failed to connect to Rainbow. This shouldn't happen");

                    }
                }
            }
        }
        else {
            DiagramEditor de = EclipseHelper.getCurrentDiagramEditor();
            if (de != null) {
                try {
                    Rainbow.getRainbowListener().attachToSystem(de.getSystem().getName(), de.getSystem());
                }
                catch (IOException e) {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "Connection error",
                            "Failed to connect to Rainbow. This shouldn't happen");

                }
            }
        }
        return null;
    }

    @Override
    public boolean isEnabled () {
        return super.isEnabled() /* && Rainbow.getRainbowListener() == null */;
    }
}
