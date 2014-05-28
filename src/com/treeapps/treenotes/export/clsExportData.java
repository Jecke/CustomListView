package com.treeapps.treenotes.export;

import java.util.ArrayList;

import android.app.Activity;

import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;

// Container to transport data to be exported.
// Note: At the moment the entire topmost entry of the note is used. At the end only the info
// required for the export should be in this class.
public class clsExportData {

	//protected ArrayList<clsTreeNode> _top;
	//protected String _treeNotesDir;
	protected static String _topNodeName;
	
	//public Activity _objActivity;
	public static clsTreeview _objTreeview;
	public static clsMessaging _objMessaging;
	public static clsGroupMembers _objGroupMembers;
	
	// Constructor
//	public clsExportData(Activity objActivity, clsTreeview objTreeview, clsMessaging objMessaging, clsGroupMembers objGroupMembers, String noteName)
//	{
//		_topNodeName = noteName;
//		//_objActivity = objActivity;
//		_objTreeview = objTreeview;
//		_objMessaging = objMessaging;
//		_objGroupMembers = objGroupMembers;
//	}
		
//	public clsExportData(ArrayList<clsTreeNode> top, String treeNotesDir, String noteName)
//	{
//		_top = top;
//		_treeNotesDir = treeNotesDir;
//		_topNodeName = noteName;
//	}
//
//	// Copyconstructor
//	public clsExportData(clsExportData source)
//	{
//		
//	}
}
