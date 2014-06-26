package org.sa.rainbow.core.models;

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
}
