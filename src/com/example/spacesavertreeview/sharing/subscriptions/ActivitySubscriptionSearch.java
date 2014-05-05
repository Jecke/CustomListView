package com.example.spacesavertreeview.sharing.subscriptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsUtils;
import com.example.spacesavertreeview.sharing.clsMessaging;
import com.example.spacesavertreeview.sharing.clsMessaging.clsMsg;
import com.example.spacesavertreeview.sharing.clsMessaging.clsSyncResult;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ActivitySubscriptionSearch  extends ListActivity {

	public static class clsListViewStateSearch 
	{
		String strNoteName;
		String strNoteUuid;
		String strUserName;
		String strUserUuid;
		boolean boolIsChecked = false;
	}
	
	static Context objContext;
	
	public static final String SEARCH_RESULTS = "com.example.spacesavertreeview.sharing.subscriptions_searchresults";
	public static final String AUTHOR_SEARCHTERM = "com.example.spacesavertreeview.sharing.subscriptions_searchresults_author_searchterm";
	public static final String NOTENAME_SEARCHTERM = "com.example.spacesavertreeview.sharing.subscriptions_searchresults_notename_searchterm";
	public static final String URL = "com.example.spacesavertreeview.sharing.subscriptions_searchresults_url";
	
	public static ArrayList<clsListViewStateSearch> objListViewStates = new ArrayList<clsListViewStateSearch>();
	public static clsActivitySubscriptionSearchArrayAdapter objArrayAdapter;
	public static clsMessaging objMessaging = new clsMessaging();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		objContext = this;
		
		setContentView(R.layout.activity_subscription_search);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}

		objArrayAdapter = new clsActivitySubscriptionSearchArrayAdapter(this,
				R.layout.activity_subscriptions_list_item, objListViewStates);

		setListAdapter(objArrayAdapter);
		
		Bundle objBundle = getIntent().getExtras();   
		String strUrl    = objBundle.getString(URL);
		URL url;
		try {
			url = new URL(strUrl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			clsUtils.MessageBox(objContext, e.getMessage());
			return;
		}
		
		String strAuthorSearchTerm    = objBundle.getString(AUTHOR_SEARCHTERM);
		String strNoteNameSearchTerm    = objBundle.getString(NOTENAME_SEARCHTERM);
		clsPublicationSearchCommandMsg objCommandMsg = new clsPublicationSearchCommandMsg();
		objCommandMsg.strAuthorUserNameSearchTerm = strAuthorSearchTerm;
		objCommandMsg.stNoteNameSearchTerm = strNoteNameSearchTerm;
		
		
		ActivityPublicationsSearchAsyncTask objAsyncTask = new ActivityPublicationsSearchAsyncTask(this, url, objCommandMsg);
		objAsyncTask.execute("");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_subscription_search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_publications_add) {
			Intent intent = getIntent();
			// Make list smaller
			ArrayList<clsListViewStateSearch> objToBeRemoved = new ArrayList<clsListViewStateSearch>();
			for (clsListViewStateSearch objListViewState: objListViewStates) {
				if (objListViewState.boolIsChecked == false) {
					objToBeRemoved.add(objListViewState);
				}		
			}
			objListViewStates.removeAll(objToBeRemoved);
			// Pass list back
			String strSelection = clsUtils.SerializeToString(objListViewStates);
			intent.putExtra(ActivitySubscriptionSearch.SEARCH_RESULTS, strSelection);
			setResult(RESULT_OK, intent);
			this.finish();
			super.onBackPressed();
			return true;
		}
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_activity_subscription_search, container,
					false);
			return rootView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}































	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub 	
		this.finish();
		super.onBackPressed();
		return; 

	}

	
	// -------------------------- Communications ----------------------------------
	

	 	public class clsPublicationSearchCommandMsg extends clsMsg{
	        public String strAuthorUserNameSearchTerm = "";
	        public String stNoteNameSearchTerm = "";
	        
	        clsPublicationSearchCommandMsg() { objMessaging.super();}
	    }
	    
	 	public class clsPublicationData {
	 		String strNoteName;
			String strNoteUuid;
			String strUserName;
			String strUserUuid;
	 	}
	 	
	    public class clsPublicationSearchResponseMsg extends clsMsg {
	    	public ArrayList<clsPublicationData> lstPublicationDatas;
	    	clsPublicationSearchResponseMsg() { objMessaging.super();}
	    }
	   		

	        
	    public static class ActivityPublicationsSearchAsyncTask extends AsyncTask<String, Void, clsPublicationSearchResponseMsg> {
	   	
		 	public static final int ERROR_NONE = 0;
	    	public static final int ERROR_NETWORK = 1;
	   		
	   		static Exception mException = null;
	   		static clsPublicationSearchCommandMsg objCommand;
	   		static URL urlFeed;
	   		ProgressDialog objProgressDialog;

	   		public ActivityPublicationsSearchAsyncTask (Activity objActivity,URL urlFeed, clsPublicationSearchCommandMsg objCommandMsg) {
	   			ActivityPublicationsSearchAsyncTask.objCommand = objCommandMsg;
	   			ActivityPublicationsSearchAsyncTask.urlFeed = urlFeed;
	   			objProgressDialog = new ProgressDialog(objActivity);
	   		}
	   		
	   		
	   		@Override
	   	    protected void onPreExecute()
	   	    {
	   	        super.onPreExecute();
	   	        mException = null;
	   	        objProgressDialog.setMessage("Processing..., please wait.");
				objProgressDialog.show();
	   	    }

	   		@Override
	   		protected clsPublicationSearchResponseMsg doInBackground(String... arg0) {
	   			// TODO Auto-generated method stub
	   			Gson gson = new Gson();
	   			JSONObject objJsonResult = null;
	   			clsPublicationSearchResponseMsg objResponseMsg = ((ActivitySubscriptionSearch)objContext).new clsPublicationSearchResponseMsg();
	   			
	   			try {
	   	            InputStream stream = null;
	   	            String strJsonCommand = gson.toJson(objCommand);

	   	            try {
	   	                Log.i("myCustom", "Streaming data from network: " + urlFeed);
	   	                stream = clsMessaging.downloadUrl(urlFeed, strJsonCommand);
	   	                objJsonResult = clsMessaging.updateLocalFeedData(stream);
	   	                // Makes sure that the InputStream is closed after the app is
	   	                // finished using it.
	   				} catch (JSONException e) {
	   					// TODO Auto-generated catch block
	   					objResponseMsg.intErrorCode=clsSyncResult.ERROR_NETWORK;
	   					objResponseMsg.strErrorMessage = "JSON exception. " + e;
	   		            return objResponseMsg;
	   				} finally {
	   	                if (stream != null) {
	   	                    stream.close();
	   	                }
	   	            }
	   	        } catch (MalformedURLException e) {
	   	        	objResponseMsg.intErrorCode=clsSyncResult.ERROR_NETWORK;
	   	        	objResponseMsg.strErrorMessage = "Feed URL is malformed. " + e;
			        return objResponseMsg;
	   	        } catch (IOException e) {
	   	        	objResponseMsg.intErrorCode=clsSyncResult.ERROR_NETWORK;
	   	        	objResponseMsg.strErrorMessage = "Error reading from network. " + e;
			        return objResponseMsg;
	   	        }
	   			// Convert data from server	
	   			objResponseMsg = gson.fromJson(objJsonResult.toString(),clsPublicationSearchResponseMsg.class);
	   	        return objResponseMsg;
	   		}
	   		@Override
	   		protected void onPostExecute(clsPublicationSearchResponseMsg objResponseMsg) {
	   			// TODO Auto-generated method stub
	   			super.onPostExecute(objResponseMsg);
	   			
	   			if (objProgressDialog.isShowing()) {
		        	objProgressDialog.dismiss();
		        }
	   			
	   			if (objResponseMsg.intErrorCode == ERROR_NONE) {
	   				objListViewStates.clear();
	   				for (clsPublicationData objPublicationData: objResponseMsg.lstPublicationDatas ) {
	   					clsListViewStateSearch objListViewState = new clsListViewStateSearch();
	   					objListViewState.strNoteName = objPublicationData.strNoteName;
	   					objListViewState.strNoteUuid = objPublicationData.strNoteUuid;
	   					objListViewState.strUserName = objPublicationData.strUserName;
	   					objListViewState.strUserUuid = objPublicationData.strUserUuid;
	   					objListViewState.boolIsChecked= false;
	   					objListViewStates.add(objListViewState);
	   				}
   					ListView objListView = ((ActivitySubscriptionSearch) objContext).getListView(); 
   					objListView.invalidateViews();
	   			} else {
	   				clsUtils.MessageBox(objContext, objResponseMsg.strErrorMessage);
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
