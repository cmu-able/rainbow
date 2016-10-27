package com.example.raindroidxposed.hooks;

import android.app.Application;
import android.content.Context;
import android.os.Message;

import com.example.raindroidxposed.RaindroidBridge;

import org.sa.rainbow.raindroid.util.RaindroidMessages;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by schmerl on 10/30/2015.
 */
public class RegisterWithAndroidHook extends XC_MethodHook {
    private RaindroidBridge m_bridge;

    public RegisterWithAndroidHook(RaindroidBridge bridge) {
        m_bridge = bridge;
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        Application application = (Application) param.thisObject;
        Context m_context = (Context )param.args [0];
        if (m_context != null) {
            XposedBridge.log(application.getPackageName() + " has been given a context");
            RaindroidBridge.bindToRaindroid(m_context);
        }
        Message msg = Message.obtain(null, RaindroidMessages.MSG_RAINDROID_APP_STARTED);
        msg.getData().putString (RaindroidMessages.MSG_APP_PACKAGE, application.getPackageName());
        RaindroidBridge.sendToRaindroidService(msg);

    }
}
