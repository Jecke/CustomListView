package com.treeapps.treenotes.export;

import android.app.Activity;
import android.app.ProgressDialog;

import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageAsyncTask;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageResponse;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;

public class clsExportToMail implements clsExportNoteAsWebPageAsyncTask.OnWebPagePostedListener,
										clsExportNoteAsWebPage.OnImageUploadFinishedListener,
										clsExportNoteAsWebPage.OnImageUploadProgressListener
{

	private Activity objActivity;
	private clsTreeview objTreeview;
	private String strServerUrl;
	
	// Counter of pending export steps. If it is zero then the export was successful.
	private int stepsToSuccess; 
	private int uploadCurrent;
	private int uploadMax;

	private ProgressDialog objProgressDialog;
	private String defaultMessage = "Exporting. Please wait...";

	
	private	clsExportNoteAsWebPage objExportNoteAsWebPage;

	private void checkExportSuccess()
	{
		switch(stepsToSuccess)
		{
		case 0:
			// dismiss progress dialog
			objProgressDialog.dismiss();
			break;
			
		case 1:
			// Export of web page and upload of images to servers is finished.
			// Now we can start to generate the mail. 
			objProgressDialog.dismiss();
			generateMailRequest();
			break;
		}
	}
	
	@Override
	public void onPosted(clsExportNoteAsWebPageResponse objResponse) {
		if (objResponse.intErrorCode == clsExportNoteAsWebPageResponse.ERROR_NONE) {
			
			stepsToSuccess--;
			
			checkExportSuccess();
		} else {
			clsUtils.MessageBox(objActivity, "Error while exporting: " + objResponse.strErrorMessage, false);
		}
	}
	
	// implementation from clsExportNoteAsWebPage.OnImageUploadFinishedListener
	public void imageUploadFinished(boolean success, String errorMessage)
	{
		if(success)
		{
			stepsToSuccess--;
			
			checkExportSuccess();
		}
		else
		{
			clsUtils.MessageBox(objActivity, "Error while exporting: " + errorMessage, false);
		}
	}
	
	// Helper to change the text of the progress dialog
	private Runnable changeMessage = new Runnable() {
		
		@Override
		public void run() {
			String msg = String.format("%s\nUploading file %d of %d", defaultMessage, 
										uploadCurrent, uploadMax);
			objProgressDialog.setMessage(msg);
		}
	};
	
	// implementation from clsExportNoteAsWebPage.OnImageUploadProgressListener
	public void imageUploadProgress(int current, int max)
	{
		uploadCurrent = current;
		uploadMax     = max;
		
		// The text of the progress dialog cannot be changed directly. Instead
		// we store the new values and call the helper above.
		objActivity.runOnUiThread(changeMessage);
	}

	private void generateMailRequest()
	{
		String strSubject = objTreeview.getRepository().getName();
		String strBody;
//		String strBody = "<font face='verdana' size='3'>Please press <a href='<<TREENOTES:WEB_PAGE_URL>>'>here</a> for the message.</font><br><br><font face='verdana' size='2' color='blue'>Message sent using <a src='" + strServerUrl + "'>TreeNotes</a></font>";
//		strBody = strBody.replace("<<TREENOTES:WEB_PAGE_URL>>", objExportNoteAsWebPage.GetWebPageUrl());
//		clsUtils.SendGmail(objActivity, strSubject, strBody );
		strBody = "Hi there, for your message, please click on: \n<<TREENOTES:WEB_PAGE_URL>>.\n\nMessage sent using TreeNotes.";
		strBody = strBody.replace("<<TREENOTES:WEB_PAGE_URL>>", objExportNoteAsWebPage.GetWebPageUrl());
		clsUtils.ShareUrl(objActivity, strSubject, strBody);
	}
	
	public clsExportToMail(Activity objActivity, clsTreeview objTreeview, clsMessaging objMessaging, clsGroupMembers objGroupMembers) {
		
		objExportNoteAsWebPage = new clsExportNoteAsWebPage(objActivity, objTreeview, objMessaging, objGroupMembers);
		this.objActivity = objActivity;
		this.objTreeview = objTreeview;
	}
	
	public void Execute() {
		
		objProgressDialog = new ProgressDialog(objActivity);
		objProgressDialog.setMessage("Creating web page, uploading images, creating Mail.\nPlease wait ...");
		objProgressDialog.show();

		// Three steps to success
		stepsToSuccess = 3;

		try {
			objExportNoteAsWebPage.GenerateWebPageHtml();
			objExportNoteAsWebPage.PostWebPageHtmlToServer(this);
			objExportNoteAsWebPage.UploadRequiredImages(this, this);
		} catch (Exception e) {
			clsUtils.MessageBox(objActivity, "Error exporting the note as webpage: " + e, false);
			return;
		}
	}
}
