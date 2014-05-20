package com.example.spacesavertreeview.export;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsTreeview;
import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.clsTreeviewIterator;
import com.example.spacesavertreeview.clsUtils;
import com.example.spacesavertreeview.sharing.clsMessaging;
import com.example.spacesavertreeview.sharing.clsMessaging.clsImageLoadData;
import com.example.spacesavertreeview.sharing.clsMessaging.clsImageUpDownloadAsyncTask;
import com.google.gson.Gson;

public class clsExportToMail {
	
	private Activity objActivity;
	private clsTreeview objTreeview;
	private clsMessaging objMessaging;
	private String strWebPageHtml;
	private String strServerUrl;
	private int intLevelAmount;
	private ArrayList<clsImageLoadData> objImageLoadDatas;
	private clsImageLoadData objImageLoadData;
	
	
	
	public clsExportToMail(Activity objActivity, clsTreeview objTreeview, clsMessaging objMessaging) {
		this.objTreeview = objTreeview;
		this.objActivity = objActivity;
		this.objMessaging = objMessaging;
	}
	
	public void Execute() {
		try {
			GenerateWebPageHtml();
			PostWebPageToServer();
			UploadRequiredImages();
		} catch (Exception e) {
			clsUtils.MessageBox(objActivity, "MalformedURLException: " + e, true);
			return;
		}
	}
	


	private String GetWebPageUrl () {
		
		if(objMessaging.objRepository.boolIsServerIisExpress) {
			strServerUrl =  clsMessaging.SERVER_URL_IIS_EXPRESS;
		}
		else {
			strServerUrl =  clsMessaging.SERVER_URL_AZURE;
		}
		return strServerUrl + "/TreeNotesSave/Pages/" + objTreeview.getRepository().uuidRepository.toString()+ ".html";
	}
	
	public void GenerateWebPageHtml() {
		// This method assumes all the images are already on the webserver
		intLevelAmount = GetLevelAmount(objTreeview);
		strWebPageHtml  = "<div class='datagrid'>";
		strWebPageHtml += "<table><tbody>";
		clsTreeviewIterator objTreeviewIterator = new clsTreeviewIterator(objTreeview) {		
			@Override
			public void ProcessTreeNode(clsTreeNode objTreeNode, int intLevel) {
				strWebPageHtml += GenTableRow(intLevelAmount, intLevel, objTreeNode);			
			}
		};
		objTreeviewIterator.Execute();
		strWebPageHtml += "</tbody></table>";
		strWebPageHtml +="</div>";

	}
	




	private void PostWebPageToServer() {
		
		class clsMyPostWebPageOnServerAsyncTask extends clsPostWebPageOnServerAsyncTask {
			private clsMyPostWebPageOnServerAsyncTask (Activity objActivity, URL urlFeed, clsPostWebPageOnServerCommand objCommand, clsPostWebPageOnServerResponse objResponse) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			clsUtils.MessageBox(objActivity, "MalformedURLException: " + e, true);
			return;
		}
		clsPostWebPageOnServerCommand objCommand = new clsPostWebPageOnServerCommand();
		objCommand.strNoteUuid = objTreeview.getRepository().uuidRepository.toString();
		objCommand.strWebPageHtml = strWebPageHtml;
		clsPostWebPageOnServerResponse objResponse = new clsPostWebPageOnServerResponse();
		clsMyPostWebPageOnServerAsyncTask objAsyncTask = new clsMyPostWebPageOnServerAsyncTask(objActivity, url,objCommand, objResponse);
		objAsyncTask.SetOnPostExecTrigger(new com.example.spacesavertreeview.export.clsExportToMail.clsPostWebPageOnServerAsyncTask.OnPostExecTrigger() {			
			@Override
			public void Trigger(clsPostWebPageOnServerResponse objResponse) {
				if (objResponse.intErrorCode == clsPostWebPageOnServerResponse.ERROR_NONE) {
					// Open mail with message
					String strSubject = objTreeview.getRepository().getName();
					String strBody = "<font face='verdana' size='3'>Please press <a href='<<TREENOTES:WEB_PAGE_URL>>'>here</a> for the message.</font><br><br><font face='verdana' size='2' color='blue'>Message sent using <a src='" + strServerUrl + "'>TreeNotes</a></font>";
					strBody = strBody.replace("<<TREENOTES:WEB_PAGE_URL>>", GetWebPageUrl());
					clsUtils.SendGmail(objActivity, strSubject, strBody );
				} else {
					clsUtils.MessageBox(objActivity, "Error when generating mail webpage: " + objResponse.strErrorMessage, true);
					return;
				}
			}
		});
		objAsyncTask.execute();
		
		
	}
	
	private void UploadRequiredImages() {
		objImageLoadDatas = new ArrayList<clsImageLoadData>();
		objImageLoadData = objMessaging.new clsImageLoadData();
		objImageLoadData.strNoteUuid = objTreeview.getRepository().uuidRepository.toString();
		objImageLoadDatas.add(objImageLoadData);
		clsTreeviewIterator objTreeviewIterator = new clsTreeviewIterator(objTreeview) {		
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
		clsImageUpDownloadAsyncTask objImageUpDownloadAsyncTask = new clsImageUpDownloadAsyncTask(objActivity, objMessaging, false, objImageLoadDatas);
		objImageUpDownloadAsyncTask.execute();
	}
	
	// ---------------------- Utilities --------------------------
	
	private class clsPostWebPageOnServerCommand{
		public String strNoteUuid;
		public String strWebPageHtml;
	}
	
	private class clsPostWebPageOnServerResponse {
		public static final int ERROR_NONE = 0;
    	public static final int ERROR_NETWORK = 1;
		public int intErrorCode;
		public String strErrorMessage = "";
	}
	
	private static class clsPostWebPageOnServerAsyncTask extends AsyncTask<Void, Void, clsPostWebPageOnServerResponse> {
		static clsPostWebPageOnServerCommand objCommand;
		static clsPostWebPageOnServerResponse objResponse;
		static URL urlFeed;
		ProgressDialog objProgressDialog;
		public OnPostExecTrigger objOnPostExecTrigger;
		
		private clsPostWebPageOnServerAsyncTask (Activity objActivity, URL urlFeed, clsPostWebPageOnServerCommand objCommand, clsPostWebPageOnServerResponse objResponse) {
			clsPostWebPageOnServerAsyncTask.urlFeed = urlFeed;
			clsPostWebPageOnServerAsyncTask.objCommand =objCommand;
			clsPostWebPageOnServerAsyncTask.objResponse = objResponse;
			objProgressDialog = new ProgressDialog(objActivity);
		}
		
		public void SetOnPostExecTrigger (OnPostExecTrigger objOnPostExecTrigger) {
			this.objOnPostExecTrigger = objOnPostExecTrigger;
		}
		
		@Override
	    protected void onPreExecute()
	    {
	        super.onPreExecute();
	        objProgressDialog.setMessage("Processing..., please wait.");
		    objProgressDialog.show();
	    }

		@Override
		protected clsPostWebPageOnServerResponse doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
   			Gson gson = new Gson();
   			JSONObject objJsonResult = null;
   			
   			try {
   	            InputStream stream = null;
   	            String strJsonCommand = gson.toJson(objCommand);

   	            try {
   	                stream = clsMessaging.downloadUrl(urlFeed, strJsonCommand);
   	                objJsonResult = clsMessaging.updateLocalFeedData(stream);
   	                // Makes sure that the InputStream is closed after finished using it.
   				} catch (JSONException e) {
   					// TODO Auto-generated catch block
   					objResponse.intErrorCode=clsPostWebPageOnServerResponse.ERROR_NETWORK;
   					objResponse.strErrorMessage = "JSON exception. " + e;
   		            return objResponse;
   				} finally {
   	                if (stream != null) {
   	                    stream.close();
   	                }
   	            }
   	        } catch (MalformedURLException e) {
   	        	objResponse.intErrorCode=clsPostWebPageOnServerResponse.ERROR_NETWORK;
   	        	objResponse.strErrorMessage = "Feed URL is malformed. " + e;
		        return objResponse;
   	        } catch (IOException e) {
   	        	objResponse.intErrorCode=clsPostWebPageOnServerResponse.ERROR_NETWORK;
   	        	objResponse.strErrorMessage = "IO Exception from network. " + e;
		        return objResponse;
   	        }
   			// Analyze data from server	
   			clsPostWebPageOnServerResponse objResponse = gson.fromJson(objJsonResult.toString(),clsPostWebPageOnServerResponse.class);
   	        return objResponse;
		}
		
		@Override
   		protected void onPostExecute(clsPostWebPageOnServerResponse objResponse) {
   			// TODO Auto-generated method stub
   			super.onPostExecute(objResponse);
   			if (objProgressDialog.isShowing()) {
	        	objProgressDialog.dismiss();
	        }
   			objOnPostExecTrigger.Trigger(objResponse);
   		}
		
		public interface OnPostExecTrigger {
			public void Trigger(clsPostWebPageOnServerResponse objResponse);
		}
   		
   		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			if (objProgressDialog.isShowing()) {
				objProgressDialog.dismiss();
			}
		}
		
	}
	
	private int GetLevelAmount(clsTreeview objTreeview) {
		// TODO Auto-generated method stub
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
	
	static boolean boolIsAltRow = false;
	protected String GenTableRow(int intLevelAmount, int intLevel, clsTreeNode objTreeNode) {
		
		String strImageUrl = "";
		
		if ((objTreeNode.resourceId == clsTreeview.IMAGE_RESOURCE) || 
				(objTreeNode.resourceId == clsTreeview.ANNOTATION_RESOURCE) ||
				(objTreeNode.resourceId == clsTreeview.WEB_RESOURCE)){
			if (objTreeNode.annotation != null) {
				strImageUrl = "../Images/" + objTreeNode.guidTreeNode + "_annotate.jpg";
			} else {
				strImageUrl = "../Images/" + objTreeNode.guidTreeNode + "_full.jpg";
			}	
		}
		
		String strRowHtml;
		if (boolIsAltRow) {
			strRowHtml = "<tr class='alt'>";
			boolIsAltRow = false;
		} else {
			strRowHtml = "<tr>";
			boolIsAltRow = true;
		}

		for (int intCol = 0; intCol < intLevelAmount + 1; intCol++ ){					
			if (intCol < intLevel-1) {
				strRowHtml += "<td></td>";
			} else if (intCol == intLevel-1) {
				strRowHtml += "<td><img src='./images/empty.gif'></td>";
			} else if (intCol == intLevel) {
				int intColSpan = intLevelAmount - intCol;
				strRowHtml += "<td width='100%' colspan='" + intColSpan + "'>" + objTreeNode.getName() + "</td>";
				intCol += intColSpan-1;
			} else if (intCol == intLevelAmount) { 
				if (strImageUrl.isEmpty()) {
					strRowHtml += "<td></td>";
				} else {
					strRowHtml += "<td><img class='displayed' src='" + strImageUrl +"' height='42' align='middle'></td>";
				}				
			} else {
				strRowHtml += "<td></td>";
			}	
		}
		strRowHtml += "</tr>";

		return strRowHtml;
	}

}
