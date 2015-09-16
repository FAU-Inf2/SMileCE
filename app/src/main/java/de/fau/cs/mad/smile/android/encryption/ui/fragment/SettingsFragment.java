package de.fau.cs.mad.smile.android.encryption.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager preferenceManager = getPreferenceManager();
        SharedPreferences sharedPreferences = preferenceManager.getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        int deleteDistance = sharedPreferences.getInt("delete_distance", 30);
        Preference deletePreference = findPreference("delete_distance");
        String deleteSummary = getString(R.string.swipe_distance_summary, deleteDistance);
        deletePreference.setSummary(deleteSummary);

        int shareDistance = sharedPreferences.getInt("share_distance", 20);
        Preference sharePreference = findPreference("share_distance");
        String shareSummary = getString(R.string.swipe_distance_summary, shareDistance);
        sharePreference.setSummary(shareSummary);
    }

    @Override
    public void onResume() {
        PreferenceManager preferenceManager = getPreferenceManager();
        SharedPreferences sharedPreferences = preferenceManager.getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        PreferenceManager preferenceManager = getPreferenceManager();
        SharedPreferences sharedPreferences = preferenceManager.getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("pref_key_debug")) {
            Preference pref = findPreference(key);
            SMileCrypto.setDEBUG(sharedPreferences.getBoolean(key, false));
        } else if(key.equals("delete_distance")) {
            int value = sharedPreferences.getInt(key, 30);
            Preference pref = findPreference(key);
            String summary = getString(R.string.swipe_distance_summary, value);
            pref.setSummary(summary);
        } else if(key.equals("share_distance")) {
            int value = sharedPreferences.getInt(key, 20);
            Preference pref = findPreference(key);
            String summary = getString(R.string.swipe_distance_summary, value);
            pref.setSummary(summary);
        }
    }
}
