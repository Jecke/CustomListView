package com.example.spacesavertreeview.sharing;

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
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsTreeview;
import com.example.spacesavertreeview.clsTreeview.clsSyncRepository;
import com.example.spacesavertreeview.clsUtils;
import com.example.spacesavertreeview.sharing.clsGroupMembers.clsSyncMembersRepository;
import com.example.spacesavertreeview.sharing.subscriptions.ActivitySubscriptions.ActivitySubscriptionsAddAsyncTask;
import com.example.spacesavertreeview.sharing.subscriptions.ActivitySubscriptions.clsSubscriptionsAddCommandMsg;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class clsMessaging {
	
	private static final int NET_CONNECT_TIMEOUT_MILLIS = 60000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 60000;  // 10 seconds
    public static String SERVER_URL_AZURE = "http://treenotes.azurewebsites.net";
    public static String SERVER_URL_IIS_EXPRESS = "http://10.0.0.7";
	
    // Webserver instructions
    public static final int SERVER_INSTRUCT_KEEP_ORIGINAL = 0;
    public static final int SERVER_INSTRUCT_REPLACE_ORIGINAL = 1;
    public static final int SERVER_INSTRUCT_CREATE_NEW_SHARED = 2;
    public static final int SERVER_INSTRUCT_NO_MORE_NOTES = 3;
    public static final int SERVER_INSTRUCT_PROBLEM = 4;
    public static final int SERVER_INSTRUCT_NO_PROBLEM = 5;
    public static final int SERVER_INSTRUCTION_REMOVE = 6;
    public static final int SERVER_INSTRUCT_CREATE_NEW_PUBLISHED = 7;

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

	// Persistent items
	public class clsRepository {
		public boolean boolIsServerIisExpress = true;
		public boolean boolIsServerAlive = false;	
	}
	public clsRepository objRepository = new clsRepository();
	// End of persistence items
	
	public boolean getBoolIsServerIisExpress() {
		return objRepository.boolIsServerIisExpress;
	}

	public void setBoolIsServerIisExpress(boolean boolIsServerIisExpress) {
		objRepository.boolIsServerIisExpress = boolIsServerIisExpress;
	}
	
	public boolean IsServerAlive() {
    	return objRepository.boolIsServerAlive;
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
				// TODO Auto-generated constructor stub
				this.objContext = objContext;
			}
			
			@Override
		    protected void onPostExecute(JSONObject result)
		    {
		        super.onPostExecute(result);
		        if (result == null)  {
		        	objRepository.boolIsServerAlive = false;
		        	clsUtils.CustomLog("WebService is unavailable");
		        	Toast.makeText(objContext, "WebService is unavailable", Toast.LENGTH_SHORT).show();
		        	return;
		        }
		        clsClearWebServiceRepositoryResponseMsg objResponseMsg = clsUtils.DeSerializeFromString(result.toString(), clsClearWebServiceRepositoryResponseMsg.class);
		        if (objResponseMsg.intErrorCode == ERROR_NONE) {
		        	clsUtils.CustomLog("WebService repository has been successfully cleared");
		        	Toast.makeText(objContext, "WebService repository has been successfully cleared", Toast.LENGTH_SHORT).show();
		        } else {
		        	objRepository.boolIsServerAlive = false;
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
	
	 public void UpdateIsServerAlive(Activity objContext) {
		// Ping server to check
		URL urlFeed = null;
		String strServerUrl;
		try {
			if(objRepository.boolIsServerIisExpress) {
				strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
			}
			else {
				strServerUrl =  clsMessaging.SERVER_URL_AZURE;
			}
			urlFeed = new URL(strServerUrl + objContext.getResources().getString(R.string.url_is_server_alive));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		class clsMyAsyncTask  extends WebServiceAsyncTask {

			private Activity objContext;
			public clsMyAsyncTask(Activity objContext, URL urlFeed, String strJsonCommand) {
				super(objContext, false, urlFeed, strJsonCommand);
				// TODO Auto-generated constructor stub
				this.objContext = objContext;
			}
			
			@Override
		    protected void onPostExecute(JSONObject result)
		    {
		        super.onPostExecute(result);
		        if (result == null)  {
		        	objRepository.boolIsServerAlive = false;
		        	clsUtils.CustomLog("WebService is unavailable");
		        	Toast.makeText(objContext, "Syncing is unavailable", Toast.LENGTH_SHORT).show();
		        	return;
		        }
		        clsIsServerAliveResponseMsg objResponseMsg = clsUtils.DeSerializeFromString(result.toString(), clsIsServerAliveResponseMsg.class);
		        if (objResponseMsg.intErrorCode == ERROR_NONE) {
		        	objRepository.boolIsServerAlive = true;
		        	clsUtils.CustomLog("WebService is available");
		        	Toast.makeText(objContext, "Syncing is available", Toast.LENGTH_SHORT).show();
		        } else {
		        	objRepository.boolIsServerAlive = false;
		        	clsUtils.CustomLog("WebService is unavailable");
		        	Toast.makeText(objContext, "Syncing is unavailable", Toast.LENGTH_SHORT).show();
		        } 			        
		    }
		}
		clsIsServerAliveCommandMsg objCommandMsg = new clsIsServerAliveCommandMsg();
		String strJsonCommand = clsUtils.SerializeToString(objCommandMsg);
		clsMyAsyncTask objAsyncTask = new clsMyAsyncTask(objContext, urlFeed, strJsonCommand);
		objAsyncTask.execute("");	    		

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


    public class clsAddUserCmd  extends clsMsg
    {
    	private String strUsername = "";
    	private String strPassword = "";
        
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

    public class clsAddUserResponse extends clsMsg
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
			this.boolDisplayProgress = boolDisplayProgress;
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
	                stream = downloadUrl(urlFeed, strJsonCommand);
	                object = updateLocalFeedData(stream);
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
    // -------------- UploadImageFileAsyncTask
    
    
    public class clsUploadImageFileCommandMsg extends clsMsg {
    	public  String strImageUuid;
    	public  String strFileExtentionWithoutDot;
    	public  String strImageLocalFullPathName;
    }
    
    public class clsUploadImageFileResponseMsg extends clsMsg {
    	public Integer intServerInstructions;
    	public String strServerMessages;
    }
    
    public static class clsUploadImageFileAsyncTask extends AsyncTask<String, Void, clsUploadImageFileResponseMsg>
   	{
   		static Exception mException = null;
   		static clsUploadImageFileCommandMsg objCommand;
   		static clsUploadImageFileResponseMsg objResponse;
   		static String strUrl;
   		ProgressDialog objProgressDialog;
   		static boolean boolDisplayProgress = true;
   		
   		public clsUploadImageFileAsyncTask (Activity objActivity, boolean boolDisplayProgress, String strUrl,
   				clsUploadImageFileCommandMsg objCommand, clsUploadImageFileResponseMsg objResponse) {
   			clsUploadImageFileAsyncTask.objCommand = objCommand;
   			clsUploadImageFileAsyncTask.objResponse = objResponse;
   			clsUploadImageFileAsyncTask.strUrl = strUrl;
   			objProgressDialog = new ProgressDialog(objActivity);
   			clsUploadImageFileAsyncTask.boolDisplayProgress = boolDisplayProgress;
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
   		protected clsUploadImageFileResponseMsg doInBackground(String... arg0) {
   			HttpURLConnection connection = null;
   			DataOutputStream outputStream = null;
   			DataInputStream inputStream = null;
   			String pathToOurFile = objCommand.strImageLocalFullPathName;
   			String urlServer = strUrl;
   			String lineEnd = "\r\n";
   			String twoHyphens = "--";
   			String boundary =  "*****";
   			 
   			int bytesRead, bytesAvailable, bufferSize;
   			byte[] buffer;
   			int maxBufferSize = 1*1024*1024;
   			  			
   			try
   			{
   			    FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
   			 
   			    URL url = new URL(urlServer);
   			    connection = (HttpURLConnection) url.openConnection();
   			 
   			    // Allow Inputs &amp; Outputs.
   			    connection.setDoInput(true);
   			    connection.setDoOutput(true);
   			    connection.setUseCaches(false);
   			 
   			    // Set HTTP method to POST.
   			    connection.setRequestMethod("POST");
   			 
   			    connection.setRequestProperty("Connection", "Keep-Alive");
   			    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
   			 
   			    outputStream = new DataOutputStream( connection.getOutputStream() );
   			    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
   			    String strImageFilename = objCommand.strImageUuid + "." + objCommand.strFileExtentionWithoutDot;
   			    outputStream.writeBytes("Content-Disposition: form-data; name=\"UploadImageFile\";filename=\"" + strImageFilename +"\"" + lineEnd);
   			    outputStream.writeBytes(lineEnd);
   			 
   			    bytesAvailable = fileInputStream.available();
   			    bufferSize = Math.min(bytesAvailable, maxBufferSize);
   			    buffer = new byte[bufferSize];
   			 
   			    // Read file
   			    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
   			 
   			    while (bytesRead > 0)
   			    {
   			        outputStream.write(buffer, 0, bufferSize);
   			        bytesAvailable = fileInputStream.available();
   			        bufferSize = Math.min(bytesAvailable, maxBufferSize);
   			        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
   			    }
   			 
   			    outputStream.writeBytes(lineEnd);
   			    outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
   			 
   			    // Responses from the server (code and message)
   			    int serverResponseCode = connection.getResponseCode();
   			    String serverResponseMessage = connection.getResponseMessage();
   			 
   			    fileInputStream.close();
   			    outputStream.flush();
   			    outputStream.close();
   			}
   			catch (Exception ex)
   			{
   			    //Exception handling
   				ex.printStackTrace();
   			}
   	        return objResponse;
   		}
   		
   		@Override
   	    protected void onPostExecute(clsUploadImageFileResponseMsg objResponse)
   	    {
   	        super.onPostExecute(objResponse);
   	        
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
   	
// -------------- DownloadImageFileAsyncTask
    
    
    public class clsDownloadImageFileCommandMsg extends clsMsg {
    	public  String strImageUuid;
    	public  String strFileExtentionWithoutDot;
    	public  String strImageLocalFullPathName;
    }
    
    public class clsDownloadImageFileResponseMsg extends clsMsg {

    }
    
    public static class clsDownloadImageFileAsyncTask extends AsyncTask<String, Void, clsDownloadImageFileResponseMsg>
   	{
   		static Exception mException = null;
   		static clsDownloadImageFileCommandMsg objCommand;
   		static clsDownloadImageFileResponseMsg objResponse;
   		static String strUrl;
   		ProgressDialog objProgressDialog;
   		static boolean boolDisplayProgress = true;
   		
   		public clsDownloadImageFileAsyncTask (Activity objActivity, boolean boolDisplayProgress, String strUrl,
   				clsDownloadImageFileCommandMsg objCommand, clsDownloadImageFileResponseMsg objResponse) {
   			clsDownloadImageFileAsyncTask.objCommand = objCommand;
   			clsDownloadImageFileAsyncTask.objResponse = objResponse;
   			clsUploadImageFileAsyncTask.strUrl = strUrl;
   			objProgressDialog = new ProgressDialog(objActivity);
   			clsUploadImageFileAsyncTask.boolDisplayProgress = boolDisplayProgress;
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
		protected clsDownloadImageFileResponseMsg doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			objResponse.intErrorCode = ERROR_NONE;
	        objResponse.strErrorMessage = "";
			try {
			    URL url = new URL(strUrl);
			    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			    urlConnection.setRequestMethod("GET");
			    urlConnection.setDoOutput(true);
			    urlConnection.connect();

			    File file = new File(objCommand.strImageLocalFullPathName);

			    FileOutputStream fileOutput = new FileOutputStream(file);
			    InputStream inputStream = urlConnection.getInputStream();

			    int maxBufferSize = 1*1024*1024;
			    byte[] buffer = new byte[maxBufferSize];
			    int bufferLength = 0;

			    while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
			        fileOutput.write(buffer, 0, bufferLength);
			    }
			    fileOutput.close();

			} catch (MalformedURLException e) {
			        e.printStackTrace();
			        objResponse.intErrorCode = ERROR_DOWNLOAD_IMAGE;
			        objResponse.strErrorMessage = e.getMessage();
			} catch (IOException e) {
			        e.printStackTrace();
			        objResponse.intErrorCode = ERROR_DOWNLOAD_IMAGE;
			        objResponse.strErrorMessage = e.getMessage();
			}

			return objResponse;
		}
		
		@Override
   	    protected void onPostExecute(clsDownloadImageFileResponseMsg objResponse)
   	    {
   	        super.onPostExecute(objResponse);
   	        
   	        if (objProgressDialog.isShowing()) {
   	        	objProgressDialog.dismiss();
   	        }

   	        if (clsDownloadImageFileAsyncTask.mException != null) {
   	        	clsUtils.CustomLog(clsDownloadImageFileAsyncTask.mException.toString() + ". " + "Error # WebServiceAsyncTask");	
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
        public boolean boolIsMergeNeeded = true; // Return server version if set to false
		public ArrayList<clsSyncRepository> objSyncRepositories = new ArrayList<clsSyncRepository>();
    }
    
    public class clsSyncNoteResponseMsg extends clsMsg {
    	public ArrayList<clsSyncRepository> objSyncRepositories = new ArrayList<clsSyncRepository>();
    	public ArrayList<Integer> intServerInstructions = new ArrayList<Integer>();
    	public ArrayList<String> strServerMessages = new ArrayList<String>();
    }
    
    
	public class clsSyncResult {
    	public static final int ERROR_NONE = 0;
    	public static final int ERROR_NETWORK = 1;
    	public int intErrorCode = ERROR_NONE;
    	public String strErrorMessage = "";
    	public ArrayList<clsSyncRepository> objSyncRepositories;
    	public ArrayList<Integer> intServerInstructions;
    	public ArrayList<String> strServerMessages;
    }
	

        
    public static class NoteSyncAsyncTask extends AsyncTask<String, Void, clsSyncResult>
   	{
   		static Exception mException = null;
   		static clsSyncNoteCommandMsg objCommand;
   		static boolean boolDisplayToasts;
   		static URL urlFeed;
   		clsSyncResult objResult;
   		ProgressDialog objProgressDialog;
   		

   		
   		public NoteSyncAsyncTask (Activity objActivity, URL urlFeed, clsSyncNoteCommandMsg objSyncCommandMsg, 
   				clsMessaging objMessaging, boolean boolDisplayToasts) {
   			NoteSyncAsyncTask.objCommand = objSyncCommandMsg;
   			NoteSyncAsyncTask.urlFeed = urlFeed;
   			NoteSyncAsyncTask.boolDisplayToasts = boolDisplayToasts;
   			objResult  = objMessaging.new clsSyncResult();
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
   		protected clsSyncResult doInBackground(String... arg0) {
   			// TODO Auto-generated method stub
   			Gson gson = new Gson();
   			JSONObject objJsonResult = null;
   			
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

			} else {
				objResult.intErrorCode=clsSyncResult.ERROR_NETWORK;
				objResult.strErrorMessage = objResponse.getStrErrMessage();
			}
   	        return objResult;
   		}
   		
   		@Override
   		protected void onPostExecute(clsSyncResult result) {
   			// TODO Auto-generated method stub
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
    
    public static InputStream downloadUrl(final URL url, String strJsonCommand) throws IOException {
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
    
    public static JSONObject updateLocalFeedData(InputStream inStream)	throws IOException, JSONException {

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
    
    public boolean IsNetworkAvailable(Context objContext) {
    	boolean boolIsNetworkAvailable = false;
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) objContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolIsNetworkAvailable = activeNetworkInfo != null &&
        		activeNetworkInfo.isConnectedOrConnecting();
        return boolIsNetworkAvailable;
    }
    
    public String GetServerUrl(clsTreeview objTreeView){
    	if(objRepository.boolIsServerIisExpress) {
    		return SERVER_URL_IIS_EXPRESS;
    	}
    	else {
    		return SERVER_URL_AZURE;
    	}
    }

	public void SaveFile(Activity objContext) {
		// TODO Auto-generated method stub
		clsUtils.SerializeToSharedPreferences("clsMessaging", "objMessaging", objContext, objRepository);
	}
	
	public void LoadFile(Activity objContext) {
		objRepository = clsUtils.DeSerializeFromSharedPreferences("clsMessaging", "objMessaging", objContext, objRepository.getClass());
		if (objRepository == null) objRepository = new clsRepository();
	}
    
}
