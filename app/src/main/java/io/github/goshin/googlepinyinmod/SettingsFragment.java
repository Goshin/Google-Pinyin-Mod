package io.github.goshin.googlepinyinmod;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName("pref");
        //noinspection deprecation
        preferenceManager.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }

}
