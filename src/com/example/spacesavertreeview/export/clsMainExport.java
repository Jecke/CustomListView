package com.example.spacesavertreeview.export;

import com.example.spacesavertreeview.clsUtils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

//import com.facebook.Settings;

// https://graph.facebook.com/PHOTO_ID/comments?message=MESSAGE&access_token=ACCESS_TOKEN
// POST

//import com.facebook.LoggingBehavior;
//import com.facebook.Session;
//import com.facebook.SessionState;
//import com.facebook.Settings;

// The entry point to the export functionality
public class clsMainExport {

    public static final String EXPORT_DATA = "com.example.spacesavertreeview.export.DATA";
	
	public enum EXPORT_DEST
	{
		TO_FACEBOOK
		
		// to be continued
	}
	
	private Context _context;
	
	/**
	 * Constructor
	 * @param context - object context
	 * @param type - type of export determines further processing
	 */
	// TODO JE provide container with data to export
	public clsMainExport(Context context)
	{
		_context = context;
	}

	public void export(EXPORT_DEST type, clsExportData data)
	{
		switch (type)
		{
			case TO_FACEBOOK:
        		Intent fbIntent = new Intent(_context, ActivityFacebookExport.class);
        		
        		fbIntent.putExtra(EXPORT_DATA, clsUtils.SerializeToString(data));	
        		
        		_context.startActivity(fbIntent);

				break;
			
			default:
				Toast.makeText(_context, "type: " + type.toString() + " not supported", Toast.LENGTH_SHORT)
				     .show();
				break;
		}
	}
}
