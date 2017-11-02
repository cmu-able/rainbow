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
package org.sa.rainbow.core.ports.eseb;

import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

class ESEBCommandHelper implements ESEBConstants {

    public static void command2Message (IRainbowOperation command, RainbowESEBMessage msg) {
        msg.setProperty (MODEL_NAME_KEY, command.getModelReference ().getModelName ());
        msg.setProperty (COMMAND_NAME_KEY, command.getName ());
        msg.setProperty (MODEL_TYPE_KEY, command.getModelReference ().getModelType ());
        msg.setProperty (COMMAND_TARGET_KEY, command.getTarget ());
        msg.setProperty (COMMAND_PARAMETER_KEY + "_size", command.getParameters ().length);
        for (int i = 0; i < command.getParameters ().length; i++) {
            msg.setProperty (COMMAND_PARAMETER_KEY + i, command.getParameters ()[i]);
        }
        if (command.getOrigin () != null) {
            msg.setProperty (ESEBConstants.COMMAND_ORIGIN, command.getOrigin ());
        }

    }

    public static void command2Message (IRainbowOperation command, RainbowESEBMessage msg, String suffix) {
        msg.setProperty (MODEL_NAME_KEY + suffix, command.getModelReference ().getModelName ());
        msg.setProperty (COMMAND_NAME_KEY + suffix, command.getName ());
        msg.setProperty (MODEL_TYPE_KEY + suffix, command.getModelReference ().getModelType ());
        msg.setProperty (COMMAND_TARGET_KEY + suffix, command.getTarget ());
        msg.setProperty (COMMAND_PARAMETER_KEY + suffix + "_size", command.getParameters ().length);
        for (int i = 0; i < command.getParameters ().length; i++) {
            msg.setProperty (COMMAND_PARAMETER_KEY + suffix + i, command.getParameters ()[i]);
        }
        if (command.getOrigin () != null) {
            msg.setProperty (ESEBConstants.COMMAND_ORIGIN, command.getOrigin ());
        }
    }


    public static IRainbowOperation msgToCommand (RainbowESEBMessage msg) {
        String modelName = (String )msg.getProperty (MODEL_NAME_KEY);
        String modelType = (String )msg.getProperty (MODEL_TYPE_KEY);
        String commandName = (String )msg.getProperty (COMMAND_NAME_KEY);
        String target = (String )msg.getProperty (COMMAND_TARGET_KEY);
        int numParams = (Integer )msg.getProperty (COMMAND_PARAMETER_KEY + "_size");
        String[] parameters = new String[numParams];
        for (int i = 0; i < numParams; i++) {
            parameters[i] = (String )msg.getProperty (COMMAND_PARAMETER_KEY + i);
        }
        OperationRepresentation or = new OperationRepresentation (commandName,
                new ModelReference (modelName, modelType), target, parameters);
        if (msg.hasProperty (COMMAND_ORIGIN)) {
            or.setOrigin ((String )msg.getProperty (COMMAND_ORIGIN));
        }
        return or;
    }

    public static IRainbowOperation msgToCommand (RainbowESEBMessage msg, String suffix) {
        String modelName = (String )msg.getProperty (MODEL_NAME_KEY + suffix);
        if (modelName == null) return null;
        String modelType = (String )msg.getProperty (MODEL_TYPE_KEY + suffix);
        String commandName = (String )msg.getProperty (COMMAND_NAME_KEY + suffix);
        String target = (String )msg.getProperty (COMMAND_TARGET_KEY + suffix);
        int numParams = (Integer )msg.getProperty (COMMAND_PARAMETER_KEY + suffix + "_size");
        String[] parameters = new String[numParams];
        for (int i = 0; i < numParams; i++) {
            parameters[i] = (String )msg.getProperty (COMMAND_PARAMETER_KEY + suffix + i);
        }
        OperationRepresentation or = new OperationRepresentation (commandName,
                new ModelReference (modelName, modelType), target, parameters);
        if (msg.hasProperty (COMMAND_ORIGIN)) {
            or.setOrigin ((String )msg.getProperty (COMMAND_ORIGIN));
        }
        else if (msg.hasProperty (COMMAND_ORIGIN + suffix)) {
            or.setOrigin ((String )msg.getProperty (COMMAND_ORIGIN + suffix));
        }
        return or;
    }

}
