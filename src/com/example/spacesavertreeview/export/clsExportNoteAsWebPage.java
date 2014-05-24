package com.example.spacesavertreeview.export;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.example.spacesavertreeview.sharing.clsMessaging;
import com.google.gson.Gson;

public class clsExportNoteAsWebPage {
	
	// Input to async task
	public static class clsExportNoteAsWebPageCommand {
		public String strNoteUuid;
		public String strWebPageHtml;
		public String strSenderName;
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
			// TODO Auto-generated method stub
			Gson gson = new Gson();
			JSONObject objJsonResult = null;

			try {
				InputStream stream = null;
				String strJsonCommand = gson.toJson(objCommand);

				try {
					stream = clsMessaging.downloadUrl(urlFeed, strJsonCommand);
					objJsonResult = clsMessaging.updateLocalFeedData(stream);
					// Makes sure that the InputStream is closed after finished
					// using it.
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
			// TODO Auto-generated method stub
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
}
