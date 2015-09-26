package com.example.adityadev.spotifystreamermasterui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.TwoStatePreference;

import java.util.Locale;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private TwoStatePreference showLockScreenControlsPreference;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        addPreferencesFromResource(R.xml.pref_general);
        showLockScreenControlsPreference = (TwoStatePreference) findPreference(getString(R.string.lock_screen_pref_key));
        if (showLockScreenControlsPreference != null) {

            showLockScreenControlsPreference.setEnabled(true);
        }
        showLockScreenControlsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object showLockScreenControlsObj) {

                SharedPreferences.Editor sharedPrefencesEditor = sharedPreferences.edit();
                sharedPrefencesEditor.putBoolean(getString(R.string.lock_screen_controls), ((Boolean) showLockScreenControlsObj).booleanValue());
                sharedPrefencesEditor.commit();

                Intent broadcastLockScreenIntent = new Intent();
                broadcastLockScreenIntent.setAction(getString(R.string.is_lock_screen_visible));
                sendBroadcast(broadcastLockScreenIntent);

                return true;
            }
        });


        ListPreference listOfCountriesPreference = (ListPreference) findPreference(getString(R.string.country_code_pref_key));
        if (null != listOfCountriesPreference) {
            listOfCountriesPreference.setEntryValues(Locale.getISOCountries());
            String[] countries = new String[listOfCountriesPreference.getEntryValues().length];
            for (int i = 0; i < listOfCountriesPreference.getEntryValues().length; i++) {
                countries[i] = new Locale("", Locale.getISOCountries()[i]).getDisplayCountry();
            }
            listOfCountriesPreference.setEntries(countries);
            listOfCountriesPreference.setDefaultValue(Locale.US);
        }

        bindPreferenceSummaryToValue(findPreference(getString(R.string.country_code_pref_key)));

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newObj) {
        String objValue = newObj.toString();

        if (preference instanceof ListPreference) {

            ListPreference listPreference = (ListPreference) preference;
            int indexOfPref = listPreference.findIndexOfValue(objValue);
            if (indexOfPref >= 0) {
                preference.setSummary(listPreference.getEntries()[indexOfPref]);
            }
        } else {
            preference.setSummary(objValue);
        }
        return true;
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
