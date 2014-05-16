package com.example.spacesavertreeview.export;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsResourceLoader;
import com.example.spacesavertreeview.clsTreeview;
import com.example.spacesavertreeview.imageannotation.clsAnnotationData;
import com.example.spacesavertreeview.imageannotation.clsCombineAnnotateImage;
import com.facebook.*;
import com.facebook.model.*;

import android.app.DownloadManager.Request;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class clsExportToFacebook extends Fragment implements clsCombineAnnotateImage.TaskCompletedInterface {
	
	private View view;
	
	// interface from  implements clsResourceLoader.TaskCompletedInterface 
	public void loadTaskComplete(String combinedFile)
	{
		Log.d(">>>", "combined " + combinedFile);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        					Bundle savedInstanceState) {
	    view = inflater.inflate(R.layout.activity_facebook_export, container, false);

	    return view;
	}
	
	public clsExportToFacebook()
	{
	}

	public void export(Context context, clsExportData exportData)
	{
		
		// Processing: Act on each element of list and all children of each list
		// TODO JE check if treeview provides some function to traverse the exportData._top arraylist-tree
//		switch(exportData._top.resourceId)
//		{
//			case clsTreeview.IMAGE_RESOURCE:
//				
//				String strCombinedFileName = exportData._treeNotesDir + "/" + exportData._top.guidTreeNode + "_full.jpg";
//				
//				clsCombineAnnotateImage objCombineAnnotateImage = new clsCombineAnnotateImage(context, this);
//				objCombineAnnotateImage.createAnnotatedImage(strCombinedFileName, exportData._top.annotation,
//																view.getWidth(), view.getHeight());
//				break;
//		}
		
		Log.d(">>>", "export");
	}
}
