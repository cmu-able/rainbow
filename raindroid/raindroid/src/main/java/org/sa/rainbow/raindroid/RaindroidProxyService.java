package org.sa.rainbow.raindroid;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the RaindroidProxyService which should manage local
 * choices about what to do with various events that happen in apps. These
 * events are communicated to the sevice via messages from an Xposed module
 * that is attached to all apps on the device. The service should make a decision
 * about what the appropriate disposition of the event should be, by either doing
 * this locally, or communicating with Rainbow to make the decision.
 *
 * The application will wait for a response from the service, and so this decision
 * needs to be made quickly (and synchronously) to prevent user annoyance.
 *
 * Currently this class only supports intent events and makes the choices locally
 * (or through human interaction via the HuILActivity)
 */
public class RaindroidProxyService extends Service {

    public static final int MSG_INTENT_SENT = 1;
    private static final String MSG_RANDROID_INTENT_DATA_KEY = "SENT_INTENT";
    public static final  String MSG_RAINDROID_INTENT_PREVENT = "__prevent__";
    public static final  String MSG_RAINDROID_INTENT_SEND_TO_GOOD = "__send_to_good__";
    public static final  String MSG_RAINBOW_INTENT_NO_EFFECT = "__no_effect__";
    private static final String MSG_RAINDROID_EFFECT_KEY = "__RAINDROID__EFFECT__";
    private static final int MSG_RAINDROID_EFFECT = 2;
    private int m_nextState = 0;
    private boolean m_askUser = true;

    static RaindroidProxyService m_serviceIntance;

    public static RaindroidProxyService instance () {
        return m_serviceIntance;
    }

    public boolean isAskUser() {
        return m_askUser;
    }

    class ServiceState {
        public Intent m_intent;
        public Messenger m_replyTo;
    }

    private Map<Integer, ServiceState> m_states = new HashMap<>();
    protected Integer nextStateId () {
        return m_nextState++;
    }

    public void dispositionIntent (Integer id, String choice) {
        ServiceState ss = m_states.get (id);
        if (ss == null)
            return;
        handleIntentSent(ss.m_replyTo, (Intent )ss.m_intent, choice);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INTENT_SENT:
                    if (isAskUser ()) {
                        ServiceState ss = new ServiceState ();
                        ss.m_intent = (Intent) msg.getData().getParcelable(MSG_RANDROID_INTENT_DATA_KEY);
                        ss.m_replyTo = msg.replyTo;
                        Integer id = nextStateId();
                        m_states.put(id, ss);
                        Intent i = new Intent(RaindroidProxyService.this, HuILActivity.class);
                        i.putExtra("stateId", id);
                        i.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                    else {
                        RaindroidProxyService.this.handleIntentSent(msg.replyTo, (Intent) msg.getData().getParcelable(MSG_RANDROID_INTENT_DATA_KEY),MSG_RAINDROID_INTENT_PREVENT);
                    }
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
                Message msg = Message.obtain(null, MSG_RAINDROID_EFFECT);
                msg.getData().putString(MSG_RAINDROID_EFFECT_KEY, disposition);
                replyTo.send (msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
