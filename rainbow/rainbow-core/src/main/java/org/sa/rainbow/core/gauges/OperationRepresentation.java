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
package org.sa.rainbow.core.gauges;

import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.util.HashCodeUtil;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperationRepresentation implements IRainbowOperation, Cloneable {

    private final String[] m_parameters;
    private final String m_target;
    private final String m_operationName;

    private ModelReference m_modelRef;
    private String   m_origin;


    @Override
    public String toString () {
        return MessageFormat.format ("O[{0}:{1}/{2}.{3}({4}){5}]", m_modelRef.getModelName (),
                m_modelRef.getModelType (), m_operationName, m_target,
                m_parameters == null ? "" : Arrays.toString (m_parameters), m_origin == null ? "" : ("<" + m_origin));
    }

    public OperationRepresentation (IRainbowOperation cmd) {
        m_parameters = new String[cmd.getParameters ().length];
        String[] parameters = cmd.getParameters ();
        System.arraycopy (parameters, 0, m_parameters, 0, parameters.length);
        m_target = cmd.getTarget ();
        m_operationName = cmd.getName ();
        m_modelRef = new ModelReference (cmd.getModelReference ());
        m_origin = cmd.getOrigin ();

    }

    public OperationRepresentation (String name, ModelReference modelRef, String target,
            String... parameters) {
        m_operationName = name;
        m_modelRef = modelRef == null ? null : new ModelReference (modelRef);
        m_target = target;
        m_parameters = parameters;

    }


    @Override
    public String[] getParameters () {
        return m_parameters;
    }

    @Override
    public String getTarget () {
        return m_target;
    }

    @Override
    public String getName () {
        return m_operationName;
    }


    @Override
    public ModelReference getModelReference () {
        return new ModelReference (m_modelRef);
    }


    @Override
    protected OperationRepresentation clone () throws CloneNotSupportedException {
        return (OperationRepresentation )super.clone ();
    }

    @Override
    public boolean equals (Object obj) {
        if (obj != this) {
            if (obj instanceof OperationRepresentation) {
                OperationRepresentation cr = (OperationRepresentation )obj;
                return (Objects.equals (cr.getModelReference ().getModelType (), getModelReference ().getModelType ()
                ) || (getModelReference ()
                        .getModelType () != null && getModelReference ().getModelType ().equals (
                                cr.getModelReference ().getModelType ())))
                        && (Objects.equals (cr.getName (), getName ()) || (getName () != null && getName ()
                                .equals (cr.getName ())))

                        && (Objects.equals (cr.getTarget (), getTarget ()) || (getTarget () != null && getTarget ()
                        .equals (
                                        cr.getTarget ()))) && Arrays.equals (getParameters (), cr.getParameters ());
            }
            return false;
        }
        return true;
    }

    @Override
    public int hashCode () {
        int result = HashCodeUtil.SEED;
        result = HashCodeUtil.hash (result, getModelReference ().getModelType ());
        result = HashCodeUtil.hash (result, getModelReference ().getModelName ());
        result = HashCodeUtil.hash (result, getName ());
        result = HashCodeUtil.hash (result, getTarget ());
        result = HashCodeUtil.hash (result, getParameters ());
        return result;
    }

    void setModel (ModelReference modelRef) {
        m_modelRef = new ModelReference (modelRef);
    }

    void setModel (TypedAttribute modelRef) {
        m_modelRef = new ModelReference (modelRef.getName (), modelRef.getType ());
    }

    @Override
    public void setOrigin (String o) {
        m_origin = o;
    }

    static final Pattern pattern = Pattern
            .compile ("\\\"?(([\\w\\$\\<\\>\\\"\\.]+)\\.)?(\\w+)\\s*\\(([\\w, \\.{}\\$\\<\\>\\\"]*)\\)\\\"?");

    public static OperationRepresentation parseCommandSignature (String commandSignature) {
        Matcher matcher = pattern.matcher (commandSignature);
        if (matcher.find ()) {
            String target = matcher.group (2);
            String commandName = matcher.group (3);
            String unprocessedParams = matcher.group (4);
            String[] parameters = new String[0];
            if (unprocessedParams != null) {
                parameters = unprocessedParams.split ("\\s*,\\s*");
            }
            return new OperationRepresentation (commandName, null, target,
                    parameters);
        }
        return null;
    }

    @Override
    public String getOrigin () {
        return m_origin;
    }

}
