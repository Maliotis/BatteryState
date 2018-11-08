package com.maliotis.batterystate.Activities;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.maliotis.batterystate.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.app_preferences);
    }
}
