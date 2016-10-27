package org.sa.rainbow.raindroid;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.sa.rainbow.raindroid.org.sa.rainbow.randroid.util.RaindroidMessages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class implements an invisible Activity that can be used as the context for
 * bringing up a dialog to interact with the user.
 * Currently, the activity has hardwired into it responses for what to do with intents,
 * rather than policies for how to make a choice. One extension would be to make this
 * more general.
 */
public class HuILActivity extends AppCompatActivity {

    /** The choice of what to do with an implicit intent **/
    private String m_intentDisposition = RaindroidMessages.MSG_RAINDROID_INTENT_PREVENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hu_il);

        // What to do when the OK button is pressed
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Integer id = (Integer) getIntent().getIntExtra("stateId", -1);
                // Send the choice via the Proxy service back to the application-Raindroid bridge
                RaindroidProxyService.instance().dispositionIntent(id, m_intentDisposition);
                // Go back to the previous activity (this might not be the wisest thing
                onBackPressed();
            }
        };

        // Bring up an alert dialog to make a choice
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dangerous activity detected. Proceed?")
                .setSingleChoiceItems(new String[]{"Send to good", "Do not send", "Send"}, 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Send to known good
                                m_intentDisposition = RaindroidMessages.MSG_RAINDROID_INTENT_SEND_TO_GOOD;
                                break;
                            case 1: // Do not send
                                m_intentDisposition = RaindroidMessages.MSG_RAINBOW_INTENT_NO_EFFECT;
                                break;
                            case 2: // Send as normal
                                m_intentDisposition = RaindroidMessages.MSG_RAINBOW_INTENT_NO_EFFECT;
                                break;
                        }
                    }
                })
                .setPositiveButton("OK", dialogClickListener)
                .show();
    }




}
