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
package org.sa.rainbow.model.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.models.UtilityPreferenceDescription.UtilityAttributes;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class UtilityModelInstance implements IModelInstance<UtilityPreferenceDescription> {

    public static final String           UTILITY_MODEL_TYPE = "UtilityModel";
    private UtilityPreferenceDescription m_utilityPreferences;
    private String                       m_source;
    private UtilityCommandFactory        m_commandFactory;

    public UtilityModelInstance (UtilityPreferenceDescription upd, String source) {
        setModelInstance (upd);
        setOriginalSource (source);
    }

    @Override
    public UtilityPreferenceDescription getModelInstance () {
        return m_utilityPreferences;
    }

    @Override
    public void setModelInstance (UtilityPreferenceDescription model) {
        m_utilityPreferences = model;
    }

    @Override
    public IModelInstance<UtilityPreferenceDescription> copyModelInstance (String newName) throws RainbowCopyException {
        synchronized (m_utilityPreferences) {
            UtilityPreferenceDescription copy = new UtilityPreferenceDescription ();
            copy.associatedModel = new ModelReference (m_utilityPreferences.associatedModel.getModelName (),
                    m_utilityPreferences.associatedModel.getModelType ());
            for (Entry<String, UtilityAttributes> e : m_utilityPreferences.getUtilities ().entrySet ()) {
                UtilityAttributes atts = new UtilityAttributes ();
                atts.label = e.getValue ().label;
                atts.mapping = e.getValue ().mapping;
                atts.desc = e.getValue ().desc;
                atts.values = new HashMap<> ();
                atts.values.putAll (e.getValue ().values);
                copy.addAttributes (e.getKey (), atts);
            }
            for (Entry<String, Map<String, Double>> e : m_utilityPreferences.weights.entrySet ()) {
                Map<String, Double> m = new HashMap<> ();
                m.putAll (e.getValue ());
                copy.weights.put (e.getKey (), m);
            }
            for (Entry<String, Map<String, Object>> e : m_utilityPreferences.attributeVectors.entrySet ()) {
                Map<String, Object> m = new HashMap<> ();
                m.putAll (e.getValue ());
                copy.attributeVectors.put (e.getKey (), m);
            }
            return new UtilityModelInstance (copy, getOriginalSource ());
        }
    }

    @Override
    public String getModelType () {
        return UTILITY_MODEL_TYPE;
    }

    @Override
    public String getModelName () {
        return m_utilityPreferences.associatedModel.getModelName ();
    }

    @Override
    public ModelCommandFactory<UtilityPreferenceDescription> getCommandFactory () throws RainbowException {
        if (m_commandFactory == null) {
            m_commandFactory = new UtilityCommandFactory (this);
        }
        return m_commandFactory;
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public String getOriginalSource () {
        return m_source;
    }

    @Override
    public void dispose () throws RainbowException {
        m_utilityPreferences = null;
        m_source = null;
    }


}
