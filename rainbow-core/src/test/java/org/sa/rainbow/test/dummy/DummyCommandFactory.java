package org.sa.rainbow.test.dummy;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class DummyCommandFactory extends ModelCommandFactory<Integer> {
    public DummyCommandFactory () {
        super (null, null);
        // TODO Auto-generated constructor stub
    }
    public static AbstractLoadModelCmd loadCommand (ModelsManager modelsManager,
            final String modelName,
            InputStream stream,
            String source) {
        return new AbstractLoadModelCmd<Integer> ("load", modelsManager, modelName, stream, source) {

            @Override
            public ModelReference getModelReference () {
                return new ModelReference (modelName, "Acme");
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

                    @Override
                    public void setOriginalSource (String source) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void dispose () {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public String getOriginalSource () {
                        // TODO Auto-generated method stub
                        return null;
                    }

                };
            }

            @Override
            protected boolean checkModelValidForCommand (Object model) {
                return true;
            }

            @Override
            public String getOrigin () {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
    @Override
    public IRainbowModelOperation generateCommand (String commandName, final String... args)
            throws RainbowModelException {
        return new IRainbowModelOperation<Integer, Integer> () {

            private IModelChangeBusPort m_announcPort;
            private Integer                    m_m;

            @Override
            public String[] getParameters () {
                return new String[0];
            }

            @Override
            public String getTarget () {
                return "load";
            }

            @Override
            public String getName () {
                return "load";
            }

            @Override
            public ModelReference getModelReference () {
                return new ModelReference ("test", "test");

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

            @Override
            public String getOrigin () {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void setOrigin (String o) {
                // TODO Auto-generated method stub

            }

        };
    }

    @Override
    public AbstractSaveModelCmd<Integer> saveCommand (String location) throws RainbowModelException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void fillInCommandMap () {
        // TODO Auto-generated method stub

    }

}
