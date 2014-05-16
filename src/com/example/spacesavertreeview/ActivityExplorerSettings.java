package com.example.spacesavertreeview;

import java.util.List;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;

public class ActivityExplorerSettings extends PreferenceActivity {
	
	static Context objContext;
	static String strSummaryBase;
	static int intDisplayMaxWidthInDp ;
	static int intMaxIndentAmount;
	static int intMaxIndentValue;
	static int intMinIndentValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        objContext = this;

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
            
            objEditTextPreference = (EditTextPreference) findPreference("treenotes_default_user_indent_tab_width");
            strSummaryBase = getResources().getString(R.string.treenotes_default_user_indent_tab_width_summary_base);
            intDisplayMaxWidthInDp = clsUtils.GetDisplayMaxWidthInDp((Activity) objContext);
			intMaxIndentAmount = getResources().getInteger(R.integer.indent_amount_max);
			intMaxIndentValue = intDisplayMaxWidthInDp/intMaxIndentAmount;
			intMinIndentValue = getResources().getInteger(R.integer.indent_width_min_dp);
            String strValue = sharedPref.getString("treenotes_default_user_indent_tab_width", "");
			int intValue = Integer.parseInt(strValue.toString());
			if (intValue > intMaxIndentValue) {
				intValue = intMaxIndentValue;
			}
			if (intValue < intMinIndentValue) {
				intValue = intMinIndentValue;
			}
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("treenotes_default_user_indent_tab_width", Integer.toString(intValue));
			editor.commit();
		          
            objEditTextPreference.setSummary(BuildSummaryString(strSummaryBase, intValue));          		
            objEditTextPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// TODO Auto-generated method stub
					int intValue = Integer.parseInt(newValue.toString());		
					if (intValue > intMaxIndentValue) {
						clsUtils.MessageBox(objContext, "Selected value is too big. Must be less than " + intMaxIndentValue , true);
						return false;
					}
					if (intValue < intMinIndentValue) {
						clsUtils.MessageBox(objContext, "Selected value is too small. Must be larger than " + intMinIndentValue , true);
						return false;
					}
					preference.setSummary(BuildSummaryString(strSummaryBase, intValue)); 
					return true;
				}
			});
            
            sharedPref = null;
        }

		private String BuildSummaryString(String strSummaryBase, int intValue) {
			return strSummaryBase + " (Min: " + intMinIndentValue + ", Max: " + intMaxIndentValue + "): " + Integer.toString(intValue);
		}
    }
    
    @Override
    protected boolean isValidFragment(String fragmentName) {
    	// TODO Auto-generated method stub
    	return true;
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


