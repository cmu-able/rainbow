package org.sa.rainbow.core.models;

import org.sa.rainbow.util.HashCodeUtil;
import org.sa.rainbow.util.Util;

public class ModelReference {
    private final String m_modelName;
    private final String m_modelType;

    public ModelReference (String modelName, String modelType) {
        m_modelName = modelName;
        m_modelType = modelType;
    }

    public String getModelName () {
        return m_modelName;
    }

    public String getModelType () {
        return m_modelType;
    }

    @Override
    public String toString () {
        return getModelName () + ":" + getModelType ();
    }

    @Override
    public boolean equals (Object obj) {
        if (obj instanceof ModelReference) {
            ModelReference mr = (ModelReference )obj;
            return Util.safeEquals (m_modelName, mr.m_modelName) && Util.safeEquals (m_modelType, mr.m_modelType);
        }
        return false;
    }

    @Override
    public int hashCode () {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash (result, m_modelName);
        result = HashCodeUtil.hash (result, m_modelType);
        return result;
    }
}
