package org.sa.rainbow.model.acme.znn.commands;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.IRainbowModelCommand;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelCommand;

public class ZNNCommandFactory extends ModelCommandFactory {

    protected Map<String, Class<? extends ZNNAcmeModelCommand<?>>> m_commandMap = new HashMap<> ();

    private IModelInstance<IAcmeSystem> m_modelInstance;

    public static ZNNLoadModelCommand loadCommand (ModelsManager modelsManager,
            String modelName,
            InputStream stream,
            String source) {
        return new ZNNLoadModelCommand (modelName, modelsManager, stream, source);
    }

    public ZNNCommandFactory (IModelInstance<IAcmeSystem> modelInstance) {
        m_modelInstance = modelInstance;
        fillInCommandMap ();
    }

    protected void fillInCommandMap () {
        m_commandMap.put ("setResponseTime", SetResponseTimeCmd.class);
        m_commandMap.put ("setLoad", SetLoadCmd.class);
        m_commandMap.put ("connectServer", NewServerCmd.class);
    }


    public SetResponseTimeCmd setResponseTimeCmd (IAcmeComponent client, float rt) {
        assert client.declaresType ("ClientT");
        if (ModelHelper.getAcmeSystem (client) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        SetResponseTimeCmd cmd = new SetResponseTimeCmd ("setResponseTime", m_modelInstance,
                client.getQualifiedName (), Float.toString (rt));
        return cmd;
    }

    public SetLoadCmd setLoadCmd (IAcmeComponent server, float load) {
        assert server.declaresType ("ServerT") || server.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (server) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new SetLoadCmd ("setLoad", m_modelInstance, server.getQualifiedName (),
                Float.toString (load));
    }

    public AcmeModelCommand<IAcmeComponent> connectNewServerCmd (IAcmeComponent proxy, String name) {
        assert proxy.declaresType ("ProxyT");
        if (ModelHelper.getAcmeSystem (proxy) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException (
                    "Cannot create a command for a component that is not part of the system");
        return new NewServerCmd ("connectServer", m_modelInstance, proxy.getQualifiedName (), name);
    }

    @Override
    public IRainbowModelCommand generateCommand (String commandName, String... args) throws RainbowModelException {
        try {
            Class<? extends ZNNAcmeModelCommand<?>> cmdClass = m_commandMap.get (commandName);
            Constructor<? extends ZNNAcmeModelCommand<?>>[] constructors = (Constructor<? extends ZNNAcmeModelCommand<?>>[] )cmdClass
                    .getConstructors ();
            Constructor<? extends ZNNAcmeModelCommand<?>> constructor = null;
            for (Constructor<? extends ZNNAcmeModelCommand<?>> c : constructors) {
                Class<?>[] parameterTypes = c.getParameterTypes ();
                if (Arrays.equals (new Class<?>[] { String.class, IModelInstance.class, String.class },
                        Arrays.copyOfRange (parameterTypes, 0, 3))
                        && parameterTypes.length == 2 + args.length) {
                    constructor = c;
                    break;
                }
            }
            if (constructor == null) throw new NoSuchMethodException ();
            Object[] cargs = new Object[2 + args.length];
            cargs[0] = commandName;
            cargs[1] = m_modelInstance;
            for (int i = 0; i < args.length; i++) {
                cargs[2 + i] = args[i];
            }
            ZNNAcmeModelCommand<?> cmd = constructor.newInstance (cargs/*commandName, m_modelInstance.getModelInstance (),
                                                                       args*/);
            return cmd;
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RainbowModelException ("Cannot create a command for the commandName: " + commandName, e);
        }
    }



}
