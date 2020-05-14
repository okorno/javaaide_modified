/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.duy.ide.editor.editor.R;
import com.jecelyin.editor.v2.ThemeSupportActivity;
import com.jecelyin.editor.v2.Preferences;


/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 * <p/>
 */
public class EditorSettingsActivity extends ThemeSupportActivity {

    public static void open(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, EditorSettingsActivity.class), requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.settings);

        // Display the fragment as the main content.
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @author Jecelyin Peng <jecelyin@gmail.com>
     */
    public static class SettingsFragment extends PreferenceFragment implements android.preference.Preference.OnPreferenceClickListener {
        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private static android.preference.Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object value) {
                if (value == null)
                    return true;
                String stringValue = value.toString();
                String key = preference.getKey();

                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);
                } else if (preference instanceof SwitchPreference) {
                    ((SwitchPreference) preference).setChecked((boolean) value);
                } else if ("pref_highlight_file_size_limit".equals(key)) {
                    preference.setSummary(stringValue + " KB");
                } else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(String.valueOf(stringValue));
                }

                return true;
            }
        };

        private static void dependBindPreference(PreferenceGroup pg) {
            int count = pg.getPreferenceCount();
            android.preference.Preference preference;
            String key;
            Object value;

            Preferences prefercence = Preferences.getInstance(pg.getContext());

            for (int i = 0; i < count; i++) {
                preference = pg.getPreference(i);
                key = preference.getKey();

                if (preference instanceof PreferenceGroup) {
                    dependBindPreference((PreferenceGroup) preference);
                    continue;
                }

                Class<? extends android.preference.Preference> cls = preference.getClass();
                if (cls.equals(android.preference.Preference.class))
                    continue;

                value = prefercence.getValue(key);

                if (preference instanceof ListPreference) {
                } else if (preference instanceof EditTextPreference) {
                    ((EditTextPreference) preference).setText(String.valueOf(value));
                } else if (preference instanceof SwitchPreference) {
                    ((SwitchPreference) preference).setChecked(Boolean.valueOf(String.valueOf(value)));
                }

                if (!Preferences.KEY_SYMBOL.equals(key))
                    bindPreferenceSummaryToValue(preference);
            }
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         *
         * @see #sBindPreferenceSummaryToValueListener
         */
        private static void bindPreferenceSummaryToValue(android.preference.Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            String key = preference.getKey();
            Object value = Preferences.getInstance(preference.getContext()).getValue(key);
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_editor);

            dependBindPreference(getPreferenceScreen());
        }

        @Override
        public boolean onPreferenceClick(android.preference.Preference preference) {
            return true;
        }

    }
}
