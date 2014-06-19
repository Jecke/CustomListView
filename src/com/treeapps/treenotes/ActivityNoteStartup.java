package com.treeapps.treenotes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.reflect.TypeToken;
import com.treeapps.treenotes.ActivityExplorerStartup.ActivityExplorerSyncMembersAsyncTask;
import com.treeapps.treenotes.ActivityExplorerStartup.clsGetNoteSharedUsersResponse;
import com.treeapps.treenotes.ActivityExplorerStartup.clsIabLocalData;
import com.treeapps.treenotes.ActivityExplorerStartup.clsSetNoteSharedUsersAsyncTask;
import com.treeapps.treenotes.ActivityExplorerStartup.clsSetNoteSharedUsersCommand;
import com.treeapps.treenotes.ActivityExplorerStartup.clsSetNoteSharedUsersResponse;
import com.treeapps.treenotes.ActivityExplorerStartup.clsSetNoteSharedUsersAsyncTask.OnSetNoteSharedUsersResponseListener;
import com.treeapps.treenotes.clsTreeview.clsRepository;
import com.treeapps.treenotes.clsTreeview.clsShareUser;
import com.treeapps.treenotes.clsTreeview.clsSyncRepository;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsTreeview.enumCutCopyPasteState;
import com.treeapps.treenotes.export.clsExportData;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.OnImageUploadFinishedListener;
import com.treeapps.treenotes.export.clsExportToMail;
import com.treeapps.treenotes.export.clsMainExport;
import com.treeapps.treenotes.imageannotation.clsAnnotationData;
import com.treeapps.treenotes.sharing.ActivityGroupMembers;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsGroupMembers.clsUser;
import com.treeapps.treenotes.sharing.clsMessaging.NoteSyncAsyncTask;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageUpDownloadAsyncTask;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncMembersCommandMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncNoteCommandMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncRepositoryCtrlData;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncResult;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ActivityNoteStartup extends ListActivity {

	// keys to share between activities via Intents
	public static final String DESCRIPTION   = "com.treeapps.treenotes.description";
	public static final String RESOURCE_ID   = "com.treeapps.treenotes.resource_id";
	public static final String RESOURCE_PATH = "com.treeapps.treenotes.resource_path";
	public static final String TREENODE_URL = "com.treeapps.treenotes.treenode_url";
	public static final String TREENODE_UID = "com.treeapps.treenotes.treenode_uuid";
	public static final String TREENODE_OWNERNAME = "com.treeapps.treenotes.treenode_ownername";
	public static final String ANNOTATION_DATA_GSON = "com.treeapps.treenotes.annotation_data";
	public static final String READONLY = "com.treeapps.treenotes.read_only";
	public static final String USE_ANNOTATED_IMAGE = "com.treeapps.treenotes.use_annotated_image";
	public static final String ISDIRTY = "com.treeapps.treenotes.is_dirty";
	public static final String TREENODE_PARENTNAME = "com.treeapps.treenotes.treenode_parentname";
	

	private static final int GET_DESCRIPTION_ADD_SAME_LEVEL = 0;
	private static final int GET_DESCRIPTION_ADD_NEXT_LEVEL = 1;
	public static final int EDIT_DESCRIPTION = 2;
	private static final int EDIT_SETTINGS = 3;
	private static final int ANNOTATE_IMAGE = 7;
	

	// IntenrService interaction
	public static final String BROADCAST_ACTION = "com.treeapps.treenotes.broadcast_autosync";
	public static final String BROADCAST_DATA_NOTE_UUID = "com.treeapps.treenotes.broadcast_data_note_uuid";
	
	
	
	// Variables that need to be persisted
	String strNoteUuid;
	 public static ArrayList<clsListItem> listItems = new ArrayList<clsListItem>();
	 public static clsNoteTreeview objNoteTreeview;
	 public clsGroupMembers objGroupMembers = new clsGroupMembers(this);
	 
	 private clsNoteListItemArrayAdapter objListItemAdapter;
	 private clsMessaging objMessaging = new clsMessaging(); 	 
	 private boolean boolIsShortcut;
	 boolean boolIsUserRegistered = false;
	 private boolean boolUserIsNoteOwner = false;
	 static Activity objActivity;
	 public static ArrayList<clsImageLoadData> objLocalImageLoadDatas;
	 String strRegistrationId;  // Device ID for GCM purposes
 	 
	 // Temporarily locals
	 ImageView myPreviewImageView;
	 static clsImageUpDownloadAsyncTask objImageUpDownloadAsyncTask;
	 private clsExportToMail objExportToMail;
	 private clsIabLocalData objIabLocalData;
	 

	 
  
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	    
	        super.onCreate(savedInstanceState);
	        
	        objActivity = this;
			 
			objGroupMembers.LoadFile();
			objMessaging.LoadFile(this);
			        
	        // Get Treeview listItems  
	        Bundle objBundle = getIntent().getExtras();    		
			strNoteUuid    = objBundle.getString(ActivityExplorerStartup.TREENODE_UID);
		
			boolIsShortcut = objBundle.getBoolean(ActivityExplorerStartup.IS_SHORTCUT);
			
			String strImageLoadDatas = objBundle.getString(ActivityExplorerStartup.IMAGE_LOAD_DATAS);
			java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsImageLoadData>>(){}.getType();
			objLocalImageLoadDatas = clsUtils.DeSerializeFromString(strImageLoadDatas, collectionType);
			
			strRegistrationId = clsUtils.getRegistrationId(objActivity);

			File objFile = clsUtils.BuildNoteFilename(ActivityExplorerStartup.fileTreeNodesDir, strNoteUuid );
			objNoteTreeview = new clsNoteTreeview(objActivity, objGroupMembers);
			if (objFile.exists()) {
				objNoteTreeview.DeserializeFromFile(objFile);
				objNoteTreeview.SetAllIsDirty(false);		
			} else {
				objNoteTreeview.getRepository().setStrOwnerUserUuid(objGroupMembers.objMembersRepository.getStrRegisteredUserUuid());
				objNoteTreeview.getRepository().uuidRepository = UUID.fromString(strNoteUuid);
				objNoteTreeview.getRepository().setName(objBundle.getString(ActivityExplorerStartup.TREENODE_NAME));
			}
			
			// Determine if user is registered
			GetUserRegistrationStatus();
			

	        listItems = objNoteTreeview.getListItems();
	                
	        this.setTitle("Edit: " + objNoteTreeview.getRepository().getName());
	        
	        setContentView(R.layout.activity_note_startup);
	        
	        // List View 
	        int resID = R.layout.note_list_item;
			int intTabWidthInDp = clsUtils.GetDefaultTabWidthInDp(this);			
			int intTabWidthInPx = clsUtils.dpToPx(this, intTabWidthInDp);     
	        objListItemAdapter = new clsNoteListItemArrayAdapter(this, resID, listItems, objNoteTreeview, intTabWidthInPx);
	        setListAdapter(objListItemAdapter);
	        
	        // NewItemsIndicator View
	        clsNewItemsIndicatorView objClsNewItemsIndicatorView = (clsNewItemsIndicatorView)findViewById(R.id.newitems_indicator_view);
			objClsNewItemsIndicatorView.UpdateListItems(listItems);
	       	                
	        // Actionbar
	        ActionBar actionBar = getActionBar();
	        actionBar.show();
	        actionBar.setDisplayHomeAsUpEnabled(true);
	        getOverflowMenu(); 
	        
	        // AdMob
			// AdMob, only when advert removal has not been purchased
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objActivity);
			objIabLocalData = clsUtils.LoadIabLocalValues(sharedPref, objIabLocalData);
			if (!(objIabLocalData != null && objIabLocalData.boolIsAdsDisabledA && !objIabLocalData.boolIsAdsDisabledB)) {
				// Look up the AdView as a resource and load a request.
				AdView adView = (AdView)this.findViewById(R.id.adViewNote);
				AdRequest adRequest = new AdRequest.Builder()
		        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		        .addTestDevice("803D489BC46137FD0761EC7EBFBBFB09")
				.addTestDevice("C1B978D9FE1B0A6A8A58F1F44F653BE3")
				.addTestDevice("A947D095EE036142160FD3D2D4D5034C").build();

				adView.loadAd(adRequest);
			} else {
				RelativeLayout adscontainer = (RelativeLayout) findViewById(R.id.note_relative_layout);
				View admobAds = (View) findViewById(R.id.adViewNote);
				adscontainer.removeView(admobAds);
			}
			
			// Broadcast from notification service
			IntentFilter mStatusIntentFilter = new IntentFilter(ActivityNoteStartup.BROADCAST_ACTION);
	        // Instantiates a new DownloadStateReceiver
	        ResponseReceiver objResponseReceiver = new ResponseReceiver();
	        // Registers the DownloadStateReceiver and its intent filters
	        LocalBroadcastManager.getInstance(this).registerReceiver(objResponseReceiver, mStatusIntentFilter);
			
			// Session Management
	        clsUtils.CustomLog("ActivityNoteStartup onCreate");
	        if (savedInstanceState == null) {
	        	SaveTemp();
	        } else {
	        	LoadTemp();
	        }
	              
	    }


	    // Broadcast receiver for receiving status updates from the IntentService
	    private class ResponseReceiver extends BroadcastReceiver
	    {

	        // Called when the BroadcastReceiver gets an Intent it's registered to receive
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	Bundle extras = intent.getExtras();
	        	String strNoteUuid = extras.getString(BROADCAST_DATA_NOTE_UUID);
	        	if (objNoteTreeview.getRepository().uuidRepository.toString().equals(strNoteUuid)) {
	        		// Will only auto-sync an open and same repository, otherwise ignore
		        	ExecuteNoteSync(true, false);
	        	}
	        }
	    }




		private void GetUserRegistrationStatus() {
			if (!objGroupMembers.objMembersRepository.getStrRegisteredUserUuid().isEmpty()) {
	        	boolIsUserRegistered = true;
	        	// Determine if user is this note owner
	        	if (objNoteTreeview.getRepository().getNoteOwnerUserUuid().equals(objGroupMembers.GetRegisteredUser().strUserUuid)) {
					boolUserIsNoteOwner = true;
				} else {
					boolUserIsNoteOwner = false;
				}
			} else {
				boolIsUserRegistered = false;
				boolUserIsNoteOwner = false;
	        }
		}

	
	    @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	    	// TODO Auto-generated method stub
	    	super.onConfigurationChanged(newConfig);
	    	clsIndentableTextView objMyTextView = (clsIndentableTextView) findViewById(R.id.row);
	    	// objMyTextView.IndicateOrientationChanged();    	
	    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_startup_activity, menu);
        
        // Selectively enable PASTE
        if ((objNoteTreeview.objClipboardTreeNodes.size() == 0) || (objNoteTreeview.objClipboardTreeNodes == null)){
        	menu.findItem(R.id.actionPaste).setVisible(false);
        }
        else {
        	menu.findItem(R.id.actionPaste).setVisible(true);
        }
        
        // Selectively enable checklist menu items
        if(ActivityNoteStartup.objNoteTreeview.getRepository().IsCheckList()){
        	menu.findItem(R.id.actionCheckList).setVisible(true);
        	if (ActivityNoteStartup.objNoteTreeview.getRepository().boolIsCheckedItemsMadeHidden) {
        		menu.findItem(R.id.actionCheckListHideCheckedItems).setVisible(false);
        		menu.findItem(R.id.actionCheckListUnHideCheckedItems).setVisible(true);      		
        	} else {
        		menu.findItem(R.id.actionCheckListHideCheckedItems).setVisible(true);
        		menu.findItem(R.id.actionCheckListUnHideCheckedItems).setVisible(false); 
        	}       	
        } else {
        	menu.findItem(R.id.actionCheckList).setVisible(false);
        }
        
        // Selectively enable "hidden" functionality menu items
        menu.findItem(R.id.actionHidden).setVisible(true); 
        if(ActivityNoteStartup.objNoteTreeview.getRepository().boolHiddenSelectionIsActive){
    		menu.findItem(R.id.actionHiddenSelectionStart).setVisible(false);
    		menu.findItem(R.id.actionHiddenSelectionEnd).setVisible(true);      		
    	} else {
    		menu.findItem(R.id.actionHiddenSelectionStart).setVisible(true);
    		menu.findItem(R.id.actionHiddenSelectionEnd).setVisible(false);  
    	}
        	
        if(ActivityNoteStartup.objNoteTreeview.getRepository().boolIsHiddenActive){
    		menu.findItem(R.id.actionHiddenActivate).setVisible(false);
    		menu.findItem(R.id.actionHiddenInactivate).setVisible(true);      		
    	} else {
    		menu.findItem(R.id.actionHiddenActivate).setVisible(true);
    		menu.findItem(R.id.actionHiddenInactivate).setVisible(false);  
    	} 
        
        // Selectively enable "share" and "comment" functionality menu items
        // Local variables used by underlying functionality
        clsTreeNode objSelectedTreeNode = null;
        clsTreeNode objSelectedParentTreeNode = null;
        clsNoteItemStatus objNoteItemStatus = new clsNoteItemStatus();
        
        
        // Get the selected item first, if selected and build rest of data
        ArrayList<clsTreeNode> objSelectedTreeNodes = objNoteTreeview.getSelectedTreenodes();
        if (objSelectedTreeNodes.size() == 1) {
        	DetermineNoteItemStatus(objSelectedTreeNodes.get(0),objNoteItemStatus, objGroupMembers, objNoteTreeview);
        }
        // Manage display of Share items
        
        // Manage display of Comment and Add/Edit items
    	// Display only comments menu when user is noteowner
    	menu.findItem(R.id.actionComments).setVisible(false);
		menu.findItem(R.id.actionCommentsChangeOwnerToMainUser).setVisible(false);
		// A comment must be selected to display the comments
		if(objNoteItemStatus.boolSelectedNoteItemIsComment) {
			// Display only when user is note owner
	    	if(boolUserIsNoteOwner) {
	    		menu.findItem(R.id.actionComments).setVisible(true);
	    		menu.findItem(R.id.actionCommentsChangeOwnerToMainUser).setVisible(true);
	    	}
		}
    	// Manage display of Edit items
		// 	Only owner of item can edit items 
		menu.findItem(R.id.actionEdit).setVisible(true);

		// Manage display of Delete items
		// Non note owner can only delete own note items
		menu.findItem(R.id.actionDelete).setVisible(false); 
		if (objNoteItemStatus.boolSelectedNoteItemIsRegistered) {
			if(!boolUserIsNoteOwner) {
				// Display only when user is note owner
		    	if(objNoteItemStatus.boolSelectedNoteItemIsComment) {
		    		if(objNoteItemStatus.boolSelectedNoteItemBelongsToUser) {
		    			menu.findItem(R.id.actionDelete).setVisible(true);
		    		}
		    	}
			} else {
				menu.findItem(R.id.actionDelete).setVisible(true);
			}
		} else {
			menu.findItem(R.id.actionDelete).setVisible(true);
		}
		
		
		
        return super.onCreateOptionsMenu(menu);
    }
    
    public class clsNoteItemStatus { 
    	boolean boolIsUserRegistered;
   	 	boolean boolUserIsNoteOwner;
   	 	boolean boolSelectedNoteItemIsRegistered;
        boolean boolSelectedNoteItemBelongsToUser;
        boolean boolSelectedNoteItemIsComment;      
        boolean boolSelectedCommentBelongsToUser;
        boolean boolParentItemIsComment;
    }
  
	public void DetermineNoteItemStatus(clsTreeNode objSelectedTreeNode, clsNoteItemStatus objStatus, 
			clsGroupMembers objGroupMembers, clsTreeview objTreeview){
    	objStatus.boolIsUserRegistered = boolIsUserRegistered;
    	objStatus.boolUserIsNoteOwner = boolUserIsNoteOwner;
    	objStatus.boolSelectedNoteItemBelongsToUser = false;
    	objStatus.boolSelectedNoteItemIsComment = false;      
    	objStatus.boolSelectedCommentBelongsToUser = false;
    	objStatus.boolParentItemIsComment = false;
    	objStatus.boolSelectedNoteItemIsRegistered = false;
    	if (objSelectedTreeNode != null) {
    		if (!objSelectedTreeNode.getStrOwnerUserUuid().isEmpty()) {
        		objStatus.boolSelectedNoteItemIsRegistered = true;
        	}
        	
        	if (objSelectedTreeNode != null) {
        		String strNoteOwnerUuid = objTreeview.getRepository().getNoteOwnerUserUuid();
        		String strItemOwnerUuid = objSelectedTreeNode.getStrOwnerUserUuid();
        		if (!strItemOwnerUuid.equals(strNoteOwnerUuid)) {
            		objStatus.boolSelectedNoteItemIsComment = true;	
                }
            	// Determine if selected item belongs to current user
                if (objSelectedTreeNode.getStrOwnerUserUuid().equals(objGroupMembers.GetRegisteredUser().strUserUuid)) {
                	objStatus.boolSelectedNoteItemBelongsToUser = true;
                	// Determine if selected comment item belongs to current user
                	if (objStatus.boolSelectedNoteItemIsComment) {
                		objStatus.boolSelectedCommentBelongsToUser = true;
                	}
        		}
                // Determine parent treenode
                clsTreeNode objSelectedParentTreeNode = objNoteTreeview.getParentTreeNode(objSelectedTreeNode);
                if (objSelectedParentTreeNode != null) {
                	String strParentItemOwnerUuid = objSelectedParentTreeNode.getStrOwnerUserUuid();
                    if (!strParentItemOwnerUuid.equals(strNoteOwnerUuid)) {
                    	objStatus.boolParentItemIsComment = true;
                    }
                } 
        	} 
    	} 	
    }
     

	
	
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	clsUtils.CustomLog("ActivityNoteStartup onStart LoadTemp");
    	LoadTemp();
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
    	clsUtils.CustomLog("ActivityNoteStartup onStop SaveTemp");
    	SaveTemp();
    	super.onStop();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	clsUtils.CustomLog("ActivityNoteStartup onPause SaveTemp");
    	SaveTemp();
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	clsUtils.CustomLog("ActivityNoteStartup onDestroy SaveTemp");
    	SaveTemp();
    	super.onDestroy();
    }
    
    @Override
    protected void onRestart() {
    	clsUtils.CustomLog("ActivityNoteStartup onRestart LoadTemp");
    	LoadTemp();
    	super.onRestart();
    }
    
    @Override
    protected void onResume() {
    	clsUtils.CustomLog("ActivityNoteStartup onResume LoadTemp");
    	LoadTemp();
    	super.onResume();
    }
    
    private void LoadFile() {
		clsUtils.CustomLog("LoadPerm");
		objGroupMembers.LoadFile();
		objGroupMembers.UpdateEnvironment(this);
		
		objNoteTreeview = new clsNoteTreeview(objActivity, objGroupMembers);
		objNoteTreeview.UpdateEnvironment(this, clsExplorerTreeview.enumCutCopyPasteState.INACTIVE, new ArrayList<clsTreeNode>() ); // Clipboard data must be persisted at a later stage
		objNoteTreeview.DeserializeFromFile(clsUtils.BuildTempNoteFilename(ActivityExplorerStartup.fileTreeNodesDir));

   	    objMessaging.LoadFile(this);
	}
    
    private void SaveFile() {
    	clsUtils.CustomLog("SavePerm");
    	objNoteTreeview.SetAllIsDirty(false);
    	SaveTemp();
		objNoteTreeview.getRepository().SerializeToFile(clsUtils.BuildNoteFilename(ActivityExplorerStartup.fileTreeNodesDir, strNoteUuid));
		objGroupMembers.SaveFile();
		objMessaging.SaveFile(this);
	}
    
    private void LoadTemp() {
		clsUtils.CustomLog("LoadTemp");
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityNoteStartup",Context.MODE_PRIVATE);
		
		objNoteTreeview = new clsNoteTreeview(objActivity, objGroupMembers);
		objNoteTreeview.UpdateEnvironment(this, clsExplorerTreeview.enumCutCopyPasteState.INACTIVE, new ArrayList<clsTreeNode>() ); // Must be persisted at a later stage
		String strNoteTreeviewRepository = sharedPref.getString("objNoteTreeviewRepository", "");
		clsRepository objRepository = clsUtils.DeSerializeFromString(strNoteTreeviewRepository, clsRepository.class);
		objNoteTreeview.setRepository(objRepository);
		
		String strGroupMembersRepository = sharedPref.getString("objGroupMembersRepository", "");
		objGroupMembers.objMembersRepository = clsUtils.DeSerializeFromString(strGroupMembersRepository, objGroupMembers.objMembersRepository.getClass());
		objGroupMembers.UpdateEnvironment(this);
		
		String strMessagingRepository = sharedPref.getString("objMessagingRepository", "");
		objMessaging.objRepository = clsUtils.DeSerializeFromString(strMessagingRepository, objMessaging.objRepository.getClass());

		strNoteUuid = sharedPref.getString("strNoteUuid", "");
		
    	ArrayList<clsListItem> objListItems = objNoteTreeview.getListItems();
    	objListItemAdapter.UpdateEnvironment(objNoteTreeview);
    	objListItemAdapter.clear(); objListItemAdapter.addAll(objListItems);

   	    objActivity = this;
   	    
   	    boolIsShortcut = sharedPref.getBoolean("boolIsShortcut",false);
   	    
   	    String strImageLoadDatas = sharedPref.getString("objImageLoadDatas", "");
   	    java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsImageLoadData>>(){}.getType();
   	    objLocalImageLoadDatas = clsUtils.DeSerializeFromString(strImageLoadDatas, collectionType);
   	    
   	    GetUserRegistrationStatus(); // Update boolIsUserRegistered and boolUserIsNoteOwner
   	    strRegistrationId = clsUtils.getRegistrationId(objActivity);
	}
    
    private void SaveTemp() {
    	clsUtils.CustomLog("SaveTemp");
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityNoteStartup",Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPref.edit();
    	
    	String strNoteTreeviewRepository = clsUtils.SerializeToString(objNoteTreeview.getRepository());
    	editor.putString("objNoteTreeviewRepository", strNoteTreeviewRepository);
    	
    	String strGroupMembersRepository = clsUtils.SerializeToString(objGroupMembers.objMembersRepository);
    	editor.putString("objGroupMembersRepository", strGroupMembersRepository);
    	
    	String strMessagingRepository = clsUtils.SerializeToString(objMessaging.objRepository);
    	editor.putString("objMessagingRepository", strMessagingRepository);	

    	editor.putString("strNoteUuid", strNoteUuid);
    	
    	editor.putBoolean("boolIsShortcut",boolIsShortcut);
    	String strImageLoadDatas = clsUtils.SerializeToString(objLocalImageLoadDatas);
    	editor.putString("objImageLoadDatas", strImageLoadDatas);
    	editor.commit();
    	sharedPref = null;
	}
    
  
	
	private String getParentDescription(clsTreeview.clsTreeNode objSelected)
	{
		// Return description of topmost name if no row is selected. That happens when the 
		// first note is created.
		if(objSelected == null)
		{
			return ActivityNoteStartup.objNoteTreeview.getRepository().getName();
		}
		
		clsTreeNode objParentTreeNode = objNoteTreeview.getParentTreeNode(objSelected);
		
		String retval = (objParentTreeNode == null)?(ActivityNoteStartup.objNoteTreeview.getRepository().getName())
					 							  :(objParentTreeNode.getName());

		return retval;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
		 Intent intent;
		 AlertDialog.Builder builder;
		 AlertDialog dialog;
		 int intOrder;
		 boolean boolIsBusyWithCutOperation;
    	 boolean boolPasteAtSameLevel;
    	 clsNoteItemStatus objNoteItemStatus = new clsNoteItemStatus();
		 
		 // retrieve selected row (might be null)
		 ArrayList<clsTreeNode> objSelectedTreeNodes = objNoteTreeview.getSelectedTreenodes();
		 
    	 switch (item.getItemId()) {
         case R.id.actionAddAtSameLevel:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;
        	 }
        	     	             	 
        	 intent = new Intent(this, ActivityNoteAddNew.class);
        	 intent.putExtra(DESCRIPTION, "");
        	 intent.putExtra(TREENODE_UID, "temp_uuid");
        	 
        	 // Search for parent of selected note and supply description to activity
        	 String strParentDescription = getParentDescription((objSelectedTreeNodes.isEmpty())?(null):(objSelectedTreeNodes.get(0)));
        	 intent.putExtra(ActivityNoteStartup.TREENODE_PARENTNAME, strParentDescription); 
        	 
        	 startActivityForResult(intent,GET_DESCRIPTION_ADD_SAME_LEVEL);
             return true;            
         case R.id.actionButtonAddAtNextLevel:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please select an item first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;     		 
        	 }
        	     	     
        	 intent = new Intent(this, ActivityNoteAddNew.class);
        	 intent.putExtra(DESCRIPTION, "");
        	 intent.putExtra(TREENODE_UID, "temp_uuid");
        	 intent.putExtra(ActivityNoteStartup.TREENODE_PARENTNAME, objSelectedTreeNodes.get(0).getName()); 
        	 
        	 startActivityForResult(intent,GET_DESCRIPTION_ADD_NEXT_LEVEL);
             return true;
             
         case R.id.actionEdit:
        	 // Only applicable if a row is selected
        	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please add some items first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please select an item first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;     		 
        	 }
        	         	 
        	 clsTreeNode objEditTreeNode = objSelectedTreeNodes.get(0);
        	 StartNoteItemEditIntent(objEditTreeNode);
             return true;
             
         case R.id.actionDelete:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please add some items first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please select an item first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;     		 
        	 }
        	builder = new AlertDialog.Builder(this);
 		    builder.setMessage(R.string.dlg_are_you_sure_about_delete);
 		    builder.setCancelable(true);
 		    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int id) {
 	            	clsTreeNode objDeletedTreeNode = objNoteTreeview.getSelectedTreenodes().get(0);
 	            	objDeletedTreeNode.setIsDeleted(true);
 	            	objNoteTreeview.UpdateItemTypes();
 	            	if (objNoteTreeview.IsNoteShared() == false) {
 	            		// Delete immediately if note is not shared, otherwise deletion will take place during next sync
 	            		 objNoteTreeview.RemoveAllIsDeletedTreeNodes(true);
 	            		 
 	            		 // TODO JE delete all images (clsUtils.RemoveAllImagesOfNode) of tree, probably in above method because of possible sync
 	            	}
	 	           
	 	            RefreshListView();
 	            }
 	        });
 		    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int id) {
            		return;
 	            }
 	        });
 		    dialog = builder.create();
 		    dialog.show();
        	 return true;
         case R.id.actionCheckListHideCheckedItems:
        	 objNoteTreeview.getRepository().boolIsCheckedItemsMadeHidden = true;
        	 invalidateOptionsMenu();
        	 RefreshListView();
        	 return true;
         case R.id.actionCheckListUnHideCheckedItems:
        	 objNoteTreeview.getRepository().boolIsCheckedItemsMadeHidden = false;
        	 invalidateOptionsMenu();
        	 RefreshListView();
        	 return true;
         case R.id.actionCheckListUnCheckAll:
        	builder = new AlertDialog.Builder(this);
 	    	builder.setTitle("Are you sure you want to uncheck all items?");
 	    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						objNoteTreeview.RecursiveSetAllChecked(false);
						RefreshListView(); 
					}
				});
 	    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
						RefreshListView(); 
					}
				});
 	    	builder.show();
        	return true;
         case R.id.actionCheckListDeleteAllChecked:
         	builder = new AlertDialog.Builder(this);
  	    	builder.setTitle("Are you sure you want to delete all checked items?");
  	    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Auto-generated method stub
 						objNoteTreeview.RecursiveRemoveAllChecked();
 						RefreshListView(); 
 					}
 				});
  	    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// TODO Auto-generated method stub
 						dialog.cancel();
 						RefreshListView(); 
 					}
 				});
  	    	builder.show();
         	return true;	
         case R.id.actionMoveUp:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please add some items first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please select an item first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;     		 
        	 }
        	 clsTreeNode objMoveUpTreeNode = objSelectedTreeNodes.get(0);
        	 intOrder = objNoteTreeview.getTreeNodeItemOrder(objMoveUpTreeNode);
        	 objNoteTreeview.setTreeNodeItemOrder(objMoveUpTreeNode, intOrder-=1);
        	 RefreshListView();
        	 return true;  
         case R.id.actionHiddenSelectionStart:
        	 objNoteTreeview.getRepository().boolHiddenSelectionIsActive = true;
        	 RefreshListView();
        	 return true;
         case R.id.actionHiddenSelectionEnd:
        	 objNoteTreeview.getRepository().boolHiddenSelectionIsActive = false;
        	 RefreshListView();
        	 return true;
         case R.id.actionHiddenActivate:
        	 objNoteTreeview.getRepository().boolHiddenSelectionIsActive = false;
        	 objNoteTreeview.getRepository().boolIsHiddenActive = true;
        	 RefreshListView();
        	 return true;
         case R.id.actionHiddenInactivate:
        	 objNoteTreeview.getRepository().boolIsHiddenActive = false;
        	 RefreshListView();
        	 return true;
         case R.id.actionSendToMail:
        	 objExportToMail = new clsExportToMail(this, objNoteTreeview, objMessaging, objGroupMembers);
        	 objExportToMail.Execute();
        	 return true;
         case R.id.actionSendToFacebook:
        	 clsMainExport objMainExport = new clsMainExport(this, this, objNoteTreeview, objMessaging, objGroupMembers);
        	 objMainExport.Execute(clsMainExport.EXPORT_DEST.TO_FACEBOOK);
        	 return true;
         case R.id.actionMoveDown:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please add some items first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please select an item first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;     		 
        	 }
        	 clsTreeNode objMoveDownTreeNode = objSelectedTreeNodes.get(0);
        	 intOrder = objNoteTreeview.getTreeNodeItemOrder(objMoveDownTreeNode);       	 
        	 objNoteTreeview.setTreeNodeItemOrder(objMoveDownTreeNode, intOrder+=1);
        	 RefreshListView();
        	 return true;
        	 
         case R.id.actionCut:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please add some items first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please select items first.", false);
        		 return false;
        	 }
        	 objNoteTreeview.objClipboardTreeNodes = objSelectedTreeNodes; 
        	 objNoteTreeview.intCutCopyPasteState = enumCutCopyPasteState.CUTTING;
        	 objNoteTreeview.ClearSelection();
        	 RefreshListView();
        	 return true;
        	 
         case R.id.actionCopy:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please add some items first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please select items first.", false);
        		 return false;
        	 }
        	 objNoteTreeview.objClipboardTreeNodes = objSelectedTreeNodes;
        	 objNoteTreeview.intCutCopyPasteState = enumCutCopyPasteState.PASTING;
        	 objNoteTreeview.ClearSelection();
        	 RefreshListView();
        	 return true;
         case R.id.actionPasteAtSameLevel:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;     		 
        	 }
        	 boolIsBusyWithCutOperation = (objNoteTreeview.intCutCopyPasteState == enumCutCopyPasteState.CUTTING)?true:false;
        	 boolPasteAtSameLevel = true;
        	 if (objSelectedTreeNodes.size()== 0) {
        		 objNoteTreeview.PasteClipboardTreeNodes(null,boolPasteAtSameLevel,boolIsBusyWithCutOperation); // Add to root
        	 } else {
        		 objNoteTreeview.PasteClipboardTreeNodes(objSelectedTreeNodes.get(0),boolPasteAtSameLevel,boolIsBusyWithCutOperation); // Add as children
        	 }
        	 objNoteTreeview.UpdateItemTypes();
        	 RefreshListView();
        	 
        	 return true;  
        	 
         case R.id.actionPasteAtNextLevel:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;     		 
        	 }
        	 boolIsBusyWithCutOperation = (objNoteTreeview.intCutCopyPasteState == enumCutCopyPasteState.CUTTING)?true:false;
        	 boolPasteAtSameLevel = false;
        	 if (objSelectedTreeNodes.size()== 0) {
        		 objNoteTreeview.PasteClipboardTreeNodes(null,boolPasteAtSameLevel,boolIsBusyWithCutOperation); // Add to root
        	 } else {
        		 objNoteTreeview.PasteClipboardTreeNodes(objSelectedTreeNodes.get(0),boolPasteAtSameLevel,boolIsBusyWithCutOperation); // Add as children
        	 }
        	 objNoteTreeview.UpdateItemTypes();
        	 RefreshListView();
        	 
        	 return true; 
         case R.id.actionSave:
    	    builder = new AlertDialog.Builder(this);
  		    builder.setMessage("Are you sure you want to save the note?");
  		    builder.setCancelable(true);
  		    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
  	            public void onClick(DialogInterface dialog, int id) {
  	            	File objFile = clsUtils.BuildNoteFilename(ActivityExplorerStartup.fileTreeNodesDir,objNoteTreeview.getRepository().uuidRepository.toString());
  	            	objNoteTreeview.SetAllIsDirty(false);
 	 	          	objNoteTreeview.getRepository().SerializeToFile(objFile);
 	 	          	return;
  	            }
  	        });
  		    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
  	            public void onClick(DialogInterface dialog, int id) {
             		return;
  	            }
  	        });
  		    dialog = builder.create();
   		    dialog.show();		
         	return true;
         case R.id.actionShareSelect:
        	 clsGetNoteSharedUsersResponse objResponse = new clsGetNoteSharedUsersResponse();
        	 clsUtils.StartActivityForShareSelect(objActivity, objMessaging, strNoteUuid, objResponse);
          	return true;
         case R.id.actionShareSync: 
        	 clsUser objRegisteredUser = objGroupMembers.GetRegisteredUser();
        	 if (objRegisteredUser.strUserName.equals(getResources().getString(R.string.unregistered_username))) {
        		 // Not registered, cannot sync
        		 Toast.makeText(this, "You need to register first before you can sync",Toast.LENGTH_SHORT).show();
        		 return true;
        	 }
        	  
        	if (objNoteTreeview.GetAllIsDirty()) {
				builder = new AlertDialog.Builder(this);
				builder.setMessage("If you send this note, the current changes will be saved first and you cannot revert to the original. Do you want to continue?");
				builder.setCancelable(true);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Save first
						objNoteTreeview.SetAllIsDirty(false);
						SaveFile();
						// Now sync
						ExecuteNoteSync(false, true);
						return;
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						return;
					}
				});
				dialog = builder.create();
				dialog.show();
			} else {
				// Sync immediately
				ExecuteNoteSync(false, true);
			}
			return true;
         case R.id.actionShareRestoreFromServer: 
        	 URL urlFeed;
        	 objRegisteredUser = objGroupMembers.GetRegisteredUser();
        	 if (objRegisteredUser.strUserName.equals(getResources().getString(R.string.unregistered_username))) {
        		 // Not registered, cannot sync
        		 Toast.makeText(this, "You need to register first before you can sync",Toast.LENGTH_SHORT).show();
        		 return true;
        	 }
        	 try {
				urlFeed = new URL(objMessaging.GetServerUrl() + getResources().getString(R.string.url_note_sync));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			}
        	 clsSyncNoteCommandMsg objSyncCommandMsg = objMessaging.new clsSyncNoteCommandMsg();
        	 clsSyncRepositoryCtrlData objRepositoryCtrlData = objMessaging.new clsSyncRepositoryCtrlData();
        	 objRepositoryCtrlData.objSyncRepository = objNoteTreeview.getRepository().getCopy(this);
        	 objRepositoryCtrlData.boolNeedsAutoSyncWithNotification = true;
        	 objRepositoryCtrlData.boolNeedsOnlyChangeNotification = false;
        	 objSyncCommandMsg.objSyncRepositoryCtrlDatas.add(objRepositoryCtrlData);
        	 objSyncCommandMsg.strClientUserUuid = objGroupMembers.objMembersRepository.getStrRegisteredUserUuid();
        	 objSyncCommandMsg.strRegistrationId = strRegistrationId;
        	 objSyncCommandMsg.boolIsMergeNeeded = false;
        	 objSyncCommandMsg.boolIsAutoSyncCommand = false;
        	 ActivityNoteStartupSyncAsyncTask objSyncAsyncTask = new ActivityNoteStartupSyncAsyncTask(this, objNoteTreeview, urlFeed, objSyncCommandMsg,objMessaging, true, true);
        	 objSyncAsyncTask.execute("");
        	 return true;
         case R.id.actionClearAll:
          	builder = new AlertDialog.Builder(this);
   		    builder.setMessage(R.string.dlg_are_you_sure_about_delete);
   		    builder.setCancelable(true);
   		    builder.setPositiveButton("OK", ClearAllOnClickListener());
   		    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
   	            public void onClick(DialogInterface dialog, int id) {
   	            	return;
   	            }
   	        });
   		    dialog = builder.create();
		    dialog.show();
   		  return true;
         
         case R.id.actionSettings:
			 SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			 SharedPreferences.Editor editor = sharedPref.edit();
	    	 editor.putBoolean("checkbox_enable_checklist", objNoteTreeview.getRepository().IsCheckList());
	    	 editor.putString("registered_user", objGroupMembers.GetRegisteredUser().strUserName);
	    	 String strNoteOwnerUserName = objGroupMembers.GetUserNameFomUuid(objNoteTreeview.getRepository().getNoteOwnerUserUuid());
	    	 editor.putString("note_owner",strNoteOwnerUserName);
	    	 // Check if there is a single selected item
	    	 UUID guidSelectedTreeNode;
	    	 String strSubItemOwnerUserName = "";
	     	 if (objSelectedTreeNodes.size() != 0) {
	     		guidSelectedTreeNode = objSelectedTreeNodes.get(0).guidTreeNode;
	     		clsTreeNode objSelectedTreeNode = objNoteTreeview.getTreeNodeFromUuid(guidSelectedTreeNode);
		    	strSubItemOwnerUserName = objGroupMembers.GetUserNameFomUuid(objSelectedTreeNode.getStrOwnerUserUuid());
	     	 } else {
	     		strSubItemOwnerUserName = "No selected item to evaluate";
	     	 }	    	 
	    	 editor.putString("note_selecteditem_owner",strSubItemOwnerUserName);
	    	 editor.commit();
	    	 intent = new Intent(this, ActivityNoteSettings.class);
	    	 startActivityForResult(intent, ActivityNoteStartup.EDIT_SETTINGS);
	    	 return true;
         case R.id.actionTestNote:
        	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please add some items first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
        		 clsUtils.MessageBox(this, "Please select an item first.", false);
        		 return false;
        	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
        		 clsUtils.MessageBox(this, "Please select only one item at a time", false);
        		 return false;     		 
        	 }
        	 clsTreeNode objNewTreeNode = objSelectedTreeNodes.get(0);
        	 objNewTreeNode.boolIsNew = !objNewTreeNode.boolIsNew;
        	 RefreshListView();
        	 return true;
         default:
             return super.onOptionsItemSelected(item);
    	 }
    }


	private void ExecuteNoteSync(boolean boolIsAutoSyncCommand, boolean boolDisplayProgress) {
		SaveTemp();
		 URL urlFeed;
		 try {
			urlFeed = new URL(objMessaging.GetServerUrl() + getResources().getString(R.string.url_note_sync));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		 clsSyncNoteCommandMsg objSyncCommandMsg = objMessaging.new clsSyncNoteCommandMsg();
		 clsSyncRepositoryCtrlData objRepositoryCtrlData = objMessaging.new clsSyncRepositoryCtrlData();
		 objRepositoryCtrlData.objSyncRepository = objNoteTreeview.getRepository().getCopy(this);
		 objRepositoryCtrlData.boolNeedsAutoSyncWithNotification = true;
		 objRepositoryCtrlData.boolNeedsOnlyChangeNotification = false;
		 objSyncCommandMsg.objSyncRepositoryCtrlDatas.add(objRepositoryCtrlData);
		 objSyncCommandMsg.strClientUserUuid = objGroupMembers.objMembersRepository.getStrRegisteredUserUuid();
		 objSyncCommandMsg.strRegistrationId = strRegistrationId;
		 objSyncCommandMsg.boolIsMergeNeeded = true;
		 objSyncCommandMsg.boolIsAutoSyncCommand = boolIsAutoSyncCommand;
		 ActivityNoteStartupSyncAsyncTask objSyncAsyncTask = new ActivityNoteStartupSyncAsyncTask(this, objNoteTreeview, urlFeed, objSyncCommandMsg,objMessaging, true, boolDisplayProgress);
		 objSyncAsyncTask.execute("");
	}
	
	public class MyTextOnClickListenerBackup2 implements View.OnClickListener{
		@Override
		public void onClick(View v) {
		 ArrayList<clsTreeNode> objSelectedTreeNodes = objNoteTreeview.getSelectedTreenodes();
			// Only applicable if a row is selected
       	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
       		 clsUtils.MessageBox(objActivity, "Please add some items first.", false);
       		 return;
       	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
       		clsUtils.MessageBox(objActivity, "Please select an item first.", false);
       		 return;
       	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
       		clsUtils.MessageBox(objActivity, "Please select only one item at a time", false);
       		 return;     		 
       	 }
       	 clsTreeNode objEditTreeNode = objSelectedTreeNodes.get(0);
       	 Intent intent = new Intent(ActivityNoteStartup.this, ActivityNoteAddNew.class);
       	 intent.putExtra(DESCRIPTION,   objEditTreeNode.getName());
       	 intent.putExtra(RESOURCE_ID,   objEditTreeNode.resourceId);
       	 intent.putExtra(RESOURCE_PATH, objEditTreeNode.resourcePath);
       	 intent.putExtra(TREENODE_UID, objEditTreeNode.guidTreeNode.toString());

       	 startActivityForResult(intent, EDIT_DESCRIPTION);
		}
	}
	
	public class MyTextOnClickListenerBackup3 implements View.OnClickListener{
		@Override
		public void onClick(View v) {
		 ArrayList<clsTreeNode> objSelectedTreeNodes = objNoteTreeview.getSelectedTreenodes();
			// Only applicable if a row is selected
       	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
       		 clsUtils.MessageBox(objActivity, "Please add some items first.", false);
       		 return;
       	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
       		clsUtils.MessageBox(objActivity, "Please select an item first.", false);
       		 return;
       	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
       		clsUtils.MessageBox(objActivity, "Please select only one item at a time", false);
       		 return;     		 
       	 }

       	 clsTreeNode objEditTreeNode = objSelectedTreeNodes.get(0);
       	 Intent intent = new Intent(ActivityNoteStartup.this, ActivityNoteAddNew.class);
       	 intent.putExtra(DESCRIPTION,   objEditTreeNode.getName());
       	 intent.putExtra(RESOURCE_ID,   objEditTreeNode.resourceId);
       	 intent.putExtra(RESOURCE_PATH, objEditTreeNode.resourcePath);
       	 intent.putExtra(TREENODE_UID, objEditTreeNode.guidTreeNode.toString());
       	 
       	 startActivityForResult(intent, EDIT_DESCRIPTION);
		}
	}


	public void RefreshListView() {
		 SaveTemp(); 
		 ArrayList<clsListItem> objListItems = objNoteTreeview.getListItems(); 
		 objListItemAdapter.clear(); objListItemAdapter.addAll(objListItems);
		 objListItemAdapter.notifyDataSetChanged();
		 clsNewItemsIndicatorView objClsNewItemsIndicatorView = (clsNewItemsIndicatorView)findViewById(R.id.newitems_indicator_view);
		 objClsNewItemsIndicatorView.UpdateListItems(objListItems);
		 invalidateOptionsMenu();
	}
	
	private OnClickListener ClearAllOnClickListener() {
		return new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
		 	 objNoteTreeview.ClearAll();
		 	objNoteTreeview.getRepository().SerializeToFile(clsUtils.BuildNoteFilename(ActivityExplorerStartup.fileTreeNodesDir, objNoteTreeview.getRepository().uuidRepository.toString()));
			 RefreshListView();
		    }
		};
	} 
	
	// Sink for own and foreign Intents
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	AlertDialog.Builder builder;
    	AlertDialog dialog;
    	
    	UUID guidSelectedTreeNode;
    	ArrayList<clsTreeNode> objSelectedTreeNodes = objNoteTreeview.getSelectedTreenodes();
    	if (objSelectedTreeNodes.size() == 0) {
    		guidSelectedTreeNode =  null;
    	} else {
    		guidSelectedTreeNode = objSelectedTreeNodes.get(0).guidTreeNode;
    	}
    	
    	if (resultCode == Activity.RESULT_OK)
    	{
    		// get information from Intent
			Bundle objBundle = data.getExtras();
			
			String strResult    = objBundle.getString(DESCRIPTION);
			String resourcePath = objBundle.getString(RESOURCE_PATH);
			String strWebPageURL = objBundle.getString(TREENODE_URL);
			int    resourceId   = objBundle.getInt(RESOURCE_ID);
			String strAnnotationData = objBundle.getString(ANNOTATION_DATA_GSON);
			boolean boolUseAnnotatedImage = objBundle.getBoolean(ActivityNoteStartup.USE_ANNOTATED_IMAGE);
			boolean boolIsDirty = objBundle.getBoolean(ActivityNoteStartup.ISDIRTY);
			clsAnnotationData objAnnotationData = clsUtils.DeSerializeFromString(strAnnotationData, clsAnnotationData.class);
			
			List<clsListItem> objListItems;
			ListView          objListView;
			clsTreeNode       objNewTreeNode;
			clsTreeNode       objParentTreeNode;
			clsNoteItemStatus objNoteItemStatus = new clsNoteItemStatus();
	
												
    		switch(requestCode)
    		{
    			case GET_DESCRIPTION_ADD_SAME_LEVEL:
    	    		
    	    		// Toast.makeText(getApplicationContext(),strResult,Toast.LENGTH_SHORT).show();
    	    		// Add new node at the same level
    				if (guidSelectedTreeNode == null) { // e.g. when note is empty
    					objParentTreeNode = null;
    				} else {
        	    		clsTreeNode objPeerTreeNode = objNoteTreeview.getTreeNodeFromUuid(guidSelectedTreeNode);     	    		
        	    		objParentTreeNode = objNoteTreeview.getParentTreeNode(objPeerTreeNode);
    				}
 	    		
    	    		String strOwnerUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
    	    		if (!objNoteTreeview.boolIsUserCommenting(objGroupMembers.objMembersRepository.getStrRegisteredUserUuid())) {
    	    			
    	    			objNewTreeNode = objNoteTreeview.new clsTreeNode(strResult, clsTreeview.enumItemType.FOLDER_EMPTY, false,
								resourcePath, resourceId, strWebPageURL, strOwnerUserUuid, strOwnerUserUuid);
    				}
    				else {
    					objNewTreeNode = objNoteTreeview.new clsTreeNode(strResult, clsTreeview.enumItemType.FOLDER_EMPTY, false,
								resourcePath, resourceId, strWebPageURL, strOwnerUserUuid, strOwnerUserUuid);
    				}
    	    		UpdateAnnotationDataWithNewNodeUuid(objAnnotationData,objNewTreeNode.guidTreeNode.toString());
    	    		objNewTreeNode.annotation = objAnnotationData;
    	    		objNewTreeNode.setBoolUseAnnotatedImage(boolUseAnnotatedImage);
    	    		objNewTreeNode.setIsDirty(true);

    	    		if (objParentTreeNode == null) {
    	    			// Toplevel item selected
    	    			objNoteTreeview.getRepository().objRootNodes.add(objNewTreeNode);
    	    		} else {
    	    			// Child level selected
    	    			objParentTreeNode.objChildren.add(objNewTreeNode);
    	    		}

    	    		objNoteTreeview.RecursiveSetParentChecked(objNewTreeNode,false);
					objNoteTreeview.setSingleSelectedTreenode(objNewTreeNode);
					objNoteTreeview.uuidLastAddedTreeNode = objNewTreeNode.guidTreeNode;
					RefreshListView();
    		        break;
    		        
    			case GET_DESCRIPTION_ADD_NEXT_LEVEL:
    				 	
    	    		// Toast.makeText(getApplicationContext(),strResult,Toast.LENGTH_SHORT).show();
    	    		// Add new node at the same level
    	    		objParentTreeNode = objNoteTreeview.getTreeNodeFromUuid(guidSelectedTreeNode);

    	    		strOwnerUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
    	    		if (!objNoteTreeview.boolIsUserCommenting(objGroupMembers.objMembersRepository.getStrRegisteredUserUuid())) {
    	    			objNewTreeNode = objNoteTreeview.new clsTreeNode(strResult, clsTreeview.enumItemType.FOLDER_EMPTY, false,
        	    				resourcePath, resourceId, strWebPageURL, strOwnerUserUuid, strOwnerUserUuid);
    				}
    				else {
    					objNewTreeNode = objNoteTreeview.new clsTreeNode(strResult, clsTreeview.enumItemType.FOLDER_EMPTY, false,
        	    				resourcePath, resourceId, strWebPageURL, strOwnerUserUuid, strOwnerUserUuid);
    				}
    	    		UpdateAnnotationDataWithNewNodeUuid(objAnnotationData,objNewTreeNode.guidTreeNode.toString());
    	    		objNewTreeNode.annotation = objAnnotationData;
    	    		objNewTreeNode.setBoolUseAnnotatedImage(boolUseAnnotatedImage);
    	    		objNewTreeNode.setIsDirty(true);
    	    		
    	    		objNewTreeNode.setStrOwnerUserUuid(objGroupMembers.GetRegisteredUser().strUserUuid);
    	    		if (objParentTreeNode == null) {
    	    			objNoteTreeview.getRepository().objRootNodes.add(objNewTreeNode);
    	    		} else {
    	    			objParentTreeNode.objChildren.add(objNewTreeNode);
    	        		objParentTreeNode.enumItemType = clsTreeview.enumItemType.FOLDER_EXPANDED;
    	    		}
    	    		objNoteTreeview.RecursiveSetParentChecked(objNewTreeNode,false);
					objNoteTreeview.setSingleSelectedTreenode(objNewTreeNode);
					objNoteTreeview.uuidLastAddedTreeNode = objNewTreeNode.guidTreeNode;
					RefreshListView();
    				break;
    				
    			case EDIT_DESCRIPTION:
    				 		
    	    		// Toast.makeText(getApplicationContext(),strResult,Toast.LENGTH_SHORT).show();
    	    		// Add new node at the same level
    				objBundle = data.getExtras();    		
    				String strUuid    = objBundle.getString(TREENODE_UID);
    	    		clsTreeNode objEditedTreeNode = objNoteTreeview.getTreeNodeFromUuid(UUID.fromString(strUuid));
    	    		
    	    		objEditedTreeNode.setName(strResult);
    	    		objEditedTreeNode.resourceId = resourceId;
    	    		objEditedTreeNode.resourcePath = resourcePath;
    	    		objEditedTreeNode.annotation = objAnnotationData;
    	    		objEditedTreeNode.setIsDirty(boolIsDirty);
    	    		objEditedTreeNode.setBoolUseAnnotatedImage(boolUseAnnotatedImage);
    	    		strOwnerUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
    	    		objEditedTreeNode.setStrLastChangedByUserUuid(strOwnerUserUuid);
    	    		objEditedTreeNode.setStrLastChangedDateTimeStamp(clsUtils.GetStrCurrentDateTime());
    	    				
    	    		RefreshListView();
    				break;
    			
    			case ActivityExplorerStartup.SHARE_CHOOSE_GROUP_MEMBERS:
    				// Save return values to that specific note's repository share data
    				objBundle = data.getExtras();
    				String strChooseResultGson = objBundle.getString(ActivityGroupMembers.CHOOSE_MEMBERS_RESULT_GSON);
    				clsIntentMessaging.clsChosenMembers objResults = clsUtils.DeSerializeFromString(strChooseResultGson,
    						clsIntentMessaging.clsChosenMembers.class);

    				// Inform server about the selection
    				URL urlFeed;
    				try {
    					urlFeed = new URL(objMessaging.GetServerUrl()
    							+ getResources().getString(R.string.url_set_note_sharers));
    				} catch (MalformedURLException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    					return;
    				} catch (NotFoundException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    					return;
    				}
    				clsSetNoteSharedUsersCommand objCommand = new clsSetNoteSharedUsersCommand();
    				objCommand.strClientUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
    				objCommand.strNoteUuid = objResults.strNoteUuid;
    				objCommand.objSharedUsers = objResults.strUserUuids;
    				final boolean boolIsNoteShared = (objResults.strUserUuids.size() == 0) ? false: true;
    				clsSetNoteSharedUsersResponse objResponse = new clsSetNoteSharedUsersResponse();
    				clsSetNoteSharedUsersAsyncTask objSetNoteSharedUsersAsyncTask = new clsSetNoteSharedUsersAsyncTask(this, urlFeed, objCommand, objResponse);
    				objSetNoteSharedUsersAsyncTask.SetOnResponseListener(new OnSetNoteSharedUsersResponseListener() {
    					
    					@Override
    					public void onResponse(clsSetNoteSharedUsersResponse objResponse) {
    						if (objResponse.intErrorCode == clsSetNoteSharedUsersResponse.ERROR_NETWORK ) {
    							clsUtils.MessageBox(objActivity, objResponse.strErrorMessage, false);
    						} else {
    							if (boolIsNoteShared) {
    								objNoteTreeview.getRepository().boolIsShared = true;
    							} else {
    								objNoteTreeview.getRepository().boolIsShared = false;
    							}
    						}
    						RefreshListView();
    					}
    				});
    				objSetNoteSharedUsersAsyncTask.execute(null,null,null);			
    				break;				
    			case ANNOTATE_IMAGE:
    				String annotation = objBundle.getString(clsAnnotationData.DATA);
    				// retrieve annotation data from Intent
    				clsAnnotationData retval = clsUtils.DeSerializeFromString(annotation, clsAnnotationData.class); 
    				// get actual image tree node and assign annotation data
    				clsTreeNode parent = objNoteTreeview.getTreeNodeFromUuid(UUID.fromString(retval.strNodeUuid));
    				parent.annotation = retval;

    				// check whether the tree node needs to be saved because of the annotation
    				boolean changed = objBundle.getBoolean(clsAnnotationData.CHANGED);
    				if(changed)
    					parent.setIsDirty(true);

    				SaveTemp();
    				break; 
    		}
    	}
    	else {
    		switch(requestCode)
    		{
    		case EDIT_SETTINGS:
				SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
				boolean chkEnableAsCheckList = mySharedPreferences.getBoolean("checkbox_enable_checklist", false);
				ActivityNoteStartup.objNoteTreeview.SetAsCheckList(chkEnableAsCheckList);		
				RefreshListView();
				break;
				

				
    		}
    	}
    }
    
    
    
    private void UpdateAnnotationDataWithNewNodeUuid(clsAnnotationData objAnnotationData, String strNewUuid) {
		// TODO Auto-generated method stub
    	if (objAnnotationData != null) {
        	objAnnotationData.strNodeUuid = strNewUuid;
        	objAnnotationData.strLocalImage = objAnnotationData.strLocalImage.replace("temp_uuid", strNewUuid);
    	}
	}







	private void getOverflowMenu() {
    	// Hack to get to overflow action to display if there is a physical menu key
        try {
           ViewConfiguration config = ViewConfiguration.get(this);
           Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
           if(menuKeyField != null) {
               menuKeyField.setAccessible(true);
               menuKeyField.setBoolean(config, false);
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
 
    
    
    
    public static void showKeyboard(Activity activity) {
        if (activity != null) {
            activity.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            activity.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }
    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
		    throws IOException {
    	
    	if (sourceLocation.getCanonicalPath().equals(targetLocation.getCanonicalPath())) return;

		if (sourceLocation.isDirectory()) {
		    if (!targetLocation.exists()) {
		        targetLocation.mkdir();
		    }

		    String[] children = sourceLocation.list();
		    for (int i = 0; i < sourceLocation.listFiles().length; i++) {

		        copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
		                new File(targetLocation, children[i]));
		    }
		} else {

		    InputStream in = new FileInputStream(sourceLocation);

		    OutputStream out = new FileOutputStream(targetLocation);

		    // Copy the bits from instream to outstream
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    in.close();
		    out.close();
		}

	}
	
	public static void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}
	
	
	public void StartNoteItemEditIntent(clsTreeNode objTreeNode) {
		// Shell out to edit activity
		Intent intent = new Intent(objActivity, ActivityNoteAddNew.class);
		intent.putExtra(ActivityNoteStartup.DESCRIPTION, objTreeNode.getName());
		intent.putExtra(ActivityNoteStartup.RESOURCE_ID, objTreeNode.resourceId);
		intent.putExtra(ActivityNoteStartup.RESOURCE_PATH, objTreeNode.resourcePath);
		intent.putExtra(ActivityNoteStartup.TREENODE_UID, objTreeNode.guidTreeNode.toString());
		intent.putExtra(ActivityNoteStartup.TREENODE_OWNERNAME, objGroupMembers
				.GetUserNameFomUuid(objTreeNode.getStrOwnerUserUuid()));
		clsNoteItemStatus objNoteItemStatus = new clsNoteItemStatus();
		DetermineNoteItemStatus(objTreeNode, objNoteItemStatus,
				objGroupMembers, ActivityNoteStartup.objNoteTreeview);
		intent.putExtra(ActivityNoteStartup.READONLY, !objNoteItemStatus.boolSelectedNoteItemBelongsToUser);

		// Send description of node's 
		String strParentDescription;
		clsTreeNode objParentTreeNode = objNoteTreeview.getParentTreeNode(objTreeNode);
		
		strParentDescription = (objParentTreeNode == null)?(ActivityNoteStartup.objNoteTreeview.getRepository().getName())
														  :(objParentTreeNode.getName());
		
		intent.putExtra(ActivityNoteStartup.TREENODE_PARENTNAME, strParentDescription); 
		
		String strAnnotationDataGson = clsUtils.SerializeToString(objTreeNode.annotation);
		intent.putExtra(ActivityNoteStartup.ANNOTATION_DATA_GSON, strAnnotationDataGson);
		intent.putExtra(ActivityNoteStartup.USE_ANNOTATED_IMAGE, objTreeNode.getBoolUseAnnotatedImage());
		intent.putExtra(ActivityNoteStartup.ISDIRTY, false);

		startActivityForResult(intent, ActivityNoteStartup.EDIT_DESCRIPTION);
	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (objNoteTreeview.GetAllIsDirty() == false) {
			// No changes, so exit without saving
			if (boolIsShortcut) {
				// Flag there is a need to exit immediately to Android Home page
				Intent objIntent = getIntent();
				objIntent.putExtra(ActivityExplorerStartup.IS_SHORTCUT, true);
				setResult(RESULT_OK, objIntent);    	
		    	ActivityNoteStartup.this.finish();
		    	ActivityNoteStartup.super.onBackPressed();
		    	return;
			}
			else {
				// No need to save when going back to Explorer
				Intent objIntent = getIntent();
				objIntent.putExtra(ActivityExplorerStartup.IS_SHORTCUT, false);
				setResult(RESULT_OK, objIntent);    	
		    	ActivityNoteStartup.this.finish();
		    	ActivityNoteStartup.super.onBackPressed();
		    	return;
			}
		}
		// Query user if save or cancel
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Do you want to save or cancel the edits?");
    	builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {			
				// Save data first
				SaveFile();
				// Return to caller
				Intent objIntent = getIntent();
				String strImageLoadDatas = clsUtils.SerializeToString(objLocalImageLoadDatas);
				objIntent.putExtra(ActivityExplorerStartup.IMAGE_LOAD_DATAS, strImageLoadDatas);
				if (boolIsShortcut) {
					objIntent.putExtra(ActivityExplorerStartup.IS_SHORTCUT, true);
				} else {
					objIntent.putExtra(ActivityExplorerStartup.IS_SHORTCUT, false);
				}
				setResult(RESULT_OK, objIntent);    	
		    	ActivityNoteStartup.this.finish();
		    	ActivityNoteStartup.super.onBackPressed();
			}
		});
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				// Return to caller
				setResult(RESULT_OK, getIntent());    	
		    	ActivityNoteStartup.this.finish();
		    	ActivityNoteStartup.super.onBackPressed();
			}
		});
    	builder.show();
		
	}
	
	// -------------------------- Communications ----------------------------------
	
	// ---------- Synch -----------------------------------------------------------
	

	

	
	public static class ActivityNoteStartupSyncAsyncTask extends NoteSyncAsyncTask {

		static boolean boolDisplayToasts;
		clsTreeview objTreeview;
		
		public ActivityNoteStartupSyncAsyncTask(Activity objActivity, clsTreeview objTreeview, URL urlFeed, clsSyncNoteCommandMsg objSyncCommandMsg,
				clsMessaging objMessaging, boolean boolDisplayToasts, boolean boolDisplayProgress) {
			super(objActivity, urlFeed, objSyncCommandMsg, objMessaging, boolDisplayToasts, boolDisplayProgress);
			// TODO Auto-generated constructor stub
			ActivityNoteStartupSyncAsyncTask.boolDisplayToasts = boolDisplayToasts;
			this.objTreeview = objTreeview;
		}
		@Override
   	    protected void onPostExecute(clsSyncResult objResult){

	   	        super.onPostExecute(objResult);
	   	        // Do what needs to be done with the result
	   	        if (objResult.intErrorCode == clsSyncResult.ERROR_NONE) {
	   	        	String strMessage = "";
	   	        	for (int i = 0; i < objResult.intServerInstructions.size(); i++ ) {
	   	        	// Depending on server instructions
	   	   	   	        switch (objResult.intServerInstructions.get(i)) {
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_KEEP_ORIGINAL:
			   	   	   	   if (boolDisplayToasts) {
			   	   	   		   strMessage += "A new note has been created on server";
				 	        }
	   	   	   	        	break;
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_REPLACE_ORIGINAL:
	   	   	   	        	objNoteTreeview.getRepository().objRootNodes = objResult.objSyncRepositories.get(i).getRepositoryRootNodesCopy();
	   			   	   	    if (boolDisplayToasts) {
	   			   	   	    	strMessage += "This note has been replaced with an updated version";
	   			   	   	    }
	   			   	   	    objNoteTreeview.UpdateItemTypes();
	   	   	   	        	((ActivityNoteStartup) objActivity).RefreshListView();
	   	   	   	        	break;
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_CREATE_NEW_SHARED:
	   	   	   	        	break;
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_CREATE_NEW_PUBLISHED:     	
	   	   		        	break;
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_NO_MORE_NOTES:
	   	   	   	        	break;
	   	   	   	    	}
	   	        	}
	   	        	((ActivityNoteStartup)objActivity).SaveFile();
	   	        	clsUtils.MessageBox(objActivity, strMessage, true);
	   	        	clsUtils.ClearImageLoadDatas(objLocalImageLoadDatas);
	   	        	clsUtils.UpdateImageLoadDatasForDownloads(((ActivityNoteStartup)objActivity).objMessaging, ((ActivityNoteStartup)objActivity).objGroupMembers,
	   	        			objNoteTreeview, ActivityExplorerStartup.fileTreeNodesDir, objResult.objImageLoadDatas, objLocalImageLoadDatas);
	   	        	clsUtils.UpdateImageLoadDatasForUploads(((ActivityNoteStartup)objActivity).objMessaging, 
	   	        			objResult.objImageLoadDatas, objLocalImageLoadDatas);
	   	        		
	   	        } else {
	   	        	if (boolDisplayToasts) {
	   	        		clsUtils.MessageBox(objActivity, objResult.strErrorMessage, true);
	   	        	}

	   	        }
	   	        // Start background image syncing
				objImageUpDownloadAsyncTask = new clsImageUpDownloadAsyncTask((Activity) objActivity, ((ActivityNoteStartup)objActivity).objMessaging, 
						true, ActivityNoteStartup.objLocalImageLoadDatas, new OnImageUploadFinishedListener() {
							
							@Override
							public void imageUploadFinished(boolean success, String errorMessage) {
								
								clsUtils.IndicateToServiceIntentSyncIsCompleted(objActivity);
								
								if (!success) {
									clsUtils.MessageBox(objActivity, errorMessage, false);
									return;
								}
								// Once successfully downloaded, update the ResourceUrl in the relevant treenode
								clsUtils.UpdateTreeviewResourcePaths(objActivity, objTreeview, objLocalImageLoadDatas);		
								
								// Refresh
								((ActivityNoteStartup)objActivity).RefreshListView();
								
							}
						}, null);
				objImageUpDownloadAsyncTask.execute();

	   	    }
	}
        
    
    
}
