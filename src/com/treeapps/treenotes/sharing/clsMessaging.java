package com.treeapps.treenotes.sharing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;




















import com.google.gson.Gson;
import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsExplorerTreeview;
import com.treeapps.treenotes.clsResourceLoader;
import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.clsTreeview.clsRepository;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.ActivityNoteAddNew.RadioGroupOnCheckedChangeListener;
import com.treeapps.treenotes.clsTreeview.clsSyncRepository;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage;
import com.treeapps.treenotes.sharing.clsGroupMembers.clsSyncMembersRepository;
import com.treeapps.treenotes.sharing.clsGroupMembers.clsUser;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData.clsImageToBeDownLoadedData;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData.clsImageToBeUploadedData;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData.clsImageToBeUploadedConfigData;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageUpDownloadResult.clsError;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommand;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommsAsyncTask;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommsAsyncTask.OnCancelListener;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceCommsAsyncTask.OnCompleteListener;
import com.treeapps.treenotes.sharing.clsWebServiceComms.clsWebServiceResponse;
import com.treeapps.treenotes.sharing.subscriptions.ActivitySubscriptions.ActivitySubscriptionsAddAsyncTask;
import com.treeapps.treenotes.sharing.subscriptions.ActivitySubscriptions.clsSubscriptionsAddCommandMsg;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class clsMessaging {
	
	private static final int NET_CONNECT_TIMEOUT_MILLIS = 10000;  // 10000 10 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 300000;  // 15 seconds
    
    public static String SERVER_URL_AZURE = "http://treenotes.azurewebsites.net";
    public static String SERVER_URL_IIS_EXPRESS = "http://10.0.0.14";
	
    // Webserver instructions
    public static final int SERVER_INSTRUCT_KEEP_ORIGINAL = 0;
    public static final int SERVER_INSTRUCT_REPLACE_ORIGINAL = 1;
    public static final int SERVER_INSTRUCT_CREATE_NEW_SHARED = 2;
    public static final int SERVER_INSTRUCT_NO_MORE_NOTES = 3;
    public static final int SERVER_INSTRUCT_PROBLEM = 4;
    public static final int SERVER_INSTRUCT_NO_PROBLEM = 5;
    public static final int SERVER_INSTRUCTION_REMOVE = 6;
    public static final int SERVER_INSTRUCT_CREATE_NEW_PUBLISHED = 7;
    public static final int SERVER_INSTRUCTION_SERVER_ITEM_REMOVED = 8;
	

    //Client instructions
    public static int SYNC_MEMBERS_CLIENT_INSTRUCT_GET_MEMBERS = 0;
	public static int SYNC_MEMBERS_CLIENT_INSTRUCT_UPDATE_MEMBERS = 1;

    // Webserver item types
    public static  int SERVER_ITEMTYPE_FOLDER_EXPANDED = 0;
    public static  int SERVER_ITEMTYPE_FOLDER_COLLAPSE = 1;
    public static  int SERVER_ITEMTYPE_FOLDER_EMPTY = 2;
    public static  int SERVER_ITEMTYPE_OTHER = 3;

    // Webserver error types
    public static int ERROR_NONE = 0;
    public static int ERROR_ADD_USER = 1;
    public static int ERROR_DBASE = 2;
    public static int ERROR_REMOVE_USER = 3;
    public static int ERROR_LOGIN_USER = 4;
    public static int ERROR_GET_USERS = 5;
    public static int ERROR_NONE_USER_EXISTS = 6;
    public static int ERROR_SYNC_MEMBERS = 7;
    public static int ERROR_NONE_NO_DATA_RETURNED = 8;
    public static int ERROR_NONE_NEW_DATA_CREATED = 9;
    public static int ERROR_NONE_EXISTING_DATA_RETURNED = 10;
    public static int ERROR_NONE_EXISTING_DATA_UPDATED = 11;
    public static int ERROR_REMOVE_PUBLICATIONS = 12;
    public static int ERROR_IS_SERVER_ALIVE = 13;
    public static int ERROR_ADD_PUBLICATIONS = 14;
    public static int ERROR_UPLOAD_IMAGE = 15;
    public static int ERROR_DOWNLOAD_IMAGE = 16;
    public static int ERROR_NETWORK = 17;

	// Persistent items
	public class clsRepository {
		public boolean boolIsServerIisExpress = true;	
	}
	public clsRepository objRepository = new clsRepository();
	// End of persistence items
	
	public static class clsImageLoadData {
		public String strNoteUuid;
		public ArrayList<clsImageToBeUploadedData> objImageToBeUploadedDatas = new ArrayList<clsImageToBeUploadedData>();
		public ArrayList<clsImageToBeDownLoadedData> objImageToBeDownLoadedDatas = new ArrayList<clsImageToBeDownLoadedData>(); 
		
		public class clsImageToBeDownLoadedData {
			public String strImageUuid;
			public boolean boolIsAnnotatedImageToBeDownloaded;
		}
		
		public class clsImageToBeUploadedData {
			public String strUuid;
			public int intResourceId;
			public String strResourcePath;
		}
		
		public static class clsImageToBeUploadedConfigData {
			public String strImageUuid;
			public String strImageFilenameWithExt;
			public String strDateLastModified;
		}
		
		public clsImageToBeUploadedData GetItem(String strNodeUuid) throws Exception {
			for (clsImageToBeUploadedData clsImageToBeUploadedData: objImageToBeUploadedDatas) {
				if (clsImageToBeUploadedData.strUuid == strNodeUuid) {
					return clsImageToBeUploadedData;
				}
			}
			throw new Exception("Server and Client item image ID does not tie up");
		}
		
		public void AddToBeDownloadedImage (String strImageUuid, boolean boolIsAnnotatedImageToBeDownloaded) {
			clsImageToBeDownLoadedData objImageToBeDownLoadedData = new clsImageToBeDownLoadedData();
			objImageToBeDownLoadedData.strImageUuid = strImageUuid;
			objImageToBeDownLoadedData.boolIsAnnotatedImageToBeDownloaded = boolIsAnnotatedImageToBeDownloaded;
			objImageToBeDownLoadedDatas.add(objImageToBeDownLoadedData);
		}
	}
	

	public boolean IsImageLoadDatasEmpty(ArrayList<clsImageLoadData> objImageLoadDatas ) {
		for(clsImageLoadData objImageLoadData: objImageLoadDatas) {
			if (objImageLoadData.objImageToBeDownLoadedDatas.size() !=0 || 
				objImageLoadData.objImageToBeUploadedDatas.size() !=0 ) {
				return false;
			}
		}
		return true;
	}
		
	public boolean getBoolIsServerIisExpress() {
		return objRepository.boolIsServerIisExpress;
	}

	public void setBoolIsServerIisExpress(boolean boolIsServerIisExpress) {
		objRepository.boolIsServerIisExpress = boolIsServerIisExpress;
	}
	

	
	
	public void ClearWebServiceRepository (Activity objContext) {
		URL urlFeed = null;
		String strServerUrl;
		try {
			if(objRepository.boolIsServerIisExpress) {
				strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
			}
			else {
				strServerUrl =  clsMessaging.SERVER_URL_AZURE;
			}
			urlFeed = new URL(strServerUrl + objContext.getResources().getString(R.string.url_clear_webservice_repository));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		class clsClearWebServiceRepositoryAsyncTask  extends WebServiceAsyncTask {

			private Activity objContext;
			public clsClearWebServiceRepositoryAsyncTask(Activity objContext, URL urlFeed, String strJsonCommand) {
				super(objContext, false, urlFeed, strJsonCommand);
				this.objContext = objContext;
			}
			
			@Override
		    protected void onPostExecute(JSONObject result)
		    {
		        super.onPostExecute(result);
		        if (result == null)  {
		        	clsUtils.CustomLog("WebService is unavailable");
		        	Toast.makeText(objContext, "WebService is unavailable", Toast.LENGTH_SHORT).show();
		        	return;
		        }
		        clsClearWebServiceRepositoryResponseMsg objResponseMsg = clsUtils.DeSerializeFromString(result.toString(), clsClearWebServiceRepositoryResponseMsg.class);
		        if (objResponseMsg.intErrorCode == ERROR_NONE) {
		        	clsUtils.CustomLog("WebService repository has been successfully cleared");
		        	Toast.makeText(objContext, "WebService repository has been successfully cleared", Toast.LENGTH_SHORT).show();
		        } else {
		        	clsUtils.CustomLog("Unable to clear. " + objResponseMsg.strErrorMessage);
		        	Toast.makeText(objContext, "Unable to clear. " + objResponseMsg.strErrorMessage, Toast.LENGTH_SHORT).show();
		        } 			        
		    }
		}
		clsClearWebServiceRepositoryCommandMsg objCommandMsg = new clsClearWebServiceRepositoryCommandMsg();
		String strJsonCommand = clsUtils.SerializeToString(objCommandMsg);
		clsClearWebServiceRepositoryAsyncTask objAsyncTask = new clsClearWebServiceRepositoryAsyncTask(objContext, urlFeed, strJsonCommand);
		objAsyncTask.execute("");	    		

	}
	
	
	
	 public static void SetServerAlive(Activity objActivity, boolean boolIsServerAlive) {		 
		 clsUtils.SerializeToSharedPreferences("clsWorkerIfServerAlive", "IsServerAlive", objActivity, boolIsServerAlive);
	 }
	
	 public static boolean IsServerAlive(Activity objActivity) {
		 
		 boolean boolIsServerAlive = clsUtils.DeSerializeFromSharedPreferences("clsWorkerIfServerAlive", "IsServerAlive", objActivity, boolean.class);
		 return boolIsServerAlive;
	}
	 
	 public boolean IsPossibleToSync(Activity objActivity, clsGroupMembers objGroupMembers) {
		 return true;
	 }
	

	
	// ------------ Client Server Contract
    
	public class clsMsg
    {
		public int intSeqNum;
		public int intErrorCode;
		public String strErrorMessage = "";
        
		public int getIntSeqNum() {
			return intSeqNum;
		}
		public void setIntSeqNum(int intSeqNum) {
			this.intSeqNum = intSeqNum;
		}
		public int getIntErrorNum() {
			return intErrorCode;
		}
		public void setIntErrorNum(int intErrorNum) {
			this.intErrorCode = intErrorNum;
		}
		public String getStrErrMessage() {
			return strErrorMessage;
		}
		public void setStrErrMessage(String strErrMessage) {
			this.strErrorMessage = strErrMessage;
		}
    }
	
	public class clsIsServerAliveCommandMsg extends clsMsg {}
	
	public class clsIsServerAliveResponseMsg extends clsMsg {}
	
	public class clsClearWebServiceRepositoryCommandMsg extends clsMsg {}
	
	public class clsClearWebServiceRepositoryResponseMsg extends clsMsg {}
	
	 public class clsLoginUserCmd  extends clsMsg
    {
        private String strUsername = "";
        private String strPassword = "";
        private String strRegistrationId = "";
        
		public String getStrRegistrationId() {
			return strRegistrationId;
		}
		public void setStrRegistrationId(String strRegistrationId) {
			this.strRegistrationId = strRegistrationId;
		}
		public String getStrUsername() {
			return strUsername;
		}
		public void setStrUsername(String strUsername) {
			this.strUsername = strUsername;
		}
		public String getStrPassword() {
			return strPassword;
		}
		public void setStrPassword(String strPassword) {
			this.strPassword = strPassword;
		}
    }
	 
	 public class clsLoginUserResponse extends clsMsg
    {
		 private String strUserUuid = "";

		public String getStrUserUuid() {
			return strUserUuid;
		}

		public void setStrUserUuid(String strUserUuid) {
			this.strUserUuid = strUserUuid;
		}
    }

    
    public class clsRemoveUserCmd  extends clsMsg
    {
    	private String strUserUuid = "";

		public String getStrUserUid() {
			return strUserUuid;
		}

		public void setStrUserUid(String strUserUid) {
			this.strUserUuid = strUserUid;
		}
		
    }

    public class clsRemoveUserResponse extends clsMsg{}
    
    public class clsMsgUser{
    	private String strUserName = "";
    	private String strUserUuid = "";
    	
		public String getStrUserName() {
			return strUserName;
		}
		public void setStrUserName(String strUserName) {
			this.strUserName = strUserName;
		}
		public String getStrUserUuid() {
			return strUserUuid;
		}
		public void setStrUserUuid(String strUserUuid) {
			this.strUserUuid = strUserUuid;
		}
    }

    @SuppressWarnings("serial")
	public class clsMsgUsers extends ArrayList<clsMsgUser>{}
    
    public class clsGetUsersCmd extends clsMsg
    {
        public String strSearchTerm = "";

		public String getStrSearchTerm() {
			return strSearchTerm;
		}

		public void setStrSearchTerm(String strSearchTerm) {
			this.strSearchTerm = strSearchTerm;
		}
    }

    public class clsGetUsersResponse extends clsMsg
    {
        public ArrayList<clsMsgUser> objMsgUsers = new ArrayList<clsMsgUser>();

    }
    
    public class clsSyncMembersCommandMsg extends clsMsg {
    	public int intClientInstruction;
        public String strClientUserUuid = "";
        public clsSyncMembersRepository objSyncMembersRepository;
    }
    
    public class clsSyncMembersResponseMsg extends clsMsg {
    	public clsSyncMembersRepository objSyncMembersRepository;
    	public String strServerMessage = "";
    }
    
    
 // ------------ End of Client Server Contract
    
   
    
    
    
// ------------- Protocol
    public static class WebServiceAsyncTask extends AsyncTask<String, Void, JSONObject>
	{
		static Exception mException = null;
		static String strJsonCommand = "";
		static URL urlFeed;
		ProgressDialog objProgressDialog;
		static boolean boolDisplayProgress = true;
		
		public WebServiceAsyncTask (Activity objActivity, boolean boolDisplayProgress, URL urlFeed, String strJsonCommand) {
			WebServiceAsyncTask.strJsonCommand = strJsonCommand;
			WebServiceAsyncTask.urlFeed = urlFeed;
			objProgressDialog = new ProgressDialog(objActivity);
			WebServiceAsyncTask.boolDisplayProgress = boolDisplayProgress;
		}
		
		
		@Override
	    protected void onPreExecute()
	    {
	        super.onPreExecute();
	        mException = null;
	        objProgressDialog.setMessage("Processing..., please wait.");
	        if (boolDisplayProgress) {
		        objProgressDialog.show();
	        }
	    }

		@Override
		protected JSONObject doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			JSONObject object = null;
			try {
	            InputStream stream = null;

	            try {
	                Log.i("myCustom", "Streaming data from network: " + urlFeed);
	                stream = DownloadUrl(urlFeed, strJsonCommand);
	                object = UpdateLocalFeedData(stream);
	                // Makes sure that the InputStream is closed after the app is
	                // finished using it.
	            } catch (IOException e) {
		            Log.e("myCustom", "Error reading from network: " + e.toString());
		            return null;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.wtf("myCustom", "JSON exception", e);
		            return null;
				} finally {
	                if (stream != null) {
	                    stream.close();
	                }
	            }
	        } catch (MalformedURLException e) {
	            Log.wtf("myCustom", "Feed URL is malformed", e);
	            return null;
	        } catch (IOException e) {
	            Log.e("myCustom", "Error reading from network: " + e.toString());
	            return null;
	        }
	        Log.i("myCustom", "Network synchronization complete");
	        return object;
		}
		
		@Override
	    protected void onPostExecute(JSONObject result)
	    {
	        super.onPostExecute(result);
	        
	        if (objProgressDialog.isShowing()) {
	        	objProgressDialog.dismiss();
	        }

	        if (WebServiceAsyncTask.mException != null) {
	        	clsUtils.CustomLog(WebServiceAsyncTask.mException.toString() + ". " + "Error # WebServiceAsyncTask");	
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
  
    

    
    
   	
 // -------------- ImageUpDownloadAsyncTask  
    
    public class clsUploadImageFileCommandMsg extends clsMsg {
    	public  String strImageUuid;
    	public  String strImageLocalFullPathName;
    	public String strModificationDate;
    }
    
    public class clsUploadImageFileResponseMsg extends clsMsg {}
    
    public class clsDownloadImageFileCommandMsg extends clsMsg {
    	public  String strImageUuid;
    	public String strFileType; // Either "", "_full" or "_annotated"
    	public  String strFileExtentionWithoutDot;
    	public  String strImageLocalFullPathName;
    }
    
    public class clsDownloadImageFileResponseMsg extends clsMsg {}
    
   	class clsImageUpDownloadResult {
   		ArrayList<clsError> strUploadErrors = new ArrayList<clsError>();
   		ArrayList<clsError> strDownloadErrors = new ArrayList<clsError>();
   		ArrayList<String> strGeneralErrors = new ArrayList<String>();
   		
   		class clsError {
   			public String strNoteUuid;
   			public String strDescription;
   		}
   	}
   	
   	static class clsInstructUploadCompleteCommand {
   		public String strRegistrationId;
   		public boolean boolIsNeedForServerToSendNotifications;
   	}
   	
   	static class clsInstructUploadCompleteResponse extends clsWebServiceResponse {}
   	
   	
    public static class clsImageUpDownloadAsyncTask extends AsyncTask<Void, String, Void> {

    	static Activity objActivity;
    	static clsMessaging objMessaging;
   		static ArrayList<clsImageLoadData> objImageLoadDatas;
   		static clsImageUpDownloadResult objImageUpDownloadResult;
   		ProgressDialog objProgressDialog;
   		static boolean boolDisplayProgress = true;
   		static String strUploadUrl;
   		static String strDownloadUrl;
   		static String strInstructUploadCompleteUrl;

   		static String strResourceLoaderSaveFileFullFilename = "";
   		static boolean boolLoadTaskCompleted = false;
   		static String strLoadTaskCompletedStatus = "";
   		static boolean boolIsNeedForServerToSendNotificationsAtEnd;
   		
   		private clsExportNoteAsWebPage.OnImageUploadFinishedListener callbackFinished;
   		private clsExportNoteAsWebPage.OnImageUploadProgressListener callbackProgress;
    	
    	public clsImageUpDownloadAsyncTask (Activity objActivity, clsMessaging objMessaging, boolean boolDisplayProgress, 
    			ArrayList<clsImageLoadData> objImageLoadDatas, 
    			clsExportNoteAsWebPage.OnImageUploadFinishedListener cbFinished,
    			clsExportNoteAsWebPage.OnImageUploadProgressListener cbProgress,
    			boolean boolIsNeedForServerToSendNotificationsAtEnd) 
    	{
    		clsImageUpDownloadAsyncTask.objActivity = objActivity;
    		clsImageUpDownloadAsyncTask.objImageLoadDatas = objImageLoadDatas;
    		clsImageUpDownloadAsyncTask.objImageUpDownloadResult = objMessaging.new clsImageUpDownloadResult();
    		clsImageUpDownloadAsyncTask.boolDisplayProgress = boolDisplayProgress;
    		clsImageUpDownloadAsyncTask.objMessaging = objMessaging;
    		objProgressDialog = new ProgressDialog(objActivity);
    		String strServerUrl;
    		if(objMessaging.objRepository.boolIsServerIisExpress) {
				strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
			}
			else {
				strServerUrl =  clsMessaging.SERVER_URL_AZURE;
			}
			strUploadUrl = strServerUrl + objActivity.getResources().getString(R.string.url_upload_image_file);
			strDownloadUrl = strServerUrl + objActivity.getResources().getString(R.string.url_download_image_file);	
			strInstructUploadCompleteUrl = strServerUrl + objActivity.getResources().getString(R.string.url_instruct_uploads_complete);
			callbackFinished = cbFinished;
			callbackProgress = cbProgress;
			clsImageUpDownloadAsyncTask.boolIsNeedForServerToSendNotificationsAtEnd = boolIsNeedForServerToSendNotificationsAtEnd;
		}
    	
    	@Override
   	    protected void onPreExecute()
   	    {
   	        super.onPreExecute();
   	        objProgressDialog.setMessage("Synching note images ..., please wait.");
   	        if (boolDisplayProgress) {
   	        	if (!objProgressDialog.isShowing()) {
   	   		        objProgressDialog.show();
   	        	}
   	        }
   	    }
    	
    	@Override
    	protected void onProgressUpdate(String... values) {
    		if (boolDisplayProgress) {
   	        	if (objProgressDialog.isShowing()) {
   	   		        objProgressDialog.setMessage(values[0]);
   	        	}
   	        }
    	}
    	
    	@Override
		protected Void doInBackground(Void... arg0) {
    		
    		clsUtils.CustomLog("UpDownLoadDoInBackground running");
    		
			for (clsImageLoadData objImageLoadData: objImageLoadDatas) {
				
				int countUploads = 0;
				// Uploads
				for(clsImageToBeUploadedData objImageToBeUploadedData: objImageLoadData.objImageToBeUploadedDatas) {
					
					if(callbackProgress != null)
					{
						callbackProgress.imageUploadProgress(++countUploads, objImageLoadData.objImageToBeUploadedDatas.size());
					}
					
					publishProgress("Uploading image (" + 
							(objImageLoadData.objImageToBeUploadedDatas.indexOf(objImageToBeUploadedData) + 1) +
							" of " + objImageLoadData.objImageToBeUploadedDatas.size() + ")");

					
					// See if the required image to be uploaded actually exists locally
					ArrayList<clsImageToBeUploadedConfigData> objImagesToBeUploadedFileDatas = new ArrayList<clsImageToBeUploadedConfigData>();
					String strReturnMsg = EnsureUploadImageVersionsExists(objImageToBeUploadedData, objImagesToBeUploadedFileDatas);
					if (!strReturnMsg.isEmpty()) {
						// Nope, so error
						clsImageUpDownloadResult.clsError objError = objImageUpDownloadResult.new clsError();
						objError.strNoteUuid = objImageToBeUploadedData.strUuid;
						objError.strDescription = strReturnMsg;
						objImageUpDownloadResult.strUploadErrors.add(objError);
					} else {
						// Yes, so continue							
						// Do the actual image upload
						for (clsImageToBeUploadedConfigData objImageToBeUploadedLocalData: objImagesToBeUploadedFileDatas ) {
							// Run through each file type and upload
							clsUploadImageFileCommandMsg objUploadCommand = clsImageUpDownloadAsyncTask.objMessaging.new clsUploadImageFileCommandMsg();
							objUploadCommand.strImageLocalFullPathName = clsUtils.GetTreeNotesDirectoryName(objActivity) + "/" + objImageToBeUploadedLocalData.strImageFilenameWithExt;
							objUploadCommand.strImageUuid = objImageToBeUploadedLocalData.strImageUuid;
							objUploadCommand.strModificationDate = objImageToBeUploadedLocalData.strDateLastModified;
							String strUploadReturn = UploadImageToServer(strUploadUrl, objUploadCommand);
							if (!strUploadReturn.isEmpty()) {
								clsImageUpDownloadResult.clsError objError = objImageUpDownloadResult.new clsError();
								objError.strNoteUuid = objImageToBeUploadedLocalData.strImageFilenameWithExt;
								objError.strDescription = strUploadReturn;
								objImageUpDownloadResult.strUploadErrors.add(objError);
							}						
						} 
					}				
				}
				// Indicate to server uploads are complete, so as to start with notifications to other sharers
				clsInstructUploadCompleteCommand objInstructUploadCompleteCommand = new clsInstructUploadCompleteCommand();
				objInstructUploadCompleteCommand.strRegistrationId = clsUtils.getRegistrationId(objActivity);
				objInstructUploadCompleteCommand.boolIsNeedForServerToSendNotifications = boolIsNeedForServerToSendNotificationsAtEnd;
				clsInstructUploadCompleteResponse objInstructUploadCompleteResponse = InstructUploadComplete(strInstructUploadCompleteUrl, 
						objInstructUploadCompleteCommand);
				if (objInstructUploadCompleteResponse.intErrorCode != clsMessaging.ERROR_NONE) {
					objImageUpDownloadResult.strGeneralErrors.add(objInstructUploadCompleteResponse.strErrorMessage);
				}

				// Downloads
				File fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(objActivity));
				File objNoteFile = clsUtils.BuildNoteFilename(fileTreeNodesDir, objImageLoadData.strNoteUuid);
				clsExplorerTreeview.clsRepository objNoteRepository = clsExplorerTreeview.DeserializeNoteFromFile(objNoteFile);
				for(clsImageToBeDownLoadedData objImageToBeDownLoadedData: objImageLoadData.objImageToBeDownLoadedDatas) {
					publishProgress("Downloading image (" + 
							(objImageLoadData.objImageToBeDownLoadedDatas.indexOf(objImageToBeDownLoadedData) + 1) +
							" of " + objImageLoadData.objImageToBeDownLoadedDatas.size() + ")");

					// Thumbnail image
					clsDownloadImageFileCommandMsg objDownloadCommand = clsImageUpDownloadAsyncTask.objMessaging.new clsDownloadImageFileCommandMsg();
					objDownloadCommand.strImageUuid = objImageToBeDownLoadedData.strImageUuid;
					objDownloadCommand.strFileType = "";
					objDownloadCommand.strFileExtentionWithoutDot = "jpg";
					objDownloadCommand.strImageLocalFullPathName = clsUtils.GetTreeNotesDirectoryName(objActivity) + "/" + objImageToBeDownLoadedData.strImageUuid +
							objDownloadCommand.strFileType + "." + objDownloadCommand.strFileExtentionWithoutDot;				
					
					String strDownloadReturn = DownloadImageToServer(strDownloadUrl, objDownloadCommand);
					if (!strDownloadReturn.isEmpty()) {
						clsImageUpDownloadResult.clsError objError = objImageUpDownloadResult.new clsError();
						objError.strNoteUuid = objImageToBeDownLoadedData.strImageUuid;
						objError.strDescription = strDownloadReturn;
						objImageUpDownloadResult.strDownloadErrors.add(objError);
					} 
					// Full image
					objDownloadCommand = clsImageUpDownloadAsyncTask.objMessaging.new clsDownloadImageFileCommandMsg();
					objDownloadCommand.strImageUuid = objImageToBeDownLoadedData.strImageUuid;
					objDownloadCommand.strFileType = "_full";
					objDownloadCommand.strFileExtentionWithoutDot = "jpg";
					objDownloadCommand.strImageLocalFullPathName = clsUtils.GetTreeNotesDirectoryName(objActivity) + "/" + objImageToBeDownLoadedData.strImageUuid +
							objDownloadCommand.strFileType + "." + objDownloadCommand.strFileExtentionWithoutDot;
					strDownloadReturn = DownloadImageToServer(strDownloadUrl, objDownloadCommand);
					if (!strDownloadReturn.isEmpty()) {
						clsImageUpDownloadResult.clsError objError = objImageUpDownloadResult.new clsError();
						objError.strNoteUuid = objImageToBeDownLoadedData.strImageUuid;
						objError.strDescription = strDownloadReturn;
						objImageUpDownloadResult.strDownloadErrors.add(objError);
					} 
					// Annotated image
					if (objImageToBeDownLoadedData.boolIsAnnotatedImageToBeDownloaded) {
						objDownloadCommand.strImageUuid = objImageToBeDownLoadedData.strImageUuid;
						objDownloadCommand.strFileType = "_annotated";
						objDownloadCommand.strFileExtentionWithoutDot = "jpg";
						objDownloadCommand.strImageLocalFullPathName = clsUtils.GetTreeNotesDirectoryName(objActivity) + "/" + objImageToBeDownLoadedData.strImageUuid +
								objDownloadCommand.strFileType + "." + objDownloadCommand.strFileExtentionWithoutDot;
						strDownloadReturn = DownloadImageToServer(strDownloadUrl, objDownloadCommand);
						if (!strDownloadReturn.isEmpty()) {
							clsImageUpDownloadResult.clsError objError = objImageUpDownloadResult.new clsError();
							objError.strNoteUuid = objImageToBeDownLoadedData.strImageUuid;
							objError.strDescription = strDownloadReturn;
							objImageUpDownloadResult.strDownloadErrors.add(objError);
						} 
					}
				}	
			}
			
			return null;
		}
    			

		private String EnsureUploadImageVersionsExists(
				clsImageToBeUploadedData objImageToBeUploadedData, ArrayList<clsImageToBeUploadedConfigData> objImagesToBeUploadedConfigDatas) {
			
			String fileName;
			String strImageVersionFullFilename;
			
			// Thumbnail, only check for existence, already created because of
			// it appearing in note
			strImageVersionFullFilename = 
					clsUtils.GetThumbnailImageFileName(objActivity, objImageToBeUploadedData.strUuid);
			
			File fileImageVersion = new File(strImageVersionFullFilename);
			fileName = fileImageVersion.getName();
			
			if (!fileImageVersion.exists()) {
				return fileName + " does not exist on client";
			}
			clsImageToBeUploadedConfigData objImageToBeUploadedConfigData = new clsImageToBeUploadedConfigData();
			objImageToBeUploadedConfigData.strImageUuid = objImageToBeUploadedData.strUuid;
			objImageToBeUploadedConfigData.strImageFilenameWithExt = fileName;
			objImageToBeUploadedConfigData.strDateLastModified = clsUtils.GetFileLastModifiedDate(strImageVersionFullFilename);
			objImagesToBeUploadedConfigDatas.add(objImageToBeUploadedConfigData);
			
			// Annotated image
			strImageVersionFullFilename = 
					clsUtils.GetAnnotatedImageFileName(objActivity, objImageToBeUploadedData.strUuid);
			
			fileImageVersion = new File(strImageVersionFullFilename);
			fileName = fileImageVersion.getName();

			if (fileImageVersion.exists()) {
				objImageToBeUploadedConfigData = new clsImageToBeUploadedConfigData();
				objImageToBeUploadedConfigData.strImageUuid = objImageToBeUploadedData.strUuid;
				objImageToBeUploadedConfigData.strImageFilenameWithExt = fileName;
				objImageToBeUploadedConfigData.strDateLastModified = clsUtils.GetFileLastModifiedDate(strImageVersionFullFilename);
				objImagesToBeUploadedConfigDatas.add(objImageToBeUploadedConfigData);
			}

			// Full image
			strImageVersionFullFilename = 
					clsUtils.GetFullImageFileName(objActivity, objImageToBeUploadedData.strUuid);
			
			fileImageVersion = new File(strImageVersionFullFilename);
			fileName = fileImageVersion.getName();
			
			if (!fileImageVersion.exists()) {
				return fileName + " does not exist on client";
			} else {
				objImageToBeUploadedConfigData = new clsImageToBeUploadedConfigData();
				objImageToBeUploadedConfigData.strImageUuid = objImageToBeUploadedData.strUuid;
				objImageToBeUploadedConfigData.strImageFilenameWithExt = fileName;
				objImageToBeUploadedConfigData.strDateLastModified = clsUtils.GetFileLastModifiedDate(strImageVersionFullFilename);
				objImagesToBeUploadedConfigDatas.add(objImageToBeUploadedConfigData);
			}
			return "";
		}

		@Override
   	    protected void onPostExecute(Void result)
   	    {
			String strMessage = new String();
			boolean success = false;
			
   	        super.onPostExecute(result);
   	        
   	        if (objProgressDialog.isShowing()) {
   	        	objProgressDialog.dismiss();
   	        }
   	        
   	        if ((objImageUpDownloadResult.strDownloadErrors.size() == 0) && 
   	        		(objImageUpDownloadResult.strUploadErrors.size() == 0)) {
   	        	
   	        	success = true;
   	        	
   	        } else {
   	        	
   	        	success = false;
   	        	
   	        	strMessage = "Image sync completed with errors\n";
   	        	if (objImageUpDownloadResult.strDownloadErrors.size() != 0) {
   	        		strMessage += "Downloads:\n";
   	        		for (clsError objError:objImageUpDownloadResult.strDownloadErrors) {
   	        			strMessage += "File: " +  objError.strNoteUuid + ". " + objError.strDescription + "\n";
   	   	        	}
   	        	}
   	        	
   	        	if (objImageUpDownloadResult.strUploadErrors.size() != 0) {
   	        		strMessage += "Uploads:\n";
   	        		for (clsError objError:objImageUpDownloadResult.strUploadErrors) {
   	        			strMessage += "File: " +  objError.strNoteUuid + ". " + objError.strDescription + "\n";
   	   	        	}
   	        	}
   	        	
   	        	if (objImageUpDownloadResult.strGeneralErrors.size() != 0) {
   	        		strMessage += "General:\n";
   	        		for (String strError:objImageUpDownloadResult.strGeneralErrors) {
   	        			strMessage += strError + "\n";
   	   	        	}
   	        	}

   	        	// If no callback is provided then just toast the error message.
   	        	if(callbackFinished == null)
   	   	        {
   	        		clsUtils.MessageBox(objActivity, strMessage, false);
   	   	        }
   	        }
   	        
   	        if(callbackFinished != null)
   	        {
   	        	callbackFinished.imageUploadFinished(success, strMessage);
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
    
 // ------------------------------------------------------------------------------------
    public class clsSyncNoteCommandMsg extends clsMsg {
        public String strClientUserUuid = "";
        public String strRegistrationId = "";
        public boolean boolIsMergeNeeded = true;
        public boolean boolIsAutoSyncCommand;
		public ArrayList<clsSyncRepositoryCtrlData> objSyncRepositoryCtrlDatas = new ArrayList<clsSyncRepositoryCtrlData>();
    }
    
    public class clsSyncRepositoryCtrlData {
    	public clsSyncRepository objSyncRepository;
    }
    
    public class clsSyncNoteResponseMsg extends clsMsg {
    	public ArrayList<clsSyncRepository> objSyncRepositories = new ArrayList<clsSyncRepository>();
    	public ArrayList<Integer> intServerInstructions = new ArrayList<Integer>();
    	public ArrayList<String> strServerMessages = new ArrayList<String>();
    	public ArrayList<clsImageLoadData> objImageLoadDatas = new ArrayList<clsImageLoadData>();
    }
    
    
	public class clsSyncResult {
    	public static final int ERROR_NONE = 0;
    	public static final int ERROR_NETWORK = 1;
    	public int intErrorCode = ERROR_NONE;
    	public String strErrorMessage = "";
    	public ArrayList<clsSyncRepository> objSyncRepositories;
    	public ArrayList<Integer> intServerInstructions;
    	public ArrayList<String> strServerMessages;
    	public ArrayList<clsImageLoadData> objImageLoadDatas = new ArrayList<clsImageLoadData>();
    }
	

        
    public static class NoteSyncAsyncTask extends AsyncTask<String, Void, clsSyncResult>
   	{
   		static Exception mException = null;
   		static clsSyncNoteCommandMsg objCommand;
   		static boolean boolDisplayToasts;
   		static boolean boolDisplayProgress;
   		static URL urlFeed;
   		clsSyncResult objResult;
   		ProgressDialog objProgressDialog;
   		

   		
   		public NoteSyncAsyncTask (Activity objActivity, URL urlFeed, clsSyncNoteCommandMsg objSyncCommandMsg, 
   				clsMessaging objMessaging, boolean boolDisplayToasts, boolean boolDisplayProgress) {
   			NoteSyncAsyncTask.objCommand = objSyncCommandMsg;
   			NoteSyncAsyncTask.urlFeed = urlFeed;
   			NoteSyncAsyncTask.boolDisplayToasts = boolDisplayToasts;
   			NoteSyncAsyncTask.boolDisplayProgress = boolDisplayProgress;
   			objResult  = objMessaging.new clsSyncResult();
   			objProgressDialog = new ProgressDialog(objActivity);
   		}
   		
   		
   		@Override
   	    protected void onPreExecute()
   	    {
   	        super.onPreExecute();
   	        mException = null;
   	        if (boolDisplayProgress) {
   	   	        objProgressDialog.setMessage("Synching note text ..., please wait.");
   		        objProgressDialog.show();
   	        }

   	    }

   		@Override
   		protected clsSyncResult doInBackground(String... arg0) {
   			Gson gson = new Gson();
   			JSONObject objJsonResult = null;
   			
   			try {
   	            InputStream stream = null;
   	            String strJsonCommand = gson.toJson(objCommand);

   	            try {
   	                Log.i("myCustom", "Streaming data from network: " + urlFeed);
   	                stream = clsMessaging.DownloadUrl(urlFeed, strJsonCommand);
   	                objJsonResult = clsMessaging.UpdateLocalFeedData(stream);
   	                // Makes sure that the InputStream is closed after the app is
   	                // finished using it.
   				} catch (JSONException e) {
   					objResult.intErrorCode=clsSyncResult.ERROR_NETWORK;
   					objResult.strErrorMessage = "JSON exception. " + e;
   		            return objResult;
   				} finally {
   	                if (stream != null) {
   	                    stream.close();
   	                }
   	            }
   	        } catch (MalformedURLException e) {
   	        	objResult.intErrorCode=clsSyncResult.ERROR_NETWORK;
				objResult.strErrorMessage = "Feed URL is malformed. " + e;
		        return objResult;
   	        } catch (IOException e) {
   	            objResult.intErrorCode=clsSyncResult.ERROR_NETWORK;
				objResult.strErrorMessage = "Error reading from network. " + e;
		        return objResult;
   	        }
   			// Analize data from server	
   			clsSyncNoteResponseMsg objResponse = gson.fromJson(objJsonResult.toString(),clsSyncNoteResponseMsg.class);
			if (objResponse.getIntErrorNum() == 0) {

					objResult.intErrorCode=clsSyncResult.ERROR_NONE;
					// Other data transfer
					objResult.objSyncRepositories = objResponse.objSyncRepositories;
					objResult.intServerInstructions = objResponse.intServerInstructions;
					objResult.strServerMessages = objResponse.strServerMessages;
					objResult.objImageLoadDatas = objResponse.objImageLoadDatas;

			} else {
				objResult.intErrorCode=clsSyncResult.ERROR_NETWORK;
				objResult.strErrorMessage = objResponse.getStrErrMessage();
			}
   	        return objResult;
   		}
   		
   		@Override
   		protected void onPostExecute(clsSyncResult result) {
   			super.onPostExecute(result);
   			if (objProgressDialog.isShowing()) {
	        	objProgressDialog.dismiss();
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
   	
   
    

    
    
    
    // -------------- Utilities ------------------------------
    
    public static InputStream DownloadUrl(final URL url, String strJsonCommand) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */); 
        conn.setRequestMethod("POST");  
        conn.setUseCaches(false); 
        conn.setRequestProperty("Content-Type","application/json"); 
        
        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Starts the query
        conn.connect();
        OutputStreamWriter out = new   OutputStreamWriter(conn.getOutputStream());
        out.write(strJsonCommand);
        out.close();  

        return conn.getInputStream();
    }
 
	public static String UploadImageToServer(String strUploadUrl,
			clsUploadImageFileCommandMsg objUploadCommand) {
		int serverResponseCode = HttpStatus.SC_OK;
		String serverResponseMessage = "";
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;
		String pathToOurFile = objUploadCommand.strImageLocalFullPathName;
		String strImageUuid = objUploadCommand.strImageUuid;
		String urlServer = strUploadUrl;
		String strModificationDate = objUploadCommand.strModificationDate;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		try {
			FileInputStream fileInputStream = new FileInputStream(new File(
					pathToOurFile));

			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs &amp; Outputs.
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Set HTTP method to POST.
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			String strImageFilename = new File(pathToOurFile).getName();
			
			outputStream
					.writeBytes("Content-Disposition: form-data; name=\"" + strImageUuid + "\";filename=\""
							+ strImageFilename +
							"\"; modification-date=\"" + strModificationDate + "\"" +
							lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens
					+ lineEnd);

			// Responses from the server (code and message)
			serverResponseCode = connection.getResponseCode();
			serverResponseMessage = connection.getResponseMessage();

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		} catch (Exception ex) {
			// Exception handling
			ex.printStackTrace();
			return ex.getMessage();
		}
		if (serverResponseCode != HttpStatus.SC_OK) {
			return serverResponseMessage;
		}

		return "";
	}

	public static String DownloadImageToServer(String strUrl,
			clsDownloadImageFileCommandMsg objCommand) {
		try {
			
			
			URL url = new URL(strUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection
					.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
			urlConnection
					.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
			urlConnection.setRequestMethod("POST");
			urlConnection.setUseCaches(false);
			urlConnection
					.setRequestProperty("Content-Type", "application/json");

			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);

			// Starts the query
			urlConnection.connect();

			// Output
			OutputStreamWriter out = new OutputStreamWriter(
					urlConnection.getOutputStream());
			Gson gson = new Gson();
			String strJsonCommand = gson.toJson(objCommand);
			out.write(strJsonCommand);
			out.close();

			// Input
			File file = new File(objCommand.strImageLocalFullPathName);
			InputStream inputStream = urlConnection.getInputStream();

			FileOutputStream fileOutput = new FileOutputStream(file);
			int maxBufferSize = 1 * 1024 * 1024;
			byte[] buffer = new byte[maxBufferSize];
			int bufferLength = 0;

			while ((bufferLength = inputStream.read(buffer)) > 0) {
				fileOutput.write(buffer, 0, bufferLength);
			}
			fileOutput.close();

		} catch (MalformedURLException e) {
			return "MalformedURLException. " + e.getMessage() + ". " + e.getCause();
		} catch (IOException e) {
			return "IOException. " + e.getMessage() + ". " + e.getCause();
		} catch (Exception e) {
			return "Exception. " + e.getMessage() + ". " + e.getCause();
		}
		
		return "";
	}
	
	public static  clsInstructUploadCompleteResponse InstructUploadComplete(String strUrl,
			clsInstructUploadCompleteCommand objInstructUploadCompleteCommand) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		JSONObject objJsonResult = null;
		clsInstructUploadCompleteResponse objInstructUploadCompleteResponse = new clsInstructUploadCompleteResponse();

		try {
			InputStream stream = null;
			URL urlFeed = new URL(strUrl);
			String strJsonCommand = gson.toJson(objInstructUploadCompleteCommand);

			try {
				stream = clsMessaging.DownloadUrl(urlFeed, strJsonCommand);
				objJsonResult = clsMessaging.UpdateLocalFeedData(stream);
				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			} catch (JSONException e) {
				objInstructUploadCompleteResponse.intErrorCode = clsMessaging.ERROR_NETWORK;
				objInstructUploadCompleteResponse.strErrorMessage = "JSON exception. " + e;
				return objInstructUploadCompleteResponse;
			} finally {
				if (stream != null) {
					stream.close();
				}
			}
		} catch (MalformedURLException e) {
			objInstructUploadCompleteResponse.intErrorCode = clsMessaging.ERROR_NETWORK;
			objInstructUploadCompleteResponse.strErrorMessage = "Feed URL is malformed. " + e;
			return objInstructUploadCompleteResponse;
		} catch (IOException e) {
			objInstructUploadCompleteResponse.intErrorCode = clsMessaging.ERROR_NETWORK;
			objInstructUploadCompleteResponse.strErrorMessage = "Error reading from network. " + e;
			return objInstructUploadCompleteResponse;
		}
		// Analize data from server
		objInstructUploadCompleteResponse = gson.fromJson(objJsonResult.toString(), clsInstructUploadCompleteResponse.class);
		return objInstructUploadCompleteResponse;
	}

	public static JSONObject UpdateLocalFeedData(InputStream inStream)	throws IOException, JSONException {

		JSONObject object = null;
		try {
			BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
			String temp, response = "";
			while ((temp = bReader.readLine()) != null)
				response += temp;
			bReader.close();
			inStream.close();
			object =  (JSONObject) new JSONTokener(response).nextValue();
		}
		catch (IOException e)
		{
			WebServiceAsyncTask.mException = e;
		}
		return object;

	}
    
    public static boolean IsNetworkAvailable(Context objContext) {
    	boolean boolIsNetworkAvailable = false;
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) objContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolIsNetworkAvailable = activeNetworkInfo != null &&
        		activeNetworkInfo.isConnectedOrConnecting();
        return boolIsNetworkAvailable;
    }
    
    
    
    public static void ToastSyncingIsAvailable (Context objContext) {
    	clsUtils.CustomLog("WebService is available");
    	SpannableString text = new SpannableString("Syncing is available");
		text.setSpan(new ForegroundColorSpan(Color.GREEN), 11, 20, 0);
		Toast.makeText(objContext, text, Toast.LENGTH_SHORT).show();
    }
    
    public static void ToastSyncingIsUnavailable (Context objContext, String strReason) {
    	clsUtils.CustomLog("WebService is unavailable");
    	SpannableString text = new SpannableString("Syncing is unavailable" + ((strReason.isEmpty() == false)? "\n" + strReason:""));
		text.setSpan(new ForegroundColorSpan(Color.RED), 11, 22, 0);
		Toast.makeText(objContext, text, Toast.LENGTH_SHORT).show();
    }
    
    public String GetServerUrl(){
    	if(objRepository.boolIsServerIisExpress) {
    		return SERVER_URL_IIS_EXPRESS;
    	}
    	else {
    		return SERVER_URL_AZURE;
    	}
    }

	public void SaveFile(Activity objContext) {
		clsUtils.SerializeToSharedPreferences("clsMessaging", "objMessaging", objContext, objRepository);
	}
	
	public void LoadFile(Activity objContext) {
		objRepository = clsUtils.DeSerializeFromSharedPreferences("clsMessaging", "objMessaging", objContext, objRepository.getClass());
		if (objRepository == null) objRepository = new clsRepository();
	}
    
}
