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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.translator.effectors.IEffector;

/**
 * This class holds effector description information parsed from its description
 * file (usu. Yaml).
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class EffectorDescription {

    public static class EffectorAttributes extends DescriptionAttributes {
        public IEffector.Kind kind = null;
        protected OperationRepresentation commandPattern;
        public EffectorAttributes      effectorType;

        @Override
        public String getKindName () {
            String kindName = super.getKindName ();
            if (kindName == null && effectorType != null) return effectorType.getKindName ();
            return kindName;
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
            Map<String, String[]> a = new HashMap<String, String[]> ();
            if (effectorType != null) {
                a.putAll (effectorType.getArrays ());
            }
            a.putAll (super.getArrays ());
            return Collections.<String, String[]> unmodifiableMap (a);
        }

        @Override
        public Map<String, String> getInfo () {
            Map<String, String> a = new HashMap<> ();
            if (effectorType != null) {
                a.putAll (effectorType.getInfo ());
            }
            a.putAll (super.getInfo ());
            return Collections.<String, String> unmodifiableMap (a);
        }
    }

    public SortedSet<EffectorAttributes> effectors = null;
    public TreeMap<String, EffectorAttributes> effectorTypes;

    /**
     * Default Constructor.
     */
    public EffectorDescription() {
        effectors = new TreeSet<EffectorAttributes>();
        effectorTypes = new TreeMap<String, EffectorAttributes> ();
    }


}
