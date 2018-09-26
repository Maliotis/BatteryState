package com.maliotis.batterystate.Activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.maliotis.batterystate.BatteryChanged;
import com.maliotis.batterystate.PluginCharge;
import com.maliotis.batterystate.R;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.util.Calendar;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    public static final String MY_PREFS_NAME = "BatteryAlarm";
    public static final String PROGRESS_PREF = "Progress";

    @BindView(R.id.progressBar)
    ColorfulRingProgressView pb;
    @BindView(R.id.editText)
    EditText et;
    @BindView(R.id.my_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tempValueTextView)
    TextView tempValueTextView;
    @BindView(R.id.voltValueTextView)
    TextView voltValueTextView;
    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    Context mContext = this;
    boolean textChanged = false;
    int progress;
    SharedPreferences.Editor editor;
    SharedPreferences prefs;

     BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
            float temp = (float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0) / 10;
            float volt = (float) intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0) / 1000;
            Log.d("TemperatureMain", "onReceive: "+ temp);
            Log.d("VoltMain","onReceive: " + volt);
            tempValueTextView.setText(String.valueOf(temp).concat(" ").concat((char) 0x00B0 +"C"));
            voltValueTextView.setText(String.valueOf(volt).concat("V"));

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
                AlarmManager am = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),pendingIntent);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                //WindowManager.LayoutParams.FLAG_FULLSCREEN);

        checkPermissions();
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.drawer_icon);

        editor = getSharedPreferences(MY_PREFS_NAME,MODE_PRIVATE).edit();

        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int restoredProgress = prefs.getInt(PROGRESS_PREF, 0);
        if (restoredProgress != 0) {
            progress = prefs.getInt(PROGRESS_PREF, 80); //80 is the default value.
        } else {
            editor.putInt(PROGRESS_PREF,80);
            editor.apply();
            progress = 80;
        }

        //Showing notification show the app doesn't go to idle mode
        showNotification();

        //TODO: Register the receiver and check from preferences if you should enable it or not
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        boolean startReceiverBatteryChanged = p.getBoolean("Enable Alert",true);
        if (startReceiverBatteryChanged) {
            //Start the receiver
            startTheReceiverBatteryChanged();
        }

        boolean startReceiverPluginCharge = p.getBoolean("Start Automatically", true);
        if (startReceiverPluginCharge) {

            startTheReceiverPluginCharge();
        }

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawer_icon);

        pb.setPercent(0);
        animateProgressBar(progress,1400);
        et.setText(String.valueOf(progress));

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //No need to implement this method
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //No need to implement this method
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString().equals("") ? "0" : editable.toString();
                int progress1 = Integer.parseInt(text);
                animateProgressBar(progress1,800);

                textChanged = true;
                final View activityRootView = findViewById(R.id.activityRoot);
                activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                    Rect r = new Rect();
                    activityRootView.getWindowVisibleDisplayFrame(r);
                    int screenHeight = activityRootView.getRootView().getHeight();

                    // r.bottom is the position above soft keypad or device button.
                    // if keypad is shown, the r.bottom is smaller than that before.
                    int keypadHeight = screenHeight - r.bottom;


                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                        // keyboard is opened
                    }
                    else {
                        // keyboard is closed
                        showSweetAlertDialog();
                        editor.putInt(PROGRESS_PREF,progress1);
                        editor.apply();
                    }
                });

            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.nav_open,
                R.string.material_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.setHomeAsUpIndicator(R.drawable.drawer_icon);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setBackground(getResources().getDrawable(R.drawable.background));
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.nav_settings:
                    Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intent);
                    break;
                case R.id.nav_themes:
                    //TODO navigate to themes Activity
                case R.id.nav_info:
                    //TODO show alert dialog with the info of the app
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });

    }

    private void startTheReceiverPluginCharge() {
//        PluginCharge charge = new PluginCharge();
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
//        try {
//            unregisterReceiver(charge);
//        } catch (Exception e) {
//            e.getMessage();
//        }
//        registerReceiver(charge,intentFilter);

        PackageManager pm = getPackageManager();
        ComponentName compName =
                new ComponentName(getApplicationContext(),
                        PluginCharge.class);
        pm.setComponentEnabledSetting(
                compName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void startTheReceiverBatteryChanged() {
        BatteryChanged changed = new BatteryChanged(tempValueTextView,voltValueTextView,prefs);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        try {
            unregisterReceiver(changed);
        } catch (Exception e) {
            e.getMessage();
        }
        registerReceiver(changed,intentFilter);

        PackageManager pm = getPackageManager();
        ComponentName compName =
                new ComponentName(getApplicationContext(),
                        PluginCharge.class);
        pm.setComponentEnabledSetting(
                compName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }


    private void showSweetAlertDialog() {
        if (textChanged) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE);
            sweetAlertDialog
                    .setTitleText("Saved!")
                    .setContentText("Your alarm is set to "+et.getText().toString()+"%")
                    .setConfirmText("OK")
                    .show();
            textChanged = false;
        }
    }

    private void animateProgressBar(int progress, int duration) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(pb, "percent",
                pb.getPercent(), progress);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(duration);
        anim.start();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WAKE_LOCK},
                    2);
        }
    }

    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager)
                this.getSystemService(NOTIFICATION_SERVICE);

        Intent intent1 = new Intent(this, MainActivity.class);

        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent1, 0);

        Notification notification  = new Notification.Builder(this.getApplicationContext())
                .setContentTitle("Battery Alarm is running")
                //.setContentText("Subject")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        if (notificationManager != null) {
            notificationManager.notify(1,notification);
        }
    }

}