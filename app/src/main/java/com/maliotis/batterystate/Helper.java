package com.maliotis.batterystate;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class Helper {

    public static boolean isAppRunning(Context context) {
        String packageName = context.getApplicationContext().getPackageName();
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
