package com.example.raindroidxposed.hooks;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;

import com.example.raindroidxposed.RaindroidBridge;

import org.sa.rainbow.raindroid.util.RaindroidMessages;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 *
 */
public class OnCreateMethodHook extends XC_MethodHook {
    private final Set<String> m_classes;

    public OnCreateMethodHook(Set<String> classes) {
        super();
        m_classes = classes;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        RaindroidBridge.bindToRaindroid();

        if (m_classes != null && m_classes.contains(param.thisObject.getClass().getName())) {
            Activity a = (Activity) param.thisObject;
            Intent intent = a.getIntent();
            String rcvr = "UNKNOWN";
            if (intent.hasExtra(RaindroidMessages.RAINDROID_CALLER)) {
                rcvr = intent.getStringExtra(RaindroidMessages.RAINDROID_CALLER);
            }
            XposedBridge.log(a.getPackageName() + " received an intent from " + rcvr);
            // Send this event to Rainbow
            Message msg = Message.obtain (null, RaindroidMessages.MSG_RAINDROID_INTENT_RECEIVED);
            msg.getData().putParcelable(RaindroidMessages.MSG_RANDROID_INTENT_DATA_KEY, intent);
            RaindroidBridge.m_raindroidService.send (msg);
        }
    }
}
