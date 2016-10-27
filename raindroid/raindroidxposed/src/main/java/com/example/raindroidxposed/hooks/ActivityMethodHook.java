package com.example.raindroidxposed.hooks;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;

import com.example.raindroidxposed.RaindroidBridge;

import org.sa.rainbow.raindroid.util.RaindroidMessages;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * The method to hook before sending an intent. This will check to see if
 * the intent is implicit and then communicate with Raindroid to find out
 * what to do.
 */
public class ActivityMethodHook extends XC_MethodHook {
    private RaindroidBridge bridge;
    private final Set<String> m_classes;
    private DialogInterface.OnClickListener dialogClickListener;
    private int choice = 1;

    public ActivityMethodHook(RaindroidBridge bridge, Set<String> classes) {
        super();
        this.bridge = bridge;
        m_classes = classes;
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        // After the method has been invoked, let's make sure to reintercept.
        bridge.setIntercept (true);
    }

    @Override
    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
        // Make sure we bind to Raindroid. Need to do this here because we need a
        // valid context.
        RaindroidBridge.bindToRaindroid(NewActivityHook.getCurrentActivity());
        if (bridge.isIntercept () && m_classes != null && m_classes.contains(param.thisObject.getClass().getName())) {
            Intent i = (Intent) param.args[0];
            XposedBridge.log(param.thisObject.getClass().toString() + " is sending an intent");
            // Add the component that is sending the intent to the intent so that we can
            // track it on the ohter end, if necessary
            ComponentName cmp = i.getComponent();
            i.putExtra(RaindroidMessages.RAINDROID_CALLER, param.thisObject.getClass().getName());

            // Send a message to the RaindroidService to find out what to do. The actual
            // effect is implemented in the IntentDispositioner
            Message msg = Message.obtain(null, RaindroidMessages.MSG_RAINDROID_INTENT_SENT);
            msg.replyTo = new Messenger(new IntentDispositioner (bridge, param));
            msg.getData().putParcelable(RaindroidMessages.MSG_RANDROID_INTENT_DATA_KEY, i);
            RaindroidBridge.sendToRaindroidService(msg);
            // This prevents the method from being called -- we will wait for Raindroid
            // to tell us what to do, before sending the intent (if we are allowed).
            param.setResult(null);
        }
    }
}
