package com.example.spacesavertreeview.export;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsTreeview;
import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.clsTreeviewIterator;
import com.example.spacesavertreeview.clsUtils;
import com.example.spacesavertreeview.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageAsyncTask;
import com.example.spacesavertreeview.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageCommand;
import com.example.spacesavertreeview.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageResponse;
import com.example.spacesavertreeview.sharing.clsMessaging;
import com.example.spacesavertreeview.sharing.clsMessaging.clsImageLoadData;
import com.example.spacesavertreeview.sharing.clsMessaging.clsImageUpDownloadAsyncTask;

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
			PostWebPageHtmlToServer();
			UploadRequiredImages();
		} catch (Exception e) {
			clsUtils.MessageBox(objActivity, "Error exporting the note as webpage: " + e, true);
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
	




	private void PostWebPageHtmlToServer() {
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			clsUtils.MessageBox(objActivity, "MalformedURLException: " + e, true);
			return;
		}
		clsExportNoteAsWebPageCommand objCommand = new clsExportNoteAsWebPageCommand();
		objCommand.strNoteUuid = objTreeview.getRepository().uuidRepository.toString();
		objCommand.strWebPageHtml = strWebPageHtml;
		clsExportNoteAsWebPageResponse objResponse = new clsExportNoteAsWebPageResponse();
		clsMyExportNoteAsWebPageAsyncTask objAsyncTask = new clsMyExportNoteAsWebPageAsyncTask(objActivity, url,objCommand, objResponse);
		objAsyncTask.SetOnWebPagePostedListener(new clsExportNoteAsWebPageAsyncTask.OnWebPagePostedListener() {			
			@Override
			public void onPosted(clsExportNoteAsWebPageResponse objResponse) {
				if (objResponse.intErrorCode == clsExportNoteAsWebPageResponse.ERROR_NONE) {
					// Do work here ...
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
		
		// Get image urls
		if ((objTreeNode.resourceId == clsTreeview.IMAGE_RESOURCE) || 
				(objTreeNode.resourceId == clsTreeview.ANNOTATION_RESOURCE) ||
				(objTreeNode.resourceId == clsTreeview.WEB_RESOURCE)){
			if (objTreeNode.annotation != null) {
				strImageUrl = "../Images/" + objTreeNode.guidTreeNode + "_annotate.jpg";
			} else {
				strImageUrl = "../Images/" + objTreeNode.guidTreeNode + "_full.jpg";
			}	
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
				strRowHtml += "<img src='./images/cb_chk_black.png'>";
			} else {
				strRowHtml += "<img src='./images/cb_unchk_black.png'>";
			}			
			strRowHtml += "</td>";
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
