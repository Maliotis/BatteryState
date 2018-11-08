package com.maliotis.batterystate;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {


    private SeekBar mSeekBar;
    private int mProgress;
    private AudioManager audioManager;

    public SeekBarPreference(Context context) {
        this(context, null, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_seekbar);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int deviceMaxAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

        mSeekBar = view.findViewById(R.id.seekbar);
        mSeekBar.setMax(deviceMaxAlarm);
        mSeekBar.setProgress(mProgress);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser)
            return;

        setValue(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // not used
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mSeekBar.setProgress(mProgress);
        //Set new volume alarm
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM,mProgress,AudioManager.STREAM_ALARM);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }

        if (value != mProgress) {
            mProgress = value;
            //notifyChanged();
            callChangeListener(value);
        }
    }



    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }
}
