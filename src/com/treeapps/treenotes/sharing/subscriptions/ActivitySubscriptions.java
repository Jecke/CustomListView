package com.treeapps.treenotes.sharing.subscriptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;









import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.treeapps.treenotes.ActivityRegister;
import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsExplorerTreeview;
import com.treeapps.treenotes.clsListItem;
import com.treeapps.treenotes.clsNoteTreeview;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.clsTreeview.clsRepository;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsTreeview.enumItemType;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsMessaging.clsMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncResult;
import com.treeapps.treenotes.sharing.clsWebServiceComms;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommand;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommsAsyncTask.OnWebPagePostedListener;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceResponse;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommsAsyncTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

public class ActivitySubscriptions extends ListActivity {

	public static class clsSubcriptionsIntentData {
		public boolean boolIsServerIisExpress;
		public String strRegisteredUserUuid;
	}

	public static class clsListViewState 
	{
		String strNoteName;
		String strNoteUuid;
		String strUserName;
		String strUserUuid;
		boolean boolIsChecked = false;
	}

	public static final String INTENT_DATA = "com.treeapps.treenotes.sharing.subscriptions_intentdata";
	private static final int SEARCH = 0;

	static Activity objContext;
	static clsSubcriptionsIntentData objSubcriptionsIntentData;
	class clsSubscriptionsRemoveCommandMsg extends clsWebServiceCommand {
		public String strRegisteredUserUuid = "";
		public ArrayList<String> lstRemoveNoteSubscriptions = new ArrayList<String>();	
	}
	clsSubscriptionsRemoveCommandMsg objSubscriptionsRemoveCommand;
	public static ArrayList<clsListViewState> objListViewStates = new ArrayList<clsListViewState>();
	public static clsActivitySubscriptionArrayAdapter objArrayAdapter;
	public static clsMessaging objMessaging = new clsMessaging();
	public clsGroupMembers objGroupMembers = new clsGroupMembers(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		objContext = this;
		setContentView(R.layout.activity_subscriptions);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
		Bundle objBundle = getIntent().getExtras();   
		String strSubcriptionsIntentData    = objBundle.getString(INTENT_DATA);

		objSubcriptionsIntentData = clsUtils.DeSerializeFromString(strSubcriptionsIntentData,
				clsSubcriptionsIntentData.class);

		objArrayAdapter = new clsActivitySubscriptionArrayAdapter(this,	R.layout.activity_subscriptions_list_item, objListViewStates);
		setListAdapter(objArrayAdapter);
		
		SaveFile();

		// Get subscription data
		URL urlFeed = null;
		String strServerUrl;
		try {
			if(objSubcriptionsIntentData.boolIsServerIisExpress) {
				strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
			}
			else {
				strServerUrl =  clsMessaging.SERVER_URL_AZURE;
			}
			urlFeed = new URL(strServerUrl + getResources().getString(R.string.url_subscriptions_search));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clsSubscriptionsSearchCommandMsg objCommandMsg = new clsSubscriptionsSearchCommandMsg();
		objCommandMsg.strRegisteredUserUuid = objSubcriptionsIntentData.strRegisteredUserUuid;
		ActivitySubscriptionsSearchAsyncTask objAsyncTask = new ActivitySubscriptionsSearchAsyncTask(this, urlFeed, objCommandMsg);
		objAsyncTask.execute("");	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_subscriptions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_subscriptions_remove:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dlg_are_you_sure_about_delete);
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					final URL urlFeed;
					try {
						urlFeed = new URL(objMessaging.GetServerUrl()
								+ getResources().getString(R.string.url_subscriptions_remove));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					objSubscriptionsRemoveCommand = new clsSubscriptionsRemoveCommandMsg();
					objSubscriptionsRemoveCommand.strRegisteredUserUuid = objSubcriptionsIntentData.strRegisteredUserUuid;
					for (clsListViewState objListViewState:  objListViewStates) {
						if (objListViewState.boolIsChecked) {
							objSubscriptionsRemoveCommand.lstRemoveNoteSubscriptions.add(objListViewState.strNoteUuid);
						}
					}
					clsWebServiceCommsAsyncTask objMyAsyncTask = new clsWebServiceCommsAsyncTask(objContext, 
							urlFeed, objSubscriptionsRemoveCommand);
					objMyAsyncTask.SetOnWebPagePostedListener(new OnWebPagePostedListener() {
						
						@Override
						public void onPosted(JSONObject objJsonResponse) {
							// TODO Auto-generated method stub
							clsWebServiceResponse objResponse = clsUtils.DeSerializeFromString(objJsonResponse.toString(),
									clsWebServiceResponse.class);
							if (objResponse.intErrorCode != clsWebServiceComms.ERROR_NONE) {
								clsUtils.MessageBox(objContext, objResponse.strErrorMessage, false);
							} else {
								for (clsListViewState objListViewState:  objListViewStates) {
									if (objListViewState.boolIsChecked) {
										objListViewStates.remove(objListViewState);
									}
								}
								objArrayAdapter.clear(); objArrayAdapter.addAll(ActivitySubscriptions.objListViewStates);
								objArrayAdapter.notifyDataSetChanged();
								clsUtils.MessageBox(objContext, "Subscriptions successfully deleted", true);
							}
						}
					});
					objMyAsyncTask.execute(null,null,null);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					return;
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();	
			

		case R.id.action_settings:
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
					R.layout.fragment_activity_subscriptions, container, false);
			return rootView;
		}
	}


	public void onButtonSearchClick(View v) {
		Intent intentSearch = new Intent(this, ActivitySubscriptionSearch.class);

		EditText objAuthorEditText = (EditText) this.findViewById(R.id.subscriptions_search_author);
		String strAuthor = objAuthorEditText.getText().toString();
		intentSearch.putExtra(ActivitySubscriptionSearch.AUTHOR_SEARCHTERM, strAuthor);

		EditText objNoteNameEditText = (EditText) this.findViewById(R.id.subscriptions_search_notename);
		String strNoteName = objNoteNameEditText.getText().toString();
		intentSearch.putExtra(ActivitySubscriptionSearch.NOTENAME_SEARCHTERM, strNoteName );
		URL urlFeed = null;
		String strServerUrl;
		try {
			if(objSubcriptionsIntentData.boolIsServerIisExpress) {
				strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
			}
			else {
				strServerUrl =  clsMessaging.SERVER_URL_AZURE;
			}
			urlFeed = new URL(strServerUrl + getResources().getString(R.string.url_publications_search));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		intentSearch.putExtra(ActivitySubscriptionSearch.URL, urlFeed.toString());

		startActivityForResult(intentSearch, SEARCH);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK)
		{
			switch (requestCode) {
			case SEARCH:
				Bundle objBundle = data.getExtras();
				String strSearchResult = objBundle.getString(ActivitySubscriptionSearch.SEARCH_RESULTS);
				java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsListViewState>>(){}.getType();
				ArrayList<clsListViewState> objResultListViewStates = clsUtils.DeSerializeFromString(strSearchResult,collectionType );
				ArrayList<String> lstNewSubScriptions = new ArrayList<String>();
				// Update local display
				for (clsListViewState objResultListViewState: objResultListViewStates) {
					AddListViewStateIfNew(objResultListViewState, lstNewSubScriptions);	
				}
				SaveFile();
				
				// Save all subscriptions to server
				URL urlFeed = null;
				String strServerUrl;
				try {
					if(objSubcriptionsIntentData.boolIsServerIisExpress) {
						strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
					}
					else {
						strServerUrl =  clsMessaging.SERVER_URL_AZURE;
					}
					urlFeed = new URL(strServerUrl + getResources().getString(R.string.url_subscriptions_add));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				clsSubscriptionsAddCommandMsg objCommandMsg = new clsSubscriptionsAddCommandMsg();
				objCommandMsg.strRegisteredUserUuid = objSubcriptionsIntentData.strRegisteredUserUuid;
				objCommandMsg.lstAddSubscriptions = lstNewSubScriptions;
				ActivitySubscriptionsAddAsyncTask objAsyncTask = new ActivitySubscriptionsAddAsyncTask(this, urlFeed, objCommandMsg);
				objAsyncTask.execute("");
				break;
			}
		}	
	}

	private void AddListViewStateIfNew(clsListViewState objNewListViewState, ArrayList<String> lstNewSubScriptions) {
		// TODO Auto-generated method stub
		boolean boolIsNew = true;
		for (clsListViewState objListViewState: objListViewStates) {
			if  (objListViewState.strUserUuid != objGroupMembers.GetRegisteredUser().strUserUuid) {
				if ((objListViewState.strNoteUuid.equals(objNewListViewState.strNoteUuid)) &&
						(objListViewState.strUserUuid.equals(objNewListViewState.strUserUuid))) {
					boolIsNew = false;
					break;
				}
			}	
		}
		if (boolIsNew) {
			objListViewStates.add(objNewListViewState);
			lstNewSubScriptions.add(objNewListViewState.strNoteUuid);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
		
		
		
		super.onBackPressed();
		objListViewStates.clear();
	}
	
	
	 private void SaveFile() {
			clsUtils.SerializeToSharedPreferences("ActivitySubscriptions", "objSubcriptionsIntentData", this, objSubcriptionsIntentData);
			clsUtils.SerializeToSharedPreferences("ActivitySubscriptions", "objListViewStates", this, objListViewStates);
		}
	 
		private void LoadFile() {
			objContext = this;
			objSubcriptionsIntentData = clsUtils.DeSerializeFromSharedPreferences("ActivitySubscriptions", "objSubcriptionsIntentData",this, objSubcriptionsIntentData.getClass());
			java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsListViewState>>(){}.getType();
			objListViewStates = clsUtils.DeSerializeFromSharedPreferences("ActivitySubscriptions", "objListViewStates",this, collectionType);
			objMessaging = new clsMessaging();
			objGroupMembers = new clsGroupMembers(this);
		}
	    
    
    @Override
    protected void onStop() {
    	SaveFile();
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	SaveFile();
    	super.onDestroy();
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	LoadFile();
    	super.onStart();
    }
    
    @Override
    protected void onRestart() {
    	LoadFile();
    	super.onRestart();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }

	// -------------------------- Communications ----------------------------------


	public class clsSubscriptionsSearchCommandMsg extends clsMsg{
		public String strRegisteredUserUuid = "";	        
		clsSubscriptionsSearchCommandMsg() { objMessaging.super();}
	}

	public class clsSubscriptionData {
		String strNoteName;
		String strNoteUuid;
		String strUserName;
		String strUserUuid;
	}

	public class clsSubscriptionsSearchResponseMsg extends clsMsg {
		public ArrayList<clsSubscriptionData> lstSubscriptionDatas;
		clsSubscriptionsSearchResponseMsg() { objMessaging.super();}
	}



	public static class ActivitySubscriptionsSearchAsyncTask extends AsyncTask<String, Void, clsSubscriptionsSearchResponseMsg> {

		public static final int ERROR_NONE = 0;
		public static final int ERROR_NETWORK = 1;

		static Exception mException = null;
		static clsSubscriptionsSearchCommandMsg objCommand;
		static URL urlFeed;
		ProgressDialog objProgressDialog;

		public ActivitySubscriptionsSearchAsyncTask (Activity objActivity, URL urlFeed, clsSubscriptionsSearchCommandMsg objCommandMsg) {
			ActivitySubscriptionsSearchAsyncTask.objCommand = objCommandMsg;
			ActivitySubscriptionsSearchAsyncTask.urlFeed = urlFeed;
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
		protected clsSubscriptionsSearchResponseMsg doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			Gson gson = new Gson();
			JSONObject objJsonResult = null;
			clsSubscriptionsSearchResponseMsg objResponseMsg = ((ActivitySubscriptions)objContext).new clsSubscriptionsSearchResponseMsg();

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
			objResponseMsg = gson.fromJson(objJsonResult.toString(),clsSubscriptionsSearchResponseMsg.class);
			return objResponseMsg;
		}
		@Override
		protected void onPostExecute(clsSubscriptionsSearchResponseMsg objResponseMsg) {
			// TODO Auto-generated method stub
			super.onPostExecute(objResponseMsg);
			
			if (objProgressDialog.isShowing()) {
	        	objProgressDialog.dismiss();
	        }

			if (objResponseMsg.intErrorCode == ERROR_NONE) {
				for (clsSubscriptionData objSubscriptionData: objResponseMsg.lstSubscriptionDatas ) {
					clsListViewState objListViewState = new clsListViewState();
					objListViewState.strNoteName = objSubscriptionData.strNoteName;
					objListViewState.strNoteUuid = objSubscriptionData.strNoteUuid;
					objListViewState.strUserName = objSubscriptionData.strUserName;
					objListViewState.strUserUuid = objSubscriptionData.strUserUuid;
					objListViewState.boolIsChecked= false;
					if (!ListViewStatesExist(objListViewState)) {
						ActivitySubscriptions.objListViewStates.add(objListViewState);
					}
				}
				objArrayAdapter.clear(); objArrayAdapter.addAll(ActivitySubscriptions.objListViewStates);
				objArrayAdapter.notifyDataSetChanged();
			} else {
				clsUtils.MessageBox(objContext, objResponseMsg.strErrorMessage, false);
			}	
		}


		private boolean ListViewStatesExist(clsListViewState objCheckedListViewState) {
			// TODO Auto-generated method stub
			for (clsListViewState objListViewState: ActivitySubscriptions.objListViewStates) {
				if ((objListViewState.strNoteUuid.equals(objCheckedListViewState.strNoteUuid)) &&
						(objListViewState.strUserUuid.equals(objCheckedListViewState.strUserUuid))) {
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

	// ------------------ Subscriptions add

	public class clsSubscriptionsAddCommandMsg extends clsMsg {
		public String strRegisteredUserUuid;
		public ArrayList<String> lstAddSubscriptions = new ArrayList<String>();	        
		clsSubscriptionsAddCommandMsg() { objMessaging.super();}
	}


	public class clsSubscriptionsAddResponseMsg extends clsMsg {
		clsSubscriptionsAddResponseMsg() { objMessaging.super();}
	}



	public static class ActivitySubscriptionsAddAsyncTask extends AsyncTask<String, Void, clsSubscriptionsAddResponseMsg> {

		public static final int ERROR_NONE = 0;
		public static final int ERROR_NETWORK = 1;

		static Exception mException = null;
		static clsSubscriptionsAddCommandMsg objCommand;
		static URL urlFeed;
		ProgressDialog objProgressDialog;

		public ActivitySubscriptionsAddAsyncTask (Activity objActivity, URL urlFeed, clsSubscriptionsAddCommandMsg objCommandMsg) {
			ActivitySubscriptionsAddAsyncTask.objCommand = objCommandMsg;
			ActivitySubscriptionsAddAsyncTask.urlFeed = urlFeed;
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
		protected clsSubscriptionsAddResponseMsg doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			Gson gson = new Gson();
			JSONObject objJsonResult = null;
			clsSubscriptionsAddResponseMsg objResponseMsg = ((ActivitySubscriptions) objContext).new clsSubscriptionsAddResponseMsg();

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
			objResponseMsg = gson.fromJson(objJsonResult.toString(),clsSubscriptionsAddResponseMsg.class);
			return objResponseMsg;
		}
		@Override
		protected void onPostExecute(clsSubscriptionsAddResponseMsg objResponseMsg) {
			// TODO Auto-generated method stub
			super.onPostExecute(objResponseMsg);
			
			if (objProgressDialog.isShowing()) {
	        	objProgressDialog.dismiss();
	        }

			if (objResponseMsg.intErrorCode == ERROR_NONE) {
				objArrayAdapter.clear(); objArrayAdapter.addAll(ActivitySubscriptions.objListViewStates);
				objArrayAdapter.notifyDataSetInvalidated();
			} else {
				Toast.makeText((ActivitySubscriptions) objContext, objResponseMsg.strErrorMessage, Toast.LENGTH_LONG).show();
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
