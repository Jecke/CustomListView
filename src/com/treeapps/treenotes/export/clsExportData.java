package com.treeapps.treenotes.export;

import java.util.ArrayList;

import com.treeapps.treenotes.clsTreeview.clsTreeNode;

// Container to transport data to be exported.
// Note: At the moment the entire topmost entry of the note is used. At the end only the info
// required for the export should be in this class.
public class clsExportData {

	protected ArrayList<clsTreeNode> _top;
	protected String _treeNotesDir;
	protected String _topNodeName;
	
	// Constructor
	public clsExportData(ArrayList<clsTreeNode> top, String treeNotesDir, String noteName)
	{
		_top = top;
		_treeNotesDir = treeNotesDir;
		_topNodeName = noteName;
	}

	// Copyconstructor
	public clsExportData(clsExportData source)
	{
		
	}
}
