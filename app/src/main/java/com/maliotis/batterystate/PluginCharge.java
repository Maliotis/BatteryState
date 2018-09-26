package com.maliotis.batterystate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maliotis.batterystate.Activities.MainActivity;


public class PluginCharge extends BroadcastReceiver {
    Context context;
    Intent intent;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.intent = intent;
        this.context = context;
        Log.d("MyReceiver","It works");
            Intent intent1 = new Intent(context, MainActivity.class);
            context.startActivity(intent1);
    }

    private boolean appIsNotRunning() {
        return !Helper.isAppRunning(context);
    }

    //For safety reasons
    private boolean actualIntentEqualsConnectedToPower() {
        return intent.getAction().equals(Intent.ACTION_POWER_CONNECTED);
    }
}
