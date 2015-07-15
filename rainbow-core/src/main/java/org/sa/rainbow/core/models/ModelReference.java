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

    public ModelReference (ModelReference modelRef) {
        m_modelName = modelRef.getModelName ();
        m_modelType = modelRef.getModelType ();
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

    public static ModelReference fromString (String ref) {
        String[] split = ref.split (":");
        return new ModelReference (split[0], split[1]);
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
