package com.maliotis.batterystate.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import com.gelitenight.waveview.library.WaveView;
import com.maliotis.batterystate.R;

import java.io.IOException;

public class AlarmReceiverActivity extends AppCompatActivity {

    public MediaPlayer mMediaPlayer;
    ImageView mImageView;
    WaveView mWaveView;
    int level;
    UnpluggedCharge receiver = new UnpluggedCharge();

    public class UnpluggedCharge extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    finish();
                }
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

        mImageView = findViewById(R.id.imageView);
        mWaveView = findViewById(R.id.waveView);

        PowerManager pm = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm != null ? pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "") : null;
        wl.acquire(60*1000L /*1 minute*/);

        Button stopAlarm = findViewById(R.id.button);


        stopAlarm.setOnClickListener(view -> {
           mMediaPlayer.stop();
           finish();
        });
        level = getIntent().getIntExtra("level",80);


        playSound(this, getAlarmUri());


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

    private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context,alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();

                    mImageView.getBackground().setLevel(level*100);
                    mWaveView.setShapeType(WaveView.ShapeType.SQUARE);
                    mWaveView.setWaveColor(Color.CYAN,Color.GREEN);
                    mWaveView.setWaterLevelRatio(20);
                    mWaveView.setAmplitudeRatio(5);
                    mWaveView.setWaveLengthRatio(5);
                    mWaveView.setWaveShiftRatio(5);
                    mWaveView.setShowWave(true);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }
}
