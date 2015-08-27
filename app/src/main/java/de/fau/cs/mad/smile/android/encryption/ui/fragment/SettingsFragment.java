package de.fau.cs.mad.smile.android.encryption.ui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.fau.cs.mad.smile.android.encryption.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equalsIgnoreCase("pref_key_direction")) {
            Preference pref = findPreference(key);
            if(sharedPreferences.getString(key, "1").equals("1")) {
                pref.setSummary(getString(R.string.sort_order_summary, R.string.sort_asc));
            } else {
                pref.setSummary(getString(R.string.sort_order_summary, R.string.sort_dec));
            }
        }
    }
}
