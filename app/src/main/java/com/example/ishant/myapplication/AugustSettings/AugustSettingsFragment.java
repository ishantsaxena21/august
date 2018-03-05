package com.example.ishant.myapplication.AugustSettings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.ishant.myapplication.R;

/**
 * Created by ishant on 7/1/18.
 */

public class AugustSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_august_settings);
    }
}
