package com.maliotis.batterystate;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.maliotis.batterystate.Activities.MainActivity;

import java.util.List;
import java.util.Objects;


public class PluginCharge extends BroadcastReceiver {
    Context context;
    Intent intent;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.intent = intent;
        this.context = context;
        Log.d("MyReceiver","It works");
        if (actualIntentEqualsConnectedToPower() && !appIsInForeground()) {
            Intent intent1 = new Intent(context, MainActivity.class);
            context.startActivity(intent1);
        }
    }

    private boolean appIsNotRunning() {
        return !Helper.isAppRunning(context);
    }

    private boolean appIsInForeground() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        if (appProcessInfos == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessInfos) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    //For safety reasons
    private boolean actualIntentEqualsConnectedToPower() {
        return Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_POWER_CONNECTED);
    }
}
