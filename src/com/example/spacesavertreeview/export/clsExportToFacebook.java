package com.example.spacesavertreeview.export;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

	private ProgressDialog pd;
	
	private void imageNodeExportResult(boolean success, String exportID, ExportInputContainer node)
	{
		if(success)
		{
			//Log.d(">>imageCmtNode", "L"+String.valueOf(node.level)+ " " +node.guidTreeNode.toString());
			
			// Add the FB export ID to the element in order to be able to add further notes (i.e. comments)
			// to it should a note on the next lower level appear.
			// Scenario:
			// currentNode
			//	L2
			//		L3
			//  L2 <- must be attached to first L2 and not L3 (in FB that is a comment on a comment)
			node.fbExportID = exportID;
			
			// Export annotation texts if necessary.
			if(!node.annotationText.isEmpty())
			{
				exportComment(exportID, node, node.annotationText);
			}
			else
			{
				// Export next entry in exportInput
				exportPendingId++;
				if(exportInput.size() > exportPendingId)
				{
					exportNode(exportInput.get(exportPendingId));
				}
				else
				{
					pd.dismiss();
				}
			}
		}
		else
		{
			clsUtils.MessageBox(_context, "Image upload failed. Abort export.", false);
			pd.dismiss();
		}
	}
	
	private void textNodeExportResult(boolean success, String exportID, ExportInputContainer node)
	{
		//Log.d(">>idx", "textNodeExportResult");
		if(success)
		{
			if(node.fbExportID.isEmpty())
			{
				node.fbExportID = exportID;
			}

			// Export next entry in exportInput
			exportPendingId++;
			if(exportInput.size() > exportPendingId)
			{
				exportNode(exportInput.get(exportPendingId));
			}
			else
			{
				pd.dismiss();
			}
		}
		else
		{
			clsUtils.MessageBox(_context, "Text upload failed. Abort export.", false);
			pd.dismiss();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        					Bundle savedInstanceState) {
	    view = inflater.inflate(R.layout.activity_facebook_export, container, false);

	    return view;
	}
	
	class ExportInputContainer
	{
		public UUID guidTreeNode;
		public String strName;
		public int resourceId;
		public boolean boolUseAnnotatedImage;
		
		public int level;
		
//		public ArrayList<String> annotationText = new ArrayList<String>();
		public String annotationText = new String();

		// specific to export functionality
		public String fbExportID = new String();
		//public String parentGuidTreeNode;
		
//		public ExportInputContainer()
//		{
//			
//		}
	}
	
	List<ExportInputContainer> exportInput = new ArrayList<ExportInputContainer>();
	
	// TODO JE I would prefer to change the resource types in clsTreeview to an enumeration and use an EnumMap
	// here to implement the counters. That seems to be overkill for a one-off counter but would be more flexible
	// if new resource types get added.
	private int countText  = 0;
	private int countImage = 0;
	private int countVideo = 0;
	private int countWeb   = 0;
	
	// Index of element currently in export
	private int exportPendingId;
	
	// Create a linked list of data to export in order to keep track of export progress.
	// The export of nodes happens asynchronous which means that smaller exports (e.g. text notes) will 
	// happen quicker than bigger (e.g. annotated images) ones. We need to make sure that the hierarchy
	// is kept intact meaning that children of a note should also appear as children on Facebook. That requires an
	// upper level export to be finished before the lower level export starts.
	// The current approach is to create a PhotoAlbum with the name of the topmost node (from ExplorerStartup)
	// and to add the actual notes as a hierarchy of comments to it. Facebook supports images in comments so
	// it should be possible to export images from lower levels.
	private void createListToExport(ArrayList<clsTreeNode> top, int level)
	{
		for(clsTreeNode node: top)
		{
			//Log.d(">>>Exp", String.valueOf(level) + " --> " + node.getName());
			
			ExportInputContainer exp = new ExportInputContainer();
			exp.guidTreeNode = node.guidTreeNode;
			exp.strName = node.getName();
			exp.resourceId = node.resourceId;
			exp.boolUseAnnotatedImage = node.boolUseAnnotatedImage;
			
			exp.level = level;
			
			// The annotation texts will be combined in a multi-line string to create a
			// single comment on facebook. That makes it more readable because every comment
			// will be prefixed with the originator's name on FB.
			if(node.annotation != null && !node.annotation.items.isEmpty())
			{
				String annotationText = new String();
				
				for(clsAnnotationItem item : node.annotation.items)
				{
					if(!item.getAnnotationText().isEmpty())
					{
						annotationText = annotationText + "\n";
						annotationText = annotationText + item.getAnnotationText();
					}
				}
				if(!annotationText.isEmpty())
				{
					exp.annotationText = annotationText;
				}
			}
			
			exportInput.add(exp);
			
			// Count the number of notes per type to give the user an idea how much
			// data is about to be exported.
			switch(exp.resourceId)
			{
			case clsTreeview.IMAGE_RESOURCE:
				countImage++;
				break;
			case clsTreeview.TEXT_RESOURCE:
				countText++;
				break;
			case clsTreeview.VIDEO_RESOURCE:
				countVideo++;
				break;
			case clsTreeview.WEB_RESOURCE:
				countWeb++;
				break;
			}
			
			if(!node.objChildren.isEmpty())
			{
				createListToExport(node.objChildren, level + 1);
			}
		}
	}

	private void startExport()
	{
		if(!exportInput.isEmpty())
		{
			pd = ProgressDialog.show(_context, "Processing...", "Please wait", 
					true, true, null);
			
			exportPendingId = 0;
			
			exportNode(exportInput.get(exportPendingId));
		}
	}
	
	private void exportNode(ExportInputContainer node)
	{
		postToFacebook(node);
	}
	
	/**
	 * Main entry to export class
	 * 
	 * @param context
	 * @param data
	 */
	public void export(Context context, clsExportData data)
	{
		_data = data;
		_context = context;
		
		createListToExport(_data._top, level);

		// Create a dialog showing the number and types of notes in that export to let the user decide
		// whether he still wants to start the export.
		int countImages = countImage + countWeb;
		String msg = "Export contains \n";
		msg = ((countText  > 0)?(msg + "\t"+String.valueOf(countText)  + " text note(s)\n"):msg);
		msg = ((countVideo > 0)?(msg + "\t"+String.valueOf(countVideo) + " video note(s)\n"):msg);
		msg = ((countImages > 0)?(msg + "\t"+String.valueOf(countImages) + " image note(s)\n"):msg);
		msg = msg + "\nStart Export?";
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    builder.setMessage(msg);
	    builder.setCancelable(true);
	    
	    builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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

//	private void export()
//	{
//		// Processing: Act on each element of list and all children of each list
//		for(clsTreeNode node: _data._top)
//		{
//			Log.d(">>>", "call: "+node.getName()+" "+String.valueOf(level));
//			
//			// Create a POST to Facebook
//			// Note: At the moment we only upload the images to Facebook and add the description and
//			// other text notes as comments.
//			// TODO JE Create a Facebook page or something alike to show the exported data.		
//			postToFacebook(node, level);
//						
//			if(!node.objChildren.isEmpty())
//			{
//				exportChild(_context, node.objChildren, level + 1);
//			}
//		}
//	}
//	
//	public void exportChild(Context context, ArrayList<clsTreeNode> top, int level)
//	{
//		for(clsTreeNode node: top)
//		{
//			Log.d(">>>", "callChild: "+node.getName()+" "+String.valueOf(level));
//			
//			// do something with the parent
//			postToFacebook(node, level);
//
//			if(!node.objChildren.isEmpty())
//			{
//				exportChild(context, node.objChildren, level + 1);
//			}
//		}
//	}
	
	
	/**
	 * This method checks if the node has a predecessor and returns its FB Id so that the export
	 * can create a sub-comment.
	 * 
	 * @param node
	 */
	private String getFbIdOfParent(ExportInputContainer node)
	{
		String retval = new String();

		if(node.level > 0)
		{
			int i = exportInput.indexOf(node);
			for(; i >= 0; i--)
			{
				ExportInputContainer pred = exportInput.get(i); 
				
				if(pred.level < node.level)
				{
					retval = pred.fbExportID; 
					return retval; 
				}
			}
		}
		
		return retval; 
	}

	// That method communicates with facebook in order to post data
	private void postToFacebook(ExportInputContainer node)
	{
		switch(node.resourceId)
		{
			case clsTreeview.IMAGE_RESOURCE:
				// Create annotated image if required
				String strCombinedFileName = _data._treeNotesDir + "/" + node.guidTreeNode + "_full.jpg";	
				if(node.boolUseAnnotatedImage)
				{
					new ExportAnnotatedImage(node).createAndExport(strCombinedFileName);
				}
				else
				{
					exportImage(node, getFbIdOfParent(node), strCombinedFileName);
				}
				break;
			
			case clsTreeview.TEXT_RESOURCE:
				exportComment(getFbIdOfParent(node), node, node.strName);

				//Toast.makeText(_context, "clsExportToFacebook:TEXT_RESOURCE not implemented", Toast.LENGTH_SHORT).show();
				break;
			
			case clsTreeview.VIDEO_RESOURCE:
				Toast.makeText(_context, "clsExportToFacebook:VIDEO_RESOURCE not implemented", Toast.LENGTH_SHORT).show();
				break;
			
			case clsTreeview.WEB_RESOURCE:
				Toast.makeText(_context, "clsExportToFacebook:WEB_RESOURCE not implemented", Toast.LENGTH_SHORT).show();
				break;
			
		}
	}
	
	private void exportImage(ExportInputContainer node, String parentID, String file)
	{
		String target;
		
		//clsTreeNode _node = node;

		Log.d(">>>fbAlbumID", "onCompleteExport "+parentID);

		
		byte[] data = null;
		Bitmap bi = BitmapFactory.decodeFile(file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		data = baos.toByteArray();

		Bundle params = new Bundle();

		if(parentID.isEmpty())
		{
			params.putString("caption", node.strName);
			params.putByteArray("picture", data);
			
			target = "/photos";
		}
		else
		{
			Log.d(">>", "PhotoComment");
			params.putString("message", node.strName);
//			params.putByteArray("source", data);
			params.putParcelable("source", bi);
			// source
						
			target = "/" + parentID + "/comments";
			Log.d(">>", "target: " + target);
		}

		// test test 
//		textNodeExportResult(true, "42", node);

		new Request(Session.getActiveSession(), target, params, HttpMethod.POST, new RequestCallback(node, false))
			.executeAsync();
	}
	
	private void exportComment(String parentID, ExportInputContainer node, String message)
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

// test test 
//		textNodeExportResult(true, "23", node);

		new Request(Session.getActiveSession(), target, params, HttpMethod.POST, new RequestCallback(node, true))
			.executeAsync();
	}

	// Helper to handle Facebook request responses
	class RequestCallback implements Request.Callback
	{
		ExportInputContainer _node;
		boolean _textNote;
		
		public RequestCallback(ExportInputContainer node, boolean textNote)
		{
			_node = node;
			_textNote = textNote;
		}
		
		public void onCompleted(Response response) {
			boolean success = false;
			String fbId = "";
			
			FacebookRequestError fbError = response.getError();

			if(fbError == null || fbError.getRequestStatusCode() == HttpStatus.SC_OK)
			{
//Log.d(">>>", "onCompleteExport "+String.valueOf(_textNote));
//				Log.d(">>> resp", response.toString());
				GraphObject responseGraphObject = response.getGraphObject();
				JSONObject json = responseGraphObject.getInnerJSONObject();

				try {
					fbId = json.getString("id");
					success = true;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					// unsuccessful
					success = false;
				}
			}
			Log.d(">>> resp", response.toString());

			if(_textNote)
				textNodeExportResult(success, fbId, _node);
			else
				imageNodeExportResult(success, fbId, _node);
		}
	}
	
	class ExportAnnotatedImage implements clsCombineAnnotateImage.TaskCompletedInterface
	{
		private ExportInputContainer _node;
		
		public ExportAnnotatedImage(ExportInputContainer node)
		{
			_node = node;
		}
		
		// interface from  implements clsCombineAnnotateImage.TaskCompletedInterface 
		public void loadTaskComplete(String combinedFile)
		{
//			Log.d(">>>", "combined " + combinedFile);
//
//			Log.d(">>imageNode", _node.guidTreeNode.toString());
//			Log.d(">>imageNode", String.valueOf(_node.annotation.items.isEmpty()));

			exportImage(_node, getFbIdOfParent(_node), combinedFile);
			
			
		}

		public void createAndExport(String original)
		{
			// Set dimensions of resulting image to the size of the view. 
			new clsCombineAnnotateImage(_context, this)
//				.createAnnotatedImage(original, _node.annotation, view.getWidth(), view.getHeight());
			.createAnnotatedImage(original, null, view.getWidth(), view.getHeight());
		}
	}
}
