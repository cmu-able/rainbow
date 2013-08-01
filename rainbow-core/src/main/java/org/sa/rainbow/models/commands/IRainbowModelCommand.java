package org.sa.rainbow.models.commands;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.ports.IRainbowModelChangeBusPort;

public interface IRainbowModelCommand<Type, Model> extends IRainbowModelCommandRepresentation {

    public boolean canExecute ();

    public boolean canUndo ();

    public boolean canRedo ();

    public Type execute (IModelInstance<Model> context) throws IllegalStateException, RainbowException;

    public Type redo () throws IllegalStateException, RainbowException;

    public Type undo () throws IllegalStateException, RainbowException;

    public void setEventAnnouncePort (IRainbowModelChangeBusPort announcPort);

    public void setModel (Model m);

    public List<? extends IRainbowMessage> getGeneratedEvents ();

}
