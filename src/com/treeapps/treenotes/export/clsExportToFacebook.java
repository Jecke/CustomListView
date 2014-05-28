package com.treeapps.treenotes.export;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.*;
import com.facebook.model.*;
import com.treeapps.treenotes.R;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageAsyncTask;
import com.treeapps.treenotes.export.clsExportNoteAsWebPage.clsExportNoteAsWebPageResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

// The basic way to export to Facebook:
// 1. Create WebPage from note and upload to TreeNote server
// 2. Create an FB album with the name of the note if necessary
// 3. Upload a standard TreeNote picture to the album with its caption set to the URL of the exported HTML document
//
// The optimal solution would be to display the web page directly on Facebook but there is no easy way to do that
// because Facebook does not support HTML directly.
public class clsExportToFacebook extends Fragment implements clsExportNoteAsWebPageAsyncTask.OnWebPagePostedListener {
	
	private View view;
	//private clsExportData _data;
	private Context _context;
	private Activity _activity;

	private String appVersion;

	private String albumId = new String();
	private String coverId = new String();
	
	private	clsExportNoteAsWebPage objExportNoteAsWebPage;
	
	private ProgressDialog objProgressDialog;
	
	// implementation from clsExportNoteAsWebPageAsyncTask.OnWebPagePostedListener
	@Override
	public void onPosted(clsExportNoteAsWebPageResponse objResponse) {
		if (objResponse.intErrorCode == clsExportNoteAsWebPageResponse.ERROR_NONE) {

			// Start FB export by creating the album for the URL of the note if necessary
			requestCreateAlbum();
			
		} else {
			
			displayDialogFail("Error while exporting: " + objResponse.strErrorMessage, true);
		}
	}
	
	// Starting point of fragment
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        					Bundle savedInstanceState) {
	    view = inflater.inflate(R.layout.activity_facebook_export, container, false);

	    return view;
	}
	
	/**
	 * Main entry to export class
	 * 
	 * @param context
	 * @param data
	 */
	public void export(Activity activity)//, clsExportData data)
	{
		//_data = data;
		_activity = activity;
		_context = (Context)activity;
		
		PackageInfo pInfo;
		try {
			
			// The version of the app gets used as description of the album
			pInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
			appVersion = " version " + pInfo.versionName;
			
		} catch (NameNotFoundException e) {

			e.printStackTrace();
			appVersion = " ";
		}

		// Show a dialog to get the confirmation from the user to start the export.
		requestUserConfirmationAndStart();
	}

	// Show a dialog to get the confirmation from the user to start the export.
	private void requestUserConfirmationAndStart()
	{
		// Create a dialog showing the number and types of notes in that export to let the user decide
		// whether he still wants to start the export.
		String msg = "The link to the Note will be exported to Album (" + clsExportData._topNodeName + ")\n"
					+ "Start Export?";
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(_context);
	    builder.setMessage(msg);
	    builder.setCancelable(true);
	    
	    builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	
            	// Start export of note as web page
            	startExport();
            }
        });
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

	    AlertDialog dialog = builder.create();
	    dialog.show();
	}
	
	// Export note as web page 
	private void startExport()
	{
		objExportNoteAsWebPage = new clsExportNoteAsWebPage(_activity, clsExportData._objTreeview, clsExportData._objMessaging, clsExportData._objGroupMembers);
		
		// TODO JE create webpage and add URL as caption to coverId
		try {
			
			objExportNoteAsWebPage.GenerateWebPageHtml();
			objExportNoteAsWebPage.PostWebPageHtmlToServer(this);
			objExportNoteAsWebPage.UploadRequiredImages();
			
		} catch (Exception e) {
			
			displayDialogFail("Unable to export note: " + e, true);
			return;
		}
	}

	private void requestCreateAlbum()
	{
		objProgressDialog = new ProgressDialog(_context);
		objProgressDialog.setMessage("Exporting to Facebook..., please wait.");
		objProgressDialog.show();
		
		Bundle params = new Bundle();
		params.putString("fields", "id,name");

		new Request(Session.getActiveSession(), "/me/albums", params, 
					HttpMethod.GET, new Request.Callback() {
					
			@Override
			public void onCompleted(Response response) {
				FacebookRequestError fbError = response.getError();

				if(fbError == null || fbError.getRequestStatusCode() == HttpStatus.SC_OK)
				{
					GraphObject responseGraphObject = response.getGraphObject();
					JSONObject json = responseGraphObject.getInnerJSONObject();

					String id;
					String name;

					JSONArray jArray = null;

					try {
						jArray = json.getJSONArray("data");
						for(int i =0;i<jArray.length();i++){
							id = jArray.getJSONObject(i).getString("id");
							name = jArray.getJSONObject(i).getString("name");

							if(0 == name.compareTo(clsExportData._topNodeName))
							{
								albumId = id;
							}
						}

						if(albumId.isEmpty())
						{
							createAlbum();
						}
						else
						{
							exportDefaultCoverImage();
						}

					} catch (JSONException e1) {
						displayDialogFail("Unable to request albums from Facebook. Aborting.", true);
						e1.printStackTrace();
					}
				}
			}
		}).executeAsync();
	}
	
	// Create a FB album by using the Note's name.
	private void createAlbum()
	{
		Bundle params = new Bundle();

		params.putString("name", clsExportData._topNodeName);
		params.putString("description", "Created by TreeNotes" + appVersion);

		new Request(Session.getActiveSession(), "/me/albums/", params, HttpMethod.POST, new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {
				
				FacebookRequestError fbError = response.getError();

				if(fbError == null || fbError.getRequestStatusCode() == HttpStatus.SC_OK)
				{
					GraphObject responseGraphObject = response.getGraphObject();
					JSONObject json = responseGraphObject.getInnerJSONObject();

					String id;
					
					try {
						id = json.getString("id");
						
						albumId = id;
						
						exportDefaultCoverImage();
	            		
					} catch (JSONException e) {
						e.printStackTrace();
						return;
					}
				}
				else
				{
					displayDialogFail("Unable to create album. Aborting.", true);
				}
			}
		})
		.executeAsync();
	}
	
	// Export the default image with the URL as caption to FB. If successful request FB Link and display
	// it to the user.
	private void exportDefaultCoverImage()
	{
		byte[] data = null;
		Bitmap bi = BitmapFactory.decodeResource(_context.getResources(), R.drawable.fb_album_icon);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		data = baos.toByteArray();

		String caption = "Click here for full information: " + objExportNoteAsWebPage.GetWebPageUrl();
		
		Bundle params = new Bundle();
		params.putString("caption", caption);
		params.putByteArray("picture", data);

		String target = "/" + albumId + "/photos";
		
		new Request(Session.getActiveSession(), target, params, HttpMethod.POST, 
				new Request.Callback() {
			
			@Override
			public void onCompleted(Response response) {

				FacebookRequestError fbError = response.getError();

				// Remove progress dialog
				objProgressDialog.dismiss();

				if(fbError == null || fbError.getRequestStatusCode() == HttpStatus.SC_OK)
				{
					GraphObject responseGraphObject = response.getGraphObject();
					JSONObject json = responseGraphObject.getInnerJSONObject();

					String id;
					try
					{
						id = json.getString("id");
						coverId = id;
						
						// Request link of recent export and display to user
						Bundle params = new Bundle();
						params.putString("fields", "link");
						new Request(Session.getActiveSession(), coverId, params, HttpMethod.GET, 
								new Request.Callback() {
									
									@Override
									public void onCompleted(Response response) {
										FacebookRequestError fbError = response.getError();

										if(fbError == null || fbError.getRequestStatusCode() == HttpStatus.SC_OK)
										{
											GraphObject responseGraphObject = response.getGraphObject();
											JSONObject json = responseGraphObject.getInnerJSONObject();

											String link;
											try
											{
												link = json.getString("link");

												// Display FB link to user
												displayDialogSuccess(link);
												
											} catch (JSONException e1) {
												e1.printStackTrace();
											}
										}
										else
										{
											_activity.finish();
										}
									}
						}).executeAsync();
						
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
				else
				{
					displayDialogFail("Export failed.", true);
				}
			}
		}).executeAsync();
	}

	// Display a custom dialog providing the link to the FB export
	private void displayDialogSuccess(String link)
	{
		AlertDialog dlg;
		
		AlertDialog.Builder b = new AlertDialog.Builder(_context);
		b.setTitle("Finished");

		LayoutInflater infl = LayoutInflater.from(_context);
		View view = infl.inflate(R.layout.fb_link_dialog, null);

		b.setView(view);
	
		String text = "Export successfully finished to " + link;
		
		TextView te = (TextView)view.findViewById(R.id.textFBMessage);
		te.setText(Html.fromHtml(text));

		Linkify.addLinks(te, Linkify.ALL);
		
		// Enable OK and Cancel button but override onClickListeners later
		// if required to prevent dialog from closing.
		b.setPositiveButton("Close",  new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {

    			_activity.finish();
    		}
    	});
		
		dlg = b.create();

		// It is important to use the below command instead of Builder.show
		// otherwise dlg.getButton would return null. Also the onClickListener
		// only works if registered AFTER dlg.show.
		dlg.show();
	}

	private void displayDialogFail(String msg, boolean dismissActivity)
	{
    	AlertDialog.Builder builder = new AlertDialog.Builder(_context);
	    builder.setMessage(msg);
	    builder.setCancelable(true);

	    if(dismissActivity)
    	{
	    	builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {

	    			_activity.finish();
	    		}
	    	});
    	}
	    else
	    {
	    	builder.setPositiveButton("Close", null);
	    }
	    AlertDialog dialog = builder.create();
	    dialog.show();

	}

}
