package org.sa.rainbow.stitch2.util;

/**
 * Created by schmerl on 10/10/2016.
 */
public class KDebug {
    public static void logCallingClass () {
        StackTraceElement[] stElements = Thread.currentThread ().getStackTrace ();
        if (stElements.length > 3) {
            String classAndMethodCalled = stElements[2].getClassName () + "." + stElements[2].getMethodName ();
            int i = 3;
            StackTraceElement caller = stElements[i];
            while (i < stElements.length && caller.getMethodName ().equals (stElements[2].getMethodName ())) {
                caller = stElements[++i];
            }
            if (i < stElements.length) {
                System.out.println (stElements[i].getClassName () + "." + stElements[i].getMethodName
                        () + " called "
                                            + classAndMethodCalled + "  on line " + stElements[i].getLineNumber ());
            }
        }
    }

    public static String getCallTrace () {
        StackTraceElement[] stElements = Thread.currentThread ().getStackTrace ();
        if (stElements.length > 3) {
            String classAndMethodCalled = stElements[2].getClassName () + "." + stElements[2].getMethodName ();
            int i = 3;
            StackTraceElement caller = stElements[i];
            while (i < stElements.length && caller.getMethodName ().equals (stElements[2].getMethodName ())) {
                caller = stElements[++i];
            }
            if (i < stElements.length) {
                return stElements[i].getClassName () + "." + stElements[i].getMethodName
                        () + " called "
                        + classAndMethodCalled + "  on line " + stElements[i].getLineNumber ();
            }
        }
        return "**UNKNOWN TRACE**";
    }
}
