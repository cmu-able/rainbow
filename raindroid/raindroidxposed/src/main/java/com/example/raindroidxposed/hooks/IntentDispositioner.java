package com.example.raindroidxposed.hooks;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.example.raindroidxposed.RaindroidBridge;

import org.sa.rainbow.raindroid.util.RaindroidMessages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * Implements various effectors to deal with implicit intents
 * 1. Do not send the intent
 * 2. Turn the intent into an explicit intent, and send to that activity
 * 3. Do nothing - start the activity anyway.
 */
public class IntentDispositioner extends Handler  {
    private RaindroidBridge m_bridge;
    private XC_MethodHook.MethodHookParam m_methodHookParam;

    public IntentDispositioner(RaindroidBridge bridge, XC_MethodHook.MethodHookParam methodHookParam) {
        m_bridge = bridge;
        m_methodHookParam = methodHookParam;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case RaindroidMessages.MSG_RAINDROID_EFFECT:
                String effect = msg.getData().getString (RaindroidMessages.MSG_RAINDROID_EFFECT_KEY);
                doDispositionIntent (effect);
                break;
            default:
                super.handleMessage(msg);

        }
    }

    private void doDispositionIntent(String effect) {
        if (effect.equals(RaindroidMessages.MSG_RAINBOW_INTENT_NO_EFFECT)) {
            sendIntent();

        } else if (effect.equals(RaindroidMessages.MSG_RAINDROID_INTENT_PREVENT)) {
            blockIntent();

        } else if (effect.equals(RaindroidMessages.MSG_RAINDROID_INTENT_SEND_TO_GOOD)) {
            sendIntentToGoodReceiver();
        }
    }

    private void sendIntentToGoodReceiver() {
        Intent i = (Intent )m_methodHookParam.args[0];
        i.setComponent(new ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
        sendIntent();
    }

    private void sendIntent() {
        m_bridge.setIntercept(false);
        try {
            ((Method) m_methodHookParam.method).invoke (m_methodHookParam.thisObject, m_methodHookParam.args);
        } catch (IllegalAccessException e) {
            XposedBridge.log(e);
        } catch (InvocationTargetException e) {
            XposedBridge.log(e);
        }
    }

    private void blockIntent() {
        m_bridge.setIntercept (true);
        XposedBridge.log("Blocking intent sending from " + getIntent ().getStringExtra(RaindroidMessages.RAINDROID_CALLER));
        AlertDialog.Builder builder = new AlertDialog.Builder(NewActivityHook.getCurrentActivity());
        builder.setTitle("The sending of this event has been blocked by Raindroid!")

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private Intent getIntent() {
        return (Intent )m_methodHookParam.args [0];
    }
}
