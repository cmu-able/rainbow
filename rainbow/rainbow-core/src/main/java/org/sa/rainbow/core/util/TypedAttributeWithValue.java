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
package org.sa.rainbow.core.util;


public class TypedAttributeWithValue extends Pair<TypedAttribute, Object> {

    private static final long serialVersionUID = 7005191042752015697L;

    public TypedAttributeWithValue (String name, String type, Object value) {
        super (new TypedAttribute (name, type), value);
    }

    public TypedAttributeWithValue (TypedAttribute attr, Object value) {
        super (attr, value);
    }


    public Object getValue () {
        return secondValue ();
    }

    public void setValue (Object value) {
        setSecondValue (value);
    }


    public String getName () {
        return firstValue ().getName ();
    }

    public void setName (String name) {
        firstValue ().setName (name);
    }


    public String getType () {
        return firstValue ().getType ();
    }

    public void setType (String type) {
        firstValue ().setType (type);
    }


    @Override
    public Object clone () {
        return super.clone ();
    }
}
