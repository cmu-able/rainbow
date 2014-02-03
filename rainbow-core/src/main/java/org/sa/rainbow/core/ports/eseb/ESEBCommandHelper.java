package org.sa.rainbow.core.ports.eseb;

import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public class ESEBCommandHelper implements ESEBConstants {

    public static void command2Message (IRainbowOperation command, RainbowESEBMessage msg) {
        msg.setProperty (MODEL_NAME_KEY, command.getModelName ());
        msg.setProperty (COMMAND_NAME_KEY, command.getName ());
        msg.setProperty (MODEL_TYPE_KEY, command.getModelType ());
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
        msg.setProperty (MODEL_NAME_KEY + suffix, command.getModelName ());
        msg.setProperty (COMMAND_NAME_KEY + suffix, command.getName ());
        msg.setProperty (MODEL_TYPE_KEY + suffix, command.getModelType ());
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
        OperationRepresentation or = new OperationRepresentation (commandName, modelName, modelType, target, parameters);
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
        OperationRepresentation or = new OperationRepresentation (commandName, modelName, modelType, target, parameters);
        if (msg.hasProperty (COMMAND_ORIGIN)) {
            or.setOrigin ((String )msg.getProperty (COMMAND_ORIGIN));
        }
        else if (msg.hasProperty (COMMAND_ORIGIN + suffix)) {
            or.setOrigin ((String )msg.getProperty (COMMAND_ORIGIN + suffix));
        }
        return or;
    }

}
