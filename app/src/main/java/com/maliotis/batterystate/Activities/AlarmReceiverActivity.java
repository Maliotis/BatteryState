package com.maliotis.batterystate.Activities;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import com.maliotis.batterystate.R;
import com.timqi.sectorprogressview.ColorfulRingProgressView;
import java.io.IOException;

public class AlarmReceiverActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    ColorfulRingProgressView progressView;
    TextView textView;

    int level;

    SharedPreferences prefs;
    UnpluggedCharge receiver = new UnpluggedCharge();

    public class UnpluggedCharge extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_alarm_receiver);

        progressView = findViewById(R.id.progressBarAlarm);
        progressView.setPercent(0);

        textView = findViewById(R.id.textViewAlarm);

        PowerManager pm = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm != null ? pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "batterstate:mywakeLock") : null;
        if (wl != null) {
            wl.acquire(60*1000L /*1 minute*/);
        }

        Button stopAlarm = findViewById(R.id.button);

        stopAlarm.setOnClickListener(view -> {
           mMediaPlayer.stop();
           //finish();
           this.onBackPressed();
        });
        level = getIntent().getIntExtra("level",80);


        if (playSound(this, getAlarmUri())) {
            animateProgressBar(level,1300);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        registerReceiver(receiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private boolean playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context,alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.start();
                    return true;
                }
            }

        } catch (IOException e) {
            Log.d(this.getLocalClassName(),e.getMessage());
        }

        return false;
    }

    private Uri getAlarmUri() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ringtone = prefs.getString(SettingsActivity.RINGTONE,"");
        Uri alert = Uri.parse(ringtone);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        } else if (alert.toString().isEmpty()) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        return alert;
    }

    private void animateProgressBar(int progress, int duration) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(progressView, "percent",
                progressView.getPercent(), progress);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(duration);
        anim.start();
        anim.addUpdateListener(valueAnimator -> {
            Log.d("Animator",valueAnimator.getAnimatedValue().toString());
            int textValue = (int) Float.parseFloat(valueAnimator.getAnimatedValue().toString());
            textView.setText(String.format("%d", textValue));
        });
    }

}



