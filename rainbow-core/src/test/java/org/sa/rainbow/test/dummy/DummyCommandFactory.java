package org.sa.rainbow.test.dummy;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.ModelsManager;
import org.sa.rainbow.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.models.commands.IRainbowModelCommand;
import org.sa.rainbow.models.commands.ModelCommandFactory;
import org.sa.rainbow.models.ports.IRainbowMessageFactory;
import org.sa.rainbow.models.ports.IRainbowModelChangeBusPort;

public class DummyCommandFactory extends ModelCommandFactory<Integer> {
    public static AbstractLoadModelCmd loadCommand (ModelsManager modelsManager,
            final String modelName,
            InputStream stream,
            String source) {
        return new AbstractLoadModelCmd<Integer> ("load", modelsManager, modelName, stream, source) {

            @Override
            public String getModelName () {
                return modelName;
            }

            @Override
            public String getModelType () {
                return "Acme";
            }


            @Override
            protected void subExecute () throws RainbowException {
                doPostExecute ();
            }

            @Override
            protected void subRedo () throws RainbowException {

            }

            @Override
            protected void subUndo () throws RainbowException {

            }

            @Override
            public IModelInstance<Integer> getResult () {
                return new IModelInstance<Integer> () {

                    @Override
                    public Integer getModelInstance () {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public void setModelInstance (Integer model) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public IModelInstance<Integer> copyModelInstance (String newName) throws RainbowCopyException {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String getModelType () {
                        return "Acme";
                    }

                    @Override
                    public String getModelName () {
                        return modelName;
                    }

                    @Override
                    public ModelCommandFactory<Integer> getCommandFactory () {
                        return new DummyCommandFactory ();
                    }
                };
            }

            @Override
            protected boolean checkModelValidForCommand (Object model) {
                return true;
            }
        };
    }
    @Override
    public IRainbowModelCommand generateCommand (String commandName, final String... args)
            throws RainbowModelException {
        return new IRainbowModelCommand<Integer, Integer> () {

            private IRainbowModelChangeBusPort m_announcPort;
            private Integer                    m_m;

            @Override
            public String getLabel () {
                return "load";
            }

            @Override
            public String[] getParameters () {
                return new String[0];
            }

            @Override
            public String getTarget () {
                return "load";
            }

            @Override
            public String getCommandName () {
                return "load";
            }

            @Override
            public String getModelName () {
                return "test";
            }

            @Override
            public String getModelType () {
                return "test";
            }

            @Override
            public boolean canExecute () {
                return true;
            }

            @Override
            public boolean canUndo () {
                return false;
            }

            @Override
            public boolean canRedo () {
                return false;
            }

            @Override
            public List<? extends IRainbowMessage> execute (IModelInstance<Integer> context, IRainbowMessageFactory messageFactory) throws IllegalStateException, RainbowException {
                return Collections.<IRainbowMessage> emptyList ();
            }

            @Override
            public List<? extends IRainbowMessage> redo () throws IllegalStateException, RainbowException {
                return Collections.<IRainbowMessage> emptyList ();

            }

            @Override
            public List<? extends IRainbowMessage> undo () throws IllegalStateException, RainbowException {
                return Collections.<IRainbowMessage> emptyList ();

            }


            @Override
            public Integer getResult () throws IllegalStateException {
                return Integer.parseInt (args[1]);
            }

        };
    }

}
