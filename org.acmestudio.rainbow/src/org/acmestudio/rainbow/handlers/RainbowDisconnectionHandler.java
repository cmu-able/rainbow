package org.acmestudio.rainbow.handlers;
import org.acmestudio.rainbow.Rainbow;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class RainbowDisconnectionHandler extends AbstractHandler {

    @Override
    public Object execute (ExecutionEvent event) throws ExecutionException {
        return null;
        //        Rainbow.getRainbowListener().disconnect();
    }


    @Override
    public boolean isEnabled () {
        return super.isEnabled() && Rainbow.getRainbowListener() != null;
    }

}
