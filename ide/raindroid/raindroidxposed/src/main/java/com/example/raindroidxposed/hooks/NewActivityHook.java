package com.example.raindroidxposed.hooks;

import android.app.Activity;

import de.robv.android.xposed.XC_MethodHook;

/**
 * This class sets up the context, which is used to connect to
 * the service. (There might be a better way to do this.)
 */
public class NewActivityHook extends XC_MethodHook {
    private static volatile Activity m_currentActivity = null;

    public static Activity getCurrentActivity() {
        return m_currentActivity;
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        m_currentActivity = (Activity) param.getResult();
    }
}
