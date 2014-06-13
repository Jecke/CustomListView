package com.treeapps.treenotes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;




import com.google.gson.Gson;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsTreeview.enumItemType;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging;
import com.treeapps.treenotes.sharing.clsMessaging.clsSyncRepositoryCtrlData;

public class clsExplorerTreeview extends clsTreeview {
	

	
	public clsExplorerTreeview(Activity objActivity, clsGroupMembers objGroupMembers){
		super(objActivity, objGroupMembers);
	}
	
	public void SerializeToFile(File filePath, String strFilename){
		// Serialize own treeview first
		File objFile = clsUtils.BuildExplorerFilename(filePath, strFilename);
		super.getRepository().SerializeToFile(objFile);
	}
	
	public void SerializeToFile(File objFile){
		super.getRepository().SerializeToFile(objFile);
	}
			
	public void DeserializeFromFile(File filePath, String strFilename) {
		// TODO Auto-generated method stub
		File objFile = clsUtils.BuildExplorerFilename(filePath, strFilename);
		super.DeserializeFromFile(objFile);	
	}
	
	public void BackupToFile(String strBackupName) {
		// TODO Auto-generated method stub
		File fileTreeNotesDir = new File(clsUtils.GetTreeNotesDirectoryName(objActivity));
		File fileBackupFolderName = BuildBackupFoldername(fileTreeNotesDir, strBackupName);
		if (!fileBackupFolderName.exists()) {
			fileBackupFolderName.mkdir();
		}
		
		// Copy all files (not directories) in fileTreeNotesDir to fileBackupFolderName
		File[]fileItems = fileTreeNotesDir.listFiles(); 
		 try{
			 for(File fileItem: fileItems) {
				 if (fileItem.isFile()) {
					 clsUtils.CopyFile(fileItem, new File(fileBackupFolderName,fileItem.getName()), false);
				 }			 
			 }
		 }catch(Exception e) {
			 clsUtils.CustomLog("Error when copying file. " + e.getMessage());
			 
		 }
	}
	
	
	public void RestoreFromFile(String strBackupName) {
		// TODO Auto-generated method stub
		File fileTreeNotesDir = new File(clsUtils.GetTreeNotesDirectoryName(objActivity));
		File fileBackupFolderName = BuildBackupFoldername(fileTreeNotesDir, strBackupName);
		if (!fileBackupFolderName.exists()) {
			clsUtils.CustomLog("Backup folder does not exist");
			return;
		}
		
		// Delete all files in root directory
		File[]fileDelItems = fileTreeNotesDir.listFiles(); 
		for(File fileItem: fileDelItems) {
			 if (fileItem.isFile()) {
				 fileItem.delete();
			 }			 
		 }
			
	
		// Copy all files (not directories) in fileBackupFolderName to fileTreeNotesDir
		File[]fileItems = fileBackupFolderName.listFiles(); 
		 try{
			 for(File fileItem: fileItems) {
				 if (fileItem.isFile()) {
					 clsUtils.CopyFile(fileItem, new File(fileTreeNotesDir,fileItem.getName()), false);
				 }			 
			 }
		 } catch(Exception e) {
			 clsUtils.CustomLog("Error when copying file. " + e.getMessage());			 
		 }
		 
		 // Load the data from the new explore file
		 DeserializeFromFile(fileTreeNotesDir,objActivity.getResources().getString(R.string.working_file_name));
	}

	private File BuildBackupFoldername(File fileTreeNotesRootPath, String strBackupName) {
		String strBackupFoldername = strBackupName + objActivity.getResources().getString(R.string.explorer_file_extention);
		File fileBackupPath = new File(fileTreeNotesRootPath,strBackupFoldername);
		return fileBackupPath;
	}
	
	public void addShortcut(Context context, clsTreeNode objTreeNode) {
	    //Adding shortcut for MainActivity, once entered, the note with UUID gets started 
		
		if (objTreeNode.enumItemType != enumItemType.OTHER) return;
	    Intent shortcutIntent = new Intent(context, ActivityExplorerStartup.class);
	    shortcutIntent.putExtra(ActivityExplorerStartup.SHORTCUT_ACTIVE, true);
	    shortcutIntent.putExtra(ActivityExplorerStartup.SHORTCUT_NOTE_UUID, objTreeNode.guidTreeNode.toString());

	    shortcutIntent.setAction(Intent.ACTION_MAIN);

	    Intent addIntent = new Intent();
	    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, objTreeNode.getName());
	    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.icon_green_shortcut));

	    addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	    context.sendBroadcast(addIntent);
	}
	
	

	public class clsExplorerTreeNode extends clsTreeNode{

		private static final long serialVersionUID = 1L;
		
		clsExplorerTreeNode(String strName, enumItemType enumItemType, boolean	boolIsSelected, String strOwnerUserUuid, String strLastChangedByUserUuid){
			super(strName,enumItemType,boolIsSelected,"",0,strOwnerUserUuid, strLastChangedByUserUuid,"");
		}
		
		
		
		
	}
	
	@Override
	public clsTreeNode getClone(clsTreeNode objTreeNode, boolean boolResetUuid){
		clsTreeNode objNewTreeNode = new clsTreeNode();
		objNewTreeNode.setName(objTreeNode.getName());
		String strSourceGuidTreeNode = objTreeNode.guidTreeNode.toString();
		objNewTreeNode.guidTreeNode = (boolResetUuid)? UUID.randomUUID():objTreeNode.guidTreeNode;
		objNewTreeNode.enumItemType = objTreeNode.enumItemType;
		objNewTreeNode.boolIsSelected = false;
		objNewTreeNode.resourcePath = objTreeNode.resourcePath;
		objNewTreeNode.resourceId = objTreeNode.resourceId;
		objNewTreeNode.strWebPageURL = objTreeNode.strWebPageURL;
		if (objNewTreeNode.resourcePath != "") {
			// Make copy of thumbnail file since the UUID is different
			String strSourceFilename = objTreeNode.guidTreeNode.toString() + ".jpg";
			String strDestFilename = objNewTreeNode.guidTreeNode.toString() + ".jpg";
			File objFileSource =new File(((ActivityExplorerStartup)objActivity).fileTreeNodesDir, strSourceFilename);
			File objFileDest =new File(((ActivityExplorerStartup)objActivity).fileTreeNodesDir, strDestFilename);
			try {
				ActivityNoteStartup.copyDirectoryOneLocationToAnotherLocation(objFileSource, objFileDest);
			}
			catch (IOException ex){
				ex.printStackTrace();
			}
		}
		if (boolResetUuid && objNewTreeNode.enumItemType == enumItemType.OTHER) {
			// Make copy of note since the UUID is different
			File objFileSource =clsUtils.BuildNoteFilename(((ActivityExplorerStartup)objActivity).fileTreeNodesDir, strSourceGuidTreeNode);
			File objFileDest =clsUtils.BuildNoteFilename(((ActivityExplorerStartup)objActivity).fileTreeNodesDir, objNewTreeNode.guidTreeNode.toString());
			try {
				ActivityNoteStartup.copyDirectoryOneLocationToAnotherLocation(objFileSource, objFileDest);
			}
			catch (IOException ex){
				ex.printStackTrace();
			}
		}
		
		for (clsTreeNode objNewChildNode:objTreeNode.objChildren){
			objNewTreeNode.objChildren.add(getClone(objNewChildNode,boolResetUuid));
		}
		return objNewTreeNode;
	}
	
	public class clsExplorerFolderTreeNode extends clsExplorerTreeNode{
		private static final long serialVersionUID = -2641773753694333301L;

		clsExplorerFolderTreeNode(String strName, enumItemType enumItemType, boolean	boolIsSelected, String strOwnerUserUuid, String strLastChangedByUserUuid){
			super(strName,enumItemType,boolIsSelected, strOwnerUserUuid, strLastChangedByUserUuid);
		}
	}
	
	public class clsExplorerNoteTreeNode extends clsExplorerTreeNode{
		private static final long serialVersionUID = 2987196740778934783L;

		clsExplorerNoteTreeNode(String strName, enumItemType enumItemType, boolean	boolIsSelected, String strOwnerUserUuid, String strLastChangedByUserUuid){
			super(strName,enumItemType,boolIsSelected, strOwnerUserUuid, strLastChangedByUserUuid);
		}
	}

	public String GetUserNameFromNoteFile(Activity context, clsGroupMembers objGroupMembers, UUID guidTreeNode) {
		String strNoteOwnerUserName = "";
		String strUuid = guidTreeNode.toString();
		File fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(context));
		File objFile = clsUtils.BuildNoteFilename(fileTreeNodesDir, strUuid );
		if (objFile.exists()) {
			clsRepository objRepository = DeserializeNoteFromFile(objFile);
			if (objRepository != null) {
				String strNoteOwnerUserUuid = objRepository.getNoteOwnerUserUuid();
				if (!objRepository.getNoteOwnerUserUuid().isEmpty() ) {					
					strNoteOwnerUserName = objGroupMembers.GetUserNameFomUuid(strNoteOwnerUserUuid);
				}
			}		
		} 
		return strNoteOwnerUserName;
	}
	
	public clsRepository DeserializeNoteFromFile(File objFile){
		clsRepository objRepository;
		if (objFile.exists()== false) {
			clsUtils.CustomLog("Note file does not exist");
			return null;
		}
		BufferedReader br = null;
		String strSerialize = "";
		try
		{
			br = new BufferedReader(new FileReader(objFile));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				strSerialize += strLine;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				return null;
			}
		}
		
		if (strSerialize != "") {
			Gson gson = new Gson();		
			objRepository = gson.fromJson(strSerialize,clsRepository.class);
		} else {
			clsUtils.CustomLog("Note file is empty");
			return null;
		}
		return objRepository;
	}
	
	public void SerializeNoteToFile(clsRepository objNoteRepository, File objNoteFile) {
		// TODO Auto-generated method stub
		objNoteRepository.SerializeToFile(objNoteFile);
	}

	public ArrayList<clsSyncRepositoryCtrlData> GetAllSyncNotes(clsMessaging objMessaging) {
		// TODO Auto-generated method stub
		ArrayList<clsSyncRepositoryCtrlData> objSyncRepositoryCtrlDatas = new ArrayList<clsSyncRepositoryCtrlData>();
		for (clsTreeNode objTreeNode: this.getRepository().objRootNodes) {
			AddToSyncRepositoriesRecursively(objSyncRepositoryCtrlDatas,objTreeNode, objMessaging);
		}
		return objSyncRepositoryCtrlDatas;
	}

	private void AddToSyncRepositoriesRecursively(ArrayList<clsSyncRepositoryCtrlData> objSyncRepositoryCtrlDatas,
			clsTreeNode objTreeNode, clsMessaging objMessaging) {
		// TODO Auto-generated method stub
		if(objTreeNode.enumItemType == enumItemType.OTHER) {
			if (objTreeNode.IsToBeSynched()) {
				// Get note file from file repository
				File fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(objActivity));
				File objNoteFile = clsUtils.BuildNoteFilename(fileTreeNodesDir, objTreeNode.guidTreeNode.toString() );
				if (objNoteFile.exists()) {
					clsRepository objNoteRepository = DeserializeNoteFromFile(objNoteFile);
					// Add to sync repository
					clsSyncRepositoryCtrlData objRepositoryCtrlData = objMessaging.new clsSyncRepositoryCtrlData();
		        	objRepositoryCtrlData.objSyncRepository = objNoteRepository.getCopy(objActivity);
		        	objRepositoryCtrlData.boolNeedsAutoSyncWithNotification = false;
		        	objRepositoryCtrlData.boolNeedsOnlyChangeNotification = true;
		        	objSyncRepositoryCtrlDatas.add(objRepositoryCtrlData);
				}
			}
		}
		for (clsTreeNode objChildTreeNode:objTreeNode.objChildren ){
			AddToSyncRepositoriesRecursively(objSyncRepositoryCtrlDatas,objChildTreeNode, objMessaging);
		}
	}

	public void SaveFile() {
		// TODO Auto-generated method stub
		File fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(objActivity));
		 if (!fileTreeNodesDir.exists()) {
			fileTreeNodesDir.mkdirs();				 
		 }
		 getRepository().SerializeToFile(clsUtils.BuildExplorerFilename(fileTreeNodesDir, objActivity.getResources().getString(R.string.working_file_name)));
	}

	public void SetOwnerOfNotesWithoutOwner(String strRegisteredUserUuid) {
		// TODO Auto-generated method stub
		for (clsTreeNode objChildTreeNode:this.getRepository().objRootNodes ) {
			SetOwnerOfNotesWithoutOwnerRecursively(objChildTreeNode, strRegisteredUserUuid);
		}
	}

	private void SetOwnerOfNotesWithoutOwnerRecursively(clsTreeNode objTreeNode,	String strRegisteredUserUuid) {
		File fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(objActivity));
		File objNoteFile;
		if (objTreeNode.enumItemType == enumItemType.OTHER) {
			// Add owner info to treenode (a note)
  	        objNoteFile = clsUtils.BuildNoteFilename(fileTreeNodesDir, objTreeNode.guidTreeNode.toString() );
  	        if(objNoteFile.exists()) {
  	        	clsRepository objRepository = this.DeserializeNoteFromFile(objNoteFile);
				objRepository.setStrOwnerUserUuid(strRegisteredUserUuid);
				objRepository.SetOwnerOfAllSubNotes(strRegisteredUserUuid);
				objRepository.SerializeToFile(objNoteFile);
  	        }	
		}
		for (clsTreeNode objChildTreeNode:objTreeNode.objChildren ) {
			SetOwnerOfNotesWithoutOwnerRecursively(objChildTreeNode, strRegisteredUserUuid);
		}
	
	}

	@Override
	public void UpdateItemTypes() {
		// Correct possible mistakes due to pasting
		for (clsTreeNode objTreeNode: this.getRepository().objRootNodes) {
			UpdateItemTypeRecursively(objTreeNode);
		}
		
	}
	
	private void UpdateItemTypeRecursively(clsTreeNode objTreeNode) {
		// Correct mistakes due to pasting
		switch (objTreeNode.enumItemType) {
			case FOLDER_EXPANDED:
			case FOLDER_COLLAPSED:
				if (objTreeNode.objChildren.size() == 0) {
					objTreeNode.enumItemType = enumItemType.FOLDER_EMPTY;
				}
				break;
			case FOLDER_EMPTY:
				if (objTreeNode.objChildren.size() != 0) {
					objTreeNode.enumItemType = enumItemType.FOLDER_COLLAPSED;
				}
				break;
			case OTHER:
		}
		for (clsTreeNode objChildTreeNode: objTreeNode.objChildren) {
			UpdateItemTypeRecursively(objChildTreeNode);
		}
	}
	
	public void RemoveTreeNode(clsTreeNode objDeletedTreeNode) {
		// Not used by notes, notes are synced and removal needs to be delayed
		// Delete children first recursively
		while (objDeletedTreeNode.objChildren.size()!= 0) {
			RemoveTreeNode (objDeletedTreeNode.objChildren.get(0));
		}
		

		// Delete this element	
		// --------- Delete file first, if a note
		if (objDeletedTreeNode.enumItemType == enumItemType.OTHER) {
			File objFileDelete =new File(ActivityExplorerStartup.fileTreeNodesDir, objDeletedTreeNode.guidTreeNode.toString());
			objFileDelete.delete();
		}

		// --------- Delete node
		clsTreeNode objPeerTreeNode = getTreeNodeFromUuid(objDeletedTreeNode.guidTreeNode);
		clsTreeNode objParentTreeNode = getParentTreeNode(objPeerTreeNode);
		if (objParentTreeNode == null) {
			getRepository().objRootNodes.remove(objDeletedTreeNode);
		} else {
			objParentTreeNode.objChildren.remove(objDeletedTreeNode);
		}
		
		
	}

	
	public void setIsDeleted(clsTreeNode objDeletedTreeNode, boolean boolIsDeleted) {
		objDeletedTreeNode.boolIsDeleted = boolIsDeleted;
		// If node is a note, set its repository to be deleted after syncing
		if (objDeletedTreeNode.enumItemType == enumItemType.OTHER) {
			File fileTreeNodesDir = new File(clsUtils.GetTreeNotesDirectoryName(objActivity));
			File objNoteFile = clsUtils.BuildNoteFilename(fileTreeNodesDir, objDeletedTreeNode.guidTreeNode.toString() );
			if (objNoteFile.exists()) {
				clsRepository objNoteRepository = DeserializeNoteFromFile(objNoteFile);
				objNoteRepository.setBoolIsDeleted(true);
				SerializeNoteToFile(objNoteRepository, objNoteFile);
			}
		}
	
		// Mark all children as deleted
		for (clsTreeNode objChildTreeNode: objDeletedTreeNode.objChildren) {
			objChildTreeNode.setIsDeleted(boolIsDeleted);
		}			
	}


	
	@Override
	public boolean IsSourceDropableOnTarget(clsTreeNode objSourceTreeNode, clsTreeNode objTargetTreeNode) {
		if (objTargetTreeNode.enumItemType == enumItemType.OTHER) return false;
		return true;
	}

	

	
}
