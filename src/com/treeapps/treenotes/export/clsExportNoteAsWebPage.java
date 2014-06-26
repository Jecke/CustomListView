package com.treeapps.treenotes.export;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.clsTreeviewIterator;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageUpDownloadAsyncTask;

public class clsExportNoteAsWebPage {

	private Activity objActivity;
	private clsTreeview objTreeview;
	private clsMessaging objMessaging;
	private String strWebPageHtml;
	private String strWebHeadOGHtml;
	private String strServerUrl;
	private int intLevelAmount;
	private clsGroupMembers objGroupMembers;
	private ArrayList<clsImageLoadData> objImageLoadDatas;
	private clsImageLoadData objImageLoadData;
	private static String strExportTimeStampWinFilenameSafe;
	
	clsExportNoteAsWebPageAsyncTask.OnWebPagePostedListener callback;
	
	public interface OnImageUploadFinishedListener 
	{
		public void imageUploadFinished(boolean success, String errorMessage);
	}
	
	public interface OnImageUploadProgressListener 
	{
		public void imageUploadProgress(int current, int max);
	}
	
	public clsExportNoteAsWebPage(Activity objActivity, clsTreeview objTreeview, 
									clsMessaging objMessaging, clsGroupMembers objGroupMembers)
	{
		this.objTreeview = objTreeview;
		this.objActivity = objActivity;
		this.objMessaging = objMessaging;
		this.objGroupMembers=objGroupMembers;
		this.strExportTimeStampWinFilenameSafe = "";
		this.strWebHeadOGHtml = " ";
	}
	
	// Input to async task
	public static class clsExportNoteAsWebPageCommand {
		public String strNoteUuid;
		public String strWebPageHtml;
		public String strSenderName;
		public String strOgTags; // Open Graph HTML meta tags
		public String strExportTimeStampWinFilenameSafe;	// To make each URL unique
	}

	// Output from async task
	public static class clsExportNoteAsWebPageResponse {
		public static final int ERROR_NONE = 0;
		public static final int ERROR_NETWORK = 1;
		public int intErrorCode;
		public String strErrorMessage = "";
	}

	public static class clsExportNoteAsWebPageAsyncTask extends AsyncTask<Void, Void, clsExportNoteAsWebPageResponse> {
		static clsExportNoteAsWebPageCommand objCommand;
		static clsExportNoteAsWebPageResponse objResponse;
		static URL urlFeed;
		ProgressDialog objProgressDialog;
		public OnWebPagePostedListener objOnWebPagePostedListener;

		public clsExportNoteAsWebPageAsyncTask(Activity objActivity, URL urlFeed,
				clsExportNoteAsWebPageCommand objCommand, clsExportNoteAsWebPageResponse objResponse) {
			clsExportNoteAsWebPageAsyncTask.urlFeed = urlFeed;
			clsExportNoteAsWebPageAsyncTask.objCommand = objCommand;
			clsExportNoteAsWebPageAsyncTask.objResponse = objResponse;
			objProgressDialog = new ProgressDialog(objActivity);
		}

		public void SetOnWebPagePostedListener(OnWebPagePostedListener objOnWebPagePostedListener) {
			this.objOnWebPagePostedListener = objOnWebPagePostedListener;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			objProgressDialog.setMessage("Processing..., please wait.");
			objProgressDialog.show();
		}

		@Override
		protected clsExportNoteAsWebPageResponse doInBackground(Void... arg0) {
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
			clsExportNoteAsWebPageResponse objResponse = gson.fromJson(objJsonResult.toString(),
					clsExportNoteAsWebPageResponse.class);
			return objResponse;
		}

		@Override
		protected void onPostExecute(clsExportNoteAsWebPageResponse objResponse) {
			super.onPostExecute(objResponse);
			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}
			objOnWebPagePostedListener.onPosted(objResponse);
		}

		public interface OnWebPagePostedListener {
			public void onPosted(clsExportNoteAsWebPageResponse objResponse);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}
		}

	}
	
	public void GenerateOGHeaderHtml(String title, String description, String coverImage, boolean useAnnotated)
	{
		String header;
		
		// og:type
		header = "<meta property=\"og:type\" content=\"website\"/>\n";
		// fb:app_id
		header += "\t" + "<meta property=\"fb:app_id\" content=\"" + 
				objActivity.getResources().getString(R.string.app_id) +
					"\"/>" + "\n";
		// og:url
		header += "\t" + "<meta property= \"og:url\" content=\"" + GetWebPageUrl() + "\"/>" + "\n";
		// og:title
		header += "\t" + "<meta property=\"og:title\" content=\"" + title + "\" />" + "\n";
		// og:description
		header += "\t" + "<meta property=\"og:description\" content=\"" + description + "\"/>" + "\n";
		// og:image
		if(coverImage.isEmpty())
		{
			header += "\t" + "<meta property=\"og:image\" content=\"http://treenotes.azurewebsites.net/Views/images/treenotes_logo.png\"/>";
		}
		else
		{
			header += "\t" + "<meta property=\"og:image\" content=\"http://treenotes.azurewebsites.net/" + 
					getServerPathToImage(coverImage, useAnnotated) + "\"/>";
		}
		strWebHeadOGHtml = header;
	}


	public void GenerateWebPageHtml() {
		// Generate the timestamp value
		strExportTimeStampWinFilenameSafe = clsUtils.GetStrCurrentDateTimeWinFilenameSafe();
				
		// This method assumes all the images are already on the webserver
		intLevelAmount = GetLevelAmount(objTreeview);
		strWebPageHtml  = "<div class='datagrid'>";
		strWebPageHtml += "<table><tbody>";
		clsTreeviewIterator objTreeviewIterator = new clsTreeviewIterator(objTreeview.getRepository()) {		
			@Override
			public void ProcessTreeNode(clsTreeNode objTreeNode, int intLevel) {
				strWebPageHtml += GenTableRow(intLevelAmount, intLevel, objTreeNode);			
			}
		};
		objTreeviewIterator.Execute();
		strWebPageHtml += "</tbody></table>";
		strWebPageHtml +="</div>";

	}
	
	public void PostWebPageHtmlToServer(clsExportNoteAsWebPageAsyncTask.OnWebPagePostedListener callback) {
		
		this.callback = callback;
		
		class clsMyExportNoteAsWebPageAsyncTask extends clsExportNoteAsWebPageAsyncTask {
			private clsMyExportNoteAsWebPageAsyncTask (Activity objActivity, URL urlFeed, clsExportNoteAsWebPageCommand objCommand, clsExportNoteAsWebPageResponse objResponse) {
				super(objActivity, urlFeed, objCommand, objResponse);
			}	
		}
		
		// Execution starts here
		if(objMessaging.objRepository.boolIsServerIisExpress) {
			strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
		}
		else {
			strServerUrl =  clsMessaging.SERVER_URL_AZURE;
		}
		String strUrl = strServerUrl + objActivity.getResources().getString(R.string.url_upload_webpage_html);
		URL url;
		try {
			url = new URL(strUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			clsUtils.MessageBox(objActivity, "MalformedURLException: " + e, false);
			return;
		}
		clsExportNoteAsWebPageCommand objCommand = new clsExportNoteAsWebPageCommand();
		objCommand.strNoteUuid = objTreeview.getRepository().uuidRepository.toString();
		objCommand.strWebPageHtml = strWebPageHtml;
		objCommand.strSenderName = objGroupMembers.GetRegisteredUser().strUserName;
		objCommand.strOgTags = strWebHeadOGHtml;
		objCommand.strExportTimeStampWinFilenameSafe = strExportTimeStampWinFilenameSafe;
		clsExportNoteAsWebPageResponse objResponse = new clsExportNoteAsWebPageResponse();
		clsMyExportNoteAsWebPageAsyncTask objAsyncTask = new clsMyExportNoteAsWebPageAsyncTask(objActivity, url,objCommand, objResponse);
		objAsyncTask.SetOnWebPagePostedListener(callback);			
		objAsyncTask.execute();
	}

	public void UploadRequiredImages(OnImageUploadFinishedListener cbFinished, OnImageUploadProgressListener cbProgress)
	{
		objImageLoadDatas = new ArrayList<clsImageLoadData>();
		objImageLoadData = new clsImageLoadData();
		objImageLoadData.strNoteUuid = objTreeview.getRepository().uuidRepository.toString();
		objImageLoadDatas.add(objImageLoadData);
		clsTreeviewIterator objTreeviewIterator = new clsTreeviewIterator(objTreeview.getRepository()) {		
			@Override
			public void ProcessTreeNode(clsTreeNode objTreeNode, int intLevel) {
				if ((objTreeNode.resourceId == clsTreeview.IMAGE_RESOURCE) || 
				(objTreeNode.resourceId == clsTreeview.ANNOTATION_RESOURCE) ||
				(objTreeNode.resourceId == clsTreeview.WEB_RESOURCE)){

					clsImageLoadData.clsImageToBeUploadedData objImageToBeUploadedData = objImageLoadData.new clsImageToBeUploadedData();
					objImageToBeUploadedData.intResourceId = objTreeNode.resourceId;
					objImageToBeUploadedData.strResourcePath = objTreeNode.resourcePath;
					objImageToBeUploadedData.strUuid = objTreeNode.guidTreeNode.toString();
					objImageLoadData.objImageToBeUploadedDatas.add(objImageToBeUploadedData);
				};			
			}
		};
		objTreeviewIterator.Execute();
		clsImageUpDownloadAsyncTask objImageUpDownloadAsyncTask = 
			new clsImageUpDownloadAsyncTask(objActivity, objMessaging, false, objImageLoadDatas, 
											cbFinished, cbProgress, false);
		objImageUpDownloadAsyncTask.execute();
	}
	
	private int GetLevelAmount(clsTreeview objTreeview) {
		int intLevelAmount = 0;
		int intCurrentLevel = 0;
		for (clsTreeNode objTreeNode: objTreeview.getRepository().objRootNodes) {
			intCurrentLevel = 0;
			intLevelAmount = GetLevelAmountRecursively(objTreeNode, intLevelAmount, intCurrentLevel);
		}
		return intLevelAmount;
	}
	
	private int GetLevelAmountRecursively(clsTreeNode objTreeNode, int intLevelAmount, int intCurrentLevel) {
		intCurrentLevel += 1;
		if (intCurrentLevel > intLevelAmount) {
			intLevelAmount = intCurrentLevel;
		}
		for (clsTreeNode objChildTreeNode: objTreeNode.objChildren) {
			intLevelAmount = GetLevelAmountRecursively(objChildTreeNode, intLevelAmount, intCurrentLevel);
		}
		return intLevelAmount;
	}

	// Returns the path and name of an uploaded image on the server
	private String getServerPathToImage(String guidTreeNode, boolean annotated)
	{
		if(annotated)
		{
			return "TreeNotesSave/Images/" + guidTreeNode + "_annotated.jpg";
		}
		else
		{
			return "TreeNotesSave/Images/" + guidTreeNode + "_full.jpg";
		}
	}

	static boolean boolIsAltRow = false;
	protected String GenTableRow(int intLevelAmount, int intLevel, clsTreeNode objTreeNode) {
		
		String strImageUrl = "";
		
		// Get image urls
		if ((objTreeNode.resourceId == clsTreeview.IMAGE_RESOURCE) || 
				(objTreeNode.resourceId == clsTreeview.ANNOTATION_RESOURCE) ||
				(objTreeNode.resourceId == clsTreeview.WEB_RESOURCE)){

			strImageUrl = getServerPathToImage(objTreeNode.guidTreeNode.toString(), objTreeNode.getBoolUseAnnotatedImage());
			strImageUrl = "../../../" + strImageUrl; 
		}
				
		// Determine how many columns required
		int intExtraColumns = ((strImageUrl.isEmpty())?0:1) + ((objTreeview.getRepository().IsCheckList())?1:0);
		
		// Build the table
		String strRowHtml;
		if (boolIsAltRow) {
			strRowHtml = "<tr class='alt'>";
			boolIsAltRow = false;
		} else {
			strRowHtml = "<tr>";
			boolIsAltRow = true;
		}
		
		if(objTreeview.getRepository().IsCheckList()) {
			strRowHtml += "<td>";
			if (objTreeNode.boolIsChecked) {
				strRowHtml += "<img src='../images/cb_chk_black.png'>";
			} else {
				strRowHtml += "<img src='../images/cb_unchk_black.png'>";
			}			
			strRowHtml += "</td>";
		}

		for (int intCol = 0; intCol < intLevelAmount + 1; intCol++ ){					
			if (intCol < intLevel-1) {
				strRowHtml += "<td></td>";
			} else if (intCol == intLevel-1) {
				strRowHtml += "<td><img src='../images/empty.gif'></td>";
			} else if (intCol == intLevel) {
				int intColSpan = intLevelAmount - intCol;
				strRowHtml += "<td width='100%' colspan='" + intColSpan + "'>" + objTreeNode.getName() + "</td>";
				intCol += intColSpan-1;
			} else if (intCol == intLevelAmount) { 
				if (strImageUrl.isEmpty()) {
					strRowHtml += "<td></td>";
				} else {
					//strRowHtml += "<td><img class='displayed' src='" + strImageUrl +"' height='42' align='middle'></td>";
					
					strRowHtml += "<td><a href='" + strImageUrl +"'><img class='displayed' src='" + strImageUrl +"' height='42' align='middle'></a></td>";
				}				
			} else {
				strRowHtml += "<td></td>";
			}	
		}
		strRowHtml += "</tr>";

		return strRowHtml;
	}
	
	public String GetWebPageUrl () {
		
		if(objMessaging.objRepository.boolIsServerIisExpress) {
			strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
		}
		else {
			strServerUrl =  clsMessaging.SERVER_URL_AZURE;
		}
		return strServerUrl + "/Views/Pages/Messages/" + objTreeview.getRepository().uuidRepository.toString() +
				"_" + strExportTimeStampWinFilenameSafe + ".html";
	}

}
