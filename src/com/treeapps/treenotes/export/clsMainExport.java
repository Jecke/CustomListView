package com.treeapps.treenotes.export;

import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// The entry point to the export functionality
public class clsMainExport {

    public static final String EXPORT_DATA = "com.treeapps.treenotes.export.DATA";
	
	public enum EXPORT_DEST
	{
		TO_FACEBOOK
		
		// to be continued
	}
	
	private Context _context;
	
	public clsMainExport(Context context, Activity objActivity, clsTreeview objTreeview, clsMessaging objMessaging, clsGroupMembers objGroupMembers)
	{
		clsExportData._topNodeName = objTreeview.getRepository().getName();
		clsExportData._objTreeview = objTreeview;
		clsExportData._objMessaging = objMessaging;
		clsExportData._objGroupMembers = objGroupMembers;
		
		_context = context;
	}

	public void Execute(EXPORT_DEST type)
	{
		switch (type)
		{
			case TO_FACEBOOK:
        		Intent fbIntent = new Intent(_context, ActivityFacebookExport.class);
        		
        		_context.startActivity(fbIntent);

				break;
			
			default:
				Toast.makeText(_context, "type: " + type.toString() + " not supported", Toast.LENGTH_SHORT)
				     .show();
				break;
		}
	}
}
