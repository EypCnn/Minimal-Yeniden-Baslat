package com.eypcnn.reboot;

import android.os.Bundle;
import android.preference.PreferenceActivity;


/**
 * EypCnn
 */
public class SettingsActivity extends PreferenceActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource( R.xml.ayyarlar);
    }
}
