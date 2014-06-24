package com.treeapps.treenotes.sharing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.google.gson.Gson;

public class clsWebServiceComms {
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_NETWORK = 1;
	
	// Input to async task
		public static class clsWebServiceCommand {

		}

		// Output from async task
		public static class clsWebServiceResponse {
			public int intErrorCode;
			public String strErrorMessage = "";
		}

		public static class clsWebServiceCommsAsyncTask extends AsyncTask<Void, Void, JSONObject> {
			static clsWebServiceCommand objCommand;
			static URL urlFeed;
			ProgressDialog objProgressDialog;
			public OnCompleteListener objOnCompleteListener;

			public clsWebServiceCommsAsyncTask(Activity objActivity, URL urlFeed, clsWebServiceCommand objCommand) {
				clsWebServiceCommsAsyncTask.urlFeed = urlFeed;
				clsWebServiceCommsAsyncTask.objCommand = objCommand;
				objProgressDialog = new ProgressDialog(objActivity);
			}

			public void SetOnCompleteListener(OnCompleteListener objOnCompleteListener) {
				this.objOnCompleteListener = objOnCompleteListener;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				objProgressDialog.setMessage("Processing..., please wait.");
				objProgressDialog.show();
			}

			@Override
			protected JSONObject doInBackground(Void... arg0) {
				clsWebServiceResponse objResponse = new clsWebServiceResponse();
				Gson gson = new Gson();
				JSONObject objJsonResult = null;

				try {
					try {
						InputStream stream = null;
						String strJsonCommand = gson.toJson(objCommand);

						try {
							stream = downloadUrl(urlFeed, strJsonCommand);
							objJsonResult = updateLocalFeedData(stream);
							// Makes sure that the InputStream is closed after finished
							// using it.
						
						} finally {
							if (stream != null) {
								stream.close();
							}
						}
					} catch (MalformedURLException e) {
						objResponse.intErrorCode = ERROR_NETWORK;
						objResponse.strErrorMessage = "Feed URL is malformed. " + e;
						return new JSONObject(gson.toJson(objResponse));
					} catch (IOException e) {
						objResponse.intErrorCode = ERROR_NETWORK;
						objResponse.strErrorMessage = "IO Exception from network. " + e;
						return new JSONObject(gson.toJson(objResponse));
					} 
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Analyze data from server
				return objJsonResult;
			}

			@Override
			protected void onPostExecute(JSONObject objJsonResponse) {
				super.onPostExecute(objJsonResponse);
				if (objProgressDialog.isShowing()) {
					objProgressDialog.dismiss();
				}
				objOnCompleteListener.onComplete(objJsonResponse);
			}

			public interface OnCompleteListener {
				public void onComplete(JSONObject objJsonResponse);
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();

				if (objProgressDialog.isShowing()) {
					objProgressDialog.dismiss();
				}
			}

		}

	private static final int NET_READ_TIMEOUT_MILLIS = 100000;
	private static final int NET_CONNECT_TIMEOUT_MILLIS = 100000;

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
			// WebServiceAsyncTask.mException = e;
		}
		return object;

	}
}


