package com.theducksparadise.jukebox;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.util.List;

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
 */
public class SettingsActivity extends PreferenceActivity {
    public static final String PREFERENCE_FILE = "JukeboxPreferences";
    public static final String PREFERENCE_KEY_WHITELIST = "whitelist_picker";
    public static final String PREFERENCE_KEY_BLACKLIST = "blacklist_picker";
    public static final String PREFERENCE_KEY_TWITCH_ENABLED = "twitch_enabled";
    public static final String PREFERENCE_KEY_TWITCH_ACCOUNT = "twitch_account";
    public static final String PREFERENCE_KEY_TWITCH_PASSWORD = "twitch_password";
    public static final String PREFERENCE_KEY_TWITCH_CHANNEL = "twitch_channel";
    public static final String PREFERENCE_KEY_TWITCH_REQUEST_LIMIT = "twitch_request_limit";
    public static final String PREFERENCE_KEY_TWITCH_REQUEST_ALL = "twitch_request_all";

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static final int WHITELIST_WINDOW = 1;
    private static final int BLACKLIST_WINDOW = 2;

    private PreferenceScreen filePickerControl;

    private Preference refreshDatabaseControl;

    private Preference clearDatabaseControl;

    private CheckBoxPreference twitchBotEnabledControl;

    private EditTextPreference twitchUserControl;

    private EditTextPreference twitchPasswordControl;

    private EditTextPreference twitchChannelControl;

    private EditTextPreference twitchRequestLimitControl;

    private CheckBoxPreference twitchRequestAllControl;

    private PreferenceScreen whiteListPickerControl;

    private PreferenceScreen blackListPickerControl;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_general);

        filePickerControl = (PreferenceScreen)findPreference("file_picker");

        filePickerControl.getIntent().putExtra(DirectoryPicker.ONLY_DIRS, true);
        filePickerControl.getIntent().putExtra(DirectoryPicker.ALLOW_BACKTRACK, true);
        setDefaultDirectory(loadStringPreference(filePickerControl.getKey(), null));

        filePickerControl.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(preference.getIntent(), DirectoryPicker.PICK_DIRECTORY);
                return true;
            }
        });

        whiteListPickerControl = (PreferenceScreen)findPreference(PREFERENCE_KEY_WHITELIST);

        setWhitelist(loadStringPreference(PREFERENCE_KEY_WHITELIST, ""));

        whiteListPickerControl.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(preference.getIntent(), TagListActivity.PICK_TAGS);
                return true;
            }
        });

        blackListPickerControl = (PreferenceScreen)findPreference(PREFERENCE_KEY_BLACKLIST);

        setBlacklist(loadStringPreference(PREFERENCE_KEY_BLACKLIST, ""));

        blackListPickerControl.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivityForResult(preference.getIntent(), TagListActivity.PICK_TAGS);
                return true;
            }
        });

        refreshDatabaseControl = findPreference("refresh_db");

        refreshDatabaseControl.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                setWhitelist("");
                saveStringPreference(PREFERENCE_KEY_WHITELIST, "");
                Intent intent = new Intent(getApplicationContext(), RefreshDatabaseWaitActivity.class);
                intent.putExtra(RefreshDatabaseWaitActivity.PATH, loadStringPreference(filePickerControl.getKey(), null));
                startActivity(intent);
                return true;
            }
        });

        clearDatabaseControl = findPreference("clear_db");

        clearDatabaseControl.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                doClearDatabase(preference.getContext());
                return true;
            }
        });

        twitchBotEnabledControl = (CheckBoxPreference)findPreference("twitch_enable");
        initializeBooleanControl(twitchBotEnabledControl, PREFERENCE_KEY_TWITCH_ENABLED);

        twitchUserControl = (EditTextPreference)findPreference("twitch_account");
        initializeTextControl(twitchUserControl, PREFERENCE_KEY_TWITCH_ACCOUNT);

        twitchPasswordControl = (EditTextPreference)findPreference("twitch_password");
        initializeTextControl(twitchPasswordControl, PREFERENCE_KEY_TWITCH_PASSWORD);

        twitchChannelControl = (EditTextPreference)findPreference("twitch_channel");
        initializeTextControl(twitchChannelControl, PREFERENCE_KEY_TWITCH_CHANNEL);

        twitchRequestAllControl = (CheckBoxPreference)findPreference("twitch_request_all");
        initializeBooleanControl(twitchRequestAllControl, PREFERENCE_KEY_TWITCH_REQUEST_ALL);

        twitchRequestLimitControl = (EditTextPreference)findPreference("twitch_request_limit");
        initializeNumericControl(twitchRequestLimitControl, PREFERENCE_KEY_TWITCH_REQUEST_LIMIT, 300);

    }

    private void initializeTextControl(final EditTextPreference control, final String key) {
        String value = loadStringPreference(key, "");
        control.setDefaultValue(value);
        control.setSummary(value);

        control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                saveStringPreference(key, newValue.toString());
                control.setSummary(newValue.toString());
                return true;
            }
        });
    }

    private void initializeNumericControl(final EditTextPreference control, final String key, int defaultValue) {
        final int value = getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE).getInt(key, defaultValue);
        control.setDefaultValue(value);
        control.setSummary(value);

        control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(key, Integer.valueOf(newValue.toString()));
                editor.apply();
                control.setSummary(newValue.toString());
                return true;
            }
        });
    }

    private void initializeBooleanControl(final CheckBoxPreference control, final String key) {
        control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(key, (Boolean)newValue);
                editor.apply();
                return true;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            bindPreferenceSummaryToValue(findPreference("example_list"));
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras = data.getExtras();

        if (requestCode == DirectoryPicker.PICK_DIRECTORY && resultCode == RESULT_OK) {
            String path = (String)extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
            saveStringPreference(filePickerControl.getKey(), path);
            setDefaultDirectory(path);
        } else if (requestCode == TagListActivity.PICK_TAGS && resultCode == RESULT_OK) {
            String tags = (String)extras.get(TagListActivity.CHOSEN_TAGS);
            Integer windowType = (Integer)extras.get(TagListActivity.WINDOW_TYPE);

            if (windowType == WHITELIST_WINDOW) {
                saveStringPreference(PREFERENCE_KEY_WHITELIST, tags);
                setWhitelist(tags);
                MusicDatabase.getInstance(getApplicationContext()).filter(tags, loadStringPreference(PREFERENCE_KEY_BLACKLIST, ""));
            } else if (windowType == BLACKLIST_WINDOW) {
                saveStringPreference(PREFERENCE_KEY_BLACKLIST, tags);
                setBlacklist(tags);
                MusicDatabase.getInstance(getApplicationContext()).filter(loadStringPreference(PREFERENCE_KEY_WHITELIST, ""), tags);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TwitchBot.reconfigure(getApplicationContext());
    }

    private void saveStringPreference(String key, String value) {
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply(); // This was .commit() but Intellij thinks it's wrong
    }

    private String loadStringPreference(String key, String defaultValue) {
        return getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE).getString(key, defaultValue);
    }

    private void setDefaultDirectory(String path) {
        filePickerControl.getIntent().putExtra(DirectoryPicker.START_DIR, path);
        filePickerControl.setSummary(path == null ? "Nothing Selected" : path);
    }

    private void setWhitelist(String tags) {
        whiteListPickerControl.getIntent().putExtra(TagListActivity.SELECTED_TAGS, tags);
        whiteListPickerControl.getIntent().putExtra(TagListActivity.WINDOW_TYPE, WHITELIST_WINDOW);
        whiteListPickerControl.setSummary(tags == null || tags.equals("") ? "All" : tags);
    }

    private void setBlacklist(String tags) {
        blackListPickerControl.getIntent().putExtra(TagListActivity.SELECTED_TAGS, tags);
        blackListPickerControl.getIntent().putExtra(TagListActivity.WINDOW_TYPE, BLACKLIST_WINDOW);
        blackListPickerControl.setSummary(tags == null || tags.equals("") ? "None" : tags);
    }

    private void doClearDatabase(final Context context) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        saveStringPreference(PREFERENCE_KEY_WHITELIST, "");
                        setWhitelist("");
                        MusicDatabase.getInstance(context).clearDatabase();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // 'No' button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}
