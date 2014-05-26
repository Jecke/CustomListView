package com.treeapps.treenotes.sharing.subscriptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;



import com.google.gson.Gson;
import com.treeapps.treenotes.ActivityExplorerStartup;
import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsExplorerTreeview;
import com.treeapps.treenotes.clsListItem;
import com.treeapps.treenotes.clsNoteTreeview;
import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.clsTreeview.clsRepository;
import com.treeapps.treenotes.clsTreeview.clsSyncRepository;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsTreeview.enumItemType;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsMessaging.clsMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncResult;
import com.treeapps.treenotes.sharing.subscriptions.ActivitySubscriptionSearch.clsPublicationData;
import com.treeapps.treenotes.sharing.subscriptions.ActivitySubscriptions.clsListViewState;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityPublications extends ListActivity {
	
	public static class clsSelectedNoteData {
		public String strNoteUuid;
		public String strNoteName;
	}
	
	public static class clsPublicationsIntentData {
		public boolean boolIsServerIisExpress;
		public String strRegisteredUserUuid;
		public ArrayList<clsSelectedNoteData> lstSelectedNoteDatas;
		
		public ArrayList<String> GetSelectedNoteUuids() {
			ArrayList<String> objNewList = new ArrayList<String>();
			for (clsSelectedNoteData objSelectedNoteData: this.lstSelectedNoteDatas) {
				objNewList.add(objSelectedNoteData.strNoteUuid);				
			}
			return objNewList;
		}
	}
	
	public static class clsListViewState 
	{
		String strNoteName;
		String strNoteUuid;
		boolean boolIsChecked = false;
	}

	public static final String INTENT_DATA = "com.treeapps.treenotes.sharing.subscriptions_intentdata";
	private static final int SEARCH = 0;
	
	static Context objContext;
	public File fileTreeNodesDir;
	clsGroupMembers objGroupMembers= new clsGroupMembers(this); 
	public clsExplorerTreeview objExplorerTreeview;
	static clsPublicationsIntentData objPublicationsIntentData;
	public static ArrayList<clsListViewState> objListViewStates = new ArrayList<clsListViewState>();
	public static clsActivityPublicationArrayAdapter objArrayAdapter;
	public static clsMessaging objMessaging = new clsMessaging();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(this));
		objContext = this;
		objGroupMembers.LoadFile();
		objExplorerTreeview = new clsExplorerTreeview(this,objGroupMembers);
		objExplorerTreeview.DeserializeFromFile(fileTreeNodesDir,getResources().getString(R.string.working_file_name));
		setContentView(R.layout.activity_publications);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		Bundle objBundle = getIntent().getExtras();   
		String strSubcriptionsIntentData    = objBundle.getString(INTENT_DATA);
		
		objPublicationsIntentData = clsUtils.DeSerializeFromString(strSubcriptionsIntentData,
				clsPublicationsIntentData.class);
		
		// Fill notice label
		TextView objTextView = (TextView)findViewById(R.id.lbl_publication_notice);
		String strLabelText = "";
		for (clsSelectedNoteData objSelectedNoteData: objPublicationsIntentData.lstSelectedNoteDatas) {
			if (strLabelText.isEmpty()) {
				strLabelText = "\"" + objSelectedNoteData.strNoteName + "\"";
			} else {
				strLabelText += ", \"" + objSelectedNoteData.strNoteName + "\"";
			}
		}
		objTextView.setText("Selected note(s) are " + strLabelText + ". Press + to accept");
		
		// Fill listview
		objArrayAdapter = new clsActivityPublicationArrayAdapter(this,	R.layout.activity_subscriptions_list_item, objListViewStates);
		setListAdapter(objArrayAdapter);
		
		// Get all publications for this registered user
		URL urlFeed = null;
		String strServerUrl;
		try {
			if(objPublicationsIntentData.boolIsServerIisExpress) {
				strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
	    	}
	    	else {
	    		strServerUrl =  clsMessaging.SERVER_URL_AZURE;
	    	}
			urlFeed = new URL(strServerUrl + getResources().getString(R.string.url_publications_by_author_search));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clsPublicationsByAuthorSearchCommandMsg objCommandMsg = new clsPublicationsByAuthorSearchCommandMsg();
		objCommandMsg.strRegisteredUserUuid = objPublicationsIntentData.strRegisteredUserUuid;
		ActivityPublicationsByAuthorSearchAsyncTask objAsyncTask = new ActivityPublicationsByAuthorSearchAsyncTask(this, urlFeed, objCommandMsg);
		objAsyncTask.execute("");	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_publications, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_publications_add:
			if (objPublicationsIntentData.lstSelectedNoteDatas.size() == 0) {
				clsUtils.MessageBox(this, "No notes has been selected in the Explorer. Please select at least one to publish", false);
				return true;
			}
			if (AreNotesAlreadyPublished(objPublicationsIntentData.lstSelectedNoteDatas)) {
				RemoveNotesAlreadyPublished(objPublicationsIntentData.lstSelectedNoteDatas);
			}
			URL urlFeed = null;
			String strServerUrl;
			try {
				if(objPublicationsIntentData.boolIsServerIisExpress) {
					strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
		    	}
		    	else {
		    		strServerUrl =  clsMessaging.SERVER_URL_AZURE;
		    	}
				urlFeed = new URL(strServerUrl + getResources().getString(R.string.url_publications_add));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clsPublicationsAddCommandMsg objAddCommandMsg = new clsPublicationsAddCommandMsg();
			objAddCommandMsg.objPublicationRepositories = GetRepositories(objPublicationsIntentData.GetSelectedNoteUuids());
			ActivityPublicationsAddAsyncTask objAddAsyncTask = new ActivityPublicationsAddAsyncTask(this, urlFeed, objAddCommandMsg);
			objAddAsyncTask.execute("");
			return true;			
		case R.id.action_publications_remove:
			urlFeed = null;
			try {
				if(objPublicationsIntentData.boolIsServerIisExpress) {
					strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
		    	}
		    	else {
		    		strServerUrl =  clsMessaging.SERVER_URL_AZURE;
		    	}
				urlFeed = new URL(strServerUrl + getResources().getString(R.string.url_publications_remove));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clsPublicationsRemoveCommandMsg objRemoveCommandMsg = new clsPublicationsRemoveCommandMsg();
			objRemoveCommandMsg.lstSelectedNoteUuids = GetSelectedListItems();
			ActivityPublicationsRemoveAsyncTask objRemoveAsyncTask = new ActivityPublicationsRemoveAsyncTask(this, urlFeed, objRemoveCommandMsg);
			objRemoveAsyncTask.execute("");			
			return true;
			
		case R.id.action_settings:
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private ArrayList<clsSyncRepository> GetRepositories(ArrayList<String> lstSelectedNoteUuids) {
		// TODO Auto-generated method stub
		ArrayList<clsSyncRepository> objPublishRepositories = new ArrayList<clsSyncRepository>();
		for (String strRepositoryUuid: lstSelectedNoteUuids){
			clsTreeNode objTreeNode = objExplorerTreeview.getTreeNodeFromUuid(UUID.fromString(strRepositoryUuid));
			if (objTreeNode != null ) {
				if (objTreeNode.enumItemType == enumItemType.OTHER) {
					// Get note file from file repository
					File fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(objContext));
					File objNoteFile = clsUtils.BuildNoteFilename(fileTreeNodesDir, objTreeNode.guidTreeNode.toString() );
					clsRepository objNoteRepository = objExplorerTreeview.DeserializeNoteFromFile(objNoteFile);
					// Add to sync repository
					if (objNoteRepository != null) {
						objPublishRepositories.add(objNoteRepository.getCopy());
					}
				}
			}
		}
		return objPublishRepositories;
	}

	private ArrayList<String> GetSelectedListItems() {
		// TODO Auto-generated method stub
		ArrayList<String> lstSelectedListItems = new ArrayList<String>();
		for(clsListViewState objListViewState: objListViewStates) {
			if (objListViewState.boolIsChecked) {
				lstSelectedListItems.add(objListViewState.strNoteUuid);
			}
			
		}
		return lstSelectedListItems;
	}

	private boolean AreNotesAlreadyPublished(ArrayList<clsSelectedNoteData> objSelectedNoteDatas) {
		// TODO Auto-generated method stub
		for (clsListViewState objListViewState: objListViewStates) {
			for (clsSelectedNoteData objSelectedNoteData: objSelectedNoteDatas ) {
				if (objListViewState.strNoteUuid.equals(objSelectedNoteData.strNoteUuid)) return true;
			}
		}
		return false;
	}
	
	private void RemoveNotesAlreadyPublished(ArrayList<clsSelectedNoteData> objSelectedNoteDatas) {
		// TODO Auto-generated method stub
		ArrayList<clsSelectedNoteData> objUpdatedSelectedNoteDatas = new ArrayList<clsSelectedNoteData>();
		for (clsListViewState objListViewState: objListViewStates) {
			for (clsSelectedNoteData objSelectedNoteData: objSelectedNoteDatas ) {
				if (!objListViewState.strNoteUuid.equals(objSelectedNoteData.strNoteUuid)) {
					objUpdatedSelectedNoteDatas.add(objSelectedNoteData);
				}
			}
		}
		objSelectedNoteDatas = objUpdatedSelectedNoteDatas;
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
					R.layout.fragment_activity_subscriptions, container, false);
			return rootView;
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	// -------------------------- Communications ----------------------------------
	
	// -------------- Search all notes already published by registered user
	
	public class clsPublicationsByAuthorSearchCommandMsg extends clsMsg{
		public String strRegisteredUserUuid = "";	        
		clsPublicationsByAuthorSearchCommandMsg() { objMessaging.super();}
	}


	public class clsPublicationsByAuthorSearchResponseMsg extends clsMsg {
		public ArrayList<clsPublicationData> lstPublicationDatas;
		clsPublicationsByAuthorSearchResponseMsg() { objMessaging.super();}
	}



	public static class ActivityPublicationsByAuthorSearchAsyncTask extends AsyncTask<String, Void, clsPublicationsByAuthorSearchResponseMsg> {

		public static final int ERROR_NONE = 0;
		public static final int ERROR_NETWORK = 1;

		static Exception mException = null;
		static clsPublicationsByAuthorSearchCommandMsg objCommand;
		static URL urlFeed;
		ProgressDialog objProgressDialog;

		public ActivityPublicationsByAuthorSearchAsyncTask (Activity objActivity, URL urlFeed, clsPublicationsByAuthorSearchCommandMsg objCommandMsg) {
			ActivityPublicationsByAuthorSearchAsyncTask.objCommand = objCommandMsg;
			ActivityPublicationsByAuthorSearchAsyncTask.urlFeed = urlFeed;
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
		protected clsPublicationsByAuthorSearchResponseMsg doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			Gson gson = new Gson();
			JSONObject objJsonResult = null;
			clsPublicationsByAuthorSearchResponseMsg objResponseMsg = ((ActivityPublications)objContext).new clsPublicationsByAuthorSearchResponseMsg();

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
			objResponseMsg = gson.fromJson(objJsonResult.toString(),clsPublicationsByAuthorSearchResponseMsg.class);
			return objResponseMsg;
		}
		@Override
		protected void onPostExecute(clsPublicationsByAuthorSearchResponseMsg objResponseMsg) {
			// TODO Auto-generated method stub
			super.onPostExecute(objResponseMsg);
			
			if (objProgressDialog.isShowing()) {
	        	objProgressDialog.dismiss();
	        }

			if (objResponseMsg.intErrorCode == ERROR_NONE) {
				for (clsPublicationData objPublicationData: objResponseMsg.lstPublicationDatas ) {
					clsListViewState objListViewState = new clsListViewState();
					objListViewState.strNoteName = objPublicationData.strNoteName;
					objListViewState.strNoteUuid = objPublicationData.strNoteUuid;
					objListViewState.boolIsChecked= false;
					ActivityPublications.objListViewStates.add(objListViewState);
				}
				ListView objListView = ((ActivityPublications) objContext).getListView();  
				objListView.invalidateViews();
			} else {
				clsUtils.MessageBox(objContext, objResponseMsg.strErrorMessage, false);
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
	
	// ----------- Remove publish notes 
	public class clsPublicationsRemoveCommandMsg extends clsMsg{
		public ArrayList<String> lstSelectedNoteUuids;	
		clsPublicationsRemoveCommandMsg() { objMessaging.super();}
	}


	public class clsPublicationsRemoveResponseMsg extends clsMsg {
		public ArrayList<String> lstRemovedPublicationUuids;
		clsPublicationsRemoveResponseMsg() { objMessaging.super();}
	}



	public static class ActivityPublicationsRemoveAsyncTask extends AsyncTask<String, Void, clsPublicationsRemoveResponseMsg> {

		public static final int ERROR_NONE = 0;
		public static final int ERROR_NETWORK = 1;

		static Exception mException = null;
		static clsPublicationsRemoveCommandMsg objCommand;
		static URL urlFeed;
		ProgressDialog objProgressDialog;

		public ActivityPublicationsRemoveAsyncTask (Activity objActivity, URL urlFeed, clsPublicationsRemoveCommandMsg objCommandMsg) {
			ActivityPublicationsRemoveAsyncTask.objCommand = objCommandMsg;
			ActivityPublicationsRemoveAsyncTask.urlFeed = urlFeed;
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
		protected clsPublicationsRemoveResponseMsg doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			Gson gson = new Gson();
			JSONObject objJsonResult = null;
			clsPublicationsRemoveResponseMsg objResponseMsg = ((ActivityPublications)objContext).new clsPublicationsRemoveResponseMsg();

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
			objResponseMsg = gson.fromJson(objJsonResult.toString(),clsPublicationsRemoveResponseMsg.class);
			return objResponseMsg;
		}
		@Override
		protected void onPostExecute(clsPublicationsRemoveResponseMsg objResponseMsg) {
			// TODO Auto-generated method stub
			super.onPostExecute(objResponseMsg);
			
			if (objProgressDialog.isShowing()) {
	        	objProgressDialog.dismiss();
	        }

			if (objResponseMsg.intErrorCode == ERROR_NONE) {
				for (String strRemovedPublicationUuid: objResponseMsg.lstRemovedPublicationUuids ) {				
					clsListViewState obToBeRemovedListViewState = null;
					for (clsListViewState objListViewState: ActivityPublications.objListViewStates) {
						if (objListViewState.strNoteUuid.equals(strRemovedPublicationUuid))  {
							obToBeRemovedListViewState = objListViewState;
							break;
						}
					}
					if (obToBeRemovedListViewState != null) {
						ActivityPublications.objListViewStates.remove(obToBeRemovedListViewState);
					}
				}
				clsUtils.MessageBox(objContext," All publications successfully removed", false);
				ListView objListView = ((ActivityPublications) objContext).getListView();  
				objListView.invalidateViews();
			} else {
				clsUtils.MessageBox(objContext, objResponseMsg.strErrorMessage, false);
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
		objListViewStates.clear();
		finish();
		super.onBackPressed();
	}
	
	// ----------- Add new publish notes 
		public class clsPublicationsAddCommandMsg extends clsMsg{
			public ArrayList<clsSyncRepository> objPublicationRepositories = new ArrayList<clsSyncRepository>();	
			clsPublicationsAddCommandMsg() { objMessaging.super();}
		}


		public class clsPublicationsAddResponseMsg extends clsMsg {
			public ArrayList<clsPublicationData> objPublicationDatas = new ArrayList<clsPublicationData>();
	    	public ArrayList<Integer> intServerInstructions = new ArrayList<Integer>();
	    	public ArrayList<String> strServerMessages = new ArrayList<String>();
			clsPublicationsAddResponseMsg() { objMessaging.super();}
		}


		public static class ActivityPublicationsAddAsyncTask extends AsyncTask<String, Void, clsPublicationsAddResponseMsg> {

			public static final int ERROR_NONE = 0;
			public static final int ERROR_NETWORK = 1;

			static Exception mException = null;
			static clsPublicationsAddCommandMsg objCommand;
			static URL urlFeed;
			ProgressDialog objProgressDialog;

			public ActivityPublicationsAddAsyncTask (Activity objActivity, URL urlFeed, clsPublicationsAddCommandMsg objCommandMsg) {
				ActivityPublicationsAddAsyncTask.objCommand = objCommandMsg;
				ActivityPublicationsAddAsyncTask.urlFeed = urlFeed;
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
			protected clsPublicationsAddResponseMsg doInBackground(String... arg0) {
				// TODO Auto-generated method stub
				Gson gson = new Gson();
				JSONObject objJsonResult = null;
				clsPublicationsAddResponseMsg objResponseMsg = ((ActivityPublications)objContext).new clsPublicationsAddResponseMsg();

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
				objResponseMsg = gson.fromJson(objJsonResult.toString(),clsPublicationsAddResponseMsg.class);
				return objResponseMsg;
			}
			@Override
	   	    protected void onPostExecute(clsPublicationsAddResponseMsg objResult){
	        	String strMessage = "";
	   	        super.onPostExecute(objResult);
	   	        if (objProgressDialog.isShowing()) {
		        	objProgressDialog.dismiss();
		        }
	   	        // Do what needs to be done with the result
	   	        if (objResult.intErrorCode == clsSyncResult.ERROR_NONE) {
	   	        	for (int i = 0; i < objResult.intServerInstructions.size(); i++ ) {
	   	        	// Depending on server instructions
	   	   	   	        switch (objResult.intServerInstructions.get(i)) {
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_NO_PROBLEM:
	   	   	   	        	clsPublicationData objPublicationData = objResult.objPublicationDatas.get(i);
	   	   	   	        	clsListViewState objListViewState = new clsListViewState();
	   	   	   	        	objListViewState.strNoteName = objPublicationData.strNoteName;
	   	   	   	        	objListViewState.strNoteUuid = objPublicationData.strNoteUuid;
	   	   	   	        	objListViewState.boolIsChecked= false;
	   	   	   	        	if (!ListViewStatesExist(objListViewState)) {
	   	   	   	        		ActivityPublications.objListViewStates.add(objListViewState);
	   	   	   	        	}
	   	   	   	        	strMessage += "Note '" + objPublicationData.strNoteName + " had been successfully published.\n";
	   	   	   	        	break;
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_PROBLEM:
	   	   	   	        	objPublicationData = objResult.objPublicationDatas.get(i);
	   	   	   	    		strMessage += "Note '" + objPublicationData.strNoteName + "' had a publication problem. " + objResult.strServerMessages.get(i) + ".\n";
	   	   	        	break;
	   	   	   	    	}
			   	   	   	ListView objListView = ((ActivityPublications) objContext).getListView();  
						objListView.invalidateViews();
	   	        	}	   	        		
	   	        } else {
   	        		strMessage += objResult.strErrorMessage + ".\n";
	   	        }
	   	   	    clsUtils.MessageBox(objContext, strMessage, false);
	   	    }
			
			private boolean ListViewStatesExist(clsListViewState objCheckedListViewState) {
				// TODO Auto-generated method stub
				for (clsListViewState objListViewState: ActivityPublications.objListViewStates) {
					if (objListViewState.strNoteUuid.equals(objCheckedListViewState.strNoteUuid)) {
						return true;
					}
				}
				return false;
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
