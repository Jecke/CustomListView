package com.treeapps.treenotes;

import java.util.List;

import com.treeapps.android.in_app_billing.util.IabHelper;
import com.treeapps.android.in_app_billing.util.IabResult;
import com.treeapps.android.in_app_billing.util.Inventory;
import com.treeapps.android.in_app_billing.util.Purchase;
import com.treeapps.treenotes.ActivityExplorerStartup.clsIabLocalData;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ActivityExplorerSettings extends PreferenceActivity {

	static Context objContext;
	static String strSummaryBase;
	static int intDisplayMaxWidthInDp;
	static int intMaxIndentAmount;
	static int intMaxIndentValue;
	static int intMinIndentValue;
	
	// In-app purchasing
	static IabHelper mHelper; 

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

			// Make sure default values are applied. In a real app, you would
			// want this in a shared function that is used to retrieve the
			// SharedPreferences wherever they are needed.

			PreferenceManager.setDefaultValues(getActivity(), R.layout.activity_explorer_startup_setting_pref1, false);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.layout.activity_explorer_startup_setting_pref1);

			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

			EditTextPreference objEditTextPreference = (EditTextPreference) findPreference("registered_user");
			objEditTextPreference.setTitle(getResources().getString(R.string.pref_title_display_name) + ": "
					+ sharedPref.getString("registered_user", ""));

			objEditTextPreference = (EditTextPreference) findPreference("note_selecteditem_owner");
			objEditTextPreference.setTitle(getResources().getString(R.string.pref_title_note_subitem_owner_name) + ": "
					+ sharedPref.getString("note_selecteditem_owner", ""));

			objEditTextPreference = (EditTextPreference) findPreference("treenotes_root_folder_name");
			objEditTextPreference.setTitle("Root folder name: "
					+ sharedPref.getString("treenotes_root_folder_name", ""));
			objEditTextPreference.setSummary("");

			objEditTextPreference = (EditTextPreference) findPreference("treenotes_about");
			objEditTextPreference.setTitle(getResources().getString(R.string.pref_treenotes_about) + ": "
					+ getResources().getString(R.string.app_name));
			objEditTextPreference.setSummary(getResources().getString(R.string.pref_treenotes_version) + ": "
					+ sharedPref.getString("treenotes_version", ""));

			objEditTextPreference = (EditTextPreference) findPreference("treenotes_default_user_indent_tab_width");
			strSummaryBase = getResources().getString(R.string.treenotes_default_user_indent_tab_width_summary_base);
			intDisplayMaxWidthInDp = clsUtils.GetDisplayMaxWidthInDp((Activity) objContext);
			intMaxIndentAmount = getResources().getInteger(R.integer.indent_amount_max);
			intMaxIndentValue = intDisplayMaxWidthInDp / intMaxIndentAmount;
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
						clsUtils.MessageBox(objContext, "Selected value is too big. Must be less than "
								+ intMaxIndentValue, true);
						return false;
					}
					if (intValue < intMinIndentValue) {
						clsUtils.MessageBox(objContext, "Selected value is too small. Must be larger than "
								+ intMinIndentValue, true);
						return false;
					}
					preference.setSummary(BuildSummaryString(strSummaryBase, intValue));
					return true;
				}
			});

			sharedPref = null;
		}

		private String BuildSummaryString(String strSummaryBase, int intValue) {
			return strSummaryBase + " (Min: " + intMinIndentValue + ", Max: " + intMaxIndentValue + "): "
					+ Integer.toString(intValue);
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

		Activity objActivity;
		ProgressDialog objProgressDialog;

		
		ActivityExplorerStartup.clsIabLocalData objIabLocalData;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			objActivity = getActivity();

			// Can retrieve arguments from headers XML.
			Log.i("args", "Arguments: " + getArguments());

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.layout.activity_explorer_startup_setting_pref2);

			// In-app billing
			objProgressDialog = new ProgressDialog(objActivity);
			SetupInAppBilling();
			
			// Check local Iab values
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objActivity);			
			objIabLocalData = clsUtils.LoadIabLocalValues(sharedPref, objIabLocalData);
			boolean boolIsAdvertsRemoved = objIabLocalData.boolIsAdsDisabledA || (!objIabLocalData.boolIsAdsDisabledB);
			
			// Update UI
			Preference pref = findPreference("button_remove_advertisements");
			pref.setEnabled(!boolIsAdvertsRemoved);
			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					// TODO Auto-generated method stub
					setWaitScreen(true);
					Log.d(ActivityExplorerStartup.TAG, "Launching purchase of adverts removal.");

					/*
					 * TODO: for security, generate your payload here for
					 * verification. See the comments on
					 * verifyDeveloperPayload() for more info. Since this is a
					 * SAMPLE, we just use an empty string, but on a production
					 * app you should carefully generate this.
					 */
					String payload = "";

					mHelper.launchPurchaseFlow(getActivity(), ActivityExplorerStartup.SKU_ADVERTS_REMOVED, 
							ActivityExplorerStartup.RC_REQUEST, mPurchaseFinishedListener, payload);
					return false;
				}
			});

			pref = findPreference("button_allow_time_travel");
			pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					// TODO Auto-generated method stub
					clsUtils.MessageBox(objContext, "We have it almost licked. Watch this space", true);
					return false;
				}
			});
		}
		
		@Override
		public void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			
			// very important:
	        Log.d(ActivityExplorerStartup.TAG, "Destroying helper.");
	        if (mHelper != null) {
	            mHelper.dispose();
	            mHelper = null;
	        }
		}

		// -------- In App Billing ------------------------------
		public void SetupInAppBilling() {

			/*
			 * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
			 * (that you got from the Google Play developer console). This is
			 * not your developer public key, it's the *app-specific* public
			 * key.
			 * 
			 * Instead of just storing the entire literal string here embedded
			 * in the program, construct the key at runtime from pieces or use
			 * bit manipulation (for example, XOR with some other string) to
			 * hide the actual key. The key itself is not secret information,
			 * but we don't want to make it easy for an attacker to replace the
			 * public key with one of their own and then fake messages from the
			 * server.
			 */

			// Create the helper, passing it our context and the public key to
			// verify signatures with
			Log.d(ActivityExplorerStartup.TAG, "Creating IAB helper.");
			mHelper = new IabHelper(objActivity,
					clsUtils.Unscramble(ActivityExplorerStartup.base64EncodedPublicKeyPartScrambled));

			// enable debug logging (for a production application, you should
			// set this to false).
			mHelper.enableDebugLogging(true);

			// Start setup. This is asynchronous and the specified listener
			// will be called once setup completes.
			Log.d(ActivityExplorerStartup.TAG, "Starting setup.");
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) {
					Log.d(ActivityExplorerStartup.TAG, "Setup finished.");

					if (!result.isSuccess()) {
						// There was a problem.
						clsUtils.MessageBox(objContext, "Problem setting up in-app billing: " + result, true);
						return;
					}

					// Have we been disposed of in the meantime? If so, quit.
					if (mHelper == null)
						return;

					// IAB is fully set up. Now, let's get an inventory of stuff
					// we own.
					Log.d(ActivityExplorerStartup.TAG, "Setup successful. Querying inventory.");
					mHelper.queryInventoryAsync(mGotInventoryListener);
				}
			});
		}

		// Listener that's called when we finish querying the items and
		// subscriptions we own
		IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
			public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
				Log.d(ActivityExplorerStartup.TAG, "Query inventory finished.");

				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null)
					return;

				// Is it a failure?
				if (result.isFailure()) {
					clsUtils.MessageBox(objContext, "Failed to query inventory: " + result, true);
					return;
				}

				Log.d(ActivityExplorerStartup.TAG, "Query inventory was successful.");

				/*
				 * Check for items we own. Notice that for each purchase, we
				 * check the developer payload to see if it's correct! See
				 * verifyDeveloperPayload().
				 */

				// Do we have the premium upgrade?
				Purchase objAdvertsDisabledPurchase = inventory
						.getPurchase(ActivityExplorerStartup.SKU_ADVERTS_REMOVED);
				boolean boolIsAdsDisabled = (objAdvertsDisabledPurchase != null && verifyDeveloperPayload(objAdvertsDisabledPurchase));
				clsIabLocalData objIabLocalData = new clsIabLocalData();
	            objIabLocalData.boolIsAdsDisabledA = boolIsAdsDisabled;
	            objIabLocalData.boolIsAdsDisabledB = !boolIsAdsDisabled;
	            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objContext);
	            clsUtils.SaveIabLocalValues(sharedPref, objIabLocalData);
	            
				Preference pref = findPreference("button_remove_advertisements");
				pref.setEnabled(true);
				
				Log.d(ActivityExplorerStartup.TAG, "User is "
						+ (boolIsAdsDisabled ? "WITHOUT ADVERTS" : "WITH_ADVERTS"));

				setWaitScreen(false);
				Log.d(ActivityExplorerStartup.TAG, "Initial inventory query finished; enabling main UI.");
			}
		};

		/** Verifies the developer payload of a purchase. */
		boolean verifyDeveloperPayload(Purchase p) {
			String payload = p.getDeveloperPayload();

			/*
			 * TODO: verify that the developer payload of the purchase is
			 * correct. It will be the same one that you sent when initiating
			 * the purchase.
			 * 
			 * WARNING: Locally generating a random string when starting a
			 * purchase and verifying it here might seem like a good approach,
			 * but this will fail in the case where the user purchases an item
			 * on one device and then uses your app on a different device,
			 * because on the other device you will not have access to the
			 * random string you originally generated.
			 * 
			 * So a good developer payload has these characteristics:
			 * 
			 * 1. If two different users purchase an item, the payload is
			 * different between them, so that one user's purchase can't be
			 * replayed to another user.
			 * 
			 * 2. The payload must be such that you can verify it even when the
			 * app wasn't the one who initiated the purchase flow (so that items
			 * purchased by the user on one device work on other devices owned
			 * by the user).
			 * 
			 * Using your own server to store and verify developer payloads
			 * across app installations is recommended.
			 */

			return true;
		}
		
		// Callback for when a purchase is finished
	    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
	        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
	            Log.d(ActivityExplorerStartup.TAG, "Purchase finished: " + result + ", purchase: " + purchase);

	            // if we were disposed of in the meantime, quit.
	            if (mHelper == null) return;

	            if (result.isFailure()) {
	                clsUtils.MessageBox(objActivity,"Error purchasing: " + result,true);
	                setWaitScreen(false);
	                return;
	            }
	            if (!verifyDeveloperPayload(purchase)) {
	            	clsUtils.MessageBox(objActivity,"Error purchasing. Authenticity verification failed.",true);
	                setWaitScreen(false);
	                return;
	            }

	            Log.d(ActivityExplorerStartup.TAG, "Purchase successful.");

	            if (purchase.getSku().equals(ActivityExplorerStartup.SKU_ADVERTS_REMOVED)) {
	            	SharedPreferences objSharedPreferences = PreferenceManager.getDefaultSharedPreferences(objActivity);
	            	objIabLocalData.boolIsAdsDisabledA = true;
	            	objIabLocalData.boolIsAdsDisabledB = false;
	                clsUtils.SaveIabLocalValues(objSharedPreferences, objIabLocalData);
	                Preference pref = findPreference("button_remove_advertisements");
	    			pref.setEnabled(false);
	            }
	            
	            setWaitScreen(false);
	        }
	    };
	    
	 
		// Enables or disables the "please wait" screen.
		void setWaitScreen(boolean set) {
			if (objProgressDialog == null) {
				objProgressDialog = new ProgressDialog(objActivity);
				objProgressDialog.setMessage("Processing..., please wait.");
			}
			if (set) {
				if (!objProgressDialog.isShowing()) {
					objProgressDialog.show();
				}
			} else {
				if (objProgressDialog.isShowing()) {
					objProgressDialog.dismiss();
				}
			}
		}

	}

	/**
	 * This fragment contains a second-level set of preference that you can get
	 * to by tapping an item in the first preferences fragment.
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d(ActivityExplorerStartup.TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

	    // Pass on the activity result to the helper for handling
	    if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
	        // not handled, so handle it ourselves (here's where you'd
	        // perform any handling of activity results not related to in-app
	        // billing...
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	    else {
	        Log.d(ActivityExplorerStartup.TAG, "onActivityResult handled by IABUtil.");
	    }
	}

}
