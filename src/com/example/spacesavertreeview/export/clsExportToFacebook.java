package com.example.spacesavertreeview.export;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsResourceLoader;
import com.example.spacesavertreeview.clsTreeview;
import com.example.spacesavertreeview.clsUtils;
import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.imageannotation.clsAnnotationData;
import com.example.spacesavertreeview.imageannotation.clsAnnotationData.clsAnnotationItem;
import com.example.spacesavertreeview.imageannotation.clsCombineAnnotateImage;
import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.*; 

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class clsExportToFacebook extends Fragment {
	
	private View view;
	private clsExportData _data;
	private int level = 0;
	private Context _context;
	
	private String albumID = "";
	private clsTreeNode currentNode; // current node to export
	
	private void imageNodeExportResult(boolean success, String exportID, clsTreeNode node)
	{
		if(success)
		{
			Log.d(">>imageCommentNode", node.guidTreeNode.toString());

			
			// currentNode should point to the node information. 
			// Export annotation texts if necessary.
			// Upload successful. Retrieve ID of photo and add annotation as comments if necessary.
			if(node.annotation != null && !node.annotation.items.isEmpty())
			{
				for(clsAnnotationItem item : node.annotation.items)
				{
					if(!item.getAnnotationText().isEmpty())
					{
						Log.d(">>export", "comment");
						exportComment(exportID, item.getAnnotationText());
						
					}
				}
			}

		}
		else
		{
			clsUtils.MessageBox(_context, "Image upload failed. Abort export.", false);
		}
	}
	private void textNodeExportResult(boolean success, String exportID)
	{
		if(success)
		{
			
		}
		else
		{
			clsUtils.MessageBox(_context, "Text upload failed. Abort export.", false);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        					Bundle savedInstanceState) {
	    view = inflater.inflate(R.layout.activity_facebook_export, container, false);

	    return view;
	}
	
	public void export(Context context, clsExportData data)
	{
		_data = data;
		_context = context;
		
		// TODO JE Count notes per type to be published and ask user if he really wants to start
		
		// Processing: Act on each element of list and all children of each list
		for(clsTreeNode node: data._top)
		{
			Log.d(">>>", "call: "+node.getName()+" "+String.valueOf(level));
			
			// Create a POST to Facebook
			// Note: At the moment we only upload the images to Facebook and add the description and
			// other text notes as comments.
			// TODO JE Create a Facebook page or something alike to show the exported data.		
			postToFacebook(node, level);
						
			if(!node.objChildren.isEmpty())
			{
				exportChild(context, node.objChildren, level + 1);
			}
		}
		
		Log.d(">>>", "export");
	}
	
	public void exportChild(Context context, ArrayList<clsTreeNode> top, int level)
	{
		for(clsTreeNode node: top)
		{
			Log.d(">>>", "call: "+node.getName()+" "+String.valueOf(level));
			
			// do something with the parent
			postToFacebook(node, level);

			if(!node.objChildren.isEmpty())
			{
				exportChild(context, node.objChildren, level + 1);
			}
		}
	}
	
	// That method communicates with facebook in order to post data
	private void postToFacebook(clsTreeNode node, int level)
	{
		class ExportAnnotatedImage implements clsCombineAnnotateImage.TaskCompletedInterface
		{
			private clsTreeNode _node;
			
			public ExportAnnotatedImage(clsTreeNode node)
			{
				_node = node;
			}
			
			// interface from  implements clsCombineAnnotateImage.TaskCompletedInterface 
			public void loadTaskComplete(String combinedFile)
			{
				Log.d(">>>", "combined " + combinedFile);

				Log.d(">>imageNode", _node.guidTreeNode.toString());
				Log.d(">>imageNode", String.valueOf(_node.annotation.items.isEmpty()));

				exportImage(_node, "", combinedFile, _node.getName());
				
//				// test: create photo album
//				Bundle params = new Bundle();
//				params.putString("name", "Note name");
//				params.putString("message", "Contains the content of the note");
//				
//				new Request(Session.getActiveSession(), 
//						"/me/albums/",
//						params,
//						HttpMethod.POST,
//						new Request.Callback()
//						{
//							public void onCompleted(Response response) {
//									FacebookRequestError fbError = response.getError();
//Log.d(">>> Complete", "<<<");									
//									if(fbError == null || fbError.getRequestStatusCode() == HttpStatus.SC_OK)
//									{
//										/* handle the result */
//										GraphObject responseGraphObject = response.getGraphObject();
//
//										Log.d(">>> resp", response.toString());
//
//										//Create the JSON object
//										JSONObject json = responseGraphObject.getInnerJSONObject();
//
//										try {
//											String joi = json.getString("id");
//													
//													//getJSONObject("id");
//											Log.d(">>>JOI", joi);
//										} catch (JSONException e2) {
//											// TODO Auto-generated catch block
//											e2.printStackTrace();
//										}
//									}
//									else
//									{
//						        	   clsUtils.MessageBox(_context, "Creation of album failed", false);
//									}
//						           
////						           JSONArray jArray = null;
////								try {
////									jArray = json.getJSONArray("data");
////								} catch (JSONException e1) {
////									// TODO Auto-generated catch block
////									e1.printStackTrace();
////								}
////						           
////						           for(int i =0;i<jArray.length();i++){
////
////						        	   try {
////										JSONObject jo = jArray.getJSONObject(i);
////									} catch (JSONException e1) {
////										// TODO Auto-generated catch block
////										e1.printStackTrace();
////									}
////						        	   
////						               try {
////										String name= jArray.getJSONObject(i).getString("name");
////									} catch (JSONException e) {
////										// TODO Auto-generated catch block
////										e.printStackTrace();
////									}
////						               try {
////										String location= jArray.getJSONObject(i).getString("location");
////									} catch (JSONException e) {
////										// TODO Auto-generated catch block
////										e.printStackTrace();
////									}
////						           }
//							}
//
//						}
//						
//						
//						).executeAsync();
				
			}

			public void createAndExport(String original)
			{
				// Set dimensions of resulting image to the size of the view. 
				new clsCombineAnnotateImage(_context, this)
					.createAnnotatedImage(original, _node.annotation, view.getWidth(), view.getHeight());
			}
		}
		
		switch(node.resourceId)
		{
			case clsTreeview.IMAGE_RESOURCE:
				// 1. Create annotated image if required
				String strCombinedFileName = _data._treeNotesDir + "/" + node.guidTreeNode + "_full.jpg";	
				if(node.boolUseAnnotatedImage)
				{
					new ExportAnnotatedImage(node).createAndExport(strCombinedFileName);
				}
				else
				{
					exportImage(node, "", strCombinedFileName, node.getName());
				}
				break;
			
			case clsTreeview.TEXT_RESOURCE:
				Toast.makeText(_context, "clsExportToFacebook:TEXT_RESOURCE not implemented", Toast.LENGTH_SHORT).show();
				break;
			
			case clsTreeview.VIDEO_RESOURCE:
				Toast.makeText(_context, "clsExportToFacebook:VIDEO_RESOURCE not implemented", Toast.LENGTH_SHORT).show();
				break;
			
			case clsTreeview.WEB_RESOURCE:
				Toast.makeText(_context, "clsExportToFacebook:WEB_RESOURCE not implemented", Toast.LENGTH_SHORT).show();
				break;
			
		}
	}
	
	private void exportImage(clsTreeNode node, String fbAlbumID, String file, String caption)
	{
		String target;
		
		clsTreeNode _node = node;
		
		byte[] data = null;
		Bitmap bi = BitmapFactory.decodeFile(file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		data = baos.toByteArray();

		Bundle params = new Bundle();
		params.putString("caption", caption);
		params.putByteArray("picture", data);

		if(fbAlbumID.isEmpty())
		{
			target = "/photos";
		}
		else
		{
			target = "/" + fbAlbumID;
		}

		new Request(Session.getActiveSession(), target, params, HttpMethod.POST, new RequestCallback(_node))
			.executeAsync();
	}
	
	private void exportComment(String parentID, String message)
	{
		String target;
		
		Bundle params = new Bundle();
		params.putString("message", message);

		if(parentID.isEmpty())
		{
			target = "/feed";
		}
		else
		{
			target = "/" + parentID + "/comments";
		}

		new Request(Session.getActiveSession(), 
			target, params, HttpMethod.POST,
			new Request.Callback()
			{
			public void onCompleted(Response response) {
				FacebookRequestError fbError = response.getError();

				if(fbError == null || fbError.getRequestStatusCode() == HttpStatus.SC_OK)
				{
					String fbCommentID = "";

					//Log.d(">>> resp", response.toString());
					GraphObject responseGraphObject = response.getGraphObject();
					JSONObject json = responseGraphObject.getInnerJSONObject();

					try {
						fbCommentID = json.getString("id");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						textNodeExportResult(false, fbCommentID);
						return;
					}
					textNodeExportResult(true, fbCommentID);
				}
				else
				{
					textNodeExportResult(false, "");
				}
			}
			}).executeAsync();
	}

	// Helper to handle Facebook request responses
	class RequestCallback implements Request.Callback
	{
		clsTreeNode _node;
		
		public RequestCallback(clsTreeNode node)
		{
			_node = node;
		}
		
		public void onCompleted(Response response) {
			FacebookRequestError fbError = response.getError();

			if(fbError == null || fbError.getRequestStatusCode() == HttpStatus.SC_OK)
			{
				String fbImageID = "";

				//Log.d(">>> resp", response.toString());
				GraphObject responseGraphObject = response.getGraphObject();
				JSONObject json = responseGraphObject.getInnerJSONObject();

				try {
					fbImageID = json.getString("id");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					imageNodeExportResult(false, fbImageID, null);
					return;
				}
				imageNodeExportResult(true, fbImageID, _node);
			}
			else
			{
				imageNodeExportResult(false, "", null);
			}
		}
	}
}
