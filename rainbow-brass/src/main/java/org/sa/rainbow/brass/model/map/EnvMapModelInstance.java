package org.sa.rainbow.brass.model.map;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * @author jcamara
 *
 */
public class EnvMapModelInstance implements IModelInstance<EnvMap> {

    public static final String ENV_MAP_TYPE = "EnvMap";
    private EnvMap m_map;
    private EnvMapCommandFactory m_commandFactory;
    private String m_source;

    public EnvMapModelInstance (EnvMap m, String source){
        setModelInstance (m);
        setOriginalSource (source);
    }

    @Override
    public EnvMap getModelInstance(){
        return m_map;
    }

    @Override
    public void setModelInstance (EnvMap model){
        m_map = model;
    }

    @Override 
    public IModelInstance<EnvMap> copyModelInstance (String newName) throws RainbowCopyException {
        return new EnvMapModelInstance (getModelInstance ().copy (), getOriginalSource());
    }

    @Override
    public String getModelType () {
        return ENV_MAP_TYPE;
    }

    @Override
    public String getModelName () { 
        return getModelInstance().getModelReference().getModelName();
    }

    @Override
    public EnvMapCommandFactory getCommandFactory () {
        if (m_commandFactory==null){
            m_commandFactory = new EnvMapCommandFactory(this);
        }
        return m_commandFactory;
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public String getOriginalSource(){
        return m_source;
    }

    @Override 
    public void dispose () throws RainbowException {

    }
}
