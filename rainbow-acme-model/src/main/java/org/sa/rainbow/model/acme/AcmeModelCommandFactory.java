package org.sa.rainbow.model.acme;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public abstract class AcmeModelCommandFactory extends ModelCommandFactory<IAcmeSystem> {
    protected AcmeModelInstance                                 m_modelInstance;
    protected Map<String, Class<? extends AcmeModelOperation<?>>> m_commandMap = new HashMap<> ();

    public AcmeModelCommandFactory (AcmeModelInstance model) {
        m_modelInstance = model;
        fillInCommandMap ();
    }

    protected void fillInCommandMap () {
        m_commandMap.put ("setTypecheckResult".toLowerCase (), AcmeTypecheckSetCmd.class);
    }

    public AcmeTypecheckSetCmd acmeTypecheckSetCmd (boolean typechecks) {
        return new AcmeTypecheckSetCmd ("setTypecheckResult", m_modelInstance, "self", Boolean.toString (typechecks));
    }

    @Override
    public IRainbowModelOperation generateCommand (String commandName, String... args) throws RainbowModelException {
        try {
            Class<? extends AcmeModelOperation<?>> cmdClass = m_commandMap.get (commandName.toLowerCase ());
            if (cmdClass == null) {
                cmdClass = tryThroughReflection (commandName);
            }
            if (cmdClass == null)
                throw new RainbowModelException ("Cannot find a command that matches " + commandName);
            Constructor<? extends AcmeModelOperation<?>>[] constructors = (Constructor<? extends AcmeModelOperation<?>>[] )cmdClass
                    .getConstructors ();
            Constructor<? extends AcmeModelOperation<?>> constructor = null;
            for (Constructor<? extends AcmeModelOperation<?>> c : constructors) {
                Class<?>[] parameterTypes = c.getParameterTypes ();
                if (Arrays.equals (new Class<?>[] { String.class, AcmeModelInstance.class, String.class },
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
            AcmeModelOperation<?> cmd = constructor.newInstance (cargs/*commandName, m_modelInstance.getModelInstance (),
                                                                       args*/);
            return cmd;
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RainbowModelException ("Cannot create a command for the commandName: " + commandName, e);
        }
    }

    private Class<? extends AcmeModelOperation<?>> tryThroughReflection (String commandName) {
        Method[] methods = this.getClass ().getMethods ();
        for (Method method : methods) {
            if (method.getName ().equals (commandName + "Cmd"))
                return (Class<? extends AcmeModelOperation<?>> )method.getReturnType ();
        }
        return null;
    }

}
