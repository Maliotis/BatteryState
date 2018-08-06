package com.maliotis.batterystate.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maliotis.batterystate.Activities.MainActivity;
import com.maliotis.batterystate.Helper;

public class PluginCharge extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Helper.isAppRunning(context)) {
            Intent intent1 = new Intent(context, MainActivity.class);
            context.startActivity(intent1);
            Log.d("Receiver",intent.getAction());
        }
        Intent intent1 = new Intent(context, MainActivity.class);
        context.startActivity(intent1);
    }



}
