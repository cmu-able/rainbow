package com.example.raindroidxposed;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import com.example.raindroidxposed.hooks.OnCreateMethodHook;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import com.example.raindroidxposed.hooks.*;

import org.sa.rainbow.raindroid.util.RaindroidMessages;

/**
 * This class implements a bridge from an Android app to
 * Rainbow. It uses Xposed to insert probes into appropriate
 * methods on apps that are identified as potential vulnerability
 * points.
 * <p/>
 * Created by Bradley Schmerl on 9/23/2015.
 */
public class RaindroidBridge implements IXposedHookLoadPackage {

    /** The list of packages that should be modified by Xposed
     *  This should be refactored into the Raindroid service
     */
    private final Map<String, Set<String>> m_interestingPackages;

    /** Whether to intercept. This is used when Raindroid says to execute
     * the methods, so that we don't get into infinite loops.
     */
    private boolean m_intercept = true;
    public void setIntercept(boolean intercept) {
        this.m_intercept = intercept;
    }
    public boolean isIntercept() {
        return m_intercept;
    }


    /** Indicates whether the bridge has connected with the Raindroid service
     *
     */
    private static boolean m_serviceBound = false;

    private void log(Throwable e) {
        e.printStackTrace(System.out);
    }

    public static Messenger m_raindroidService = null;

    private static ServiceConnection m_raindroidServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            m_raindroidService = new Messenger (service);
            XposedBridge.log("Connected to Raindroid Service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_raindroidService = null;
            XposedBridge.log("Disconnected from Raindroid Service");
        }
    };


    /** This method is used when an application is loaded, for example when the user launches it or
     * when an activity is started
     */
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // Ensure that we are in an app that we are interested in
        if (!m_interestingPackages.keySet().contains(loadPackageParam.packageName)) {
            Log.i(RaindroidMessages.getTag(), "Raindroid: package loaded " + loadPackageParam.packageName + " not interested.");
            return;
        }

        // Only hook the classes that we are interested in
        Log.i(RaindroidMessages.getTag(), "Raindroid: package loaded " + loadPackageParam.packageName + " attempting to hook.");
        Set<String> classes = m_interestingPackages.get(loadPackageParam.packageName);
        if (classes != null && !classes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String cls : classes) {
                sb.append(cls).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            String l = sb.toString();
            Log.i(RaindroidMessages.getTag(), "Raindroid: attempting to hook into " + l);
            try {
                XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "startActivity", Intent.class, new ActivityMethodHook(this, classes));
                XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "onCreate", Bundle.class, new OnCreateMethodHook(classes));
                XposedHelpers.findAndHookMethod("android.app.Instrumentation", loadPackageParam.classLoader, "newActivity", ClassLoader.class, String.class, Intent.class, new NewActivityHook());
            } catch (Throwable e) {
                Log.e(RaindroidMessages.getTag(), "Error! could not hook method '" + l + "'.startActivity", e);
            }
        }

    }


    public RaindroidBridge() {
        // Initialize packages that we are interested in
        // For now, let's hardwire the example. In future,
        // we'll need to do this dynamically, perhaps through
        // a file
        XposedBridge.log("Loaded RaindroidBridge");
        m_interestingPackages = new HashMap();
        m_interestingPackages.put("sosf.cmu.edu.intentintercept", Collections.singleton("sosf.cmu.edu.activity1.Activity1"));
        m_interestingPackages.put("sosf.cmu.edu.maliciousapp", Collections.singleton("sosf.cmu.edu.maliciousapp.MaliciousActivity"));

    }

    public static void bindToRaindroid() {
        // Establish a connection to the Raindroid service. This will be used for communicating
        // events to Rainbow and communicating decisions/adaptations back to this bridge

        if (!m_serviceBound) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName ("org.sa.rainbow.raindroid", "org.sa.rainbow.raindroid.RaindroidProxyService"));
            NewActivityHook.getCurrentActivity().bindService(intent, m_raindroidServiceConnection, Context.BIND_AUTO_CREATE);
            m_serviceBound = true;
        }
    }

}
