package com.maliotis.batterystate.Activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.maliotis.batterystate.R;

public class SetActivity extends AppCompatActivity {

    public static final String SETTINGS_FRAGMENT = "Settings_Fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        SettingsFragment settingsFragment = new SettingsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.placeholder, settingsFragment, SETTINGS_FRAGMENT);
        transaction.commit();
    }
}
