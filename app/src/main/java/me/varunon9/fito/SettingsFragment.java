package me.varunon9.fito;


import android.os.Bundle;

import android.support.v7.preference.PreferenceFragmentCompat;
public class SettingsFragment extends PreferenceFragmentCompat {


    public SettingsFragment() {
        // Required empty public constructor
    }
    //getDefaultSharedPreferences

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.app_preferences);
    }

}
