package com.example.spacesavertreeview.export;

import com.example.spacesavertreeview.clsTreeview.clsTreeNode;

// Container to transport data to be exported.
// Note: At the moment the entire topmost entry of the note is used. At the end only the info
// required for the export should be in this class.
public class clsExportData {

	protected clsTreeNode _top;
	
	// Constructor
	public clsExportData(clsTreeNode top)
	{
		_top = top;
	}

	// Copyconstructor
	public clsExportData(clsExportData source)
	{
		
	}
}
