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
/**
 * Created January 31, 2007.
 */
package org.sa.rainbow.core.models;

import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier.Kind;

import java.util.*;

/**
 * This class holds effector description information parsed from its description
 * file (usu. Yaml).
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class EffectorDescription {

    public static class EffectorAttributes extends DescriptionAttributes {

        IEffector.Kind kind = null;
        OperationRepresentation commandPattern;
        public EffectorAttributes      effectorType;

        @Override
        public String getKindName () {
            String kindName = super.getKindName ();
            if (kindName == null && effectorType != null) return effectorType.getKindName ();
            return kindName;
        }

        public void setKindName (String kindName) {
            super.setKindName (kindName);
            try {
                kind = Kind.valueOf (kindName.toUpperCase ());
            } catch (Throwable e) {
                kind = Kind.NULL;
            }
        }

        @Override
        public String getLocation () {
            String location = super.getLocation ();
            if (location == null && effectorType != null) return effectorType.getLocation ();
            return location;
        }

        public OperationRepresentation getCommandPattern () {
            if (commandPattern == null && effectorType != null) return effectorType.commandPattern;
            return commandPattern;
        }

        public void setCommandPattern (OperationRepresentation commandPattern) {
            this.commandPattern = commandPattern;
        }


        @Override
        public Map<String, String[]> getArrays () {
            Map<String, String[]> a = new HashMap<> ();
            if (effectorType != null) {
                a.putAll (effectorType.getArrays ());
            }
            a.putAll (super.getArrays ());
            return Collections.unmodifiableMap (a);
        }


        @Override
        public Map<String, String> getInfo () {
            Map<String, String> a = new HashMap<> ();
            if (effectorType != null) {
                a.putAll (effectorType.getInfo ());
            }
            a.putAll (super.getInfo ());
            return Collections.unmodifiableMap (a);
        }


        public IEffector.Kind getKind () {
            if ((kind == null || kind == Kind.NULL) && effectorType != null)
                return effectorType.getKind ();
            else if (kind == null) return Kind.NULL;
            return kind;
        }

        public void setKind (Kind kind) {
            this.kind = kind;
        }
    }


    public SortedSet<EffectorAttributes> effectors = null;

    public final TreeMap<String, EffectorAttributes> effectorTypes;

    /**
     * Default Constructor.
     */
    public EffectorDescription() {
        effectors = new TreeSet<> ();
        effectorTypes = new TreeMap<> ();
    }


}
