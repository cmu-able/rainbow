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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypedAttribute extends Pair<String, String> {


    private static final long serialVersionUID = 8134259574614606785L;
    private static Pattern    MAPPING_PAIR_PATTERN;

    public TypedAttribute (String name, String type) {
        super (name.intern (), type);
    }


    public String getName () {
        return firstValue ();
    }

    public void setName (String name) {
        setFirstValue (name);
    }


    public String getType () {
        return secondValue ();
    }

    public void setType (String type) {
        setSecondValue (type);
    }


    @Override
    public Object clone () {
        return super.clone ();
    }


    public static TypedAttribute parsePair (String string) {
        /**
         * Parses a string of the form "name":"type" into a Pair object.
         * 
         * @param str
         *            the string to parse, must be of form n:t
         * @return TypeNamePair the resulting pair object whose type is t and name is n
         */
        if (MAPPING_PAIR_PATTERN == null) {
            MAPPING_PAIR_PATTERN = Pattern.compile ("(.+?):(.+?)");
        }

        Matcher m = MAPPING_PAIR_PATTERN.matcher (string);
        String name = null;
        String type = null;
        if (m.matches ()) { // got 2 groups matched
            name = m.group (1).trim ();
            type = m.group (2).trim ();
        }

        return new TypedAttribute (name, type);
    }

}
