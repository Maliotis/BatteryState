package com.maliotis.batterystate;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.maliotis.batterystate.Activities.AlarmReceiverActivity;
import com.maliotis.batterystate.Activities.MainActivity;

import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.maliotis.batterystate.Activities.MainActivity.PROGRESS_PREF;

public class BatteryChanged extends BroadcastReceiver {
    TextView tempValueTextView;
    TextView voltValueTextView;
    SharedPreferences prefs;

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BatteryChanged","Works");
        this.context = context;
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

        try {

            float temp = (float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
            float volt = (float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000;
            Log.d("TemperatureMain", "onReceive: " + temp);
            Log.d("VoltMain", "onReceive: " + volt);
            tempValueTextView.setText(String.valueOf(temp).concat(" ").concat((char) 0x00B0 + "C"));
            voltValueTextView.setText(String.valueOf(volt).concat("V"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int low_level = settings.getInt("low_battery",20);


        int userLevel;
        userLevel = prefs.getInt(PROGRESS_PREF,80);


        if (level == userLevel && isCharging) {
            Calendar cal  = Calendar.getInstance();
            cal.add(Calendar.MILLISECOND,500);
            Intent i = new Intent(context, AlarmReceiverActivity.class);
            i.putExtra("level",userLevel);
            PendingIntent pendingIntent = PendingIntent.getActivities(context,
                    12345,
                    new Intent[]{i},
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),pendingIntent);
        }

        if (level == low_level && !isCharging) {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(NOTIFICATION_SERVICE);

            Intent intent1 = new Intent(context, MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
            PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent1, 0);

            Notification notification  = new Notification.Builder(context.getApplicationContext())
                    .setContentTitle("New mail from " + "test@gmail.com")
                    .setContentText("Subject")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(1,notification);
        }
    }


    private boolean appIsNotRunning() {
        return !Helper.isAppRunning(context);
    }
}
