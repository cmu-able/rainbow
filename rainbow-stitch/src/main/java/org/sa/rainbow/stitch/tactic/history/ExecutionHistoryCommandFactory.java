package org.sa.rainbow.stitch.tactic.history;

import java.io.InputStream;
import java.util.Map;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;

public class ExecutionHistoryCommandFactory extends ModelCommandFactory<Map<String, ExecutionHistoryData>> {

    public static AbstractLoadModelCmd<Map<String, ExecutionHistoryData>> loadCOmmand (ModelsManager modelsManager,
            String modelName,
            InputStream stream,
            String source) {
        return new ExecutionHistoryLoadCommand (modelsManager, modelName, stream, source);
    }

    private ExecutionHistoryModelInstance m_modelInstance;

    public ExecutionHistoryCommandFactory (ExecutionHistoryModelInstance model) {
        m_modelInstance = model;
    }

    public TacticDurationCommand recordTacticDurationCmd (String qualifiedName, long dur) {
        return new TacticDurationCommand ("recordTacticDuration", m_modelInstance, qualifiedName, Long.toString (dur));
    }

    @Override
    public IRainbowModelOperation generateCommand (String commandName, String... args) throws RainbowModelException {
        switch (commandName) {
        case "recordTacticDuration":
            return new TacticDurationCommand (commandName, m_modelInstance, args[0], args[1]);
        }
        throw new RainbowModelException ("Cannot create a command for the operation: " + commandName);
    }

}
