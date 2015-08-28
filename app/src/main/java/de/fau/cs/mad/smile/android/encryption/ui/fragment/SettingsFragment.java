package de.fau.cs.mad.smile.android.encryption.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("pref_key_debug")) {
            Preference pref = findPreference(key);
            SMileCrypto.DEBUG = sharedPreferences.getBoolean(key, false);
        }
    }
}
