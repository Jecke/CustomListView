package com.example.spacesavertreeview.sharing;

import java.util.ArrayList;
import java.util.UUID;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsIntentMessaging;
import com.example.spacesavertreeview.clsTreeview;
import com.example.spacesavertreeview.clsTreeview.clsRepository;
import com.example.spacesavertreeview.clsTreeview.enumItemType;
import com.example.spacesavertreeview.clsUtils;
import com.example.spacesavertreeview.clsListItem;
import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.sharing.ActivityAllMembers.clsAllMembersListViewState;
import com.example.spacesavertreeview.sharing.clsGroupMembersTreeview.clsGroupMemberFolderTreeNode;
import com.example.spacesavertreeview.sharing.clsGroupMembersTreeview.clsGroupMemberMemberTreeNode;
import com.example.spacesavertreeview.sharing.clsGroupMembersTreeview.clsGroupMembersTreeNode;
import com.google.gson.reflect.TypeToken;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;

public class ActivityGroupMembers extends ListActivity {

	public static final String ACTION = "com.example.spacesavertreeview.ActivityExplorerStartup.action";
	public static final String ACTION_MANAGE_GROUPS = "com.example.spacesavertreeview.ActivityExplorerStartup.action_manage_groups";
	public static final String ACTION_CHOOSE_MEMBERS = "com.example.spacesavertreeview.ActivityExplorerStartup.action_choose_members";
	public static final String CHOOSE_MEMBERS_RESULT_GSON = "com.example.spacesavertreeview.ActivityExplorerStartup.action_choose_members_result";
	public static final String SHARE_SHARED_USERS = "com.example.spacesavertreeview.ActivityExplorerStartup.action_share_shared_users";
	public static final String WEBSERVER_URL = "com.example.spacesavertreeview.WEBSERVER_URL";
	
	
	private static final int GET_MEMBERS_FROM_ALL = 0;
	private static final int GET_USER_FOR_GROUPS = 1;
	
	
	

    public String strCmnd = "";
    
    clsGroupMembersTreeview objGroupMembersTreeview;
    clsGroupMembersArrayAdapter objGroupMembersArrayAdapter;
    ArrayList<clsListItem> objListItems = new ArrayList<clsListItem>();
    public clsGroupMembers objGroupMembers = new clsGroupMembers(this);
    boolean boolInhibitSearchEvent = false;
    clsIntentMessaging objIntentMessaging = new clsIntentMessaging();
    
    static String strWebserverUrl;
    static String strNoteUuid;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        
		

        
     // Get Treeview listItems
 		objGroupMembers.LoadFile();
 		objGroupMembersTreeview = new clsGroupMembersTreeview(this, objGroupMembers); // Empty one
 		objGroupMembers.BuildTreeviewFromGroupMemberData(objGroupMembersTreeview);
 		objListItems = objGroupMembersTreeview.getListItems();
        
        
        // Get the intent and setup for the requested action
        Bundle objBundle = getIntent().getExtras();    		
        strCmnd    = objBundle.getString(ACTION);
        strWebserverUrl  = objBundle.getString(ActivityGetUserForGroups.WEBSERVER_URL);

        objGroupMembersTreeview.getRepository().boolHiddenSelectionIsActive = false;
        objGroupMembersTreeview.getRepository().boolIsCheckedItemsMadeHidden = false;
        objGroupMembersTreeview.getRepository().boolIsHiddenActive = false;
		if (strCmnd.equals(ACTION_MANAGE_GROUPS)) {
			objGroupMembersTreeview.SetAsCheckList(false);
		} else if (strCmnd.equals(ACTION_CHOOSE_MEMBERS)) {
			objGroupMembersTreeview.SetAsCheckList(true);
			String strChosenMembersGson = objBundle.getString(SHARE_SHARED_USERS);
			clsIntentMessaging.clsChosenMembers objChosenMembers = clsUtils.DeSerializeFromString(strChosenMembersGson, clsIntentMessaging.clsChosenMembers.class);
			objGroupMembersTreeview.CheckAllNodesBasedOnSharedMembers(objChosenMembers);
			strNoteUuid = objChosenMembers.strNoteUuid;
		}
		objGroupMembersTreeview.SetAllIsDirty(false);
        // Show the Up button in the action bar.
        setupActionBar();
        
        objGroupMembersArrayAdapter = new clsGroupMembersArrayAdapter(this, R.layout.group_members_list_item, objListItems,objGroupMembersTreeview);
        
        setListAdapter(objGroupMembersArrayAdapter);
        
        if (savedInstanceState == null) {
        	SaveFile();
        } else {
        	LoadFile();
        }
        
        handleIntent(getIntent());
        
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
        getMenuInflater().inflate(R.menu.activity_group_members, menu);
        
        // Depending on selected item, show menu item
        ArrayList<clsTreeNode> objSelectedTreeNodes = objGroupMembersTreeview.getSelectedTreenodes();
        
        MenuItem objMenuAddAtSameLevel = menu.findItem(R.id.actionAddAtSameLevel);
        MenuItem objMenuAddAtNextLevel = menu.findItem(R.id.actionAddAtNextLevel);
        MenuItem objMenuAddAtSameLevelFolder = menu.findItem(R.id.actionAddAtSameLevelFolder);
        MenuItem objMenuAddAtSameLevelMember = menu.findItem(R.id.actionAddAtSameLevelMember);
        MenuItem objMenuAddAtNextLevelFolder = menu.findItem(R.id.actionAddAtNextLevelFolder);
        MenuItem objMenuAddAtNextLevelMember = menu.findItem(R.id.actionAddAtNextLevelMember);
        MenuItem objMenuEdit = menu.findItem(R.id.actionEdit);
        MenuItem objMenuDelete = menu.findItem(R.id.actionDelete);
        objMenuEdit.setVisible(false);
        objMenuDelete.setVisible(false);
        if (objSelectedTreeNodes.size() != 1) {
        	// All invisible if multiselection
        	objMenuAddAtSameLevel.setVisible(false);
        	objMenuAddAtNextLevel.setVisible(false);
        	objMenuAddAtSameLevelFolder.setVisible(false); 
            objMenuAddAtSameLevelMember.setVisible(false); 
            objMenuAddAtNextLevelFolder.setVisible(false);
            objMenuAddAtNextLevelMember .setVisible(false);
            // Can still delete
            objMenuDelete.setVisible(true);
        } else {
        	// Selective, based on type of item
        	clsTreeNode objTreeNode = objSelectedTreeNodes.get(0);
        	
        	if (objTreeNode.enumItemType != enumItemType.OTHER) {
        		// Folder type, so can add folders at same or next level, members only at next level
        		objMenuAddAtSameLevel.setVisible(true);
            	objMenuAddAtNextLevel.setVisible(true);
        		objMenuAddAtSameLevelFolder.setVisible(true); 
                objMenuAddAtSameLevelMember.setVisible(false); 
                objMenuAddAtNextLevelFolder.setVisible(true);
                objMenuAddAtNextLevelMember .setVisible(true);
                // Can edit and delete
                objMenuEdit.setVisible(true);
                clsGroupMemberFolderTreeNode objParentTreeNode = (clsGroupMemberFolderTreeNode) objGroupMembersTreeview.getParentTreeNode(objTreeNode);
                if (objParentTreeNode != null) {
                	// If not in AllUsers section
                	objMenuDelete.setVisible(true);
                } 
                
        	} else {
        		// Member type, can only add members and these only at same level
        		objMenuAddAtSameLevel.setVisible(true);
            	objMenuAddAtNextLevel.setVisible(false);
        		objMenuAddAtSameLevelFolder.setVisible(false); 
                objMenuAddAtSameLevelMember.setVisible(true); 
                objMenuAddAtNextLevelFolder.setVisible(false);
                objMenuAddAtNextLevelMember .setVisible(false);
                clsGroupMemberFolderTreeNode objParentTreeNode = (clsGroupMemberFolderTreeNode) objGroupMembersTreeview.getParentTreeNode(objTreeNode);
                if (objParentTreeNode.IsAllUserFolder() == false) {
                    objMenuDelete.setVisible(true);
                }
        	}    		
        }
        
        // Enable search if in the right state
        if (strCmnd.equals(ACTION_MANAGE_GROUPS)) {
        	menu.findItem(R.id.actionSearchNewMember).setVisible(true);
        } else {
        	menu.findItem(R.id.actionSearchNewMember).setVisible(false);
        }
                
        handleIntent(getIntent());
        
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	 Intent intent;
		 AlertDialog.Builder builder;
		 AlertDialog dialog;
		 int intOrder;
		 boolean boolIsBusyWithCutOperation;
    	 boolean boolPasteAtSameLevel;
    	 
   	 
    	 UUID guidSelectedTreeNode;
     	 ArrayList<clsTreeNode> objSelectedTreeNodes = objGroupMembersTreeview.getSelectedTreenodes();
     	 if (objSelectedTreeNodes.size() == 0) {
     		guidSelectedTreeNode =  null;
     	 } else {
     		guidSelectedTreeNode = objSelectedTreeNodes.get(0).guidTreeNode;
     	 }
     	 
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
            case R.id.actionSearchNewMember:
            	boolInhibitSearchEvent = false;
            	onSearchRequested();
            	return true;
             case R.id.actionAddAtSameLevelFolder:
            	 if (objGroupMembersTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
            		 Toast.makeText(this,"Please select only one item at a time",Toast.LENGTH_SHORT).show();
            		 return false;
            	 }
            	 AddEditFolder("",true, true, guidSelectedTreeNode);       	 
                 return true; 
             case R.id.actionAddAtNextLevelFolder:
            	 if (objGroupMembersTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
            		 Toast.makeText(this,"Please select only one item at a time",Toast.LENGTH_SHORT).show();
            		 return false;
            	 }
            	 AddEditFolder("",true,false, guidSelectedTreeNode);       	 
                 return true;
             case R.id.actionAddAtSameLevelMember:
            	 if (objGroupMembersTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
            		 Toast.makeText(this,"Please select only one item at a time",Toast.LENGTH_SHORT).show();
            		 return false;
            	 }
            	 intent = new Intent(this, ActivityAllMembers.class);
            	 intent.putExtra(ActivityAllMembers.ACTION, ActivityAllMembers.ACTION_CHOOSE_MEMBER);
                 startActivityForResult(intent,GET_MEMBERS_FROM_ALL);        	 
                 return true;
             case R.id.actionAddAtNextLevelMember:
            	 if (objGroupMembersTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
            		 Toast.makeText(this,"Please select only one item at a time",Toast.LENGTH_SHORT).show();
            		 return false;
            	 }
            	 intent = new Intent(this, ActivityAllMembers.class);
            	 intent.putExtra(ActivityAllMembers.ACTION, ActivityAllMembers.ACTION_CHOOSE_MEMBER);
                 startActivityForResult(intent,GET_MEMBERS_FROM_ALL);         	 
                 return true;
             case R.id.actionEdit:
            	 if (objGroupMembersTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
            		 Toast.makeText(this,"Please select only one item at a time",Toast.LENGTH_SHORT).show();
            		 return false;
            	 }
            	 EditFolderName(objSelectedTreeNodes.get(0).getName(), guidSelectedTreeNode);
            	 return true;
             case R.id.actionDelete:
            	builder = new AlertDialog.Builder(this);
      		    builder.setMessage(R.string.dlg_are_you_sure_about_delete);
      		    builder.setCancelable(true);
      		    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      	            public void onClick(DialogInterface dialog, int id) {
      	            	clsTreeNode objDeletedTreeNode = objGroupMembersTreeview.getSelectedTreenodes().get(0);
      	            	objGroupMembers.Delete(objDeletedTreeNode);
      	            	objGroupMembersTreeview.RemoveTreeNode(objDeletedTreeNode, true);
      	            	objGroupMembersTreeview.SetAllIsDirty(true);
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
             case R.id.actionClearAll:
            	 objGroupMembers.Clear();
            	 objGroupMembers.BuildTreeviewFromGroupMemberData(objGroupMembersTreeview);
            	 RefreshListView();
            	 return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
	   // Get the intent, verify the action and get the query
	   if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		   if (boolInhibitSearchEvent == false) {
		     boolInhibitSearchEvent  = true;
		     String query = intent.getStringExtra(SearchManager.QUERY);
		     // manually launch the real search activity
		     final Intent searchIntent = new Intent(getApplicationContext(),
		           ActivityGetUserForGroups.class);
		     // add query to the Intent Extras
		     searchIntent.putExtra(ActivityGetUserForGroups.WEBSERVER_URL, strWebserverUrl);
		     searchIntent.putExtra(ActivityGetUserForGroups.REGISTERED_USER_UUID, objGroupMembers.GetRegisteredUser().strUserUuid);
		     searchIntent.putExtra(SearchManager.QUERY, query);
		     startActivityForResult(searchIntent, GET_USER_FOR_GROUPS);
		   }
	   }
    }

	public void RefreshListView() {
		// TODO Auto-generated method stub
		objListItems = objGroupMembersTreeview.getListItems();
		objGroupMembersArrayAdapter.clear();objGroupMembersArrayAdapter.addAll(objListItems);
		objGroupMembersArrayAdapter.notifyDataSetChanged();
		invalidateOptionsMenu();
	}

	 public void AddEditFolder(String strInitialValue, boolean boolAddNewOperation, boolean boolAddSameLevelOperation, UUID guidSelectedTreeNode) {
	    	
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setTitle("Enter folder name");

	    	// Set up the input
	    	final EditText input = new EditText(this);
	    	final UUID _guidSelectedTreeNode = guidSelectedTreeNode;
	    	final boolean _boolAddNewOperation = boolAddNewOperation;
	    	final boolean _boolAddSameLevelOperation = boolAddSameLevelOperation;
	    	// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
	    	input.setInputType(InputType.TYPE_CLASS_TEXT);
	    	input.setText(strInitialValue);
	    	builder.setView(input);

	    	// Set up the buttons
	    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	clsTreeNode       objNewTreeNode;
	    	    	clsTreeNode       objParentTreeNode;
	    	    	String strEditMessagebox = input.getText().toString();
	    			if (strEditMessagebox.length() == 0) {
	    				strEditMessagebox = "Unnamed folder " + objGroupMembersTreeview.getRepository().intEmptyItemCounter;
	    				objGroupMembersTreeview.getRepository().intEmptyItemCounter += 1;
	    			}
	    			if (_boolAddNewOperation) {
	    					if (_boolAddSameLevelOperation) {
	    						clsTreeNode objPeerTreeNode = objGroupMembersTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);
	    	       	    		
	    	    	    		objParentTreeNode =  objGroupMembersTreeview.getParentTreeNode(objPeerTreeNode);
	    	    	    		objNewTreeNode =  objGroupMembersTreeview.new clsGroupMemberFolderTreeNode(strEditMessagebox, clsTreeview.enumItemType.FOLDER_COLLAPSED, false, false);
	    	    	    		if (_guidSelectedTreeNode != null){
	    	    					// There is an item selected
	    	   	    	    		if (objParentTreeNode == null) {
	    	   	    	    			// Toplevel item selected
	    	   	    	    			objGroupMembersTreeview.getRepository().objRootNodes.add(objNewTreeNode);
	    	   	    	    		} else {
	    	   	    	    			// Child level selected
	    	   	    	    			objParentTreeNode.objChildren.add(objNewTreeNode);
	    	   	    	    		}
	    		   				} else {
	    		   	    			// No item selected, add at top level at end
	    		   					objGroupMembersTreeview.getRepository().objRootNodes.add(objNewTreeNode);
	    		   	    		}
	    	    	    		objGroupMembersTreeview.setSingleSelectedTreenode(objNewTreeNode);
	    		   				RefreshListView(); 
	    					} else {
	    						objParentTreeNode = objGroupMembersTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);
	    	    	    		objNewTreeNode = objGroupMembersTreeview.new clsGroupMemberFolderTreeNode(strEditMessagebox, clsTreeview.enumItemType.FOLDER_COLLAPSED, false, false);
	    	    	    		
	    	    	    		if (objParentTreeNode == null) {
	    	    	    			objGroupMembersTreeview.getRepository().objRootNodes.add(objNewTreeNode);
	    	    	    		} else {
	    	    	    			objParentTreeNode.objChildren.add(objNewTreeNode);
	    	    	        		objParentTreeNode.enumItemType = clsTreeview.enumItemType.FOLDER_EXPANDED;
	    	    	    		}
	    	    	    		objGroupMembersTreeview.setSingleSelectedTreenode(objNewTreeNode);
	    						RefreshListView();
	    					}
	           		 	   				
	    			} else {
	    				clsTreeNode objEditedTreeNode = objGroupMembersTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);    	    		
	    	    		objEditedTreeNode.setName(strEditMessagebox);
	    	    		RefreshListView();
	    			}
	    	    }
	    	});
	    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	    	    @Override
	    	    public void onClick(DialogInterface dialog, int which) {
	    	    	dialog.cancel();
	    	    }
	    	});

	    	builder.show();
	    }
	
	 private void EditFolderName(String strInitialValue, UUID guidSelectedTreeNode) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Enter note name");

			// Set up the input
			final EditText input = new EditText(this);
			final UUID _guidSelectedTreeNode = guidSelectedTreeNode;

			// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			input.setText(strInitialValue);
			builder.setView(input);

			// Set up the buttons
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	String strEditMessagebox = input.getText().toString();
					if (strEditMessagebox.length() == 0) {
						strEditMessagebox = "Unnamed folder";	
					}
					
					clsTreeNode objEditedTreeNode = objGroupMembersTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);    	    		
		    		objEditedTreeNode.setName(strEditMessagebox);
		    		RefreshListView();
			    }
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	dialog.cancel();
			    }
			});

			builder.show();
		}


	private void SaveFile() {
		clsUtils.SerializeToSharedPreferences("GroupMembers", "objRepository", this, objGroupMembersTreeview.getRepository());
		clsUtils.SerializeToSharedPreferences("GroupMembers", "strNoteUuid", this, strNoteUuid);
	}
	
	private void LoadFile() {
    	clsRepository objRepository = clsUtils.DeSerializeFromSharedPreferences("GroupMembers", "objRepository", this, objGroupMembersTreeview.getRepository().getClass());  	
    	if (objRepository != null) {
        	ArrayList<clsListItem> objLoadedListItems = objGroupMembersTreeview.getListItems();
        	objListItems.clear(); objListItems.addAll(objLoadedListItems);
    	} 
    	
    	strNoteUuid = clsUtils.DeSerializeFromSharedPreferences("GroupMembers", "strNoteUuid", this, String.class); 
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

   
   
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	Bundle objBundle;
    	UUID guidSelectedTreeNode;
    	ArrayList<clsTreeNode> objSelectedTreeNodes = objGroupMembersTreeview.getSelectedTreenodes();
    	if (objSelectedTreeNodes.size() == 0) {
    		guidSelectedTreeNode =  null;
    	} else {
    		guidSelectedTreeNode = objSelectedTreeNodes.get(0).guidTreeNode;
    	}
    	if (resultCode == Activity.RESULT_OK)
    	{
    		switch(requestCode)
    		{
    			case GET_MEMBERS_FROM_ALL:
    				ArrayList<clsAllMembersListViewState> objAllMembersListViewStates = null;
    				objBundle = data.getExtras();    		
    				String strSerialize    = objBundle.getString(ActivityAllMembers.ACTION_CHOOSE_MEMBER_RESP_GSON);
    				java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsAllMembersListViewState>>(){}.getType();
    				objAllMembersListViewStates = clsUtils.DeSerializeFromString(strSerialize, collectionType);
    				// Write all selected members to treeview
    				for (clsAllMembersListViewState objAllMembersListViewState:objAllMembersListViewStates){
    					if (objAllMembersListViewState.boolIsChecked) {
    						clsGroupMembersTreeNode objTreeNode = (clsGroupMembersTreeNode) objGroupMembersTreeview.getTreeNodeFromUuid(guidSelectedTreeNode);
    						clsGroupMemberMemberTreeNode objChildTreeNode = 
    								objGroupMembersTreeview.new clsGroupMemberMemberTreeNode(objAllMembersListViewState.objUser.strUserName,
    								enumItemType.OTHER, false,objAllMembersListViewState.objUser.strUserUuid);
    						if (objTreeNode.enumItemType != enumItemType.OTHER) {
    							// objTreenode is a folder, so new users are children
        						if (!objTreeNode.ChildExists(objChildTreeNode.getStrUserUuid())) {
        							// Item does not already exist in that folder
        							objTreeNode.objChildren.add(objChildTreeNode);
        						}
    						} else {
    							// objTreenode is a member, so users are added at same level
    							clsGroupMemberFolderTreeNode objParentTreeNode = (clsGroupMemberFolderTreeNode) objGroupMembersTreeview.getParentTreeNode(objTreeNode);
    							if (!objParentTreeNode.ChildExists(objChildTreeNode.getStrUserUuid())) {				
        							objParentTreeNode.objChildren.add(objChildTreeNode);  								
    							}
    						}
    					}
    				}
    				RefreshListView();
    				break;
    			case GET_USER_FOR_GROUPS:
    				objGroupMembers.LoadFile();
    				objGroupMembers.BuildTreeviewFromGroupMemberData(objGroupMembersTreeview);
    				RefreshListView();
    				break;

    		}	
    	}
    }

    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	if (objGroupMembersTreeview.GetAllIsDirty() == false) {
			// No changes, so exit without saving
    		Intent intent = getIntent();
			if (strCmnd.equals(ACTION_CHOOSE_MEMBERS)) {
				clsIntentMessaging.clsChosenMembers objResults = objGroupMembersTreeview.GetAllSelectedMembers(objGroupMembersTreeview);
				objResults.strNoteUuid = strNoteUuid;
				intent.putExtra(CHOOSE_MEMBERS_RESULT_GSON, clsUtils.SerializeToString(objResults));
			}
			setResult(RESULT_OK, intent);    	
	    	ActivityGroupMembers.this.finish();
	    	ActivityGroupMembers.super.onBackPressed();
	    	return;
    	}
    	// Query user if save or cancel
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Do you want to save or cancel the edits?");
    	builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Save data first
				objGroupMembers.BuildGroupMemberDataFromTreeView(objGroupMembers, objGroupMembersTreeview);
				objGroupMembers.SaveFile();
				Intent intent = getIntent();
				if (strCmnd.equals(ACTION_CHOOSE_MEMBERS)) {
					clsIntentMessaging.clsChosenMembers objResults = objGroupMembersTreeview.GetAllSelectedMembers(objGroupMembersTreeview);
					objResults.strNoteUuid = strNoteUuid;
					intent.putExtra(CHOOSE_MEMBERS_RESULT_GSON, clsUtils.SerializeToString(objResults));
				}
				setResult(RESULT_OK, intent);    	
		    	ActivityGroupMembers.this.finish();
		    	ActivityGroupMembers.super.onBackPressed();
			}
		});
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				// Return to caller
				setResult(RESULT_OK, getIntent());    	
				ActivityGroupMembers.this.finish();
				ActivityGroupMembers.super.onBackPressed();
			}
		});
    	builder.show();
    }
}
