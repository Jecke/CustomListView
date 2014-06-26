package com.treeapps.treenotes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;


import com.google.gson.Gson;
import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsMessaging.clsGetUsersCmd;
import com.treeapps.treenotes.sharing.clsMessaging.clsGetUsersResponse;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.TextView;

public class ActivitySearchUsersForGroups extends ListActivity {

	public static final String WEBSERVER_URL = "com.treeapps.treenotes.WEBSERVER_URL";
	
	// Messaging
    clsMessaging objMessaging = new clsMessaging();
    ArrayList<clsMessaging.clsMsgUser> objMsgUsers =  new ArrayList<clsMessaging.clsMsgUser>();
    static String strWebserverUrl;
    
    // Views
    ListView listView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users_for_groups);
        
        Bundle objBundle = getIntent().getExtras();    		
        strWebserverUrl  = objBundle.getString(ActivitySearchUsersForGroups.WEBSERVER_URL);
        // Show the Up button in the action bar.
        setupActionBar();
        clsGetUsersArrayAdapter objGetUsersArrayAdapter = new clsGetUsersArrayAdapter(this, R.layout.get_users_list_item, objMsgUsers);
        
        setListAdapter(objGetUsersArrayAdapter);
        
		listView = getListView();
		listView.setTextFilterEnabled(true);
 
		listView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 Toast.makeText(getApplicationContext(),((TextView) v).getText(), Toast.LENGTH_SHORT).show();
			}
		});
			

        
     // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          GetUsersTask objGetUsersTask = new GetUsersTask(this);
          objGetUsersTask.execute(query);
          
        }
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
        }
        return super.onOptionsItemSelected(item);
    }
    
    public class clsGetUsersResult {
    	static final int ERROR_NONE = 0;
    	static final int ERROR_NETWORK = 1;
    	public int intErrorCode = ERROR_NONE;
    	public String strErrorMessage = "";
    	public ArrayList<clsMessaging.clsMsgUser> objUsers = new ArrayList<clsMessaging.clsMsgUser>();  	
    }

    public class GetUsersTask extends AsyncTask<String, Void, clsGetUsersResult> {
    	
    ProgressDialog objProgressDialog;
    
    public GetUsersTask (Activity objActivity) {
    	objProgressDialog = new ProgressDialog(objActivity);
    }
    
    @Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		objProgressDialog.setMessage("Processing..., please wait.");
		objProgressDialog.show();
	}
    	
    @Override
    protected clsGetUsersResult doInBackground(String... params) {
        // TODO: attempt authentication against a network service.
    	clsGetUsersResult objResult  = new clsGetUsersResult();
    	
    	// Action and get data from server
    	clsGetUsersCmd objGetUserCmd = objMessaging.new clsGetUsersCmd();
    	objGetUserCmd.setStrSearchTerm(params[0]);
    	objGetUserCmd.setIntSeqNum(0);
		Gson gson = new Gson();
		String strJsonCommand = gson.toJson(objGetUserCmd);
    	InputStream stream = null;
    	JSONObject objJsonResult;
        try {
        	URL urlFeed = new URL(strWebserverUrl + "/api/TreeManager/GetUsers");
            stream = clsMessaging.DownloadUrl(urlFeed, strJsonCommand);
            objJsonResult = clsMessaging.UpdateLocalFeedData(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.wtf("myCustom", "JSON exception", e);
			objResult.intErrorCode=clsGetUsersResult.ERROR_NETWORK;
			objResult.strErrorMessage = "JSON exception. " + e.getMessage();
            return objResult;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.wtf("myCustom", "IO exception", e);
			objResult.intErrorCode=clsGetUsersResult.ERROR_NETWORK;
			objResult.strErrorMessage = "IO exception. " + e.getMessage();
            return objResult;
		} finally {
            if (stream != null) {
                try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.wtf("myCustom", "IO exception", e);
					objResult.intErrorCode=clsGetUsersResult.ERROR_NETWORK;
					objResult.strErrorMessage = "IO exception. " + e.getMessage();
		            return objResult;
				}
            }
        }
    	
    	// Analize data from server	
		clsGetUsersResponse objGetUsersResponse = gson.fromJson(objJsonResult.toString(),clsGetUsersResponse.class);
		if (objGetUsersResponse.getIntErrorNum() == 0) {
			objResult.intErrorCode=clsGetUsersResult.ERROR_NONE;
			objResult.objUsers = objGetUsersResponse.objMsgUsers;
		} else {
			objResult.intErrorCode=clsGetUsersResult.ERROR_NETWORK;
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
        	objMsgUsers = success.objUsers;
        	listView.invalidate();
        } else {
            Toast.makeText(getApplicationContext(),success.strErrorMessage, Toast.LENGTH_SHORT).show();
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
    	
        	
        

}
