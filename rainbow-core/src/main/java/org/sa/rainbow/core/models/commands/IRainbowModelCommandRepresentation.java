package org.sa.rainbow.core.models.commands;

public interface IRainbowModelCommandRepresentation {
    public String getLabel ();

    public abstract String[] getParameters ();

    public abstract String getTarget ();

    public abstract String getCommandName ();

    public String getModelName ();

    public String getModelType ();
}
