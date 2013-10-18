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
