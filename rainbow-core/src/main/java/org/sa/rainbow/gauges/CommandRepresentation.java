package org.sa.rainbow.gauges;

import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

public class CommandRepresentation implements IRainbowModelCommandRepresentation {

    private String[] m_parameters;
    private String   m_label;
    private String   m_target;
    private String   m_commandName;
    private String   m_modelName;
    private String   m_modelType;

    public CommandRepresentation (IRainbowModelCommandRepresentation cmd) {
        m_label = cmd.getLabel ();
        m_parameters = new String[cmd.getParameters ().length];
        String[] parameters = cmd.getParameters ();
        for (int i = 0; i < parameters.length; i++) {
            m_parameters[i] = parameters[i];
        }
        m_target = cmd.getTarget ();
        m_commandName = cmd.getCommandName ();
        m_modelName = cmd.getModelName ();
        m_modelType = cmd.getModelType ();

    }

    @Override
    public String getLabel () {
        return m_label;
    }

    @Override
    public String[] getParameters () {
        return m_parameters;
    }

    @Override
    public String getTarget () {
        return m_target;
    }

    @Override
    public String getCommandName () {
        return m_commandName;
    }

    @Override
    public String getModelName () {
        return m_modelName;
    }

    @Override
    public String getModelType () {
        return m_modelType;
    }

}
