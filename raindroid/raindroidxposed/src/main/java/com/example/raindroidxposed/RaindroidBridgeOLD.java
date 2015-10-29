package com.example.raindroidxposed;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AndroidAppHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import org.sa.rainbow.raindroid.util.RaindroidMessages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * This class implements a bridge from an Android app to
 * Rainbow. It uses Xposed to insert probes into appropriate
 * methods on apps that are identified as potential vulnerability
 * points.
 * <p/>
 * Created by Bradley Schmerl on 9/23/2015.
 */
public class RaindroidBridgeOLD implements IXposedHookLoadPackage {

    private final Map<String, Set<String>> m_interestingPackages;

    private boolean m_intercept = true;
    public void setIntercept (boolean i) {
        m_intercept = i;
    }
    public boolean isIntercept () {
        return m_intercept;
    }

    private void log(String s) {
        System.out.println(s);
    }

    private void log(Throwable e) {
        e.printStackTrace(System.out);
    }

    public static class NewActivityHook extends XC_MethodHook {
        private static volatile Activity m_currentActivity = null;

        public static Activity getCurrentActivity() {
            return m_currentActivity;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            m_currentActivity = (Activity) param.getResult();
        }
    }

    class ActivityMethodHook extends XC_MethodHook {
        private final Set<String> m_classes;
        private DialogInterface.OnClickListener dialogClickListener;
        private int choice = 1;

        public ActivityMethodHook(Set<String> classes) {
            super();
            m_classes = classes;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            m_intercept = true;
        }

        @Override
        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
            if (m_intercept && m_classes != null && m_classes.contains(param.thisObject.getClass().getName())) {
                Intent i = (Intent) param.args[0];
                XposedBridge.log(param.thisObject.getClass().toString() + " is sending an intent");
                ComponentName cmp = i.getComponent();
                i.putExtra(RaindroidMessages.RAINDROID_CALLER, param.thisObject.getClass().getName());
                final Context context = AndroidAppHelper.currentApplication().getApplicationContext();

                if (cmp == null) {
                    XposedBridge.log("Implicit intent being passed");
                    final Object waiter = new Object();
                    dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (choice) {
                                case 0: // Send to known good
                                    m_intercept = false;
                                    Intent i = (Intent) param.args[0];
                                    i.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
                                    try {
                                        ((Method) param.method).invoke(param.thisObject, param.args);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        XposedBridge.log("Failed to call method");
                                    }
                                    break;
                                case 1: // Do not send
                                    break;
                                case 2: // Send as normal
                                    m_intercept = false;
                                    try {
                                        ((Method) param.method).invoke(param.thisObject, param.args);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        XposedBridge.log("Failed to call method");
                                    }
                                    break;
                            }
                            choice = -1;
                            dialog.dismiss();
                        }
                    };


                    AlertDialog.Builder builder = new AlertDialog.Builder(NewActivityHook.getCurrentActivity());
                    builder.setTitle("Dangerous activity detected. Proceed?")
                            .setSingleChoiceItems(new String[]{"Send to good", "Do not send", "Send"}, 1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    choice = which;
                                }
                            })
                            .setPositiveButton("OK", dialogClickListener)
                            .show();

                    param.setResult(null);

                }
            }
        }
    }

    class OnCreateMethodHook extends XC_MethodHook {
        private final Set<String> m_classes;

        public OnCreateMethodHook(Set<String> classes) {
            super();
            m_classes = classes;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (m_classes != null && m_classes.contains(param.thisObject.getClass().getName())) {
                Activity a = (Activity) param.thisObject;
                Intent intent = a.getIntent();
                String rcvr = "UNKNOWN";
                if (intent.hasExtra(RaindroidMessages.RAINDROID_CALLER)) {
                    rcvr = intent.getStringExtra(RaindroidMessages.RAINDROID_CALLER);
                }
                XposedBridge.log(a.getPackageName() + " received an intent from " + rcvr);
                // Send this event to Rainbow
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // Ensure that we are in an app that we are interested in
        if (!m_interestingPackages.keySet().contains(loadPackageParam.packageName)) {
            log("Raindroid: package loaded " + loadPackageParam.packageName + " not interested.");
            return;
        }

        log("Raindroid: package loaded " + loadPackageParam.packageName + " attempting to hook.");
        Set<String> classes = m_interestingPackages.get(loadPackageParam.packageName);
        if (classes != null && !classes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String cls : classes) {
                sb.append(cls).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            String l = sb.toString();
            log("Raindroid: attempting to hook into " + l);
            try {
//                XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "startActivityFromChild", Activity.class, Intent.class, int.class, Bundle.class, new ActivityMethodHook(classes));
                XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "startActivity", Intent.class, new ActivityMethodHook(classes));
                XposedHelpers.findAndHookMethod("android.app.Activity", loadPackageParam.classLoader, "onCreate", Bundle.class, new OnCreateMethodHook(classes));
                XposedHelpers.findAndHookMethod("android.app.Instrumentation", loadPackageParam.classLoader, "newActivity", ClassLoader.class, String.class, Intent.class, new NewActivityHook());
            } catch (Throwable e) {
                log("Error! could not hook method '" + l + "'.startActivity");
                log(e);
            }
        }

    }


    public RaindroidBridgeOLD() {
        // Initialize packages that we are interested in
        // For now, let's hardwire the example. In future,
        // we'll need to do this dynamically, perhaps through
        // a file
        XposedBridge.log("Loaded RaindroidBridgeOLD");
        m_interestingPackages = new HashMap();
        m_interestingPackages.put("sosf.cmu.edu.intentintercept", Collections.singleton("sosf.cmu.edu.activity1.Activity1"));
        m_interestingPackages.put("sosf.cmu.edu.maliciousapp", Collections.singleton("sosf.cmu.edu.maliciousapp.MaliciousActivity"));
    }

}
