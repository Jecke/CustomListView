package com.treeapps.treenotes.export;

import android.app.Activity;
import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageAsyncTask;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageResponse;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;

public class clsExportToMail implements clsExportNoteAsWebPageAsyncTask.OnWebPagePostedListener {
	
	private Activity objActivity;
	private clsTreeview objTreeview;
	private String strServerUrl;
	
	private	clsExportNoteAsWebPage objExportNoteAsWebPage;

	@Override
	public void onPosted(clsExportNoteAsWebPageResponse objResponse) {
		if (objResponse.intErrorCode == clsExportNoteAsWebPageResponse.ERROR_NONE) {
			// Do work here ...
			// Open mail with message
			String strSubject = objTreeview.getRepository().getName();
			String strBody = "<font face='verdana' size='3'>Please press <a href='<<TREENOTES:WEB_PAGE_URL>>'>here</a> for the message.</font><br><br><font face='verdana' size='2' color='blue'>Message sent using <a src='" + strServerUrl + "'>TreeNotes</a></font>";
			strBody = strBody.replace("<<TREENOTES:WEB_PAGE_URL>>", objExportNoteAsWebPage.GetWebPageUrl());
			clsUtils.SendGmail(objActivity, strSubject, strBody );
		} else {
			clsUtils.MessageBox(objActivity, "Error when generating mail webpage: " + objResponse.strErrorMessage, true);
		}
	}
	
	public clsExportToMail(Activity objActivity, clsTreeview objTreeview, clsMessaging objMessaging, clsGroupMembers objGroupMembers) {
		
		objExportNoteAsWebPage = new clsExportNoteAsWebPage(objActivity, objTreeview, objMessaging, objGroupMembers);
	}
	
	public void Execute() {
		try {
			objExportNoteAsWebPage.GenerateWebPageHtml();
			objExportNoteAsWebPage.PostWebPageHtmlToServer(this);
			objExportNoteAsWebPage.UploadRequiredImages();
		} catch (Exception e) {
			clsUtils.MessageBox(objActivity, "Error exporting the note as webpage: " + e, true);
			return;
		}
	}
}
package com.treeapps.treenotes.export;

import android.app.Activity;
import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageAsyncTask;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageResponse;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;

public class clsExportToMail implements clsExportNoteAsWebPageAsyncTask.OnWebPagePostedListener {
	
	private Activity objActivity;
	private clsTreeview objTreeview;
	private String strServerUrl;
	
	private	clsExportNoteAsWebPage objExportNoteAsWebPage;

	@Override
	public void onPosted(clsExportNoteAsWebPageResponse objResponse) {
		if (objResponse.intErrorCode == clsExportNoteAsWebPageResponse.ERROR_NONE) {
			// Do work here ...
			// Open mail with message
			String strSubject = objTreeview.getRepository().getName();
			String strBody = "<font face='verdana' size='3'>Please press <a href='<<TREENOTES:WEB_PAGE_URL>>'>here</a> for the message.</font><br><br><font face='verdana' size='2' color='blue'>Message sent using <a src='" + strServerUrl + "'>TreeNotes</a></font>";
			strBody = strBody.replace("<<TREENOTES:WEB_PAGE_URL>>", objExportNoteAsWebPage.GetWebPageUrl());
			clsUtils.SendGmail(objActivity, strSubject, strBody );
		} else {
			clsUtils.MessageBox(objActivity, "Error when generating mail webpage: " + objResponse.strErrorMessage, true);
		}
	}
	
	public clsExportToMail(Activity objActivity, clsTreeview objTreeview, clsMessaging objMessaging, clsGroupMembers objGroupMembers) {
		
		objExportNoteAsWebPage = new clsExportNoteAsWebPage(objActivity, objTreeview, objMessaging, objGroupMembers);
	}
	
	public void Execute() {
		try {
			objExportNoteAsWebPage.GenerateWebPageHtml();
			objExportNoteAsWebPage.PostWebPageHtmlToServer(this);
			objExportNoteAsWebPage.UploadRequiredImages();
		} catch (Exception e) {
			clsUtils.MessageBox(objActivity, "Error exporting the note as webpage: " + e, true);
			return;
		}
	}
}
