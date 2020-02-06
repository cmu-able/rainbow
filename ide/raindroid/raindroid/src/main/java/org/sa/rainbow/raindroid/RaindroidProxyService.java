package org.sa.rainbow.raindroid;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.sa.rainbow.raindroid.org.sa.rainbow.randroid.util.RaindroidMessages;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the RaindroidProxyService which should manage local
 * choices about what to do with various events that happen in apps. These
 * events are communicated to the sevice via messages from an Xposed module
 * that is attached to all apps on the device. The service should make a decision
 * about what the appropriate disposition of the event should be, by either doing
 * this locally, or communicating with Rainbow to make the decision.
 * <p/>
 * The application will wait for a response from the service, and so this decision
 * needs to be made quickly (and synchronously) to prevent user annoyance.
 * <p/>
 * Currently this class only supports intent events and makes the choices locally
 * (or through human interaction via the HuILActivity)
 */
public class RaindroidProxyService extends Service {

    private int m_nextState = 0;
    private boolean m_askUser = true;

    // The packages list contains information about installed apps
    private List<ApplicationInfo> m_packages = null;
    static RaindroidProxyService m_serviceIntance;

    private Map<String, Date> m_appData = new HashMap<> ();
    private boolean m_listedPackageInfo = false;

    public static RaindroidProxyService instance() {
        return m_serviceIntance;
    }

    public boolean isAskUser() {
        return m_askUser;
    }

    class ServiceState {
        public Intent m_intent;
        public Messenger m_replyTo;
    }

    /**
     *  Buld a list of package information for Rainbow
     */
    private void listPackageInfo() {
        if (!m_listedPackageInfo) {
            // Load the list of installed packages when the service starts
            m_packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : m_packages) {
                RainbowConnectionManager.instance().appInstalled(packageInfo.packageName);
            }
            m_listedPackageInfo = true;
        }


        // Log app information. In the future this information will be sent to Rainbow for analysis
//        for (ApplicationInfo packageInfo : m_packages) {
//            Log.d("RD_LIST", "Installed package :" + packageInfo.packageName);
//            Log.d("RD_LIST", "Source dir : " + packageInfo.sourceDir);
//            Log.d("RD_LIST", "Launch Activity :" + m_pkgMgr.getLaunchIntentForPackage(packageInfo.packageName));
//            Log.d("RD_LIST", "Permission :" + packageInfo.permission);
//           // API > 21 Log.d("RD_LIST", "Version : " + packageInfo.versionCode);
//
//            if (packageInfo.enabled == false) {
//                Log.d("LIST", packageInfo.packageName + " : disabled");
//            } else {
//                Log.d("LIST", packageInfo.packageName + " : enabled");
//            }
//        }
    }

    private Map<Integer, ServiceState> m_states = new HashMap<>();

    protected Integer nextStateId() {
        return m_nextState++;
    }

    public void dispositionIntent(Integer id, String choice) {
        ServiceState ss = m_states.get(id);
        if (ss == null)
            return;
        handleIntentSent(ss.m_replyTo, (Intent) ss.m_intent, choice);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            listPackageInfo();
            switch (msg.what) {
                case RaindroidMessages.MSG_RAINDROID_APP_STARTED:
                    String app = msg.getData().getString(RaindroidMessages.MSG_APP_PACKAGE);
                    boolean alreadySeen = m_appData.put(app, new Date()) != null;
                    if (!alreadySeen) {
                        // send app started message to Rainbow
                        Log.i (RaindroidMessages.getTag(), String.format("Application %s has started running", app));
                        RainbowConnectionManager.instance().appActivated(app);
                    }
                    break;
                case RaindroidMessages.MSG_RAINDROID_APP_STOPPED:
                    app = msg.getData().getString(RaindroidMessages.MSG_APP_PACKAGE);
                    boolean wasKnown = m_appData.remove(app) != null;
                    if (wasKnown) {
                        // send app stopped message to Rainbow
                        Log.i (RaindroidMessages.getTag(), String.format("Application %s has stopped running", app));
                        RainbowConnectionManager.instance ().appDeactivated(app);
                    }

                case RaindroidMessages.MSG_RAINDROID_INTENT_SENT:

                    if (isAskUser()) {
                        ServiceState ss = new ServiceState();
                        ss.m_intent = (Intent) msg.getData().getParcelable(RaindroidMessages.MSG_RANDROID_INTENT_DATA_KEY);
                        ss.m_replyTo = msg.replyTo;
                        Integer id = nextStateId();
                        m_states.put(id, ss);
                        Intent i = new Intent(RaindroidProxyService.this, HuILActivity.class);
                        i.putExtra("stateId", id);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    } else {
                        RaindroidProxyService.this.handleIntentSent(msg.replyTo, (Intent) msg.getData().getParcelable(RaindroidMessages.MSG_RANDROID_INTENT_DATA_KEY), RaindroidMessages.MSG_RAINDROID_INTENT_PREVENT);
                    }
                    break;
                case RaindroidMessages.MSG_RAINDROID_INTENT_RECEIVED:
                    // send intent received event to Rainbow
                    Log.d(RaindroidMessages.getTag(), String.format("%s received an intent from %s", msg.getData().getString(RaindroidMessages.MSG_RAINDROID_INTENT_RECEIVER_KEY), msg.getData().getString(RaindroidMessages.MSG_RAINDROID_INTENT_SENDER_KEY)));
                    break;
            }
            super.handleMessage(msg);
        }
    }


    private final Messenger m_messenger = new Messenger(new IncomingHandler());


    public RaindroidProxyService() {
        m_serviceIntance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // Uncomment the below -- moved to do debugging
        // listPackageInfo();
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_messenger.getBinder();
    }

    protected static void handleIntentSent(Messenger replyTo, Intent i, String disposition) {
        ComponentName cmp = i.getComponent();
        if (cmp == null) {
            try {
                Message msg = Message.obtain(null, RaindroidMessages.MSG_RAINDROID_EFFECT);
                msg.getData().putString(RaindroidMessages.MSG_RAINDROID_EFFECT_KEY, disposition);
                replyTo.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
