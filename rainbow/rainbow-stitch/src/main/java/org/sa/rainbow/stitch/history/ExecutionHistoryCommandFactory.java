/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.stitch.history;

import java.io.InputStream;
import java.util.Map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionStateT;

public class ExecutionHistoryCommandFactory extends ModelCommandFactory<Map<String, ExecutionHistoryData>> {

    public static final String STRATEGY_EXECUTION_STATE_CMD = "strategyExecutionState";

    @LoadOperation
    public static AbstractLoadModelCmd<Map<String, ExecutionHistoryData>> loadCOmmand (ModelsManager modelsManager,
            String modelName,
            InputStream stream,
            String source) {
        return new ExecutionHistoryLoadCommand (modelsManager, modelName, stream, source);
    }

    public ExecutionHistoryCommandFactory (ExecutionHistoryModelInstance model) throws RainbowException {
        super (ExecutionHistoryModelInstance.class, model);
    }

    public AbstractRainbowModelOperation<ExecutionHistoryData, Map<String, ExecutionHistoryData>>
    recordTacticDurationCmd (String qualifiedName, long dur, boolean successful) {
        return new TacticDurationCommand ("recordTacticDuration", m_modelInstance, qualifiedName, Long.toString (dur),
                Boolean.toString (successful));
    }

    @Operation(name=STRATEGY_EXECUTION_STATE_CMD)
    public StrategyExecutionStateCommand strategyExecutionStateCommand (ModelReference ref, String qualifiedName,
            String type,
            ExecutionStateT newState,
            String data) {
        return new StrategyExecutionStateCommand (STRATEGY_EXECUTION_STATE_CMD, m_modelInstance, qualifiedName, ref.toString(), type,
                newState.toString (), data == null ? "" : data);
    }

    @Override
    public IRainbowModelOperation generateCommand (String commandName, String... args) throws RainbowModelException {
        switch (commandName) {
        case "recordTacticDuration":
            return new TacticDurationCommand (commandName, m_modelInstance, args[0], args[1], args[2]);
        case STRATEGY_EXECUTION_STATE_CMD:
            return new StrategyExecutionStateCommand (commandName, m_modelInstance, args[0], args[1], args[2], args[3], args[4]);
        }
        throw new RainbowModelException ("Cannot create a command for the operation: " + commandName);
    }

    @Override
    public AbstractSaveModelCmd<Map<String, ExecutionHistoryData>> saveCommand (String location)
            throws RainbowModelException {
        return null;
    }


}
