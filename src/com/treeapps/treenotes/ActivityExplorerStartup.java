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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.treeapps.android.in_app_billing.util.IabHelper;
import com.treeapps.android.in_app_billing.util.IabResult;
import com.treeapps.android.in_app_billing.util.Inventory;
import com.treeapps.android.in_app_billing.util.Purchase;
import com.treeapps.treenotes.ActivityExplorerStartup.clsGetNoteSharedUsersAsyncTask.OnGetNoteSharedUsersResponseListener;
import com.treeapps.treenotes.ActivityExplorerStartup.clsSetNoteSharedUsersAsyncTask.OnSetNoteSharedUsersResponseListener;
import com.treeapps.treenotes.clsTreeview.clsRepository;
import com.treeapps.treenotes.clsTreeview.clsShareUser;
import com.treeapps.treenotes.clsTreeview.clsSyncRepository;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsTreeview.enumCutCopyPasteState;
import com.treeapps.treenotes.clsTreeview.enumItemType;
import com.treeapps.treenotes.export.ActivityFacebookExport;
import com.treeapps.treenotes.export.clsExportData;
import com.treeapps.treenotes.export.clsMainExport;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.OnImageUploadFinishedListener;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageAsyncTask;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageCommand;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageResponse;
import com.treeapps.treenotes.imageannotation.ActivityEditAnnotationImage;
import com.treeapps.treenotes.sharing.ActivityGroupMembers;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsGroupMembers.clsMembersRepository;
import com.treeapps.treenotes.sharing.clsGroupMembers.clsUser;
import com.treeapps.treenotes.sharing.clsMessaging.NoteSyncAsyncTask;
import com.treeapps.treenotes.sharing.clsMessaging.clsDownloadImageFileCommandMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsDownloadImageFileResponseMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageUpDownloadAsyncTask;
import com.treeapps.treenotes.sharing.clsMessaging.clsMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncMembersCommandMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncMembersResponseMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncNoteCommandMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncNoteResponseMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncRepositoryCtrlData;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncResult;
import com.treeapps.treenotes.sharing.clsMessaging.clsUploadImageFileCommandMsg;
import com.treeapps.treenotes.sharing.clsMessaging.clsUploadImageFileResponseMsg;
import com.treeapps.treenotes.sharing.clsWebServiceComms;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommand;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommsAsyncTask;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommsAsyncTask.OnCancelListener;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommsAsyncTask.OnCompleteListener;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceResponse;
import com.treeapps.treenotes.sharing.clsWorkerIfServerAlive;
import com.treeapps.treenotes.sharing.clsWorkerIfServerAlive.OnAliveCheckIncompleteListener;
import com.treeapps.treenotes.sharing.subscriptions.ActivityPublications;
import com.treeapps.treenotes.sharing.subscriptions.ActivitySubscriptions;
import com.treeapps.treenotes.sharing.subscriptions.ActivityPublications.clsPublicationsIntentData;
import com.treeapps.treenotes.sharing.subscriptions.ActivityPublications.clsSelectedNoteData;
import com.treeapps.treenotes.sharing.subscriptions.ActivitySubscriptions.clsSubcriptionsIntentData;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ActivityExplorerStartup extends ListActivity {

	// This is startup activity. First activity the user sees

	// keys to share between activities via Intents
	public static final String DESCRIPTION = "com.treeapps.treenotes.description";
	public static final String RESOURCE_ID = "com.treeapps.treenotes.resource_id";
	public static final String RESOURCE_PATH = "com.treeapps.treenotes.resource_path";
	public static final String TREENODE_UID = "com.treeapps.treenotes.treenode_uuid";
	public static final String PATH = "com.treeapps.treenotes.treenode_path";
	public static final String NEW_FILE_PROMPT = "com.treeapps.treenotes.new_file_prompt";
	public static final String APP_PREFERENCE = "com.treeapps.treenotes.app_preference";
	public static final String IS_ICON_CREATED = "com.treeapps.treenotes.is_icon_created";
	public static final String SHORTCUT_ACTIVE = "com.treeapps.treenotes.is_running_from_shortcut";
	public static final String SHORTCUT_NOTE_UUID = "com.treeapps.treenotes.shortcut_note_uuid";
	public static final String TREENODE_NAME = "com.treeapps.treenotes.treenote_name";
	public static final String SHORTCUT_NAME = "com.treeapps.treenotes.shortcut_name";
	public static final String IS_SHORTCUT = "com.treeapps.treenotes.is_shortcut";
	public static final String IMAGE_LOAD_DATAS = "com.treeapps.treenotes.image_load_datas";

	public static final int GET_DESCRIPTION_ADD_SAME_LEVEL = 0;
	public static final int GET_DESCRIPTION_ADD_NEXT_LEVEL = 1;
	public static final int EDIT_DESCRIPTION = 2;
	public static final int EDIT_SETTINGS = 3;
	public static final int FOLDER_ADD_NEW = 4;
	public static final int CREATE_NEW_NOTE_AT_SAME_LEVEL = 5;
	public static final int CREATE_NEW_NOTE_AT_NEXT_LEVEL = 6;
	public static final int ADD_NOTE = 7;
	public static final int EDIT_NOTE = 8;
	public static final int REQUEST_BACKUP_PATH = 9;
	public static final int REQUEST_RESTORE_PATH = 10;
	private static final int SHARE_REGISTER = 11;
	public static final int SHARE_MANAGE_GROUP_MEMBERS = 12;
	public static final int SHARE_CHOOSE_GROUP_MEMBERS = 13;
	private static final int ANNOTATOR = 14;
	private static final int SHARE_SUBSCRIPTIONS = 15;

	public clsExplorerTreeview objExplorerTreeview;
	public static File fileTreeNodesDir;
	private clsExplorerListItemArrayAdapter objListItemAdapter;
	private clsGroupMembers objGroupMembers = new clsGroupMembers(this);
	private clsMessaging objMessaging = new clsMessaging();
	private boolean boolDoNotSaveFile = false;
	private static Activity objContext;

	static clsImageUpDownloadAsyncTask objImageUpDownloadAsyncTask;
	clsGetNoteSharedUsersAsyncTask objGetNoteSharedUsersAsyncTask;
	clsSetNoteSharedUsersAsyncTask objSetNoteSharedUsersAsyncTask;
	
	public static ArrayList<clsImageLoadData> objLocalImageLoadDatas;

	// Billing data
	// To be persisted
	public static class clsIabLocalData {
		boolean boolIsAdsDisabledA; // True to be without adverts
		boolean boolIsAdsDisabledB; // False to be without adverts
	}

	private clsIabLocalData objIabLocalData;

	// Not to be persisted
	static final String base64EncodedPublicKeyPartScrambled = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiE1fPh+RVPFVjromhOMZCUAmkaPqrnOogfaWMBuijs4SgdlNrq74VQSm3Ud3UV9r5k41ZUN/CXU5tePvyrSCpUL16w0utVS2uDnch4OGGaHaBcI+Z5arfvlqZ9Oaoi6E9/bbAdrCleS0vRnD3ZFPvgWnCQzV9P6Nkv69jyXAtYzwrMtYwiZJyh/17TfltBPS5tnO1tnOgxphXElbWZ4lWl133YMkGcXBsItn9vLXUkKDjoMp3NcpcmFjf6M9n0isj+tvoENMQwfhOxZwuamvRarjsLGuZRFaKeGurIv3re7ZX3tmlEzQREsfmK1CaEHCY9LFd1krKJdiAhRLEzOTkQIDAQAB";
	static final String TAG_IAB = "TreeNotesIab";
	static final String SKU_ADVERTS_REMOVED = "android.test.purchased"; // treeapps.treenotes.remove_adverts
																		// OR
																		// android.test.purchased
																		// OR
																		// android.test.canceled
																		// OR
																		// android.test.refunded
																		// android.test.item_unavailable
	static final int RC_REQUEST = 10001; // (arbitrary) request code for the
											// purchase flow
	IabHelper mHelper; // The helper object
	private AdView adView;

	// End of billing data

	// GCM Data
	static final String TAG_GCM = "GCM";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String PROPERTY_REG_ID = "com.treeapps.treenotes.gcm_registration_id";
	public static final String PROPERTY_APP_VERSION = "com.treeapps.treenotes.gcm_application_version";
	public static final String EXTRA_MESSAGE = "com.treeapps.treenotes.message";
	public static final String BROADCAST_ACTION = "com.treeapps.treenotes.broadcast_sync";

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	String strRegistrationId;
	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the Google Developer API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "543138772701";

	// End of GCM data

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		// Prevent two instances of same application running
		if (!isTaskRoot()) {
		    Intent intent = getIntent();
		    String action = intent.getAction();
		    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
		        finish();
		        return;
		    }
		}

		// Save context
		objContext = this;

		// Get work folder location
		fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(this));
		if (!fileTreeNodesDir.exists()) {
			fileTreeNodesDir.mkdirs();
		}

		// Load users
		objGroupMembers.LoadFile();

		// Messaging
		objMessaging.LoadFile(this);

		// Get Treeview listItems
		objExplorerTreeview = new clsExplorerTreeview(this, objGroupMembers);
		objExplorerTreeview.DeserializeFromFile(fileTreeNodesDir, getResources().getString(R.string.working_file_name));
		ArrayList<clsListItem> listItems = objExplorerTreeview.getListItems();

		setContentView(R.layout.activity_explorer_startup);

		// ---List View---
		int resID = R.layout.note_list_item;
		int intTabWidthInDp = clsUtils.GetDefaultTabWidthInDp(this);
		int intTabWidthInPx = clsUtils.dpToPx(this, intTabWidthInDp);
		objListItemAdapter = new clsExplorerListItemArrayAdapter(this, resID, listItems, objExplorerTreeview,
				intTabWidthInPx);
		setListAdapter(objListItemAdapter);
		
		// NewItemsIndicator View
        clsNewItemsIndicatorView objClsNewItemsIndicatorView = (clsNewItemsIndicatorView)findViewById(R.id.newitems_indicator_view);
		objClsNewItemsIndicatorView.UpdateListItems(listItems);

		// Actionbar
		ActionBar actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);
		getOverflowMenu();

		// Shortcut operation
		Intent objIntent = getIntent();
		Bundle objBundle = objIntent.getExtras();
		if (objBundle != null) {
			boolean boolIsRunningFromShortcut = objBundle.getBoolean(ActivityExplorerStartup.SHORTCUT_ACTIVE);
			if (boolIsRunningFromShortcut) {
				// Check if note is in current explorer
				String strUid = objBundle.getString(ActivityExplorerStartup.SHORTCUT_NOTE_UUID);
				clsTreeNode objTreeNote = objExplorerTreeview.getTreeNodeFromUuid(UUID.fromString(strUid));
				if (objTreeNote != null) {
					// If so, just run it
					Intent intentShortcut = new Intent(this, ActivityNoteStartup.class);
					intentShortcut.putExtra(TREENODE_UID, strUid);
					intentShortcut.putExtra(TREENODE_NAME, objTreeNote.getName());
					intentShortcut.putExtra(IS_SHORTCUT, true);
					startActivityForResult(intentShortcut, EDIT_NOTE);
				} else {
					// If not, give user a message
					Toast.makeText(this,
							"The shortcut note not part of the current treenotes. Restore the originating source.",
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objContext);
		if (sharedPref.getString("treenotes_root_folder_name", "").isEmpty()) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("treenotes_root_folder_name",
					getResources().getString(R.string.pref_default_root_folder_name));
			editor.commit();
		}

		// Skip adverts if app runs on emulator
		// TODO JE remove skipping of ads if app is on emu
		if (!clsUtils.RunsOnEmu(objContext)) {
			// In-app billing
			SetupInAppBilling();

			// AdMob, only when advert removal has not been purchased
			objIabLocalData = clsUtils.LoadIabLocalValues(sharedPref, objIabLocalData);
			if (!(objIabLocalData != null && objIabLocalData.boolIsAdsDisabledA && !objIabLocalData.boolIsAdsDisabledB)) {
				// Look up the AdView as a resource and load a request.
				AdView adView = (AdView) this.findViewById(R.id.adViewExplorer);
				AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
						.addTestDevice("803D489BC46137FD0761EC7EBFBBFB09")
						.addTestDevice("C1B978D9FE1B0A6A8A58F1F44F653BE3")
						.addTestDevice("A947D095EE036142160FD3D2D4D5034C").build();
				adView.loadAd(adRequest);
			} else {
				RelativeLayout adscontainer = (RelativeLayout) findViewById(R.id.explorer_relative_layout);
				View admobAds = (View) findViewById(R.id.adViewExplorer);
				adscontainer.removeView(admobAds);
			}
			// Push notification (Google Cloud Messaging - GCM)
			if (checkPlayServices()) {
				// If this check succeeds, proceed with normal processing.
				gcm = GoogleCloudMessaging.getInstance(this);
				strRegistrationId = clsUtils.getRegistrationId(this);

				if (strRegistrationId.isEmpty()) {
					registerInBackground();
				}

			} else {
				// Otherwise, prompt user to get valid Play Services APK.
				clsUtils.MessageBox(
						this,
						"Please download Play Services APK from the Google Play Store or enable it in the device's system settings",
						false);
			}
		}
		
		// Add code to print out the key hash
	    try {
	        PackageInfo info = getPackageManager().getPackageInfo(
	                getPackageName(), 
	                PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            }
	    } catch (NameNotFoundException e) {

	    } catch (NoSuchAlgorithmException e) {

	    }
	    
		 // Broadcast from notification service
		IntentFilter mStatusIntentFilter = new IntentFilter(ActivityExplorerStartup.BROADCAST_ACTION);
	    // Instantiates a new DownloadStateReceiver
	    ResponseReceiver objResponseReceiver = new ResponseReceiver();
	    // Registers the DownloadStateReceiver and its intent filters
	    LocalBroadcastManager.getInstance(this).registerReceiver(objResponseReceiver, mStatusIntentFilter);

		// Session management
		clsUtils.CustomLog("ActivityExplorerStartup onCreate");
		if (savedInstanceState == null) {
			SaveFile();

			// Sync immediately if conditions satisfied
			clsWorkerIfServerAlive objWorkerIfServerAlive = new clsWorkerIfServerAlive(this, objMessaging, objGroupMembers,	"Syncing... please wait");
			objWorkerIfServerAlive.SetOnAliveCheckIncompleteListener(new OnAliveCheckIncompleteListener() {

				@Override
				public void onAliveCheckIncomplete(int intReason, String strMessage) {
					clsMessaging.ToastSyncingIsUnavailable(objContext, strMessage);
				}
			});
			objWorkerIfServerAlive.SetOnAliveCheckCompleteListener(new clsWorkerIfServerAlive.OnAliveCheckCompleteListener() {

				@Override
				public void onAliveCheckComplete(JSONObject objJsonResponse) {
					// Do work here, the actual syncing
					ExecuteExplorerSync(true, new clsWorkerIfServerAlive.OnAliveCheckFinishedListener() {
						
						@Override
						public void onAliveCheckFinished(boolean success, String strMessage) {
							if (success) {
								if (strMessage.isEmpty()) {
									clsMessaging.ToastSyncingIsAvailable(objContext);
								} else {
									clsUtils.MessageBox(objContext, strMessage, false);
								}
								
							} else {
								clsUtils.MessageBox(objContext, strMessage, false);
							}							
						}
					}); // AutoSync setting because no need to notify sharers					
				}
			});
			objWorkerIfServerAlive.Execute();

		} else {
			LoadFile();
		}
	}
	
	// Broadcast receiver for receiving status updates from the IntentService
    private class ResponseReceiver extends BroadcastReceiver
    {

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
        	AlertDialog dialog;
        	AlertDialog.Builder builder = new AlertDialog.Builder(objContext);
			builder.setMessage("A sync request has been received. Do you want to sync now?");
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ExecuteExplorerSync(true, null);
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					return;
				}
			});
			dialog = builder.create();
			dialog.show();	
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
		getMenuInflater().inflate(R.menu.explorer_startup_activity, menu);
		MenuItem objMenuPaste = menu.findItem(R.id.actionPaste);
		MenuItem objMenuAddAtNextLevel = menu.findItem(R.id.actionButtonAddAtNextLevel);
		MenuItem objMenuPasteAtNextLevel = menu.findItem(R.id.actionPasteAtNextLevel);

		if ((objExplorerTreeview.objClipboardTreeNodes.size() == 0)
				|| (objExplorerTreeview.objClipboardTreeNodes == null)) {
			objMenuPaste.setVisible(false);
		} else {
			objMenuPaste.setVisible(true);
		}

		// Set items based on note or folder
		UUID guidSelectedTreeNode;
		ArrayList<clsTreeNode> objSelectedTreeNodes = objExplorerTreeview.getSelectedTreenodes();
		objMenuAddAtNextLevel.setVisible(false);
		objMenuPasteAtNextLevel.setVisible(false);
		if (!((objSelectedTreeNodes.size() == 0) || (objSelectedTreeNodes.size() != 1))) {
			// Only allow note insert below if single selected node
			guidSelectedTreeNode = objSelectedTreeNodes.get(0).guidTreeNode;
			clsTreeNode objTreenode = objExplorerTreeview.getTreeNodeFromUuid(guidSelectedTreeNode);
			if (objTreenode.enumItemType != enumItemType.OTHER) {
				// Only allow note insert below if selected node is a folder
				objMenuAddAtNextLevel.setVisible(true);
				objMenuPasteAtNextLevel.setVisible(true);
			}
		}

		// Selectively enable "share" functionality menu items
		menu.findItem(R.id.actionShareRegister).setVisible(true);
		menu.findItem(R.id.actionShareReregister).setVisible(false);
		if (objGroupMembers.objMembersRepository.getStrRegisteredUserUuid() != null) {
			if (!objGroupMembers.objMembersRepository.getStrRegisteredUserUuid().isEmpty()) {
				menu.findItem(R.id.actionShareRegister).setVisible(false);
				menu.findItem(R.id.actionShareReregister).setVisible(true);
			}
		}
		MenuItem mnuShareManageGroups = menu.findItem(R.id.actionShareManageGroups);
		MenuItem mnuShareSyncSelected = menu.findItem(R.id.actionShareSelect);
		MenuItem mnuactionShareSyncAll = menu.findItem(R.id.actionShareSyncAll);
		MenuItem mnuPublications = menu.findItem(R.id.actionPublications);
		MenuItem mnuSubscriptions = menu.findItem(R.id.actionSubscriptions);

		if (objGroupMembers.GetRegisteredUser().strUserUuid.isEmpty()) {
			clsUtils.SetMenuItemEnabled(mnuShareManageGroups, false);
			clsUtils.SetMenuItemEnabled(mnuShareSyncSelected, false);
			clsUtils.SetMenuItemEnabled(mnuactionShareSyncAll, false);
			clsUtils.SetMenuItemEnabled(mnuPublications, false);
			clsUtils.SetMenuItemEnabled(mnuSubscriptions, false);
		} else {
			clsUtils.SetMenuItemEnabled(mnuShareManageGroups, true);
			clsUtils.SetMenuItemEnabled(mnuShareSyncSelected, true);
			clsUtils.SetMenuItemEnabled(mnuactionShareSyncAll, true);
			clsUtils.SetMenuItemEnabled(mnuPublications, true);
			clsUtils.SetMenuItemEnabled(mnuSubscriptions, true);
		}

		// Set Webserver selection
		if (objMessaging.getBoolIsServerIisExpress()) {
			menu.findItem(R.id.actionTestChangeToAzure).setVisible(true);
			menu.findItem(R.id.actionTestChangeToIisExpress).setVisible(false);
		} else {
			menu.findItem(R.id.actionTestChangeToAzure).setVisible(false);
			menu.findItem(R.id.actionTestChangeToIisExpress).setVisible(true);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		SaveFile();
		// very important:
		Log.d(TAG_IAB, "Destroying helper.");
		if (mHelper != null) {
			mHelper.dispose();
			mHelper = null;
		}
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
		clsUtils.CustomLog("ActivityExplorerStartup onDestroy SaveFile");
	}

	@Override
	protected void onStart() {
		super.onStart();
		LoadFile();
		clsUtils.CustomLog("ActivityExplorerStartup onStart LoadFile");
	}

	@Override
	protected void onStop() {
		SaveFile();
		super.onStop();
		clsUtils.CustomLog("ActivityExplorerStartup onStop SaveFile");
	}

	@Override
	protected void onRestart() {
		LoadFile();
		super.onRestart();
		clsUtils.CustomLog("ActivityExplorerStartup onRestart LoadFile");
	}

	@Override
	protected void onResume() {
		super.onResume();
		LoadFile();
		if (adView != null) {
			adView.resume();
		}
		// Skip adverts if app runs on emulator
		// TODO JE remove skipping of ads if app is on emu
		if (!clsUtils.RunsOnEmu(objContext)) {
			checkPlayServices();
		}
		clsUtils.CustomLog("ActivityExplorerStartup onResume LoadFile");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		SaveFile();
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
		clsUtils.CustomLog("ActivityExplorerStartup onPause SaveFile");
	}

	private void LoadFile() {
		objExplorerTreeview = new clsExplorerTreeview(this, objGroupMembers);
		objExplorerTreeview.DeserializeFromFile(clsUtils.BuildExplorerFilename(fileTreeNodesDir, getResources()
				.getString(R.string.working_file_name)));
		objExplorerTreeview.UpdateEnvironment(this, clsExplorerTreeview.enumCutCopyPasteState.INACTIVE,
				new ArrayList<clsTreeNode>()); 
		objGroupMembers.LoadFile();
		objGroupMembers.UpdateEnvironment(this);
		ArrayList<clsListItem> objListItems = objExplorerTreeview.getListItems();
		objListItemAdapter.UpdateEnvironment(this, objExplorerTreeview);
		objListItemAdapter.clear();
		objListItemAdapter.addAll(objListItems);
		fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(this));
		objMessaging.LoadFile(this);
		objContext = this;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objContext);
		objIabLocalData = clsUtils.LoadIabLocalValues(sharedPref, objIabLocalData);
		clsUtils.SerializeToSharedPreferences("ActivityExplorerStartup", "strRegistrationId", this, strRegistrationId);
		strRegistrationId = clsUtils.getRegistrationId(objContext);
		String strImageLoadDatas = sharedPref.getString("objImageLoadDatas", "");
   	    java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsImageLoadData>>(){}.getType();
   	    objLocalImageLoadDatas = clsUtils.DeSerializeFromString(strImageLoadDatas, collectionType);
		clsUtils.CustomLog("LoadFile");
	}

	private void SaveFile() {
		if (boolDoNotSaveFile)
			return;
		objExplorerTreeview.SaveFile();
		objGroupMembers.SaveFile();
		objMessaging.SaveFile(this);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objContext);
		clsUtils.SaveIabLocalValues(sharedPref, objIabLocalData);

		SharedPreferences.Editor editor = sharedPref.edit();
		String strImageLoadDatas = clsUtils.SerializeToString(objLocalImageLoadDatas);
    	editor.putString("objImageLoadDatas", strImageLoadDatas);
    	editor.commit();
    	
		clsUtils.CustomLog("SaveFile");

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Intent intent;
		AlertDialog.Builder builder;
		AlertDialog dialog;
		int intOrder;
		boolean boolIsBusyWithCutOperation;
		boolean boolPasteAtSameLevel;

		UUID guidSelectedTreeNode;
		ArrayList<clsTreeNode> objSelectedTreeNodes = objExplorerTreeview.getSelectedTreenodes();
		if (objSelectedTreeNodes.size() == 0) {
			guidSelectedTreeNode = null;
		} else {
			guidSelectedTreeNode = objSelectedTreeNodes.get(0).guidTreeNode;
		}

		switch (item.getItemId()) {
		case R.id.actionAddAtSameLevelFolder:
			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			AddEditFolder("", true, true, guidSelectedTreeNode);
			return true;
		case R.id.actionButtonAddAtNextLevelFolder:
			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			AddEditFolder("", true, false, guidSelectedTreeNode);
			return true;
		case R.id.actionAddAtSameLevelNote:
			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			AddEditNote("", true, true, guidSelectedTreeNode);
			return true;
		case R.id.actionButtonAddAtNextLevelNote:
			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			AddEditNote("", true, false, guidSelectedTreeNode);
			return true;
		case R.id.actionEdit:
			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}

			EditNoteHeading(objSelectedTreeNodes.get(0).getName(), guidSelectedTreeNode);
			return true;
		case R.id.actionDelete:
			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
				MessageBoxOk("Please select an item first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dlg_are_you_sure_about_delete);
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					clsTreeNode objDeletedTreeNode = objExplorerTreeview.getSelectedTreenodes().get(0);
					if (objDeletedTreeNode.enumItemType != enumItemType.OTHER) {
						objExplorerTreeview.RemoveTreeNode(objDeletedTreeNode);
					} else {
						objExplorerTreeview.setIsDeleted(objDeletedTreeNode, true);
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
		case R.id.actionMoveUp:
			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
				MessageBoxOk("Please select an item first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			clsTreeNode objMoveUpTreeNode = objSelectedTreeNodes.get(0);
			intOrder = objExplorerTreeview.getTreeNodeItemOrder(objMoveUpTreeNode);
			objExplorerTreeview.setTreeNodeItemOrder(objMoveUpTreeNode, intOrder -= 1);
			RefreshListView();
			return true;
		case R.id.actionMoveDown:
			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
				MessageBoxOk("Please select an item first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			clsTreeNode objMoveDownTreeNode = objSelectedTreeNodes.get(0);
			intOrder = objExplorerTreeview.getTreeNodeItemOrder(objMoveDownTreeNode);
			objExplorerTreeview.setTreeNodeItemOrder(objMoveDownTreeNode, intOrder += 1);
			RefreshListView();
			return true;
		case R.id.actionCut:
			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
				MessageBoxOk("Please select items first.");
				return false;
			}
			objExplorerTreeview.objClipboardTreeNodes = objSelectedTreeNodes;
			objExplorerTreeview.intCutCopyPasteState = enumCutCopyPasteState.CUTTING;
			objExplorerTreeview.ClearSelection();
			RefreshListView();
			return true;
		case R.id.actionCopy:
			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
				MessageBoxOk("Please select items first.");
				return false;
			}
			objExplorerTreeview.objClipboardTreeNodes = objSelectedTreeNodes;
			objExplorerTreeview.intCutCopyPasteState = enumCutCopyPasteState.PASTING;
			objExplorerTreeview.ClearSelection();
			RefreshListView();
			return true;
		case R.id.actionPasteAtSameLevel:
			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			boolIsBusyWithCutOperation = (objExplorerTreeview.intCutCopyPasteState == enumCutCopyPasteState.CUTTING) ? true
					: false;
			boolPasteAtSameLevel = true;
			if (objSelectedTreeNodes.size() == 0) {
				objExplorerTreeview.PasteClipboardTreeNodes(null, boolPasteAtSameLevel, boolIsBusyWithCutOperation); // Add
																														// to
																														// root
			} else {
				objExplorerTreeview.PasteClipboardTreeNodes(objSelectedTreeNodes.get(0), boolPasteAtSameLevel,
						boolIsBusyWithCutOperation); // Add
														// as
														// children
			}
			RefreshListView();

			return true;

		case R.id.actionPasteAtNextLevel:
			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			boolIsBusyWithCutOperation = (objExplorerTreeview.intCutCopyPasteState == enumCutCopyPasteState.CUTTING) ? true
					: false;
			boolPasteAtSameLevel = false;
			if (objSelectedTreeNodes.size() == 0) {
				objExplorerTreeview.PasteClipboardTreeNodes(null, boolPasteAtSameLevel, boolIsBusyWithCutOperation); // Add
																														// to
																														// root
			} else {
				objExplorerTreeview.PasteClipboardTreeNodes(objSelectedTreeNodes.get(0), boolPasteAtSameLevel,
						boolIsBusyWithCutOperation); // Add
														// as
														// children
			}
			RefreshListView();

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
		case R.id.actionBackup:
			// Save to default file on SD card
			intent = new Intent(this, ActivityFileChooser.class);
			intent.putExtra(PATH, clsUtils.GetTreeNotesDirectoryName(this));
			intent.putExtra(NEW_FILE_PROMPT, true);
			startActivityForResult(intent, REQUEST_BACKUP_PATH);
			return true;
		case R.id.actionRestore:
			intent = new Intent(this, ActivityFileChooser.class);
			intent.putExtra(PATH, clsUtils.GetTreeNotesDirectoryName(this));
			intent.putExtra(NEW_FILE_PROMPT, false);
			startActivityForResult(intent, REQUEST_RESTORE_PATH);
			return true;
		case R.id.actionCreateShortcut:
			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
				MessageBoxOk("Please select an item first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			clsTreeNode objShortcutTreeNode = objSelectedTreeNodes.get(0);
			if (!getSharedPreferences(APP_PREFERENCE, Activity.MODE_PRIVATE).getBoolean(IS_ICON_CREATED, false)) {
				objExplorerTreeview.addShortcut(this, objShortcutTreeNode);
				getSharedPreferences(APP_PREFERENCE, Activity.MODE_PRIVATE).edit().putBoolean(IS_ICON_CREATED, true);
			} else {
				Toast.makeText(this, "A shortcut already exists, please delete first", Toast.LENGTH_SHORT).show();
			}
			return true;
		case R.id.actionEditNoteHeading:
			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}

			EditNoteHeading(objSelectedTreeNodes.get(0).getName(), guidSelectedTreeNode);
			return true;
		case R.id.actionShareRegister:		
			clsWorkerIfServerAlive objWorkerIfServerAlive = new clsWorkerIfServerAlive(this, objMessaging, objGroupMembers,	"Registering... please wait");
			objWorkerIfServerAlive.SetOnAliveCheckIncompleteListener(new OnAliveCheckIncompleteListener() {

				@Override
				public void onAliveCheckIncomplete(int intReason, String strMessage) {
					clsUtils.MessageBox(objContext, strMessage, false);
				}
			});
			objWorkerIfServerAlive.SetOnAliveCheckCompleteListener(new clsWorkerIfServerAlive.OnAliveCheckCompleteListener() {

				@Override
				public void onAliveCheckComplete(JSONObject objJsonResponse) {
					// Do work here
					Intent intentRegister = new Intent(ActivityExplorerStartup.this, ActivityRegister.class);
					intentRegister.putExtra(ActivityRegister.USERNAME,
							objGroupMembers.objMembersRepository.getStrRegisteredUserName());
					intentRegister.putExtra(ActivityRegister.WEBSERVER_URL, objMessaging.GetServerUrl());
					startActivityForResult(intentRegister, SHARE_REGISTER);					 				
				}
			});
			objWorkerIfServerAlive.Execute();			
			return true;
		case R.id.actionShareReregister:
			objWorkerIfServerAlive = new clsWorkerIfServerAlive(objContext, objMessaging, objGroupMembers,	"Reregistering... please wait");
			objWorkerIfServerAlive.SetOnAliveCheckIncompleteListener(new OnAliveCheckIncompleteListener() {

				@Override
				public void onAliveCheckIncomplete(int intReason, String strMessage) {
					clsUtils.MessageBox(objContext, strMessage, false);
				}
			});
			objWorkerIfServerAlive.SetOnAliveCheckCompleteListener(new clsWorkerIfServerAlive.OnAliveCheckCompleteListener() {

				@Override
				public void onAliveCheckComplete(JSONObject objJsonResponse) {
					// Do work here
					Intent intentReregister = new Intent(ActivityExplorerStartup.this, ActivityRegister.class);
					intentReregister.putExtra(ActivityRegister.USERNAME,
							objGroupMembers.objMembersRepository.getStrRegisteredUserName());
					intentReregister.putExtra(ActivityRegister.WEBSERVER_URL, objMessaging.GetServerUrl());
					startActivityForResult(intentReregister, SHARE_REGISTER);					 				
				}
			});
			objWorkerIfServerAlive.Execute();
			return true;
		case R.id.actionShareManageGroups:
			if (clsMessaging.IsNetworkAvailable(this) == false) {
				Toast.makeText(this, "Network is unavailable", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (clsMessaging.IsServerAlive(this) == false) {
				Toast.makeText(this, "WebService is unavailable", Toast.LENGTH_SHORT).show();
				return true;
			}
			clsUser objRegisteredUser = objGroupMembers.GetRegisteredUser();
			if (objRegisteredUser.strUserName.equals(getResources().getString(R.string.unregistered_username))) {
				// Not registered, cannot sync
				Toast.makeText(this, "You need to register first before you can sync", Toast.LENGTH_SHORT).show();
				return true;
			}
			final URL urlFeed;
			try {
				urlFeed = new URL(objMessaging.GetServerUrl()
						+ getResources().getString(R.string.url_members_sync));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			}
			clsSyncMembersCommandMsg objSyncMembersCommandMsg = objMessaging.new clsSyncMembersCommandMsg();
			objSyncMembersCommandMsg.intClientInstruction = ActivityExplorerSyncMembersAsyncTask.SYNC_MEMBERS_CLIENT_INSTRUCT_GET_MEMBERS;
			objSyncMembersCommandMsg.strClientUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
			objSyncMembersCommandMsg.objSyncMembersRepository = objGroupMembers.new clsSyncMembersRepository(); // Empty,
																												// ask
																												// for
																												// data
			ActivityExplorerSyncMembersAsyncTask objAsyncTask = new ActivityExplorerSyncMembersAsyncTask(urlFeed, this,
					objSyncMembersCommandMsg, objGroupMembers, objMessaging);
			objAsyncTask.execute("");
			return true;
		case R.id.actionShareSelect:

			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			}

			if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}

			if (objSelectedTreeNodes.get(0).enumItemType != enumItemType.OTHER) {
				MessageBoxOk("Please select a note first");
				return false;
			}
			// Async task which gets current shared users for this note from server
			try {
				urlFeed = new URL(objMessaging.GetServerUrl()
						+ getResources().getString(R.string.url_get_note_sharers));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			} 
			clsTreeNode objSelectedTreeNode = objSelectedTreeNodes.get(0);
			clsGetNoteSharedUsersCommand objCommand = new clsGetNoteSharedUsersCommand();
			objCommand.strNoteUuid = objSelectedTreeNode.guidTreeNode.toString();
			clsGetNoteSharedUsersResponse objResponse = new clsGetNoteSharedUsersResponse(); 
			objGetNoteSharedUsersAsyncTask = new clsGetNoteSharedUsersAsyncTask(this, urlFeed, objCommand, objResponse);
			objGetNoteSharedUsersAsyncTask.SetOnResponseListener(new OnGetNoteSharedUsersResponseListener() {
				
				@Override
				public void onResponse(clsGetNoteSharedUsersResponse objResponse) {
					if (objResponse.intErrorCode == clsGetNoteSharedUsersResponse.ERROR_NONE) {
						// Data received, can start selection activity now
						Intent intentShareGroupMembers = new Intent(ActivityExplorerStartup.this, ActivityGroupMembers.class);
						intentShareGroupMembers.putExtra(ActivityGroupMembers.ACTION, ActivityGroupMembers.ACTION_CHOOSE_MEMBERS);
						clsIntentMessaging objIntentMessaging = new clsIntentMessaging();
						clsIntentMessaging.clsChosenMembers objChosenMembers = objIntentMessaging.new clsChosenMembers();
						objChosenMembers.strUserUuids = objResponse.strSharedUsers;
						objChosenMembers.strNoteUuid = objResponse.strNoteUuid;
						String strChosenMembersGson = clsUtils.SerializeToString(objChosenMembers);
						intentShareGroupMembers.putExtra(ActivityGroupMembers.SHARE_SHARED_USERS, strChosenMembersGson);
						intentShareGroupMembers.putExtra(ActivityGroupMembers.WEBSERVER_URL,
								objMessaging.GetServerUrl());
						startActivityForResult(intentShareGroupMembers, SHARE_CHOOSE_GROUP_MEMBERS);
					} else {
						clsUtils.MessageBox(objContext, objResponse.strErrorMessage, false);
					}
					return;
				}
			});
			objGetNoteSharedUsersAsyncTask.execute(null, null, null);
			return true;
		case R.id.actionShareSyncAll:
			ExecuteExplorerSync(false, null);
			return true;
		case R.id.actionPublications:
			if (clsMessaging.IsNetworkAvailable(this) == false) {
				Toast.makeText(this, "Network is unavailable", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (clsMessaging.IsServerAlive(this) == false) {
				Toast.makeText(this, "WebService is unavailable", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
				MessageBoxOk("Please select an item first.");
				return false;
			}
			Intent intentPublications = new Intent(this, ActivityPublications.class);
			ActivityPublications.clsPublicationsIntentData objPublicationsIntentData = new clsPublicationsIntentData();
			objPublicationsIntentData.boolIsServerIisExpress = objMessaging.getBoolIsServerIisExpress();
			objPublicationsIntentData.strRegisteredUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
			objPublicationsIntentData.lstSelectedNoteDatas = GetSelectedNoteDatas(objSelectedTreeNodes);
			intentPublications.putExtra(ActivitySubscriptions.INTENT_DATA,
					clsUtils.SerializeToString(objPublicationsIntentData));
			startActivityForResult(intentPublications, SHARE_SUBSCRIPTIONS);
			return true;
		case R.id.actionSubscriptions:
			if (clsMessaging.IsNetworkAvailable(this) == false) {
				Toast.makeText(this, "Network is unavailable", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (clsMessaging.IsServerAlive(this) == false) {
				Toast.makeText(this, "WebService is unavailable", Toast.LENGTH_SHORT).show();
				return true;
			}
			Intent intentSubscriptions = new Intent(this, ActivitySubscriptions.class);
			ActivitySubscriptions.clsSubcriptionsIntentData objSubscriptionsIntentData = new clsSubcriptionsIntentData();
			objSubscriptionsIntentData.boolIsServerIisExpress = objMessaging.getBoolIsServerIisExpress();
			objSubscriptionsIntentData.strRegisteredUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
			intentSubscriptions.putExtra(ActivitySubscriptions.INTENT_DATA,
					clsUtils.SerializeToString(objSubscriptionsIntentData));
			startActivityForResult(intentSubscriptions, SHARE_SUBSCRIPTIONS);
			return true;
		
		case R.id.actionSettings:

			// Set initial values
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("registered_user", objGroupMembers.GetRegisteredUser().strUserName);
			// Check if there is a single selected item and get owner value
			String strSubItemOwnerUserName = "";
			if (objSelectedTreeNodes.size() != 0) {
				guidSelectedTreeNode = objSelectedTreeNodes.get(0).guidTreeNode;
				objSelectedTreeNode = objExplorerTreeview.getTreeNodeFromUuid(guidSelectedTreeNode);
				if (objSelectedTreeNode.enumItemType == enumItemType.OTHER) {
					strSubItemOwnerUserName = objExplorerTreeview.GetUserNameFromNoteFile(this, objGroupMembers,
							objSelectedTreeNode.guidTreeNode);
				} else {
					strSubItemOwnerUserName = "Selected item is not a note";
				}
			} else {
				strSubItemOwnerUserName = "No selected item to evaluate";
			}
			editor.putString("note_selecteditem_owner", strSubItemOwnerUserName);
			PackageInfo pInfo;
			try {
				pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				editor.putString("treenotes_version", pInfo.versionName);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				editor.putString("treenotes_version", "Not readable");
			}

			editor.commit();

			// Launch intent
			intent = new Intent(this, ActivityExplorerSettings.class);
			startActivityForResult(intent, ActivityExplorerStartup.EDIT_SETTINGS);
			return true;
		case R.id.actionTestUnregister:
			objGroupMembers.Unregister();
			objGroupMembers.SaveFile();
			invalidateOptionsMenu();
			return true;
		case R.id.actionNormalUser:
			objExplorerTreeview.SaveFile();
			objGroupMembers.SaveFile();
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			editor = sharedPref.edit();
			editor.putString("treenotes_root_folder_name", "treenotes");
			editor.commit();
			boolDoNotSaveFile = true;
			finish();
			return true;
		case R.id.actionTestUser1:
			objExplorerTreeview.SaveFile();
			objGroupMembers.SaveFile();
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			editor = sharedPref.edit();
			editor.putString("treenotes_root_folder_name", "treenotes_user_1");
			editor.commit();
			boolDoNotSaveFile = true;
			finish();
			return true;
		case R.id.actionTestUser2:
			objExplorerTreeview.SaveFile();
			objGroupMembers.SaveFile();
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			editor = sharedPref.edit();
			editor.putString("treenotes_root_folder_name", "treenotes_user_2");
			editor.commit();
			boolDoNotSaveFile = true;
			finish();
			return true;
		case R.id.actionTestUser3:
			objExplorerTreeview.SaveFile();
			objGroupMembers.SaveFile();
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			editor = sharedPref.edit();
			editor.putString("treenotes_root_folder_name", "treenotes_user_3");
			editor.commit();
			boolDoNotSaveFile = true;
			finish();
			return true;
		case R.id.actionTestEditNote:
			if (objExplorerTreeview.getRepository().objRootNodes.size() == 0) {
				MessageBoxOk("Please add some items first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() == 0) {
				MessageBoxOk("Please select an item first.");
				return false;
			} else if (objExplorerTreeview.getRepository().objRootNodes.size() != 0 && objSelectedTreeNodes.size() > 1) {
				MessageBoxOk("Please select only one item at a time");
				return false;
			}
			clsTreeNode objTreeNode = objSelectedTreeNodes.get(0);
			if (objTreeNode.enumItemType == enumItemType.OTHER) {
				intent = new Intent(this, ActivityNoteStartup.class);
				intent.putExtra(TREENODE_UID, objTreeNode.guidTreeNode.toString());
				intent.putExtra(TREENODE_NAME, objTreeNode.getName());
				startActivityForResult(intent, EDIT_NOTE);
			} else {
				AddEditFolder(objTreeNode.getName(), false, false, objTreeNode.guidTreeNode);
			}
			return true;
		case R.id.actionTestChangeToIisExpress:
			objMessaging.setBoolIsServerIisExpress(true);
			objMessaging.SaveFile(this);
			boolDoNotSaveFile = true;
			finish();
			return true;
		case R.id.actionTestChangeToAzure:
			objMessaging.setBoolIsServerIisExpress(false);
			objMessaging.SaveFile(this);
			boolDoNotSaveFile = true;
			finish();
			return true;
		case R.id.actionUnregisterGcm:
			clsUtils.storeRegistrationId(this, "");
			return true;
		case R.id.actionConsumePurchases:
			mHelper.queryInventoryAsync(mGotInventoryForReversalListener);
			return true;
		case R.id.actionClearWebServiceRepository:
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you weant to clear the webservice repository?");
			builder.setCancelable(true);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					objMessaging.ClearWebServiceRepository(objContext);
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
		case R.id.actionTestQuick:
			Intent intentManageGroupMembers = new Intent(this, ActivityGroupMembers.class);
			intentManageGroupMembers.putExtra(ActivityGroupMembers.ACTION, ActivityGroupMembers.ACTION_MANAGE_GROUPS);
			intentManageGroupMembers.putExtra(ActivityGroupMembers.WEBSERVER_URL,
					objMessaging.GetServerUrl());
			startActivityForResult(intentManageGroupMembers, SHARE_MANAGE_GROUP_MEMBERS);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private ArrayList<String> GetSelectedNoteUuids(ArrayList<clsTreeNode> objSelectedTreeNodes) {
		ArrayList<String> lstSelectedNoteUuids = new ArrayList<String>();
		for (clsTreeNode objSelectedTreeNode : objSelectedTreeNodes) {
			lstSelectedNoteUuids.add(objSelectedTreeNode.guidTreeNode.toString());
		}
		// TODO Auto-generated method stub
		return lstSelectedNoteUuids;
	}

	private ArrayList<clsSelectedNoteData> GetSelectedNoteDatas(ArrayList<clsTreeNode> objSelectedTreeNodes) {
		ArrayList<clsSelectedNoteData> lstSelectedNoteDatas = new ArrayList<clsSelectedNoteData>();
		for (clsTreeNode objSelectedTreeNode : objSelectedTreeNodes) {
			clsSelectedNoteData objSelectedNoteData = new clsSelectedNoteData();
			objSelectedNoteData.strNoteUuid = objSelectedTreeNode.guidTreeNode.toString();
			objSelectedNoteData.strNoteName = objSelectedTreeNode.getName();
			lstSelectedNoteDatas.add(objSelectedNoteData);
		}
		// TODO Auto-generated method stub
		return lstSelectedNoteDatas;
	}

	public void RefreshListView() {
		SaveFile();
		ArrayList<clsListItem> objListItems = objExplorerTreeview.getListItems();
		objListItemAdapter.clear();
		objListItemAdapter.addAll(objListItems);
		objListItemAdapter.notifyDataSetChanged();
		clsNewItemsIndicatorView objClsNewItemsIndicatorView = (clsNewItemsIndicatorView)findViewById(R.id.newitems_indicator_view);
		objClsNewItemsIndicatorView.UpdateListItems(objListItems);
		invalidateOptionsMenu();
	}

	private OnClickListener ClearAllOnClickListener() {
		return new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				objExplorerTreeview.ClearAll();
				objGroupMembers.ClearAll();
				RefreshListView();
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objContext);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.commit();
			}
		};
	}

	// sink for own and foreign Intents
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		AlertDialog.Builder builder;
		AlertDialog dialog;

		UUID guidSelectedTreeNode;
		ArrayList<clsTreeNode> objSelectedTreeNodes = objExplorerTreeview.getSelectedTreenodes();
		if (objSelectedTreeNodes.size() == 0) {
			guidSelectedTreeNode = null;
		} else {
			guidSelectedTreeNode = objSelectedTreeNodes.get(0).guidTreeNode;
		}

		if (resultCode == Activity.RESULT_OK) {
			// get information from Intent
			Bundle objBundle = data.getExtras();
			switch (requestCode) {
			case ADD_NOTE:
				String strImageLoadDatas = objBundle.getString(IMAGE_LOAD_DATAS);
				java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsImageLoadData>>() {
				}.getType();
				objExplorerTreeview.getRepository().objImageLoadDatas = clsUtils.DeSerializeFromString(
						strImageLoadDatas, collectionType);
				RefreshListView();
				break;
			case EDIT_NOTE:
				strImageLoadDatas = objBundle.getString(IMAGE_LOAD_DATAS);
				collectionType = new TypeToken<ArrayList<clsImageLoadData>>() {
				}.getType();
				objExplorerTreeview.getRepository().objImageLoadDatas = clsUtils.DeSerializeFromString(
						strImageLoadDatas, collectionType);
				boolean boolIsShortcut = objBundle.getBoolean(ActivityExplorerStartup.IS_SHORTCUT);
				if (boolIsShortcut) {
					finish();
					moveTaskToBack(true);
				}
				RefreshListView();
				break;
			case REQUEST_BACKUP_PATH:
				final String strBackupFileName = clsUtils.RemoveExtension(data.getStringExtra("GetFileName"));
				if (strBackupFileName.indexOf("<" + getResources().getString(R.string.new_file_prompt) + ">") == 0) {
					BackupToNewFile();
					break;
				}
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Are you sure you want to backup to: " + strBackupFileName);
				builder.setCancelable(true);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						objExplorerTreeview.BackupToFile(strBackupFileName);
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
				break;
			case REQUEST_RESTORE_PATH:
				final String strRestoreFileName = clsUtils.RemoveExtension(data.getStringExtra("GetFileName"));
				builder = new AlertDialog.Builder(this);
				builder.setMessage("Are you sure you want to restore from: " + strRestoreFileName);
				builder.setCancelable(true);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						objExplorerTreeview.RestoreFromFile(strRestoreFileName);
						SaveFile();
						RefreshListView();
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
				break;
			case SHARE_REGISTER:
				objBundle = data.getExtras();
				String strRegisteredUserName = objBundle.getString(ActivityRegister.USERNAME);
				String strRegisteredUserUuid = objBundle.getString(ActivityRegister.USERUUID);
				boolean boolUserExists = objBundle.getBoolean(ActivityRegister.USER_EXISTS);
				objGroupMembers.objMembersRepository.setStrRegisteredUserName(strRegisteredUserName);
				objGroupMembers.objMembersRepository.setStrRegisteredUserUuid(strRegisteredUserUuid);
				objGroupMembers.SaveFile();
				// Add registry details to all notes with no owners
				objExplorerTreeview.SetOwnerOfNotesWithoutOwner(strRegisteredUserUuid);
				if (boolUserExists) {
					Toast.makeText(getApplicationContext(), "Reregistration successfull", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Registration successfull, new user created",
							Toast.LENGTH_SHORT).show();
				}
				RefreshListView();
				break;
			case SHARE_MANAGE_GROUP_MEMBERS:
				// Save return values to that specific note's repository share
				// data
				final URL urlFeed;
				try {
					urlFeed = new URL(objMessaging.GetServerUrl()
							+ getResources().getString(R.string.url_members_sync));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}

				clsSyncMembersCommandMsg objSyncMembersCommandMsg = objMessaging.new clsSyncMembersCommandMsg();
				objSyncMembersCommandMsg.intClientInstruction = ActivityExplorerSyncMembersAsyncTask.SYNC_MEMBERS_CLIENT_INSTRUCT_UPDATE_MEMBERS;
				objGroupMembers.LoadFile();
				objSyncMembersCommandMsg.strClientUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
				objSyncMembersCommandMsg.objSyncMembersRepository = objGroupMembers.objMembersRepository.Copy();
				ActivityExplorerSyncMembersAsyncTask objAsyncTask = new ActivityExplorerSyncMembersAsyncTask(urlFeed,
						this, objSyncMembersCommandMsg, objGroupMembers, objMessaging);
				objAsyncTask.execute("");
				break;
			case SHARE_CHOOSE_GROUP_MEMBERS:
				// Save return values to that specific note's repository share data
				objBundle = data.getExtras();
				String strChooseResultGson = objBundle.getString(ActivityGroupMembers.CHOOSE_MEMBERS_RESULT_GSON);
				clsIntentMessaging.clsChosenMembers objResults = clsUtils.DeSerializeFromString(strChooseResultGson,
						clsIntentMessaging.clsChosenMembers.class);

				// Inform server about the selection
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
				clsTreeNode objSelectedTreeNode = objExplorerTreeview.getTreeNodeFromUuid(UUID
						.fromString(objResults.strNoteUuid));
				clsSetNoteSharedUsersCommand objCommand = new clsSetNoteSharedUsersCommand();
				objCommand.strClientUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
				objCommand.strNoteUuid = objSelectedTreeNode.guidTreeNode.toString();
				objCommand.objSharedUsers = objResults.strUserUuids;
				final boolean boolIsNoteShared = (objResults.strUserUuids.size() == 0) ? false: true;
				clsSetNoteSharedUsersResponse objResponse = new clsSetNoteSharedUsersResponse();
				objSetNoteSharedUsersAsyncTask = new clsSetNoteSharedUsersAsyncTask(this, urlFeed, objCommand, objResponse);
				objSetNoteSharedUsersAsyncTask.SetOnResponseListener(new OnSetNoteSharedUsersResponseListener() {
					
					@Override
					public void onResponse(clsSetNoteSharedUsersResponse objResponse) {
						if (objResponse.intErrorCode == clsSetNoteSharedUsersResponse.ERROR_NETWORK ) {
							clsUtils.MessageBox(objContext, objResponse.strErrorMessage, false);
						} else {
							if (boolIsNoteShared) {
								objExplorerTreeview.getRepository().boolIsShared = true;
							} else {
								objExplorerTreeview.getRepository().boolIsShared = false;
							}
						}
						RefreshListView();
					}
				});
				objSetNoteSharedUsersAsyncTask.execute(null,null,null);			
				break;
			default:
			}
		} else {
			switch (requestCode) {
			case EDIT_SETTINGS:
				SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
				boolean my_checkbox_preference = mySharedPreferences.getBoolean("checkbox_preference", false);

				// Admob
				objIabLocalData = clsUtils.LoadIabLocalValues(mySharedPreferences, objIabLocalData);
				if (objIabLocalData != null && objIabLocalData.boolIsAdsDisabledA
						&& !objIabLocalData.boolIsAdsDisabledB) {
					RelativeLayout adscontainer = (RelativeLayout) findViewById(R.id.explorer_relative_layout);
					View admobAds = (View) adscontainer.findViewById(R.id.adViewExplorer);
					if (admobAds != null) {
						adscontainer.removeView(admobAds);
					}
				}
				break;

			}
		}
	}

	private void getOverflowMenu() {
		// Hack to get to overflow action to display if there is a physical menu
		// key
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void MessageBoxOk(String strMessage) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(strMessage);
		builder.setCancelable(true);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void AddEditFolder(String strInitialValue, boolean boolAddNewOperation, boolean boolAddSameLevelOperation,
			UUID guidSelectedTreeNode) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter folder name");

		// Set up the input
		final EditText input = new EditText(this);
		final UUID _guidSelectedTreeNode = guidSelectedTreeNode;
		final boolean _boolAddNewOperation = boolAddNewOperation;
		final boolean _boolAddSameLevelOperation = boolAddSameLevelOperation;
		// Specify the type of input expected; this, for example, sets the input
		// as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setText(strInitialValue);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				clsTreeNode objNewTreeNode;
				clsTreeNode objParentTreeNode;
				String strEditMessagebox = input.getText().toString();
				if (strEditMessagebox.length() == 0) {
					strEditMessagebox = "Unnamed folder " + objExplorerTreeview.getRepository().intEmptyItemCounter;
					objExplorerTreeview.getRepository().intEmptyItemCounter += 1;
				}
				if (_boolAddNewOperation) {
					if (_boolAddSameLevelOperation) {
						clsTreeNode objPeerTreeNode = objExplorerTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);

						objParentTreeNode = objExplorerTreeview.getParentTreeNode(objPeerTreeNode);
						String strOwnerUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
						objNewTreeNode = objExplorerTreeview.new clsExplorerFolderTreeNode(strEditMessagebox,
								clsTreeview.enumItemType.FOLDER_COLLAPSED, false, strOwnerUserUuid, strOwnerUserUuid);
						if (_guidSelectedTreeNode != null) {
							// There is an item selected
							if (objParentTreeNode == null) {
								// Toplevel item selected
								objExplorerTreeview.getRepository().objRootNodes.add(objNewTreeNode);
							} else {
								// Child level selected
								objParentTreeNode.objChildren.add(objNewTreeNode);
							}
						} else {
							// No item selected, add at top level at end
							objExplorerTreeview.getRepository().objRootNodes.add(objNewTreeNode);
						}
						objExplorerTreeview.setSingleSelectedTreenode(objNewTreeNode);
						RefreshListView();
					} else {
						objParentTreeNode = objExplorerTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);
						String strOwnerUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
						objNewTreeNode = objExplorerTreeview.new clsExplorerFolderTreeNode(strEditMessagebox,
								clsTreeview.enumItemType.FOLDER_COLLAPSED, false, strOwnerUserUuid, strOwnerUserUuid);

						if (objParentTreeNode == null) {
							objExplorerTreeview.getRepository().objRootNodes.add(objNewTreeNode);
						} else {
							objParentTreeNode.objChildren.add(objNewTreeNode);
							objParentTreeNode.enumItemType = clsTreeview.enumItemType.FOLDER_EXPANDED;
						}
						objExplorerTreeview.setSingleSelectedTreenode(objNewTreeNode);
						RefreshListView();
					}

				} else {
					clsTreeNode objEditedTreeNode = objExplorerTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);
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

	private void AddEditNote(String strInitialValue, boolean boolAddNewOperation, boolean boolAddSameLevelOperation,
			UUID guidSelectedTreeNode) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter note name");

		// Set up the input
		final EditText input = new EditText(this);
		final UUID _guidSelectedTreeNode = guidSelectedTreeNode;
		final boolean _boolAddNewOperation = boolAddNewOperation;
		final boolean _boolAddSameLevelOperation = boolAddSameLevelOperation;
		final Context _context = this;

		// Specify the type of input expected; this, for example, sets the input
		// as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setText(strInitialValue);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				clsTreeNode objNewTreeNode;
				clsTreeNode objParentTreeNode;
				String strEditMessagebox = input.getText().toString();
				if (strEditMessagebox.length() == 0) {
					strEditMessagebox = "Unnamed note " + objExplorerTreeview.getRepository().intEmptyItemCounter;
					objExplorerTreeview.getRepository().intEmptyItemCounter += 1;
				}
				if (_boolAddNewOperation) {
					if (_boolAddSameLevelOperation) {
						// Create item in explorer
						clsTreeNode objPeerTreeNode = objExplorerTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);
						objParentTreeNode = objExplorerTreeview.getParentTreeNode(objPeerTreeNode);
						String strOwnerUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
						objNewTreeNode = objExplorerTreeview.new clsExplorerNoteTreeNode(strEditMessagebox,
								clsTreeview.enumItemType.OTHER, false, strOwnerUserUuid, strOwnerUserUuid);
						if (_guidSelectedTreeNode != null) {
							// There is an item selected
							if (objParentTreeNode == null) {
								// Toplevel item selected
								objExplorerTreeview.getRepository().objRootNodes.add(objNewTreeNode);
							} else {
								// Child level selected
								objParentTreeNode.objChildren.add(objNewTreeNode);
							}
						} else {
							// No item selected, add at top level at end
							objExplorerTreeview.getRepository().objRootNodes.add(objNewTreeNode);
						}
						objExplorerTreeview.setSingleSelectedTreenode(objNewTreeNode);
						// Shell out to note activity
						Intent intent = new Intent(_context, ActivityNoteStartup.class);
						intent.putExtra(TREENODE_UID, objNewTreeNode.guidTreeNode.toString());
						intent.putExtra(TREENODE_NAME, objNewTreeNode.getName());
						intent.putExtra(IS_SHORTCUT, false);
						String strImageLoadDatas = clsUtils.SerializeToString(objExplorerTreeview.getRepository().objImageLoadDatas);
						intent.putExtra(IMAGE_LOAD_DATAS, strImageLoadDatas);
						startActivityForResult(intent, ADD_NOTE);
					} else {
						// Create item in explorer
						objParentTreeNode = objExplorerTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);
						String strOwnerUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
						objNewTreeNode = objExplorerTreeview.new clsExplorerNoteTreeNode(strEditMessagebox,
								clsTreeview.enumItemType.OTHER, false, strOwnerUserUuid, strOwnerUserUuid);

						if (objParentTreeNode == null) {
							objExplorerTreeview.getRepository().objRootNodes.add(objNewTreeNode);
						} else {
							objParentTreeNode.objChildren.add(objNewTreeNode);
						}
						objParentTreeNode.enumItemType = enumItemType.FOLDER_EXPANDED;
						objExplorerTreeview.setSingleSelectedTreenode(objNewTreeNode);
						// Shell out to note activity
						Intent intent = new Intent(_context, ActivityNoteStartup.class);
						intent.putExtra(TREENODE_UID, objNewTreeNode.guidTreeNode.toString());
						intent.putExtra(TREENODE_NAME, objNewTreeNode.getName());
						intent.putExtra(IS_SHORTCUT, false);
						String strImageLoadDatas = clsUtils.SerializeToString(objExplorerTreeview.getRepository().objImageLoadDatas);
						intent.putExtra(IMAGE_LOAD_DATAS, strImageLoadDatas);
						startActivityForResult(intent, ADD_NOTE);
					}

				} else {
					clsTreeNode objEditedTreeNode = objExplorerTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);
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

	private void EditNoteHeading(String strInitialValue, UUID guidSelectedTreeNode) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter note name");

		// Set up the input
		final EditText input = new EditText(this);
		final UUID _guidSelectedTreeNode = guidSelectedTreeNode;

		// Specify the type of input expected; this, for example, sets the input
		// as a password, and will mask the text
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

				clsTreeNode objEditedTreeNode = objExplorerTreeview.getTreeNodeFromUuid(_guidSelectedTreeNode);
				objEditedTreeNode.setName(strEditMessagebox);
				if (objEditedTreeNode.enumItemType == enumItemType.OTHER) {
					// This is a note so name needs to be changed inside file
					// also
					File fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(objContext));
					File objNoteFile = clsUtils.BuildNoteFilename(fileTreeNodesDir,
							objEditedTreeNode.guidTreeNode.toString());
					if (objNoteFile.exists()) {
						clsRepository objNoteRepository = clsExplorerTreeview.DeserializeNoteFromFile(objNoteFile);
						if (objNoteRepository != null) {
							objNoteRepository.setName(strEditMessagebox);
							objExplorerTreeview.SerializeNoteToFile(objNoteRepository, objNoteFile);
							RefreshListView();
						} else {
							MessageBoxOk("Selected note could not be read from file");
						}
					}
				}
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

	private void BackupToNewFile() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter backup name");

		// Set up the input
		final EditText input = new EditText(this);

		// Specify the type of input expected; this, for example, sets the input
		// as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setText("");
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String strEditMessagebox = input.getText().toString();
				if (strEditMessagebox.length() == 0) {
					strEditMessagebox = "Unnamed folder";
				}
				objExplorerTreeview.BackupToFile(strEditMessagebox);
				return;
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

	public static void showKeyboard(Activity activity) {
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
	}

	public static void hideKeyboard(Activity activity) {
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
	}

	public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.getCanonicalPath().equals(targetLocation.getCanonicalPath()))
			return;

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < sourceLocation.listFiles().length; i++) {

				copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
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

	// ------------------- Communications ------------------------------
	
	// Define data structures for use by ExecuteExplorerSync
    public static class clsSyncExplorerCommandNoteData {
    	public String strNoteUuid;
    	public boolean boolIsDeleted = false;
    }
    
    public static class clsSyncExplorerResponseNoteData {
    	public String strNoteUuid;
    	public String strName;
    	public String strOwnerUserUuid;
    	public int intServerInstruction;
    	public String strServerMessage;
    }
    public static class clsSyncExplorerCommandMsg extends clsWebServiceCommand {
        public String strClientUserUuid = "";
        public String strRegistrationId = "";
		public ArrayList<clsSyncExplorerCommandNoteData> objSyncExplorerCommandNoteDatas = new ArrayList<clsSyncExplorerCommandNoteData>();
		public boolean boolIsAutoSyncCommand;
    }

    public static class clsSyncExplorerResponseMsg extends clsWebServiceResponse {
    	public ArrayList<clsSyncExplorerResponseNoteData> objSyncExplorerResponseNoteDatas = new ArrayList<clsSyncExplorerResponseNoteData>();
    }

	public void ExecuteExplorerSync(boolean boolIsAutoSyncCommand, clsWorkerIfServerAlive.OnAliveCheckFinishedListener OnFinishedListener) {
		
		final clsWorkerIfServerAlive.OnAliveCheckFinishedListener objOnFinishedListener = OnFinishedListener;
		
		clsUser  objRegisteredUser = objGroupMembers.GetRegisteredUser();
		if (objRegisteredUser.strUserName.equals(getResources().getString(R.string.unregistered_username))) {
			// Not registered, cannot sync
			Toast.makeText(this, "You need to register first before you can sync", Toast.LENGTH_SHORT).show();
			return;
		}
		String strUrl = objMessaging.GetServerUrl()	+ getResources().getString(R.string.url_explorer_sync);
			
		clsSyncExplorerCommandMsg objCommandMsg = new clsSyncExplorerCommandMsg();
		objCommandMsg.strClientUserUuid = objGroupMembers.objMembersRepository.getStrRegisteredUserUuid();
		objCommandMsg.strRegistrationId = strRegistrationId;
		objCommandMsg.objSyncExplorerCommandNoteDatas = objExplorerTreeview.GetAllSyncNotes(objMessaging);
		objCommandMsg.boolIsAutoSyncCommand = boolIsAutoSyncCommand;
		
		
				
		clsWebServiceCommsAsyncTask objAsyncTask = new clsWebServiceCommsAsyncTask(this, strUrl, objCommandMsg, "Syncing... please wait");
		objAsyncTask.SetOnCompleteListener(new OnCompleteListener() {
			
			@Override
			public void onComplete(JSONObject objJsonResponse) {
				clsSyncExplorerResponseMsg objResult = clsUtils.DeSerializeFromString(objJsonResponse.toString(), clsSyncExplorerResponseMsg.class);
				String strMessage = "";
				boolean boolDisplayResult = false;
				boolean boolIsProblemEncountered = false;
				// Do what needs to be done with the result
				if (objResult.intErrorCode == clsSyncResult.ERROR_NONE) {
					for (clsSyncExplorerResponseNoteData objNoteData: objResult.objSyncExplorerResponseNoteDatas) {
						// Depending on server instructions
						switch (objNoteData.intServerInstruction) {
						case clsMessaging.SERVER_INSTRUCT_KEEP_ORIGINAL:
							// Do nothing
							break;
						case clsMessaging.SERVER_INSTRUCTION_REMOVE:
							clsTreeNode objDeletedTreeNode = objExplorerTreeview.getTreeNodeFromUuid(UUID.fromString(objNoteData.strNoteUuid));
							if (boolDisplayResult) {
								strMessage += "Note '" + objDeletedTreeNode.getName()
										+ "' has been removed.\n";
							}
							if (objDeletedTreeNode != null) {
								objExplorerTreeview.DeleteNoteAndAllImages(objNoteData.strNoteUuid);
								objExplorerTreeview.RemoveTreeNode(objDeletedTreeNode);
							}							
							break;
						case clsMessaging.SERVER_INSTRUCT_REPLACE_ORIGINAL:
							// Do nothing
							break;
						case clsMessaging.SERVER_INSTRUCT_CREATE_NEW_SHARED:							
							clsTreeNode objSharingFolderTreeNode = objExplorerTreeview.GetSharingFolder();
							String strOwnerUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
							if (objSharingFolderTreeNode == null) {
								// Create a sharing folder as it does not exist yet
								objSharingFolderTreeNode = objExplorerTreeview.new clsTreeNode("Shared notes",
										enumItemType.FOLDER_EXPANDED, false, "", clsTreeview.SHARED_FOLDER_RESOURCE, "",
										strOwnerUserUuid, strOwnerUserUuid);
								objExplorerTreeview.getRepository().objRootNodes.add(objSharingFolderTreeNode);
							}
							clsTreeNode objNewNoteTreeNode = objExplorerTreeview.new clsTreeNode(objNoteData.strName,
									enumItemType.OTHER, false, "", clsTreeview.TEXT_RESOURCE, "", objNoteData.strOwnerUserUuid,
									"");
							objNewNoteTreeNode.guidTreeNode = UUID.fromString(objNoteData.strNoteUuid);
							objNewNoteTreeNode.boolIsNew = true;
							objSharingFolderTreeNode.objChildren.add(objNewNoteTreeNode);
							if (boolDisplayResult) {
								strMessage += "Shared note '" + objNewNoteTreeNode.getName()
										+ "' has been added.\n";
							}
							break;
						case clsMessaging.SERVER_INSTRUCT_CREATE_NEW_PUBLISHED:
							clsTreeNode objPublishingFolderTreeNode = objExplorerTreeview.GetPublishingFolder();
							strOwnerUserUuid = ((ActivityExplorerStartup) objContext).objGroupMembers.GetRegisteredUser().strUserUuid;
							if (objPublishingFolderTreeNode == null) {
								// Create a subscription folder as it does not exist yet
								objPublishingFolderTreeNode = objExplorerTreeview.new clsTreeNode("Subscribed notes",
										enumItemType.FOLDER_EXPANDED, false, "", clsTreeview.PUBLISHED_FOLDER_RESOURCE, "",
										strOwnerUserUuid, strOwnerUserUuid);
								objExplorerTreeview.getRepository().objRootNodes.add(objPublishingFolderTreeNode);
							}
							objNewNoteTreeNode = objExplorerTreeview.new clsTreeNode(objNoteData.strName,
									enumItemType.OTHER, false, "", clsTreeview.TEXT_RESOURCE, "", objNoteData.strOwnerUserUuid,
									"");
							objNewNoteTreeNode.guidTreeNode = UUID.fromString(objNoteData.strNoteUuid);
							objNewNoteTreeNode.boolIsNew = true;
							objPublishingFolderTreeNode.objChildren.add(objNewNoteTreeNode);
							if (boolDisplayResult) {
								strMessage += "Subscribed note '" + objNewNoteTreeNode.getName()
										+ "' has been added.\n";
							}
							break;
						case clsMessaging.SERVER_INSTRUCT_PROBLEM:
							if (boolDisplayResult) {
								strMessage += "Note '" + objNoteData.strName + "' had a sync problem. "
										+ objNoteData.strServerMessage + ".\n";
							}
							boolIsProblemEncountered = true;
							break;
						case clsMessaging.SERVER_INSTRUCTION_SERVER_ITEM_REMOVED:
							objDeletedTreeNode = objExplorerTreeview.getTreeNodeFromUuid(UUID.fromString(objNoteData.strNoteUuid));
							if (boolDisplayResult) {
								strMessage += "Note '" + objDeletedTreeNode.getName()
										+ "' has been removed.\n";
							}
							if (objDeletedTreeNode != null) {
								objExplorerTreeview.DeleteNoteAndAllImages(objNoteData.strNoteUuid);
								objExplorerTreeview.RemoveTreeNode(objDeletedTreeNode);
							}		
							break;
						case clsMessaging.SERVER_INSTRUCT_NO_MORE_NOTES:
							if (boolDisplayResult) {
								strMessage += "Sync completed.\n";
							}
							break;
						}												
					}
				} else {
					if (boolDisplayResult) {
						strMessage += objResult.strErrorMessage + ".\n";
					}
					boolIsProblemEncountered = true;
				}
				// Display results
				if (boolDisplayResult) {				
					if(objOnFinishedListener != null) {
						objOnFinishedListener.onAliveCheckFinished(true, "");
					} else {
						clsUtils.MessageBox(objContext, strMessage, false);
					}
				} else if (boolIsProblemEncountered) {					
					if(objOnFinishedListener != null) {
						objOnFinishedListener.onAliveCheckFinished(false, strMessage);
					} else {
						clsUtils.MessageBox(objContext, strMessage, false);
					}
				}

				// Refresh UI
				((ActivityExplorerStartup)objContext).RefreshListView();
				
				clsUtils.IndicateToServiceIntentSyncIsCompleted(objContext);
			}
		});
		
		objAsyncTask.SetOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel() {
				if(objOnFinishedListener != null) {
					objOnFinishedListener.onAliveCheckFinished(true, "Cancelled");
				} else {
					clsUtils.MessageBox(objContext, "Cancelled", true);
				}
				clsUtils.IndicateToServiceIntentSyncIsCompleted(objContext);			
			}
		});
				
		objAsyncTask.execute(null, null, null);		
	}
	
	

	public static class ActivityExplorerSyncMembersAsyncTask extends AsyncTask<String, Void, clsSyncMembersResponseMsg> {

		public static final int SYNC_MEMBERS_CLIENT_INSTRUCT_GET_MEMBERS = 0;
		public static final int SYNC_MEMBERS_CLIENT_INSTRUCT_UPDATE_MEMBERS = 1;

		public static final int ERROR_SYNC_MEMBERS = 7;
		public static final int ERROR_NONE_NO_DATA_RETURNED = 8;
		public static final int ERROR_NONE_NEW_DATA_CREATED = 9;
		public static final int ERROR_NONE_EXISTING_DATA_RETURNED = 10;
		public static final int ERROR_NONE_EXISTING_DATA_UPDATED = 11;

		static Exception mException = null;
		static clsSyncMembersCommandMsg objCommand;
		clsGroupMembers objGroupMembers;
		Activity context;
		clsMessaging objMessaging;
		static URL urlFeed;
		ProgressDialog objProgressDialog;

		public ActivityExplorerSyncMembersAsyncTask(URL urlFeed, Activity context,
				clsSyncMembersCommandMsg objSyncMembersCommandMsg, clsGroupMembers objGroupMembers,
				clsMessaging objMessaging) {
			ActivityExplorerSyncMembersAsyncTask.objCommand = objSyncMembersCommandMsg;
			ActivityExplorerSyncMembersAsyncTask.urlFeed = urlFeed;
			this.context = context;
			this.objGroupMembers = objGroupMembers;
			this.objMessaging = objMessaging;
			objProgressDialog = new ProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			objProgressDialog.setMessage("Processing..., please wait.");
			objProgressDialog.show();
		}

		@Override
		protected clsSyncMembersResponseMsg doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			Gson gson = new Gson();
			JSONObject objJsonResult = null;
			clsSyncMembersResponseMsg objResult = objMessaging.new clsSyncMembersResponseMsg();

			try {
				InputStream stream = null;
				String strJsonCommand = gson.toJson(objCommand);

				try {
					Log.i("myCustom", "Streaming data from network: " + urlFeed);
					stream = clsMessaging.DownloadUrl(urlFeed, strJsonCommand);
					objJsonResult = clsMessaging.UpdateLocalFeedData(stream);
					// Makes sure that the InputStream is closed after the app
					// is
					// finished using it.
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					objResult.setIntErrorNum(ERROR_SYNC_MEMBERS);
					objResult.setStrErrMessage("JSON exception. " + e);
					return objResult;
				} finally {
					if (stream != null) {
						stream.close();
					}
				}
			} catch (MalformedURLException e) {
				objResult.setIntErrorNum(ERROR_SYNC_MEMBERS);
				objResult.setStrErrMessage("JSON exception. " + e);
				return objResult;
			} catch (IOException e) {
				objResult.setIntErrorNum(ERROR_SYNC_MEMBERS);
				objResult.setStrErrMessage("JSON exception. " + e);
				return objResult;
			}
			// Analize data from server
			objResult = gson.fromJson(objJsonResult.toString(), clsSyncMembersResponseMsg.class);
			return objResult;
		}

		@Override
		protected void onPostExecute(clsSyncMembersResponseMsg objResult) {
			// TODO Auto-generated method stub
			super.onPostExecute(objResult);
			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}
			if ((objResult.getIntErrorNum() == ERROR_NONE_NO_DATA_RETURNED)
					|| (objResult.getIntErrorNum() == ERROR_NONE_EXISTING_DATA_RETURNED)) {
				if (objResult.getIntErrorNum() == ERROR_NONE_EXISTING_DATA_RETURNED) {
					clsMembersRepository objMembersRepository = objResult.objSyncMembersRepository.Copy();
					objGroupMembers.objMembersRepository = objMembersRepository;
					objGroupMembers.SaveFile();
				}
				// Now manage the members
				Intent intentManageGroupMembers = new Intent(((ActivityExplorerStartup) context),
						ActivityGroupMembers.class);
				intentManageGroupMembers.putExtra(ActivityGroupMembers.ACTION,
						ActivityGroupMembers.ACTION_MANAGE_GROUPS);
				intentManageGroupMembers.putExtra(ActivityGroupMembers.WEBSERVER_URL,
						objMessaging.GetServerUrl());
				context.startActivityForResult(intentManageGroupMembers, SHARE_MANAGE_GROUP_MEMBERS);
			} else if ((objResult.getIntErrorNum() == ERROR_NONE_NEW_DATA_CREATED)
					|| (objResult.getIntErrorNum() == ERROR_NONE_EXISTING_DATA_UPDATED)) {
				objGroupMembers.SaveFile();
				Toast.makeText(objContext, "Members info successfully saved to server", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(objContext,
						"Could not retrieve members info from server. " + objResult.getStrErrMessage(),
						Toast.LENGTH_LONG).show();
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

	// ------------------------------ In App Billing

	public void SetupInAppBilling() {

		/*
		 * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY (that
		 * you got from the Google Play developer console). This is not your
		 * developer public key, it's the *app-specific* public key.
		 * 
		 * Instead of just storing the entire literal string here embedded in
		 * the program, construct the key at runtime from pieces or use bit
		 * manipulation (for example, XOR with some other string) to hide the
		 * actual key. The key itself is not secret information, but we don't
		 * want to make it easy for an attacker to replace the public key with
		 * one of their own and then fake messages from the server.
		 */

		// Create the helper, passing it our context and the public key to
		// verify signatures with
		Log.d(TAG_IAB, "Creating IAB helper.");
		mHelper = new IabHelper(this, clsUtils.Unscramble(base64EncodedPublicKeyPartScrambled));

		// enable debug logging (for a production application, you should set
		// this to false).
		mHelper.enableDebugLogging(true);

		// Start setup. This is asynchronous and the specified listener
		// will be called once setup completes.
		Log.d(TAG_IAB, "Starting setup.");
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				Log.d(TAG_IAB, "Setup finished.");

				if (!result.isSuccess()) {
					// There was a problem.
					clsUtils.MessageBox(objContext, "Problem setting up in-app billing: " + result, true);
					return;
				}

				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null)
					return;

				// IAB is fully set up. Now, let's get an inventory of stuff we
				// own.
				Log.d(TAG_IAB, "Setup successful. Querying inventory.");
				mHelper.queryInventoryAsync(mGotInventoryListener);
			}
		});
	}

	// Listener that's called when we finish querying the items and
	// subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			Log.d(TAG_IAB, "Query inventory finished.");

			// Have we been disposed of in the meantime? If so, quit.
			if (mHelper == null)
				return;

			// Is it a failure?
			if (result.isFailure()) {
				clsUtils.MessageBox(objContext, "Failed to query inventory: " + result, true);
				return;
			}

			Log.d(TAG_IAB, "Query inventory was successful.");

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

			// Do we have the premium upgrade?
			Purchase objAdvertsDisabledPurchase = inventory.getPurchase(SKU_ADVERTS_REMOVED);
			boolean boolIsAdsDisabled = (objAdvertsDisabledPurchase != null && verifyDeveloperPayload(objAdvertsDisabledPurchase));
			clsIabLocalData objIabLocalData = new clsIabLocalData();
			objIabLocalData.boolIsAdsDisabledA = boolIsAdsDisabled;
			objIabLocalData.boolIsAdsDisabledB = !boolIsAdsDisabled;
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(objContext);
			clsUtils.SaveIabLocalValues(sharedPref, objIabLocalData);

			Log.d(TAG_IAB, "User is " + (boolIsAdsDisabled ? "WITHOUT ADVERTS" : "WITH_ADVERTS"));

			setWaitScreen(false);
			Log.d(TAG_IAB, "Initial inventory query finished; enabling main UI.");
		}
	};

	// Listener that's called when we finish querying the items and
	// subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryForReversalListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			Log.d(TAG_IAB, "Query inventory finished.");

			// Have we been disposed of in the meantime? If so, quit.
			if (mHelper == null)
				return;

			// Is it a failure?
			if (result.isFailure()) {
				clsUtils.MessageBox(objContext, "Failed to query inventory: " + result, true);
				return;
			}

			Log.d(TAG_IAB, "Query inventory was successful.");

			if (inventory.hasPurchase(SKU_ADVERTS_REMOVED)) {
				mHelper.consumeAsync(inventory.getPurchase(SKU_ADVERTS_REMOVED), mConsumeForReversalFinishedListener);
			}

			setWaitScreen(false);
			Log.d(TAG_IAB, "Inventory reversal completed");
		}
	};

	// Called when consumption is complete
	IabHelper.OnConsumeFinishedListener mConsumeForReversalFinishedListener = new IabHelper.OnConsumeFinishedListener() {
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			Log.d(ActivityExplorerStartup.TAG_IAB, "Consumption finished. Purchase: " + purchase + ", result: "
					+ result);

			// if we were disposed of in the meantime, quit.
			if (mHelper == null)
				return;

			// We know this is the "remove_adverts" sku because it's the only
			// one we consume,
			// so we don't check which sku was consumed. If you have more than
			// one
			// sku, you probably should check...
			if (result.isSuccess()) {
				// successfully consumed, so we apply the effects of the item in
				// our
				// world's logic, which in our case means updating the local
				// values
				SharedPreferences objSharedPreferences = PreferenceManager.getDefaultSharedPreferences(objContext);
				objIabLocalData.boolIsAdsDisabledA = false;
				objIabLocalData.boolIsAdsDisabledB = true;
				clsUtils.SaveIabLocalValues(objSharedPreferences, objIabLocalData);
				clsUtils.MessageBox(objContext, "You have successfully added adverts again", true);
			} else {
				clsUtils.MessageBox(objContext, "Error while consuming: " + result, true);
			}
			setWaitScreen(false);
			Log.d(ActivityExplorerStartup.TAG_IAB, "End of consumption flow.");
		}
	};

	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();

		/*
		 * TODO: verify that the developer payload of the purchase is correct.
		 * It will be the same one that you sent when initiating the purchase.
		 * 
		 * WARNING: Locally generating a random string when starting a purchase
		 * and verifying it here might seem like a good approach, but this will
		 * fail in the case where the user purchases an item on one device and
		 * then uses your app on a different device, because on the other device
		 * you will not have access to the random string you originally
		 * generated.
		 * 
		 * So a good developer payload has these characteristics:
		 * 
		 * 1. If two different users purchase an item, the payload is different
		 * between them, so that one user's purchase can't be replayed to
		 * another user.
		 * 
		 * 2. The payload must be such that you can verify it even when the app
		 * wasn't the one who initiated the purchase flow (so that items
		 * purchased by the user on one device work on other devices owned by
		 * the user).
		 * 
		 * Using your own server to store and verify developer payloads across
		 * app installations is recommended.
		 */

		return true;
	}

	// Enables or disables the "please wait" screen.
	ProgressDialog objProgressDialog;

	void setWaitScreen(boolean set) {
		if (objProgressDialog == null) {
			objProgressDialog = new ProgressDialog(this);
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

	// ------------------- GCM ------------------------------------
	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */

	// As per http://developer.android.com/google/gcm/client.html#app

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG_IAB, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(objContext);
					}
					strRegistrationId = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + strRegistrationId;

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device
					// will send upstream messages to a server that echo back
					// the
					// message using the 'from' address in the message.

					// Persist the regID - no need to register again.
					clsUtils.storeRegistrationId(objContext, strRegistrationId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				// TODO Auto-generated method stub
				super.onPostExecute(msg);
				clsUtils.MessageBox(objContext, msg, true);
			}
		}.execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app.
	 */

	class clsSendGcmRegIdCommandMessage {
		String strRegId;
		String strClientUserUuid;
	}

	class clsSendGcmRegIdResponse {
		public int intSeqNum;
		public int intErrorCode;
		public String strErrorMessage = "";
	}

	private void sendRegistrationIdToBackend() {

		new AsyncTask<Void, Void, clsSendGcmRegIdResponse>() {

			@Override
			protected clsSendGcmRegIdResponse doInBackground(Void... params) {
				Gson gson = new Gson();
				JSONObject objJsonResult = null;
				InputStream stream = null;
				clsSendGcmRegIdCommandMessage objCommand = new clsSendGcmRegIdCommandMessage();
				clsSendGcmRegIdResponse objResponse = new clsSendGcmRegIdResponse();
				objCommand.strRegId = strRegistrationId;
				objCommand.strClientUserUuid = objGroupMembers.GetRegisteredUser().strUserUuid;
				try {

					URL urlFeed = new URL(objMessaging.GetServerUrl()
							+ getResources().getString(R.string.url_send_gcm_reg_id));
					String strJsonCommand = gson.toJson(objCommand);
					try {
						Log.i("myCustom", "Sending GCM Reg ID to server: " + urlFeed);
						stream = clsMessaging.DownloadUrl(urlFeed, strJsonCommand);
						objJsonResult = clsMessaging.UpdateLocalFeedData(stream);
						// Makes sure that the InputStream is closed after the
						// app is
						// finished using it.
					} catch (IOException e) {
						Log.e("myCustom", "Error reading from network: " + e.toString());
						objResponse.intErrorCode = clsMessaging.ERROR_NETWORK;
						objResponse.strErrorMessage = "Error reading from network: " + e.toString();
						return objResponse;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						Log.wtf("myCustom", "JSON exception", e);
						objResponse.intErrorCode = clsMessaging.ERROR_NETWORK;
						objResponse.strErrorMessage = "JSON exception: " + e.toString();
						return objResponse;
					} finally {
						if (stream != null) {
							stream.close();
						}
					}
				} catch (MalformedURLException e) {
					Log.wtf("myCustom", "Feed URL is malformed", e);
					objResponse.intErrorCode = clsMessaging.ERROR_NETWORK;
					objResponse.strErrorMessage = "Feed URL is malformed: " + e.toString();
					return objResponse;
				} catch (IOException e) {
					Log.e("myCustom", "Error reading from network: " + e.toString());
					objResponse.intErrorCode = clsMessaging.ERROR_NETWORK;
					objResponse.strErrorMessage = "Error reading from network: " + e.toString();
					return objResponse;
				}
				Log.i("myCustom", "Network synchronization complete");
				objResponse = gson.fromJson(objJsonResult.toString(), clsSendGcmRegIdResponse.class);
				return objResponse;

			}

			@Override
			protected void onPostExecute(clsSendGcmRegIdResponse objResponse) {
				// TODO Auto-generated method stub
				super.onPostExecute(objResponse);
				if (objResponse.intErrorCode != 0) {
					clsUtils.MessageBox(objContext, objResponse.strErrorMessage, true);
				}
			}

		}.execute(null, null, null);
	}
	
	
	// ------------------------ Shared Users Management -------------------------------------
	// ------------------------ Get Shared Users for specific note from server and let user add/edit/remove
	
	// Input to async task
	public static class clsGetNoteSharedUsersCommand {
		public String strNoteUuid;
	}

	// Output from async task
	public static class clsGetNoteSharedUsersResponse {
		public static final int ERROR_NONE = 0;
		public static final int ERROR_NETWORK = 1;
		public int intErrorCode;
		public String strErrorMessage = "";
	
		public String strNoteUuid;
		public ArrayList<String> strSharedUsers;
	}

	public static class clsGetNoteSharedUsersAsyncTask extends AsyncTask<Void, Void, clsGetNoteSharedUsersResponse> {
		static clsGetNoteSharedUsersCommand objCommand;
		static clsGetNoteSharedUsersResponse objResponse;
		static URL urlFeed;
		ProgressDialog objProgressDialog;
		public OnGetNoteSharedUsersResponseListener objOnResponseListener;

		public clsGetNoteSharedUsersAsyncTask(Activity objActivity, URL urlFeed,
				clsGetNoteSharedUsersCommand objCommand, clsGetNoteSharedUsersResponse objResponse) {
			clsGetNoteSharedUsersAsyncTask.urlFeed = urlFeed;
			clsGetNoteSharedUsersAsyncTask.objCommand = objCommand;
			clsGetNoteSharedUsersAsyncTask.objResponse = objResponse;
			objProgressDialog = new ProgressDialog(objActivity);
		}

		public void SetOnResponseListener(OnGetNoteSharedUsersResponseListener objOnResponseListener) {
			this.objOnResponseListener = objOnResponseListener;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			objProgressDialog.setMessage("Processing..., please wait.");
			objProgressDialog.show();
		}

		@Override
		protected clsGetNoteSharedUsersResponse doInBackground(Void... arg0) {
			Gson gson = new Gson();
			JSONObject objJsonResult = null;

			try {
				InputStream stream = null;
				String strJsonCommand = gson.toJson(objCommand);

				try {
					stream = clsMessaging.DownloadUrl(urlFeed, strJsonCommand);
					objJsonResult = clsMessaging.UpdateLocalFeedData(stream);
					// Makes sure that the InputStream is closed after finished
					// using it.
				} catch (JSONException e) {
					objResponse.intErrorCode = clsExportNoteAsWebPageResponse.ERROR_NETWORK;
					objResponse.strErrorMessage = "JSON exception. " + e;
					return objResponse;
				} finally {
					if (stream != null) {
						stream.close();
					}
				}
			} catch (MalformedURLException e) {
				objResponse.intErrorCode = clsExportNoteAsWebPageResponse.ERROR_NETWORK;
				objResponse.strErrorMessage = "Feed URL is malformed. " + e;
				return objResponse;
			} catch (IOException e) {
				objResponse.intErrorCode = clsExportNoteAsWebPageResponse.ERROR_NETWORK;
				objResponse.strErrorMessage = "IO Exception from network. " + e;
				return objResponse;
			}
			// Analyze data from server
			objResponse = gson.fromJson(objJsonResult.toString(), clsGetNoteSharedUsersResponse.class);
			return objResponse;
		}

		@Override
		protected void onPostExecute(clsGetNoteSharedUsersResponse objResponse) {
			super.onPostExecute(objResponse);
			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}
			objOnResponseListener.onResponse(objResponse);
		}

		public interface OnGetNoteSharedUsersResponseListener {
			public void onResponse(clsGetNoteSharedUsersResponse objResponse);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}
		}

	}
	
	// ------------------------ Set selected Shared Users for specific note on server
	
		// Input to async task
		public static class clsSetNoteSharedUsersCommand {
			public String strClientUserUuid;
			public String strNoteUuid;
			public ArrayList<String> objSharedUsers;
		}

		// Output from async task
		public static class clsSetNoteSharedUsersResponse {
			public static final int ERROR_NONE = 0;
			public static final int ERROR_NETWORK = 1;
			public int intErrorCode;
			public String strErrorMessage = "";
		}

		public static class clsSetNoteSharedUsersAsyncTask extends AsyncTask<Void, Void, clsSetNoteSharedUsersResponse> {
			static clsSetNoteSharedUsersCommand objCommand;
			static clsSetNoteSharedUsersResponse objResponse;
			static URL urlFeed;
			ProgressDialog objProgressDialog;
			public OnSetNoteSharedUsersResponseListener objOnResponseListener;

			public clsSetNoteSharedUsersAsyncTask(Activity objActivity, URL urlFeed,
					clsSetNoteSharedUsersCommand objCommand, clsSetNoteSharedUsersResponse objResponse) {
				clsSetNoteSharedUsersAsyncTask.urlFeed = urlFeed;
				clsSetNoteSharedUsersAsyncTask.objCommand = objCommand;
				clsSetNoteSharedUsersAsyncTask.objResponse = objResponse;
				objProgressDialog = new ProgressDialog(objActivity);
			}

			public void SetOnResponseListener(OnSetNoteSharedUsersResponseListener objOnResponseListener) {
				this.objOnResponseListener = objOnResponseListener;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				objProgressDialog.setMessage("Processing..., please wait.");
				objProgressDialog.show();
			}

			@Override
			protected clsSetNoteSharedUsersResponse doInBackground(Void... arg0) {
				Gson gson = new Gson();
				JSONObject objJsonResult = null;

				try {
					InputStream stream = null;
					String strJsonCommand = gson.toJson(objCommand);

					try {
						stream = clsMessaging.DownloadUrl(urlFeed, strJsonCommand);
						objJsonResult = clsMessaging.UpdateLocalFeedData(stream);
						// Makes sure that the InputStream is closed after finished
						// using it.
					} catch (JSONException e) {
						objResponse.intErrorCode = clsExportNoteAsWebPageResponse.ERROR_NETWORK;
						objResponse.strErrorMessage = "JSON exception. " + e;
						return objResponse;
					} finally {
						if (stream != null) {
							stream.close();
						}
					}
				} catch (MalformedURLException e) {
					objResponse.intErrorCode = clsExportNoteAsWebPageResponse.ERROR_NETWORK;
					objResponse.strErrorMessage = "Feed URL is malformed. " + e;
					return objResponse;
				} catch (IOException e) {
					objResponse.intErrorCode = clsExportNoteAsWebPageResponse.ERROR_NETWORK;
					objResponse.strErrorMessage = "IO Exception from network. " + e;
					return objResponse;
				}
				// Analyze data from server
				objResponse = gson.fromJson(objJsonResult.toString(), clsSetNoteSharedUsersResponse.class);
				return objResponse;
			}

			@Override
			protected void onPostExecute(clsSetNoteSharedUsersResponse objResponse) {
				super.onPostExecute(objResponse);
				if (objProgressDialog.isShowing()) {
					objProgressDialog.dismiss();
				}
				objOnResponseListener.onResponse(objResponse);
			}

			public interface OnSetNoteSharedUsersResponseListener {
				public void onResponse(clsSetNoteSharedUsersResponse objResponse);
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
