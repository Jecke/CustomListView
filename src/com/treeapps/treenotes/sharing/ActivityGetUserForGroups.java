package com.treeapps.treenotes.sharing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;



import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.sharing.clsMessaging.clsGetUsersCmd;
import com.treeapps.treenotes.sharing.clsMessaging.clsGetUsersResponse;
import com.treeapps.treenotes.sharing.clsMessaging.clsMsgUser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class ActivityGetUserForGroups extends ListActivity {

	// Messaging
	clsMessaging objMessaging = new clsMessaging();

	public static final String WEBSERVER_URL = "com.treeapps.treenotes.WEBSERVER_URL";
	public static final String REGISTERED_USER_UUID = "com.treeapps.treenotes.REGISTERED_USER_UUID";;

	public class clsGetUsersForGroupsListViewState {
		clsMessaging.clsMsgUser objMsgUser;
		boolean boolIsChecked = false;
	}

	public class clsSession {
		public String strWebserverUrl;
		public File fileTreeNodesDir;
		ArrayList<clsGetUsersForGroupsListViewState> objListViewStates = new ArrayList<clsGetUsersForGroupsListViewState>();
		String strRegisteredUserUuid;
	}

	public clsSession objSession = new clsSession();
	clsGetUserForGroupsArrayAdapter objGetUsersArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_users_for_groups);

		if (savedInstanceState == null) {
			Bundle objBundle = getIntent().getExtras();
			objSession.strWebserverUrl = objBundle.getString(ActivityGetUserForGroups.WEBSERVER_URL);
			objSession.strRegisteredUserUuid = objBundle.getString(ActivityGetUserForGroups.REGISTERED_USER_UUID);

			objSession.fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(this));
			if (!objSession.fileTreeNodesDir.exists()) {
				objSession.fileTreeNodesDir.mkdirs();
			}

			// Show the Up button in the action bar.
			setupActionBar();

			// Setup listView
			objGetUsersArrayAdapter = new clsGetUserForGroupsArrayAdapter(this, R.layout.get_users_list_item,
					objSession.objListViewStates);
			setListAdapter(objGetUsersArrayAdapter);

			SaveFile();
			clsUtils.CustomLog("ActivityGetUserForGroups onCreate SaveFile");
		} else {
			LoadFile();
			clsUtils.CustomLog("ActivityGetUserForGroups onCreate SaveFile");
		}

		UpdateScreen();

		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		String query = intent.getStringExtra(SearchManager.QUERY);
		GetUsersAsyncTask objGetUsersTask = new GetUsersAsyncTask(this);
		objGetUsersTask.execute(query);

	}

	private void SaveFile() {
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityName", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		String strSession = clsUtils.SerializeToString(objSession);
		editor.putString("objSession", strSession);
		editor.commit();
		sharedPref = null;
	}

	private void LoadFile() {
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityName", Context.MODE_PRIVATE);
		String strSession = sharedPref.getString("objSession", "");
		objSession = clsUtils.DeSerializeFromString(strSession, objSession.getClass());
		objSession.fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(this));
		if (objGetUsersArrayAdapter == null) {
			objGetUsersArrayAdapter = new clsGetUserForGroupsArrayAdapter(this, R.layout.get_users_list_item,
					objSession.objListViewStates);
			setListAdapter(objGetUsersArrayAdapter);
		} else {
			objGetUsersArrayAdapter.clear();
			objGetUsersArrayAdapter.addAll(objSession.objListViewStates);
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		LoadFile();
		super.onStart();
		clsUtils.CustomLog("ActivityGetUserForGroups onStart");
	}

	@Override
	protected void onRestart() {
		LoadFile();
		super.onRestart();
		clsUtils.CustomLog("ActivityGetUserForGroups onRestart");
	}

	@Override
	protected void onResume() {
		LoadFile();
		UpdateScreen();
		super.onResume();
		clsUtils.CustomLog("ActivityGetUserForGroups onResume");
	}

	@Override
	protected void onStop() {
		SaveFile();
		super.onStop();
		clsUtils.CustomLog("ActivityGetUserForGroups onStop");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		SaveFile();
		super.onPause();
		clsUtils.CustomLog("ActivityGetUserForGroups onPause");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		SaveFile();
		super.onDestroy();
		clsUtils.CustomLog("ActivityGetUserForGroups onDestroy");
	}

	private void UpdateScreen() {

	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_search_users_for_groups, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.actionAccept:
			// Add to group users local dbase
			clsGroupMembers objGroupMembers = new clsGroupMembers(this);
			objGroupMembers.LoadFile();
			AddSelectedUsersToAll(objGroupMembers);
			objGroupMembers.SaveFile();
			Intent objIntent = getIntent();
			objIntent.setAction("");
			setResult(RESULT_OK, objIntent);
			finish();
			return true;
		case R.id.actionCancel:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class clsGetUsersResult {
		static final int ERROR_NONE = 0;
		static final int ERROR_NETWORK = 1;
		public int intErrorCode = ERROR_NONE;
		public String strErrorMessage = "";
		public ArrayList<clsMessaging.clsMsgUser> objMsgUsers = new ArrayList<clsMessaging.clsMsgUser>();
	}

	public class GetUsersAsyncTask extends AsyncTask<String, Void, clsGetUsersResult> {

		ProgressDialog objProgressDialog;

		public GetUsersAsyncTask(Activity objActivity) {
			objProgressDialog = new ProgressDialog(objActivity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			objProgressDialog.setMessage("Processing..., please wait.");
			objProgressDialog.show();
		}

		@Override
		protected clsGetUsersResult doInBackground(String... params) {
			// TODO: attempt authentication against a network service.
			clsGetUsersResult objResult = new clsGetUsersResult();

			// Action and get data from server
			clsGetUsersCmd objGetUserCmd = objMessaging.new clsGetUsersCmd();
			objGetUserCmd.setStrSearchTerm(params[0]);
			objGetUserCmd.setIntSeqNum(0);
			Gson gson = new Gson();
			String strJsonCommand = gson.toJson(objGetUserCmd);
			InputStream stream = null;
			JSONObject objJsonResult;
			try {
				URL urlFeed = new URL(objSession.strWebserverUrl + "/api/TreeManager/GetUsers");
				stream = clsMessaging.downloadUrl(urlFeed, strJsonCommand);
				objJsonResult = clsMessaging.updateLocalFeedData(stream);
				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.wtf("myCustom", "JSON exception", e);
				objResult.intErrorCode = clsGetUsersResult.ERROR_NETWORK;
				objResult.strErrorMessage = "JSON exception. " + e.getMessage();
				return objResult;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.wtf("myCustom", "IO exception", e);
				objResult.intErrorCode = clsGetUsersResult.ERROR_NETWORK;
				objResult.strErrorMessage = "IO exception. " + e.getMessage();
				return objResult;
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.wtf("myCustom", "IO exception", e);
						objResult.intErrorCode = clsGetUsersResult.ERROR_NETWORK;
						objResult.strErrorMessage = "IO exception. " + e.getMessage();
						return objResult;
					}
				}
			}

			// Analize data from server
			clsGetUsersResponse objGetUsersResponse = gson
					.fromJson(objJsonResult.toString(), clsGetUsersResponse.class);
			if (objGetUsersResponse.getIntErrorNum() == 0) {
				objResult.intErrorCode = clsGetUsersResult.ERROR_NONE;
				objResult.objMsgUsers = objGetUsersResponse.objMsgUsers;
			} else {
				objResult.intErrorCode = clsGetUsersResult.ERROR_NETWORK;
				objResult.strErrorMessage = objGetUsersResponse.getStrErrMessage();
			}
			return objResult;
		}

		@Override
		protected void onPostExecute(final clsGetUsersResult success) {
			super.onPostExecute(success);

			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}

			if (success.intErrorCode == clsGetUsersResult.ERROR_NONE) {
				objSession.objListViewStates.clear();
				for (clsMsgUser objFoundMsgUser : success.objMsgUsers) {
					if (!objFoundMsgUser.getStrUserUuid().equals(objSession.strRegisteredUserUuid)) {
						clsGetUsersForGroupsListViewState objListViewState = new clsGetUsersForGroupsListViewState();
						objListViewState.objMsgUser = objFoundMsgUser;
						objListViewState.boolIsChecked = false;
						objSession.objListViewStates.add(objListViewState);
					}
				}
				SaveFile();
				objGetUsersArrayAdapter.clear();
				objGetUsersArrayAdapter.addAll(objSession.objListViewStates);
				getListView().invalidateViews();
				if (objSession.objListViewStates.size() == 0) {
					Toast.makeText(getApplicationContext(),
							"Your search did not return any results. Please change your search criteria",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getApplicationContext(), success.strErrorMessage, Toast.LENGTH_SHORT).show();
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	private void AddSelectedUsersToAll(clsGroupMembers objGroupMembers) {

		// Add or overwrite

		for (clsGetUsersForGroupsListViewState objListViewState : objSession.objListViewStates) {
			if (objListViewState.boolIsChecked) {
				// Check if not already existing
				clsGroupMembers.clsUser objUser = objGroupMembers.GetFromUuid(objListViewState.objMsgUser
						.getStrUserUuid());
				if (objUser == null) {
					// Not, add
					objUser = objGroupMembers.new clsUser();
					objUser.strUserName = objListViewState.objMsgUser.getStrUserName();
					objUser.strUserUuid = objListViewState.objMsgUser.getStrUserUuid();
					objGroupMembers.objMembersRepository.objUsers.add(objUser);
				} else {
					// Existing, overwrite
					objUser.strUserName = objListViewState.objMsgUser.getStrUserName();
					objUser.strUserUuid = objListViewState.objMsgUser.getStrUserUuid();
				}
			}
		}
	}

}
