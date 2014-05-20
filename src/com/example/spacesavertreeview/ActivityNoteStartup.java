package com.example.spacesavertreeview;

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

import com.example.spacesavertreeview.clsTreeview.clsShareUser;
import com.example.spacesavertreeview.clsTreeview.clsSyncRepository;
import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.clsTreeview.enumCutCopyPasteState;
import com.example.spacesavertreeview.export.clsExportData;
import com.example.spacesavertreeview.export.clsExportToMail;
import com.example.spacesavertreeview.export.clsMainExport;
import com.example.spacesavertreeview.imageannotation.clsAnnotationData;
import com.example.spacesavertreeview.sharing.ActivityGroupMembers;
import com.example.spacesavertreeview.sharing.clsGroupMembers;
import com.example.spacesavertreeview.sharing.clsMessaging;
import com.example.spacesavertreeview.sharing.clsGroupMembers.clsUser;
import com.example.spacesavertreeview.sharing.clsMessaging.NoteSyncAsyncTask;
import com.example.spacesavertreeview.sharing.clsMessaging.clsImageLoadData;
import com.example.spacesavertreeview.sharing.clsMessaging.clsImageUpDownloadAsyncTask;
import com.example.spacesavertreeview.sharing.clsMessaging.clsSyncNoteCommandMsg;
import com.example.spacesavertreeview.sharing.clsMessaging.clsSyncResult;
import com.google.gson.reflect.TypeToken;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class ActivityNoteStartup extends ListActivity {

	// keys to share between activities via Intents
	public static final String DESCRIPTION   = "com.example.spacesavertreeview.description";
	public static final String RESOURCE_ID   = "com.example.spacesavertreeview.resource_id";
	public static final String RESOURCE_PATH = "com.example.spacesavertreeview.resource_path";
	public static final String TREENODE_URL = "com.example.spacesavertreeview.treenode_url";
	public static final String TREENODE_UID = "com.example.spacesavertreeview.treenode_uuid";
	public static final String TREENODE_OWNERNAME = "com.example.spacesavertreeview.treenode_ownername";
	public static final String ANNOTATION_DATA_GSON = "com.example.spacesavertreeview.annotation_data";
	public static final String READONLY = "com.example.spacesavertreeview.read_only";
	public static final String USE_ANNOTATED_IMAGE = "com.example.spacesavertreeview.use_annotated_image";
	public static final String ISDIRTY = "com.example.spacesavertreeview.is_dirty";

	private static final int GET_DESCRIPTION_ADD_SAME_LEVEL = 0;
	private static final int GET_DESCRIPTION_ADD_NEXT_LEVEL = 1;
	public static final int EDIT_DESCRIPTION = 2;
	private static final int EDIT_SETTINGS = 3;
	private static final int SHARE_REGISTER = 4;
	private static final int SHARE_MANAGE_GROUP_MEMBERS = 5;
	private static final int SHARE_CHOOSE_GROUP_MEMBERS = 6;
	private static final int ANNOTATE_IMAGE = 7;
	

	
	
	
	
	// Variables that need to be persisted
	 public static ArrayList<clsListItem> listItems = new ArrayList<clsListItem>();
	 public static clsNoteTreeview objNoteTreeview;
	 public clsGroupMembers objGroupMembers = new clsGroupMembers(this);
	 
	 private clsNoteListItemArrayAdapter objListItemAdapter;
	 private clsMessaging objMessaging = new clsMessaging(); 	 
	 private boolean boolIsShortcut;
	 boolean boolIsUserRegistered = false;
	 private boolean boolUserIsNoteOwner = false;
	 static Context objContext;
	 static ArrayList<clsImageLoadData> objImageLoadDatas;
	 
	 
	 // Temporarily locals
	 ImageView myPreviewImageView;
	 static clsImageUpDownloadAsyncTask objImageUpDownloadAsyncTask;
	 private clsExportToMail objExportToMail;
	 

	 
  
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	    
	        super.onCreate(savedInstanceState);
	        
	        objContext = this;
			 
			objGroupMembers.LoadFile();
			objMessaging.LoadFile(this);
			        
	        // Get Treeview listItems  
	        Bundle objBundle = getIntent().getExtras();    		
			String strUuid    = objBundle.getString(ActivityExplorerStartup.TREENODE_UID);
		
			boolIsShortcut = objBundle.getBoolean(ActivityExplorerStartup.IS_SHORTCUT);
			
			String strImageLoadDatas = objBundle.getString(ActivityExplorerStartup.IMAGE_LOAD_DATAS);
			java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsImageLoadData>>(){}.getType();
			objImageLoadDatas = clsUtils.DeSerializeFromString(strImageLoadDatas, collectionType);

			File objFile = clsUtils.BuildNoteFilename(ActivityExplorerStartup.fileTreeNodesDir, strUuid );
			objNoteTreeview = new clsNoteTreeview(objGroupMembers);
			if (objFile.exists()) {
				objNoteTreeview.DeserializeFromFile(objFile);
				objNoteTreeview.SetAllIsDirty(false);		
			} else {
				objNoteTreeview.getRepository().setStrOwnerUserUuid(objGroupMembers.objMembersRepository.getStrRegisteredUserUuid());
				objNoteTreeview.getRepository().uuidRepository = UUID.fromString(strUuid);
				objNoteTreeview.getRepository().setName(objBundle.getString(ActivityExplorerStartup.TREENODE_NAME));
			}
			
			// Determine if user is registered
			GetUserRegistrationStatus();
			

	        listItems = objNoteTreeview.getListItems();
	                
	        this.setTitle("Edit: " + objNoteTreeview.getRepository().getName());
	        
	        setContentView(R.layout.activity_note_startup);
	        
	        //---List View---
	        int resID = R.layout.note_list_item;
			int intTabWidthInDp = clsUtils.GetDefaultTabWidthInDp(this);			
			int intTabWidthInPx = clsUtils.dpToPx(this, intTabWidthInDp);     
	        objListItemAdapter = new clsNoteListItemArrayAdapter(this, resID, listItems, objNoteTreeview, intTabWidthInPx);
	        setListAdapter(objListItemAdapter);
	        
	        // Actionbar
	        ActionBar actionBar = getActionBar();
	        actionBar.show();
	        actionBar.setDisplayHomeAsUpEnabled(true);
	        getOverflowMenu(); 
	        
	        
	        clsUtils.CustomLog("ActivityNoteStartup onCreate");
	        if (savedInstanceState == null) {
	        	SaveFile();
	        } else {
	        	LoadFile();
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
    	clsUtils.CustomLog("ActivityNoteStartup onStart LoadFile");
    	LoadFile();
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
    	clsUtils.CustomLog("ActivityNoteStartup onStop SaveFile");
    	SaveFile();
    	super.onStop();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	clsUtils.CustomLog("ActivityNoteStartup onPause SaveFile");
    	SaveFile();
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	clsUtils.CustomLog("ActivityNoteStartup onDestroy SaveFile");
    	SaveFile();
    	super.onDestroy();
    }
    
    @Override
    protected void onRestart() {
    	clsUtils.CustomLog("ActivityNoteStartup onRestart LoadFile");
    	LoadFile();
    	super.onRestart();
    }
    
    @Override
    protected void onResume() {
    	clsUtils.CustomLog("ActivityNoteStartup onResume LoadFile");
    	LoadFile();
    	super.onResume();
    }
    
    private void SaveFile() {
    	clsUtils.CustomLog("SaveFile");
		objNoteTreeview.getRepository().SerializeToFile(clsUtils.BuildTempNoteFilename(ActivityExplorerStartup.fileTreeNodesDir));
		objGroupMembers.SaveFile();
		objMessaging.SaveFile(this);
		SharedPreferences sharedPref = this.getSharedPreferences("ActivityNoteStartup",Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPref.edit();
    	editor.putBoolean("boolIsShortcut",boolIsShortcut);
    	String strImageLoadDatas = clsUtils.SerializeToString(objImageLoadDatas);
    	editor.putString("objImageLoadDatas", strImageLoadDatas);
    	editor.commit();
    	sharedPref = null;
	}
 
	private void LoadFile() {
		clsUtils.CustomLog("LoadFile");
		objNoteTreeview = new clsNoteTreeview(objGroupMembers);
		objNoteTreeview.UpdateEnvironment(clsExplorerTreeview.enumCutCopyPasteState.INACTIVE, new ArrayList<clsTreeNode>() ); // Must be persisted at a later stage
		objNoteTreeview.DeserializeFromFile(clsUtils.BuildTempNoteFilename(ActivityExplorerStartup.fileTreeNodesDir));
		objGroupMembers.LoadFile();
		objGroupMembers.UpdateEnvironment(this);
    	ArrayList<clsListItem> objListItems = objNoteTreeview.getListItems();
    	objListItemAdapter.UpdateEnvironment(objNoteTreeview);
    	objListItemAdapter.clear(); objListItemAdapter.addAll(objListItems);
   	    objMessaging.LoadFile(this);
   	    objContext = this;
   	    SharedPreferences sharedPref = this.getSharedPreferences("ActivityNoteStartup",Context.MODE_PRIVATE);
   	    boolIsShortcut = sharedPref.getBoolean("boolIsShortcut",false);
   	    String strImageLoadDatas = sharedPref.getString("objImageLoadDatas", "");
   	    java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsImageLoadData>>(){}.getType();
   	    objImageLoadDatas = clsUtils.DeSerializeFromString(strImageLoadDatas, collectionType);
   	    GetUserRegistrationStatus(); // Update boolIsUserRegistered and boolUserIsNoteOwner
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
        	 intent = new Intent(this, ActivityNoteAddNew.class);
        	 intent.putExtra(DESCRIPTION,   objEditTreeNode.getName());
        	 intent.putExtra(RESOURCE_ID,   objEditTreeNode.resourceId);
        	 intent.putExtra(RESOURCE_PATH, objEditTreeNode.resourcePath);
        	 intent.putExtra(TREENODE_UID, objEditTreeNode.guidTreeNode.toString());
        	 intent.putExtra(TREENODE_OWNERNAME, objGroupMembers.GetUserNameFomUuid(objEditTreeNode.getStrOwnerUserUuid()));
             DetermineNoteItemStatus(objSelectedTreeNodes.get(0),objNoteItemStatus, objGroupMembers,objNoteTreeview);
        	 intent.putExtra(READONLY, !objNoteItemStatus.boolSelectedNoteItemBelongsToUser); 
        	 intent.putExtra(USE_ANNOTATED_IMAGE, !objEditTreeNode.getBoolUseAnnotatedImage()); 
        	 startActivityForResult(intent, EDIT_DESCRIPTION);
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
        	 objExportToMail = new clsExportToMail(this, objNoteTreeview, objMessaging);
        	 objExportToMail.Execute();
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

         case R.id.actionShareSync: 
        	 clsUser objRegisteredUser = objGroupMembers.GetRegisteredUser();
        	 if (objRegisteredUser.strUserName.equals(getResources().getString(R.string.unregistered_username))) {
        		 // Not registered, cannot sync
        		 Toast.makeText(this, "You need to register first before you can sync",Toast.LENGTH_SHORT).show();
        		 return true;
        	 }
        	 SaveFile();
        	 URL urlFeed;
        	 try {
				urlFeed = new URL(objMessaging.GetServerUrl(objNoteTreeview) + getResources().getString(R.string.url_note_sync));
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
        	 clsSyncRepository objSyncRepository = objNoteTreeview.getRepository().getCopy();
        	 objSyncCommandMsg.objSyncRepositories.add(objSyncRepository);
        	 objSyncCommandMsg.strClientUserUuid = objGroupMembers.objMembersRepository.getStrRegisteredUserUuid();
        	 objSyncCommandMsg.boolIsMergeNeeded = true;
        	 ActivityNoteStartupSyncAsyncTask objSyncAsyncTask = new ActivityNoteStartupSyncAsyncTask(this, urlFeed, objSyncCommandMsg,objMessaging, true);
        	 objSyncAsyncTask.execute("");
        	 return true;
         case R.id.actionShareRestoreFromServer: 
        	 objRegisteredUser = objGroupMembers.GetRegisteredUser();
        	 if (objRegisteredUser.strUserName.equals(getResources().getString(R.string.unregistered_username))) {
        		 // Not registered, cannot sync
        		 Toast.makeText(this, "You need to register first before you can sync",Toast.LENGTH_SHORT).show();
        		 return true;
        	 }
        	 try {
				urlFeed = new URL(objMessaging.GetServerUrl(objNoteTreeview) + getResources().getString(R.string.url_note_sync));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			}
        	 objSyncCommandMsg = objMessaging.new clsSyncNoteCommandMsg();
        	 objSyncRepository = objNoteTreeview.getRepository().getCopy();
        	 objSyncCommandMsg.objSyncRepositories.add(objSyncRepository);
        	 objSyncCommandMsg.strClientUserUuid = objGroupMembers.objMembersRepository.getStrRegisteredUserUuid();
        	 objSyncCommandMsg.boolIsMergeNeeded = false;
        	 objSyncAsyncTask = new ActivityNoteStartupSyncAsyncTask(this, urlFeed, objSyncCommandMsg,objMessaging, true);
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
         default:
             return super.onOptionsItemSelected(item);
    	 }
    }
	
	public class MyTextOnClickListenerBackup2 implements View.OnClickListener{
		@Override
		public void onClick(View v) {
		 ArrayList<clsTreeNode> objSelectedTreeNodes = objNoteTreeview.getSelectedTreenodes();
			// Only applicable if a row is selected
       	 if (objNoteTreeview.getRepository().objRootNodes.size() == 0) {
       		 clsUtils.MessageBox(objContext, "Please add some items first.", false);
       		 return;
       	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
       		clsUtils.MessageBox(objContext, "Please select an item first.", false);
       		 return;
       	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
       		clsUtils.MessageBox(objContext, "Please select only one item at a time", false);
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
       		 clsUtils.MessageBox(objContext, "Please add some items first.", false);
       		 return;
       	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
       		clsUtils.MessageBox(objContext, "Please select an item first.", false);
       		 return;
       	 } else if (objNoteTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
       		clsUtils.MessageBox(objContext, "Please select only one item at a time", false);
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
		 SaveFile();
		 List<clsListItem> objListItems = objNoteTreeview.getListItems(); 
		 objListItemAdapter.clear(); objListItemAdapter.addAll(objListItems);
		 final ListView objListView = getListView();  
		 objListView.invalidateViews();
		 objListItemAdapter.notifyDataSetChanged();	 
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
    			
    			case SHARE_REGISTER:
    				objBundle = data.getExtras();    		
    				objGroupMembers.objMembersRepository.setStrRegisteredUserName(objBundle.getString(ActivityRegister.USERNAME));
    				objGroupMembers.objMembersRepository.setStrRegisteredUserUuid(objBundle.getString(ActivityRegister.USERUUID));
    				objGroupMembers.SaveFile();
    				Toast.makeText(getApplicationContext(),"Registration successfull",Toast.LENGTH_SHORT).show();
    				RefreshListView();
    				break;
    			case SHARE_CHOOSE_GROUP_MEMBERS:
        			// Save return values to that specific note's repository share data
    				objBundle = data.getExtras();    		
    				String strChooseResultGson    = objBundle.getString(ActivityGroupMembers.CHOOSE_MEMBERS_RESULT_GSON);
    				clsIntentMessaging.clsChosenMembers objResults = 
    						clsUtils.DeSerializeFromString(strChooseResultGson, clsIntentMessaging.clsChosenMembers.class);
    				objNoteTreeview.getRepository().setObjSharedUsers(new ArrayList<clsShareUser>()); // Create empty list
    				for (String strUserUuid: objResults.strUserUuids) {
    					clsShareUser clsShareUser = objNoteTreeview.new clsShareUser(strUserUuid,
    							objGroupMembers.GetRegisteredUser().strUserUuid,clsUtils.GetStrCurrentDateTime());
    					objNoteTreeview.AddShareUser(clsShareUser);
    				}
    				RefreshListView();
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

    				SaveFile();
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
				objNoteTreeview.getRepository().SerializeToFile(clsUtils.BuildNoteFilename(ActivityExplorerStartup.fileTreeNodesDir, objNoteTreeview.getRepository().uuidRepository.toString()));
				// Return to caller
				Intent objIntent = getIntent();
				String strImageLoadDatas = clsUtils.SerializeToString(objImageLoadDatas);
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
		
		public ActivityNoteStartupSyncAsyncTask(Activity objActivity, URL urlFeed, clsSyncNoteCommandMsg objSyncCommandMsg,
				clsMessaging objMessaging, boolean boolDisplayToasts) {
			super(objActivity, urlFeed, objSyncCommandMsg, objMessaging, boolDisplayToasts);
			// TODO Auto-generated constructor stub
			ActivityNoteStartupSyncAsyncTask.boolDisplayToasts = boolDisplayToasts;
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
	   	   	   	        	((ActivityNoteStartup) objContext).RefreshListView();
	   	   	   	        	break;
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_CREATE_NEW_SHARED:
	   	   	   	        	break;
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_CREATE_NEW_PUBLISHED:     	
	   	   		        	break;
	   	   	   	        case clsMessaging.SERVER_INSTRUCT_NO_MORE_NOTES:
	   	   	   	        	break;
	   	   	   	    	}
	   	        	}
	   	        	clsUtils.MessageBox(objContext, strMessage, true);
	   	        	clsUtils.UpdateImageLoadDatasForDownloads(((ActivityNoteStartup)objContext).objMessaging,
	   	        			objNoteTreeview, ActivityExplorerStartup.fileTreeNodesDir, objImageLoadDatas);
	   	        	clsUtils.UpdateImageLoadDatasForUploads(((ActivityNoteStartup)objContext).objMessaging, 
	   	        			objResult.objImageLoadDatas, objImageLoadDatas);
	   	        		
	   	        } else {
	   	        	if (boolDisplayToasts) {
	   	        		clsUtils.MessageBox(objContext, objResult.strErrorMessage, true);
	   	        	}

	   	        }
	   	        // Start background image syncing
				objImageUpDownloadAsyncTask = new clsImageUpDownloadAsyncTask((Activity) objContext, ((ActivityNoteStartup)objContext).objMessaging, 
						true, ActivityNoteStartup.objImageLoadDatas);
				objImageUpDownloadAsyncTask.execute();
	   	    }
	}
        
    
    
}
