package com.example.spacesavertreeview;

import java.util.List;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.widget.Button;

public class ActivityExplorerSettings extends PreferenceActivity {
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add a button to the header list.
        if (hasHeaders()) {
            Button button = new Button(this);
            button.setText("Some action");
            setListFooter(button);
        }
        
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.layout.activity_explorer_startup_setting_headers, target);
    }
    
    /**
     * This fragment shows the preferences for the first header.
     */
    public static class Prefs1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            
            PreferenceManager.setDefaultValues(getActivity(), R.layout.activity_explorer_startup_setting_pref1, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.layout.activity_explorer_startup_setting_pref1);
            
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            
            EditTextPreference objEditTextPreference = (EditTextPreference) findPreference("registered_user");
            objEditTextPreference.setTitle(getResources().getString(R.string.pref_title_display_name) + ": " +
            		sharedPref.getString("registered_user", ""));
            
            objEditTextPreference = (EditTextPreference) findPreference("note_selecteditem_owner");
            objEditTextPreference.setTitle(getResources().getString(R.string.pref_title_note_subitem_owner_name) + ": " +
            		sharedPref.getString("note_selecteditem_owner", ""));
            
            objEditTextPreference = (EditTextPreference) findPreference("treenotes_root_folder_name");
            objEditTextPreference.setTitle("Root folder name: " +
            		sharedPref.getString("treenotes_root_folder_name", ""));
            objEditTextPreference.setSummary("");
            
            objEditTextPreference = (EditTextPreference) findPreference("treenotes_about");
            objEditTextPreference.setTitle(getResources().getString(R.string.pref_treenotes_about) + ": " +
            		getResources().getString(R.string.app_name));
            objEditTextPreference.setSummary(getResources().getString(R.string.pref_treenotes_version) + ": " +
            		sharedPref.getString("treenotes_version", ""));
            
            sharedPref = null;
        }
    }

    /**
     * This fragment shows the preferences for the second header.
     */
    public static class Prefs2Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from headers XML.
            Log.i("args", "Arguments: " + getArguments());

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.layout.activity_explorer_startup_setting_pref2);
        }
    }
    
    /**
     * This fragment contains a second-level set of preference that you
     * can get to by tapping an item in the first preferences fragment.
     */
    public static class Prefs1FragmentInner extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from preference XML.
            Log.i("args", "Arguments: " + getArguments());

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.layout.activity_explorer_startup_setting_pref1_inner);
        }
    }
}


