package org.sa.rainbow.raindroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartRaindroidReceiver extends BroadcastReceiver {
    public StartRaindroidReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RANDROID", "Starting Raindroid");
        if (Intent.ACTION_BOOT_COMPLETED.equals (intent.getAction())) {
            Intent serviceIntent = new Intent(context, RaindroidProxyService.class);
            context.startService(serviceIntent);
        }
    }
}
