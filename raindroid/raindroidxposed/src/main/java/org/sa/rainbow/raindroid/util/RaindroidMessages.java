package org.sa.rainbow.raindroid.util;

/**
 * Created by schmerl on 10/27/2015.
 */
public class RaindroidMessages {

    public static final int MSG_RAINDROID_INTENT_SENT = 1;
    public static final int MSG_RAINDROID_INTENT_RECEIVED = 2;
    public static final int MSG_RAINDROID_APP_STARTED = 3;
    public static final int MSG_RAINDROID_APP_STOPPED = 4;
    
    public static final String MSG_RANDROID_INTENT_DATA_KEY = "SENT_INTENT";
    public static final  String MSG_RAINDROID_INTENT_PREVENT = "__prevent__";
    public static final  String MSG_RAINDROID_INTENT_SEND_TO_GOOD = "__send_to_good__";
    public static final  String MSG_RAINBOW_INTENT_NO_EFFECT = "__no_effect__";
    public static final String MSG_RAINDROID_EFFECT_KEY = "__RAINDROID__EFFECT__";

    public static final int MSG_RAINDROID_EFFECT = 2;
    public static final String RAINDROID_CALLER = "__RAINDROID_CALLER__";
    public static final String MSG_RAINDROID_INTENT_RECEIVER_KEY = "INTENT_SENDER";
    public static final String MSG_RAINDROID_INTENT_SENDER_KEY = "INTENT_RECEIVER";
    public static final String MSG_APP_PACKAGE = "PACKAGE_NAME";


    public static String getTag () {
        StringBuilder tag = new StringBuilder ();
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i = 0; i < ste.length; i++) {
            if (ste[i].getMethodName().equals ("getTag")) {
                tag.append ("(");
                tag.append (ste[i+1].getFileName() + ":" + ste[i+1].getLineNumber());
                tag.append ("):");
                tag.append (ste [i+1].getClassName());
                tag.append (".");
                tag.append (ste[i+1].getMethodName());
            }
        }
        return tag.toString();
    }
}
