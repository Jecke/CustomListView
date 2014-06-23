package com.treeapps.treenotes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.treeapps.treenotes.clsListItem.enumNewItemType;
import com.treeapps.treenotes.imageannotation.clsAnnotationData;
import com.treeapps.treenotes.sharing.clsGroupMembers;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData;
import com.treeapps.treenotes.sharing.clsMessaging.clsImageLoadData.clsImageToBeUploadedConfigData;

public class clsTreeview {

	// types of sources supported by treeview
	public static final int TEXT_RESOURCE = 0;
	public static final int IMAGE_RESOURCE = 1;
	public static final int VIDEO_RESOURCE = 2;
	public static final int ANNOTATION_RESOURCE = 3;
	public static final int SHARED_FOLDER_RESOURCE = 6;
	public static final int PUBLISHED_FOLDER_RESOURCE = 7;
	public static final int WEB_RESOURCE = 8;

	private clsRepository _objRepository = new clsRepository();
	public ArrayList<clsTreeNode> objClipboardTreeNodes = new ArrayList<clsTreeNode>();
	
	public clsGroupMembers objGroupMembers;
	public enumCutCopyPasteState intCutCopyPasteState;
	public UUID uuidLastAddedTreeNode;
	public Activity objActivity;

	public clsTreeview(Activity objActivity, clsGroupMembers objGroupMembers) {
		this.objActivity = objActivity;
		this.objGroupMembers = objGroupMembers;
		intCutCopyPasteState = enumCutCopyPasteState.INACTIVE;
	}

	public void UpdateEnvironment(Activity objActivity, enumCutCopyPasteState intCutCopyPasteState,
			ArrayList<clsTreeNode> objClipboardTreeNodes) {
		this.objActivity = objActivity;
		this.intCutCopyPasteState = intCutCopyPasteState;
		this.objClipboardTreeNodes = objClipboardTreeNodes;
	}

	public clsRepository getRepository() {
		return _objRepository;
	}

	public void setRepository(clsRepository objRepository) {
		_objRepository = objRepository;
	}

	public void DeserializeFromFile(File objFile) {
		if (objFile.exists() == false) {
			clsUtils.CustomLog("Backup file: " + objFile.getAbsolutePath() + " does not exist");
			return;
		}
		BufferedReader br = null;
		String strSerialize = "";
		try {
			br = new BufferedReader(new FileReader(objFile));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				strSerialize += strLine;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		if (strSerialize != "") {
			Gson gson = new Gson();
			setRepository(gson.fromJson(strSerialize, clsRepository.class));
		} else {
			clsUtils.CustomLog("Backup file is empty");
		}
		
		// To ensure some form of compatibility with older repositories
		if (getRepository().objImageLoadDatas == null) {
			getRepository().objImageLoadDatas = new ArrayList<clsImageLoadData>();
		}
		
	}

	public void SetAsCheckList(boolean boolIsCheckList) {
		getRepository().boolIsCheckList = boolIsCheckList;
	}

	private void ClearRepository() {
		getRepository().objRootNodes.clear();
		getRepository().intEmptyItemCounter = 0;
	}

	public class clsRepository implements Serializable {
		private static final long serialVersionUID = 2223496794888289098L;
		public UUID uuidRepository;
		private String strName = "";
		public ArrayList<clsTreeNode> objRootNodes = new ArrayList<clsTreeNode>();
		public int intEmptyItemCounter = 0;
		// Data for checklisting
		private boolean boolIsCheckList = false;
		public boolean boolIsCheckedItemsMadeHidden = true;
		// Data for item hiding
		public boolean boolHiddenSelectionIsActive = false;
		public boolean boolIsHiddenActive = true;
		// Data for sharing
		private String strOwnerUserUuid = "";
		public boolean boolIsShared = false;
		public boolean boolIsSubscribed = false;
		public ArrayList<clsImageLoadData> objImageLoadDatas = new ArrayList<clsImageLoadData>();

		// Data for deletion
		private boolean boolIsDeleted = false;
		
		// Data to determine if changes took place and there is a need to save
		public boolean boolIsDirty = false;

		public boolean getBoolIsDeleted() {
			return boolIsDeleted;
		}

		public void setBoolIsDeleted(boolean boolIsDeleted) {
			this.boolIsDeleted = boolIsDeleted;
		}

		// Data local to client that does not get shared with server
		private boolean boolIsNoteSyncable = true; // For ever or not, changed
													// through properties
		private boolean boolIsNoteToBeSynched = true; // To allow syncing of
														// only dirtied notes

		public boolean IsCheckList() {
			return boolIsCheckList;
		}

		public String getNoteOwnerUserUuid() {
			if (strOwnerUserUuid.isEmpty()) {
				return "";
			}
			return strOwnerUserUuid;
		}

		public void setStrOwnerUserUuid(String strOwnerUserUuid) {
			this.strOwnerUserUuid = strOwnerUserUuid;
		}

		public ArrayList<clsSyncTreeNode> getRootNodesCopy(Activity objActivity) {
			ArrayList<clsSyncTreeNode> objNewRootNodes = new ArrayList<clsSyncTreeNode>();
			for (clsTreeNode objTreeNode : this.objRootNodes) {
				objNewRootNodes.add(getTreeNode2SyncTreeNodeCopy(objActivity, objTreeNode));
			}
			return objNewRootNodes;
		}

		public clsSyncRepository getCopy(Activity objActivity) {
			clsSyncRepository objSyncRepository = new clsSyncRepository();
			objSyncRepository.strRepositoryUuid = this.uuidRepository.toString();
			objSyncRepository.strRepositoryName = this.strName;
			objSyncRepository.objRootNodes = this.getRootNodesCopy(objActivity);
			objSyncRepository.boolIsCheckList = this.boolIsCheckList;
			objSyncRepository.boolIsCheckedItemsMadeHidden = this.boolIsCheckedItemsMadeHidden;
			objSyncRepository.boolHiddenSelectionIsActive = this.boolHiddenSelectionIsActive;
			objSyncRepository.boolIsHiddenActive = this.boolIsHiddenActive;
			objSyncRepository.strOwnerUserUuid = this.strOwnerUserUuid;
			objSyncRepository.boolIsDeleted = this.boolIsDeleted;
			objSyncRepository.boolIsShared = this.boolIsShared;
			objSyncRepository.boolIsSubscribed = this.boolIsSubscribed;
			return objSyncRepository;
		}

		public clsSyncTreeNode getTreeNode2SyncTreeNodeCopy(Activity objActivity, clsTreeNode objTreeNode) {
			clsSyncTreeNode objNewTreeNode = new clsSyncTreeNode();
			objNewTreeNode.guidTreeNode = objTreeNode.guidTreeNode;
			objNewTreeNode.strName = objTreeNode.strName;
			objNewTreeNode.intItemType = objTreeNode.enumItemType.getValue();
			objNewTreeNode.resourcePath = objTreeNode.resourcePath;
			objNewTreeNode.resourceId = objTreeNode.resourceId;
			objNewTreeNode.strWebPageURL = objTreeNode.strWebPageURL;
			objNewTreeNode.boolIsChecked = objTreeNode.boolIsChecked;
			objNewTreeNode.boolIsHidden = objTreeNode.boolIsHidden;
			objNewTreeNode.intHash = objTreeNode.intHash;
			objNewTreeNode.strOwnerUserUuid = objTreeNode.strOwnerUserUuid;
			objNewTreeNode.strLastChangedByUserUuid = objTreeNode.strLastChangedByUserUuid;
			objNewTreeNode.strLastChangedDateTimeStamp = objTreeNode.strLastChangedDateTimeStamp;
			objNewTreeNode.boolIsDeleted = objTreeNode.boolIsDeleted;
			objNewTreeNode.boolIsNew = objTreeNode.boolIsNew;
			objNewTreeNode.strAnnotationGson = clsUtils.SerializeToString(objTreeNode.annotation);
			for (clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
				objNewTreeNode.objChildren.add(this.getTreeNode2SyncTreeNodeCopy(objActivity, objChildTreeNode));
			}
			objNewTreeNode.objResourceGroupData = GetLatestResourceGroupData(objActivity, objTreeNode.guidTreeNode);
			
			return objNewTreeNode;
		}

		private clsResourceGroupData GetLatestResourceGroupData(Activity objActivity, UUID guidTreeNode) {
			clsResourceGroupData objResourceGroupData = new clsResourceGroupData();
			// Thumbnail image
			String strImageVersionFullFilename = 
					clsUtils.GetThumbnailImageFileName(objActivity, guidTreeNode.toString());			
			File fileImageVersion = new File(strImageVersionFullFilename);
			String fileName = fileImageVersion.getName();
			
			if (fileImageVersion.exists()) {
				objResourceGroupData.strThumbnailChecksum = clsUtils.GetMd5Code(strImageVersionFullFilename);
				objResourceGroupData.strThumbnailLastChanged = clsUtils.GetFileLastModifiedDate(strImageVersionFullFilename);
			}
			
			// Annotated image
			strImageVersionFullFilename = 
					clsUtils.GetAnnotatedImageFileName(objActivity, guidTreeNode.toString());			
			fileImageVersion = new File(strImageVersionFullFilename);
			fileName = fileImageVersion.getName();

			if (fileImageVersion.exists()) {
				objResourceGroupData.strAnnotatedLastChanged = clsUtils.GetFileLastModifiedDate(strImageVersionFullFilename);
			}

			// Full image
			strImageVersionFullFilename = 
					clsUtils.GetFullImageFileName(objActivity, guidTreeNode.toString());		
			fileImageVersion = new File(strImageVersionFullFilename);
			fileName = fileImageVersion.getName();
			
			if (fileImageVersion.exists()) {
				objResourceGroupData.strFullLastChanged = clsUtils.GetFileLastModifiedDate(strImageVersionFullFilename);
			}
			return objResourceGroupData;
		}

		public boolean isBoolIsNoteSyncable() {
			return boolIsNoteSyncable;
		}

		public void setBoolIsNoteSyncable(boolean boolIsNoteSyncable) {
			this.boolIsNoteSyncable = boolIsNoteSyncable;
		}

		public boolean isBoolIsNoteToBeSynched() {
			return boolIsNoteToBeSynched;
		}

		public void setBoolIsNoteToBeSynched(boolean boolIsNoteToBeSynched) {
			this.boolIsNoteToBeSynched = boolIsNoteToBeSynched;
		}

		public String getName() {
			return this.strName;
		}

		public void setName(String strName) {
			this.strName = strName;
		}

		public void SerializeToFile(File objFile) {
			Gson gson = new Gson();
			String strSerialized = gson.toJson(this);
			try {
				FileWriter out = new FileWriter(objFile);
				out.write(strSerialized);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("Exception", "File write failed: " + e.toString());
			}
		}

		public void SetOwnerOfAllSubNotes(String strRegisteredUserUuid) {
			for (clsTreeNode objChildTreeNode : this.objRootNodes) {
				SetOwnerOfAllSubNotesRecursively(objChildTreeNode, strRegisteredUserUuid);
			}
		}

		private void SetOwnerOfAllSubNotesRecursively(clsTreeNode objTreeNode, String strRegisteredUserUuid) {

			objTreeNode.setStrOwnerUserUuid(strRegisteredUserUuid);
			objTreeNode.setStrLastChangedByUserUuid(strRegisteredUserUuid);
			objTreeNode.setStrLastChangedDateTimeStamp(clsUtils.GetStrCurrentDateTime());

			for (clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
				SetOwnerOfAllSubNotesRecursively(objChildTreeNode, strRegisteredUserUuid);
			}
		}

		
		public clsTreeNode getTreeNodeFromUuid(UUID guidTreeNode) {
			if (guidTreeNode == null)
				return null;
			clsTreeNode objFound;
			for (clsTreeNode objTreeNode : objRootNodes) {
				objFound = findTreeNodeByGuidRecursively(guidTreeNode, objTreeNode);
				if (objFound != null)
					return objFound;
			}
			return null;
		}

		private clsTreeNode findTreeNodeByGuidRecursively(UUID guidTreeNode, clsTreeNode objTreeNode) {
			clsTreeNode objFound;
			if (guidTreeNode.equals(objTreeNode.guidTreeNode)) {
				return objTreeNode;
			}
			for (clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
				objFound = findTreeNodeByGuidRecursively(guidTreeNode, objChildTreeNode);
				if (objFound != null)
					return objFound;
			}
			return null;
		}
		
	}

	@SuppressWarnings("serial")
	public class clsSyncRepository implements Serializable {
		public String strRepositoryUuid = "";
		public String strRepositoryName = "";
		public ArrayList<clsSyncTreeNode> objRootNodes;
		public boolean boolIsCheckList;
		public boolean boolIsCheckedItemsMadeHidden;
		public boolean boolHiddenSelectionIsActive;
		public boolean boolIsHiddenActive;
		public ArrayList<clsShareUser> objSharedUsers = new ArrayList<clsShareUser>();
		public String strOwnerUserUuid = "";
		public boolean boolIsDeleted = false;
		public boolean boolIsShared = false;
		public boolean boolIsSubscribed = false;

		public clsRepository getCopy() {
			clsRepository objRepository = new clsRepository();
			objRepository.uuidRepository = UUID.fromString(this.strRepositoryUuid);
			objRepository.strName = this.strRepositoryName;
			objRepository.objRootNodes = this.getRepositoryRootNodesCopy();
			objRepository.boolIsCheckList = this.boolIsCheckList;
			objRepository.boolIsCheckedItemsMadeHidden = this.boolIsCheckedItemsMadeHidden;
			objRepository.boolHiddenSelectionIsActive = this.boolHiddenSelectionIsActive;
			objRepository.boolIsHiddenActive = this.boolIsHiddenActive;
			objRepository.strOwnerUserUuid = this.strOwnerUserUuid;
			objRepository.boolIsDeleted = this.boolIsDeleted;
			objRepository.boolIsShared = this.boolIsShared;
			objRepository.boolIsSubscribed = this.boolIsSubscribed;
			return objRepository;
		}

		public ArrayList<clsTreeNode> getRepositoryRootNodesCopy() {
			ArrayList<clsTreeNode> objNewRootNodes = new ArrayList<clsTreeNode>();
			for (clsSyncTreeNode objSyncTreeNode : this.objRootNodes) {
				objNewRootNodes.add(getSyncTreeNode2TreeNodeCopy(objSyncTreeNode));
			}
			return objNewRootNodes;
		}

		private clsTreeNode getSyncTreeNode2TreeNodeCopy(clsSyncTreeNode objSyncTreeNode) {
			clsTreeNode objNewTreeNode = new clsTreeNode();
			objNewTreeNode.guidTreeNode = objSyncTreeNode.guidTreeNode;
			objNewTreeNode.strName = objSyncTreeNode.strName;
			enumItemType objItemType[] = enumItemType.values();
			objNewTreeNode.enumItemType = objItemType[objSyncTreeNode.intItemType];
			objNewTreeNode.resourcePath = objSyncTreeNode.resourcePath;
			objNewTreeNode.resourceId = objSyncTreeNode.resourceId;
			objNewTreeNode.objResourceGroupData =objSyncTreeNode.objResourceGroupData;
			objNewTreeNode.strWebPageURL = objSyncTreeNode.strWebPageURL;
			objNewTreeNode.boolIsChecked = objSyncTreeNode.boolIsChecked;
			objNewTreeNode.boolIsHidden = objSyncTreeNode.boolIsHidden;
			objNewTreeNode.intHash = objSyncTreeNode.intHash;
			objNewTreeNode.strOwnerUserUuid = objSyncTreeNode.strOwnerUserUuid;
			objNewTreeNode.strLastChangedByUserUuid = objSyncTreeNode.strLastChangedByUserUuid;
			objNewTreeNode.strLastChangedDateTimeStamp = objSyncTreeNode.strLastChangedDateTimeStamp;
			objNewTreeNode.boolIsDeleted = objSyncTreeNode.boolIsDeleted;
			objNewTreeNode.boolIsNew = objSyncTreeNode.boolIsNew;
			objNewTreeNode.annotation = clsUtils.DeSerializeFromString(objSyncTreeNode.strAnnotationGson,
					clsAnnotationData.class);
			for (clsSyncTreeNode objChildTreeNode : objSyncTreeNode.objChildren) {
				objNewTreeNode.objChildren.add(this.getSyncTreeNode2TreeNodeCopy(objChildTreeNode));
			}
			return objNewTreeNode;
		}
	}

	public class clsShareUser {

		public String strUserUuid = "";
		public String strAddedByUserUuid = "";
		public String strAddedByUserDateTimeStamp = "";

		public clsShareUser(String strUserUuid, String strAddedByUserUuid, String strAddedByUserDateTimeStamp) {
			this.strUserUuid = strUserUuid;
			this.strAddedByUserUuid = strAddedByUserUuid;
			this.strAddedByUserDateTimeStamp = strAddedByUserDateTimeStamp;
		}

	}

	public class clsTreeNode implements Serializable {
		private static final long serialVersionUID = 3336546898052586637L;
		public UUID guidTreeNode;
		private String strName = "";
		public enumItemType enumItemType;
		public String resourcePath = "";
		public int resourceId;
		public clsResourceGroupData objResourceGroupData; // Only used when returned from sever, filled with server data config data
		public String strWebPageURL; // Used to save the URL in case resourceId indicates a webpage resource
		public boolean boolIsSelected;
		public ArrayList<clsTreeNode> objChildren = new ArrayList<clsTreeNode>();
		public boolean boolIsChecked;
		public boolean boolIsHidden;
		private int intHash = 0;
		private boolean _boolIsDirty;
		private String strOwnerUserUuid = "";
		private String strLastChangedByUserUuid = "";
		private String strLastChangedDateTimeStamp = "";
		protected boolean boolIsDeleted = false;
		public clsAnnotationData annotation = null;
		public boolean boolUseAnnotatedImage = false;
		public boolean boolIsNew = false;

		public boolean hasImage()
		{
			return (resourceId == IMAGE_RESOURCE || resourceId == WEB_RESOURCE); 
		}
		
		public boolean getBoolUseAnnotatedImage() {
			return boolUseAnnotatedImage;
		}

		public void setBoolUseAnnotatedImage(boolean boolUseAnnotatedImage) {
			this.boolUseAnnotatedImage = boolUseAnnotatedImage;
		}

		public boolean IsDeleted() {
			return boolIsDeleted;
		}

		public void setIsDeleted(boolean boolIsDeleted) {
			this.boolIsDeleted = boolIsDeleted;
			this.setIsDirty(true);
			// Mark all children as deleted
			for (clsTreeNode objChildTreeNode : this.objChildren) {
				objChildTreeNode.setIsDeleted(boolIsDeleted);
			}
		}

		@SuppressWarnings("static-access")
		public clsTreeNode() {
			this.strName = "NoName";
			guidTreeNode = UUID.randomUUID();
			this.enumItemType = enumItemType.OTHER;
			this.boolIsSelected = false;
			this.resourcePath = "";
			this.resourceId = 0;
			this.strWebPageURL = "";
			this.boolIsChecked = false;
			this.boolIsHidden = false;
			this._boolIsDirty = true;
			this.strOwnerUserUuid = "";
			this.strLastChangedByUserUuid = "";
			this.strLastChangedDateTimeStamp = "";
		}

		public clsTreeNode(String strName, enumItemType enumItemType, boolean boolIsSelected, String resourcePath,
				int resourceId, String strWebPageURL, String strOwnerUserUuid, String strLastChangedByUserUuid) {
			this.strName = strName;
			guidTreeNode = UUID.randomUUID();
			this.enumItemType = enumItemType;
			this.boolIsSelected = boolIsSelected;
			this.resourcePath = resourcePath;
			this.resourceId = resourceId;
			this.strWebPageURL = strWebPageURL;
			this.intHash = clsUtils.GetHashCode(strName);
			this.boolIsChecked = false;
			this.boolIsHidden = false;
			this._boolIsDirty = true;
			this.strOwnerUserUuid = strOwnerUserUuid;
			this.strLastChangedByUserUuid = strLastChangedByUserUuid;
			this.strLastChangedDateTimeStamp = clsUtils.GetStrCurrentDateTime();

			if (resourcePath != "") {
				clsUtils.CustomLog("Renaming temp files");
				// When created and not empty implies there could be a thumbnail
				// file for it which needs renaming
				String strThumbnailFullFilename = ActivityExplorerStartup.fileTreeNodesDir + "/" + guidTreeNode.toString()
						+ ".jpg";
				File objThumbnailFile = new File(strThumbnailFullFilename);
				if (!objThumbnailFile.exists()) {
					clsUtils.CustomLog("Final thumbnail not existing");
					String strTempThumbnailFullFilename = ActivityExplorerStartup.fileTreeNodesDir + "/temp_uuid.jpg";
					File objTempThumbnailFile = new File(strTempThumbnailFullFilename);
					if (objTempThumbnailFile.exists()) {
						clsUtils.CustomLog("Temp thumbnail exists");
						boolean boolSuccess = objTempThumbnailFile.renameTo(objThumbnailFile);
						if (boolSuccess) {
							clsUtils.CustomLog("Success renaming thumbnail temp file");
						} else {
							clsUtils.CustomLog("Problem renaming thumbnail temp file");
						}
						objTempThumbnailFile.delete();
					} else {
						clsUtils.CustomLog("Could not find file: " + objTempThumbnailFile.getAbsolutePath());
					}
					// Create local version of full image for annotation if
					// necessary
					{
						String strTempAnnotateFullFilename = ActivityExplorerStartup.fileTreeNodesDir
								+ "/temp_uuid_full.jpg";
						File objTempAnnotateFile = new File(strTempAnnotateFullFilename);

						if (objTempAnnotateFile.exists()) {
							String strAnnotateFullFilename = ActivityExplorerStartup.fileTreeNodesDir + "/"
									+ guidTreeNode.toString() + "_full.jpg";
							File objAnnotateThumbnailFile = new File(strAnnotateFullFilename);
							
							boolean boolSuccess = objTempAnnotateFile.renameTo(objAnnotateThumbnailFile);
							if (boolSuccess) {
								clsUtils.CustomLog("Success renaming full temp file");
							} else {
								clsUtils.CustomLog("Problem renaming full temp file");
							}
							objTempAnnotateFile.delete();

							// Workaround for local files. Replace temporary resourcePath by actual 
							if(this.resourcePath.contains("temp_uuid"))
							{
								this.resourcePath = strAnnotateFullFilename;
							}
						}
					}
					// Create local version of full image for annotation if
					// necessary
					{
						String strTempAnnotateFullFilename = ActivityExplorerStartup.fileTreeNodesDir
								+ "/temp_uuid_annotated.jpg";
						File objTempAnnotateFile = new File(strTempAnnotateFullFilename);

						if (objTempAnnotateFile.exists()) {
							String strAnnotateFullFilename = ActivityExplorerStartup.fileTreeNodesDir + "/"
									+ guidTreeNode.toString() + "_annotated.jpg";
							File objAnnotateThumbnailFile = new File(strAnnotateFullFilename);
							
							objTempAnnotateFile.renameTo(objAnnotateThumbnailFile);
							objTempAnnotateFile.delete();
						}
					}
				}
			}
		}

		public String getName() {
			if (strName.isEmpty())
				return "";
			return this.strName;
		}

		public void setName(String strName) {
			int intNewHash = clsUtils.GetHashCode(strName);
			if (intNewHash == intHash) {
				setIsDirty(false);
			} else {
				setIsDirty(true);
			}
			this.strName = strName;
		}

		public boolean getIsDirty() {
			return _boolIsDirty;
		}

		public void setIsDirty(boolean boolIsDirty) {
			this._boolIsDirty = boolIsDirty;
		}

		public boolean getSelected() {
			return this.boolIsSelected;
		}

		public void setSelected(boolean boolIsSelected) {
			this.boolIsSelected = boolIsSelected;
		}

		public boolean getChecked() {
			return this.boolIsChecked;
		}

		public void setChecked(boolean boolIsChecked) {
			if (IsTreeViewPurposeIsToFindCheckStateOnly() == false) {
				// Flag dirty conditionally if check changes
				if (this.boolIsChecked != boolIsChecked) {
					setIsDirty(true);
				} else {
					setIsDirty(false);
				}
			} else {
				// Do not flag dirty if check changes
				setIsDirty(false);
			}
			this.boolIsChecked = boolIsChecked;
		}

		// To override by child classes
		public boolean IsTreeViewPurposeIsToFindCheckStateOnly() {
			return false;
		}

		public boolean getHidden() {
			return this.boolIsHidden;
		}

		public void setHidden(boolean boolIsHidden) {
			this.boolIsHidden = boolIsHidden;
		}

		public boolean IsAllUserFolder() {
			return false;
		}


		public String getStrOwnerUserUuid() {
			return strOwnerUserUuid;
		}

		public void setStrOwnerUserUuid(String strOwnerUserUuid) {
			this.strOwnerUserUuid = strOwnerUserUuid;
		}

		public boolean IsToBeSynched() {
			return true; // For now, sync all
		}

		public String getStrLastChangedByUserUuid() {
			return strLastChangedByUserUuid;
		}

		public void setStrLastChangedByUserUuid(String strLastChangedByUserUuid) {
			this.strLastChangedByUserUuid = strLastChangedByUserUuid;
		}

		public String getStrLastChangedDateTimeStamp() {
			return strLastChangedDateTimeStamp;
		}

		public void setStrLastChangedDateTimeStamp(String strLastChangedDateTimeStamp) {
			this.strLastChangedDateTimeStamp = strLastChangedDateTimeStamp;
		}

		public boolean boolIsNodeDisplayedAsComment(clsGroupMembers objGroupMembers, clsTreeview objTreeview) {
			String strRegisteredUserUuid = objGroupMembers.objMembersRepository.getStrRegisteredUserUuid();
			String strNoteOwnerUuid = objTreeview.getRepository().getNoteOwnerUserUuid();
			String strItemOwnerUuid = this.strOwnerUserUuid;

			if (strRegisteredUserUuid.isEmpty()) {
				// If unregistered, all items are displayed as normal items
				return false;
			}

			// Depending whether item is note or comment item
			if (strItemOwnerUuid.equals(strNoteOwnerUuid)) {
				// Note item
				// Note item will display as comment if child to a comment item
				clsTreeNode objParentTreeNode = objTreeview.getParentTreeNode(this);
				if (objParentTreeNode == null) {
					// Item is at topmost level
					if (strRegisteredUserUuid.equals(strItemOwnerUuid)) {
						// This note item is on owner machine. display as note
						return false;
					} else {
						// This note item is on another machine. display as comment
						return true;
					}		
				}
				// Parent is not topmost
				if (!objParentTreeNode.boolIsNodeDisplayedAsComment(objGroupMembers, objTreeview)) {
					// Parent item is not a comment, so this item could be
					// displayed as a note item
					if (strRegisteredUserUuid.equals(strItemOwnerUuid)) {
						// This note item is on owner machine. display as note
						return false;
					} else {
						// This note item is on another machine. display as comment
						return true;
					}	
				} else {
					// Parent item is a comment, so note item will be displayed
					// as a comment item
					return true;
				}
			} else {
				// Comment item
				// If comment item, always display as comment
				return true;
			}

		}

		public boolean boolIsOwnNote(clsGroupMembers objGroupMembers, clsTreeview objTreeview) {
			String strRegisteredUserUuid = objGroupMembers.objMembersRepository.getStrRegisteredUserUuid();
			String strNoteOwnerUuid = objTreeview.getRepository().getNoteOwnerUserUuid();
			if (strRegisteredUserUuid.equals(strNoteOwnerUuid)) {
				return true;
			} else {
				return false;
			}
		}

		public boolean boolIsOwnComment(clsGroupMembers objGroupMembers, clsTreeview objTreeview) {
			// Comment will be displayed as Green = Yes, Yellow = No
			String strRegisteredUserUuid = objGroupMembers.objMembersRepository.getStrRegisteredUserUuid();
			String strItemOwnerUuid = this.strOwnerUserUuid;
			if (strRegisteredUserUuid.equals(strItemOwnerUuid)) {
				// Comment belongs to user
				return true;
			} else {
				// Comment does not belongs to user
				return false;
			}
		}

		public boolean IsEmptyOrAllChildrenDeleted() {
			if (this.objChildren.size() == 0)
				return true;
			boolean boolAllChildrenDeleted = true;
			for (clsTreeNode objTreeNode : this.objChildren) {
				if (objTreeNode.boolIsDeleted == false)
					return false;
			}
			return boolAllChildrenDeleted;
		}
	}
	
	public class clsResourceGroupData {
		public String strThumbnailChecksum;
		public String strThumbnailLastChanged;
		public String strAnnotatedLastChanged;
		public String strFullLastChanged;
	}
	


	public class clsSyncTreeNode implements Serializable {
		public static final long serialVersionUID = 3336546898052586637L;
		public UUID guidTreeNode;
		public String strName = "";
		public ArrayList<clsSyncTreeNode> objChildren = new ArrayList<clsSyncTreeNode>();
		public int intItemType;
		public String resourcePath = "";
		public clsResourceGroupData objResourceGroupData;
		public int resourceId;
		public String strWebPageURL;
		public boolean boolIsChecked;
		public boolean boolIsHidden;
		public int intHash = 0;
		public String strOwnerUserUuid = "";
		public String strLastChangedByUserUuid = "";
		public String strLastChangedDateTimeStamp = "";
		public boolean boolNodeIsComment = false;
		public boolean boolIsDeleted = false;
		public boolean boolIsNew = false;
		public String strAnnotationGson;
	}

	public class clsUserGroupMember implements Serializable {
		private static final long serialVersionUID = 1L;
		public String strUserUuid = "";
		public String strUserName = "";
	}

	public class clsUserGroup implements Serializable {
		ArrayList<clsUserGroupMember> objUserGroupMembers = new ArrayList<clsUserGroupMember>();
		ArrayList<clsUserGroup> objUserGroups = new ArrayList<clsUserGroup>();
		private static final long serialVersionUID = 1L;
		public String strName = "";
	}

	public clsTreeNode getClone(clsTreeNode objTreeNode, boolean boolResetUuid) {
		clsTreeNode objNewTreeNode = new clsTreeNode();
		objNewTreeNode.setName(objTreeNode.getName());
		objNewTreeNode.guidTreeNode = (boolResetUuid) ? UUID.randomUUID() : objTreeNode.guidTreeNode;
		objNewTreeNode.enumItemType = objTreeNode.enumItemType;
		objNewTreeNode.boolIsSelected = false;
		objNewTreeNode.resourcePath = objTreeNode.resourcePath;
		objNewTreeNode.resourceId = objTreeNode.resourceId;
		objNewTreeNode.strWebPageURL = objTreeNode.strWebPageURL;
		objNewTreeNode.intHash = objTreeNode.intHash;
		objNewTreeNode.strOwnerUserUuid = objTreeNode.strOwnerUserUuid;
		objNewTreeNode.strLastChangedByUserUuid = objTreeNode.strLastChangedByUserUuid;
		objNewTreeNode.strLastChangedDateTimeStamp = objTreeNode.strLastChangedDateTimeStamp;
		objNewTreeNode.boolIsDeleted = objTreeNode.boolIsDeleted;
		objNewTreeNode.boolIsNew = objTreeNode.boolIsNew;
		objNewTreeNode.boolUseAnnotatedImage = objTreeNode.boolUseAnnotatedImage;
		objNewTreeNode.annotation = clsUtils.CloneDeep(objTreeNode.annotation, clsAnnotationData.class);
		if (objNewTreeNode.resourcePath != "") {
			// Make copy of thumbnail file since the UUID is different
			String strSourceFilename = objTreeNode.guidTreeNode.toString() + ".jpg";
			String strDestFilename = objNewTreeNode.guidTreeNode.toString() + ".jpg";
			File objFileSource = new File(ActivityExplorerStartup.fileTreeNodesDir, strSourceFilename);
			File objFileDest = new File(ActivityExplorerStartup.fileTreeNodesDir, strDestFilename);
			try {
				ActivityNoteStartup.copyDirectoryOneLocationToAnotherLocation(objFileSource, objFileDest);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		for (clsTreeNode objNewChildNode : objTreeNode.objChildren) {
			objNewTreeNode.objChildren.add(getClone(objNewChildNode, boolResetUuid));
		}
		return objNewTreeNode;
	}

	public ArrayList<clsListItem> getListItems() {
		// Build ListItemsArray which emulates what should be displayed by the
		// treeview
		ArrayList<clsListItem> objListItems = new ArrayList<clsListItem>();
		int intLevel = -1;
		for (clsTreeNode objTreeNode : getRepository().objRootNodes) {
			objListItems = getListItemsRecursively(objTreeNode, intLevel, objListItems);
		}

		// Run through objListItems again to determine their relations with each
		// other for drawing purposes
		clsListItem objAboveListItem = null;
		clsListItem objThisListItem = null;
		clsListItem objBelowListItem = null;
		for (int i = 0; i < objListItems.size(); i++) {
			objThisListItem = (clsListItem) objListItems.get(i);
			if (i == 0) {
				objAboveListItem = (clsListItem) objListItems.get(i);
			} else {
				objAboveListItem = (clsListItem) objListItems.get(i - 1);
			}
			if (i >= objListItems.size() - 1) {
				objBelowListItem = (clsListItem) objListItems.get(i);
			} else {
				objBelowListItem = (clsListItem) objListItems.get(i + 1);
			}

			if (objAboveListItem.getLevel() == objThisListItem.getLevel()) {
				objThisListItem.SetAboveItemLevelRelation(enumItemLevelRelation.SAME);
			} else if (objAboveListItem.getLevel() < objThisListItem.getLevel()) {
				objThisListItem.SetAboveItemLevelRelation(enumItemLevelRelation.LOWER);
			} else {
				objThisListItem.SetAboveItemLevelRelation(enumItemLevelRelation.HIGHER);
			}

			if (objBelowListItem.getLevel() == objThisListItem.getLevel()) {
				objThisListItem.SetBelowItemLevelRelation(enumItemLevelRelation.SAME);
			} else if (objBelowListItem.getLevel() < objThisListItem.getLevel()) {
				objThisListItem.SetBelowItemLevelRelation(enumItemLevelRelation.LOWER);
			} else {
				objThisListItem.SetBelowItemLevelRelation(enumItemLevelRelation.HIGHER);
			}
		}
		if (objListItems.size() != 0) {
			objListItems.get(0).SetAboveItemLevelRelation(enumItemLevelRelation.HIGHER);
			objListItems.get(objListItems.size() - 1).SetBelowItemLevelRelation(enumItemLevelRelation.HIGHER);
		}

		return objListItems;
	}

	private ArrayList<clsListItem> getListItemsRecursively(clsTreeNode objTreeNode, int intLevel,
			ArrayList<clsListItem> objListItems) {
		// All items in this list will be displayed. To hide, do not put them in
		// this list
		// Determine firstly if item needs to be displayed

		// Evaluate deleted first
		if (objTreeNode.boolIsDeleted)
			return objListItems;

		// Evaluate "hidden" next
		boolean boolDisplayItem = false;
		if (getRepository().boolHiddenSelectionIsActive) {
			// Busy selecting items to hide, all must display
			boolDisplayItem = true;
		} else {
			if (getRepository().boolIsHiddenActive == false) {
				// Hidden is not activated, so display all
				boolDisplayItem = true;
			} else {
				// Hidden is activated, so display only those marked as not
				// hidden
				if (objTreeNode.boolIsHidden == false) {
					boolDisplayItem = true;
				}
			}
		}
		// However if item is a topmost level item, do not hide (user have no
		// idea there are items hidden)
		if (boolDisplayItem == false && getParentTreeNode(objTreeNode) == null) {
			boolDisplayItem = true;
		}

		if (boolDisplayItem == false) {
			return objListItems;
		}

		// Now evaluate the checklist component
		boolDisplayItem = false;
		// Now check what happens if it is a checklist
		if (getRepository().boolIsCheckList) {
			if (getRepository().boolIsCheckedItemsMadeHidden == true) {
				// System is set to hide all checked items, so see if checked
				// item
				if (objTreeNode.boolIsChecked == false) {
					boolDisplayItem = true;
				}
			} else {
				// System not set to hide checked items, display all
				boolDisplayItem = true;
			}
		} else {
			// No checklist items, so display all
			boolDisplayItem = true;
		}

		if (boolDisplayItem == false) {
			return objListItems;
		}

		// If to be displayed, add it to the list item collection
		intLevel += 1;
		boolean boolIsAnnotated = (objTreeNode.annotation != null) ? true : false;
		clsListItem objListItem = new clsListItem(objTreeNode.getName(), intLevel, objTreeNode.guidTreeNode,
				getItemType(objTreeNode), objTreeNode.getSelected(), objTreeNode.resourcePath, objTreeNode.resourceId,
				boolIsAnnotated, objTreeNode.strWebPageURL);

		// Fill in the _boolFolderHasHiddenItems property. If any child with
		// hidden items, return true
		if ((objListItem.getItemType() == enumItemType.FOLDER_COLLAPSED)
				|| (objListItem.getItemType() == enumItemType.FOLDER_EXPANDED)) {
			objListItem.setFolderHasHiddenItems(IsAnyHiddenItems(objTreeNode));
		}
		// Fill in the NewItem property
		objListItem.intNewItemType = enumNewItemType.OLD;
		// If topmost item, determine if any children are new
		if (getParentTreeNode(objTreeNode) == null) {			
			if (objTreeNode.boolIsNew) {
				// Topmost item, new
				if (IsChildItemNewItem(objTreeNode)) {
					objListItem.intNewItemType = enumNewItemType.NEW_AND_ROOT_PARENT_OF_NEW;
				} else {
					objListItem.intNewItemType = enumNewItemType.NEW;
				}
			} else {
				// Topmost item, old
				if (IsChildItemNewItem(objTreeNode)) {
					objListItem.intNewItemType = enumNewItemType.ROOT_PARENT_OF_NEW;
				}
			}			
		} else {
			// Leave item
			if (objTreeNode.boolIsNew) {
				// Leave item, new
				if (IsChildItemNewItem(objTreeNode)) {
					objListItem.intNewItemType = enumNewItemType.NEW_AND_PARENT_OF_NEW;
				} else {
					objListItem.intNewItemType = enumNewItemType.NEW;
				}
			} else {
				// Leave item, old
				if (IsChildItemNewItem(objTreeNode)) {
					objListItem.intNewItemType = enumNewItemType.PARENT_OF_NEW;
				}
			}
		}

		objListItems.add(objListItem);

		if (objTreeNode.enumItemType == enumItemType.FOLDER_COLLAPSED)
			return objListItems;

		for (clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
			objListItems = getListItemsRecursively(objChildTreeNode, intLevel, objListItems);
		}

		return objListItems;
	}

	private boolean IsChildItemNewItem(clsTreeNode objTreeNode) {
		// TODO Auto-generated method stub
		for (clsTreeNode objChildTreeNode: objTreeNode.objChildren ) {
			if (objChildTreeNode.boolIsNew) {
				return true;
			}
			if (IsChildItemNewItem(objChildTreeNode)) {
				return true;
			}
		}
		return false;
	}

	public boolean IsNoteShared() {
		if (this.getRepository().boolIsShared || this.getRepository().boolIsSubscribed){
			return true;
		}
		return false;
	}

	private boolean IsAnyHiddenItems(clsTreeNode objParentTreeNode) {
		if (objParentTreeNode.getHidden() == true)
			return true;
		for (clsTreeNode objChildTreeNode : objParentTreeNode.objChildren) {
			if (objChildTreeNode.getHidden() == true)
				return true;
			if (IsAnyHiddenItems(objChildTreeNode) == true)
				return true;
		}

		return false;
	}

	private enumItemType getItemType(clsTreeNode objTreeNode) {

		if (objTreeNode.enumItemType != enumItemType.OTHER) {
			if (objTreeNode.objChildren.size() == 0) {
				return enumItemType.FOLDER_EMPTY;
			}
		}

		return objTreeNode.enumItemType;
	}

	public clsTreeNode getTreeNodeFromUuid(UUID guidTreeNode) {
		return  getRepository().getTreeNodeFromUuid(guidTreeNode);
	}


	public clsTreeNode getParentTreeNode(clsTreeNode objSearchChildTreenode) {
		clsTreeNode objFound = null;
		for (clsTreeNode objChildTreeNode : getRepository().objRootNodes) {
			objFound = findParentTreeNodeRecursively(objSearchChildTreenode, objChildTreeNode);
			if (objFound != null) {
				return objFound;
			}
		}
		return null;
	}

	private clsTreeNode findParentTreeNodeRecursively(clsTreeNode objSearchChildTreenode, clsTreeNode objParentTreenode) {
		clsTreeNode objFound = null;
		for (clsTreeNode objChildTreeNode : objParentTreenode.objChildren) {
			// Just go down one level to see if search node can be found
			if (objChildTreeNode.guidTreeNode == objSearchChildTreenode.guidTreeNode) {
				return objParentTreenode;
			}
		}
		// Could not be found, so need to go deeper
		for (clsTreeNode objChildTreeNode : objParentTreenode.objChildren) {
			objFound = findParentTreeNodeRecursively(objSearchChildTreenode, objChildTreeNode);
			if (objFound != null) {
				return objFound;
			}
		}
		return null;
	}

	public ArrayList<clsTreeNode> getSelectedTreenodes() {
		ArrayList<clsTreeNode> objsFound = new ArrayList<clsTreeNode>();
		for (clsTreeNode objTreeNode : getRepository().objRootNodes) {
			objsFound = findSelectedTreeNodesRecursively(objsFound, objTreeNode);
		}
		return objsFound;
	}

	private ArrayList<clsTreeNode> findSelectedTreeNodesRecursively(ArrayList<clsTreeNode> objsFound,
			clsTreeNode objTreeNode) {
		if (objTreeNode.getSelected()) {
			objsFound.add(objTreeNode);
		}
		for (clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
			objsFound = findSelectedTreeNodesRecursively(objsFound, objChildTreeNode);
		}
		return objsFound;
	}

	public void setSingleSelectedTreenode(clsTreeNode objTreeNode) {
		// Unselect current selection
		ArrayList<clsTreeNode> objPrevSelectedTreenodes = getSelectedTreenodes();
		if (objPrevSelectedTreenodes.size() != 0) {
			for (clsTreeNode objPrevSelectedTreeNode : objPrevSelectedTreenodes) {
				objPrevSelectedTreeNode.setSelected(false);
			}
		}
		// Select the new one
		objTreeNode.setSelected(true);
	}

	public void setMultipleSelectedTreenodesFalse() {
		// Unselect current selection
		ArrayList<clsTreeNode> objPrevSelectedTreenodes = getSelectedTreenodes();
		if (objPrevSelectedTreenodes.size() == 1)
			return; // Only delete if more than one set
		if (objPrevSelectedTreenodes.size() != 0) {
			for (clsTreeNode objPrevSelectedTreeNode : objPrevSelectedTreenodes) {
				objPrevSelectedTreeNode.setSelected(false);
			}
		}
	}

	public void setMultipleSelectedTreenodes(clsTreeNode objTreeNode) {
		// If this one already selected, unselect it (toggle)
		ArrayList<clsTreeNode> objPrevSelectedTreenodes = getSelectedTreenodes();
		if (objPrevSelectedTreenodes.size() != 0) {
			// Toggle one that is already set
			for (clsTreeNode objPrevSelectedTreeNode : objPrevSelectedTreenodes) {
				if (objPrevSelectedTreeNode.equals(objTreeNode)) {
					objTreeNode.setSelected(false);
					return;
				}
			}
		}
		// Select the new one
		objTreeNode.setSelected(true);
	}

	public void setTreeNodeItemOrder(clsTreeNode objSelectedTreeNode, int intOrder) {
		ArrayList<clsTreeNode> objNewTreeNodes;
		clsTreeNode objParentTreeNode = getParentTreeNode(objSelectedTreeNode);
		if (objParentTreeNode == null) {
			// Root level
			objNewTreeNodes = GetReorderedTreeNodes(getRepository().objRootNodes, objSelectedTreeNode, intOrder);
			getRepository().objRootNodes = objNewTreeNodes;
		} else {
			// Child level
			objNewTreeNodes = GetReorderedTreeNodes(objParentTreeNode.objChildren, objSelectedTreeNode, intOrder);
			objParentTreeNode.objChildren = objNewTreeNodes;
		}
	}

	public int getTreeNodeItemOrder(clsTreeNode objSelectedTreeNode) {
		ArrayList<clsTreeNode> objSourceTreeNodes;
		int inOrder = 0;
		clsTreeNode objParentTreeNode = getParentTreeNode(objSelectedTreeNode);
		if (objParentTreeNode == null) {
			objSourceTreeNodes = getRepository().objRootNodes;
		} else {
			objSourceTreeNodes = objParentTreeNode.objChildren;
		}
		for (clsTreeNode objTreeNode : objSourceTreeNodes) {
			if (objTreeNode.equals(objSelectedTreeNode))
				return inOrder;
			inOrder += 1;
		}
		return 0;
	}

	private ArrayList<clsTreeNode> GetReorderedTreeNodes(ArrayList<clsTreeNode> objSourceTreeNodes,
			clsTreeNode objSelectedTreeNode, int intNewOrder) {
		if (intNewOrder < 0) {
			// Clamping
			intNewOrder = 0;
		}

		if (intNewOrder >= objSourceTreeNodes.size()) {
			// Clamping
			intNewOrder = objSourceTreeNodes.size() - 1;
		}
		ArrayList<clsTreeNode> objNewTreeNodes = new ArrayList<clsTreeNode>();
		int intOrderCount = 0;
		objSourceTreeNodes.remove(objSelectedTreeNode);
		for (intOrderCount = 0; intOrderCount < objSourceTreeNodes.size(); intOrderCount++) {
			if (intNewOrder == intOrderCount) {
				objNewTreeNodes.add(objSelectedTreeNode);
			}
			objNewTreeNodes.add(objSourceTreeNodes.get(intOrderCount));
		}
		if (intNewOrder == intOrderCount) {
			// New one added is at the end
			objNewTreeNodes.add(objSelectedTreeNode);
		}
		return objNewTreeNodes;
	}

	public enum enumItemType {
		FOLDER_EXPANDED(0), FOLDER_COLLAPSED(1), FOLDER_EMPTY(2), OTHER(3);

		final int numTab;

		private enumItemType(int num) {
			this.numTab = num;
		}

		public int getValue() {
			return this.numTab;
		}

	};

	static enum enumItemLevelRelation {
		SAME, HIGHER, LOWER;
	}

	static enum enumCutCopyPasteState {
		INACTIVE, CUTTING, PASTING;
	}

	private class clsTreeNodeRemoveData {

		public clsTreeNode objTreeNode;
		public ArrayList<clsTreeNode> objParentChildren;

		public clsTreeNodeRemoveData(clsTreeNode objTreeNode, ArrayList<clsTreeNode> objParentTreeNode) {
			this.objTreeNode = objTreeNode;
			this.objParentChildren = objParentTreeNode;
		}
	}

	public void RemoveAllIsDeletedTreeNodes(boolean boolDeleteThumbnail) {

		// Build a list of nodes to be deleted
		ArrayList<clsTreeNodeRemoveData> arrTreeNodesToBeRemoved = new ArrayList<clsTreeNodeRemoveData>();

		for (clsTreeNode objTreeNode : this.getRepository().objRootNodes) {
			FindIsDeletedTreeNodes(arrTreeNodesToBeRemoved, objTreeNode, this.getRepository().objRootNodes);
		}

		// Delete the items in reverse order
		for (int i = arrTreeNodesToBeRemoved.size() - 1; i >= 0; i--) {
			clsTreeNodeRemoveData objTreeNodeRemoveData = arrTreeNodesToBeRemoved.get(i);
			// If there is a thumbnail, delete that first
			if (objTreeNodeRemoveData.objTreeNode.resourcePath != "" && boolDeleteThumbnail) {
				String strDelFilename = objTreeNodeRemoveData.objTreeNode.guidTreeNode.toString() + ".jpg";
				File objFileDelete = new File(ActivityExplorerStartup.fileTreeNodesDir, strDelFilename);
				ActivityNoteStartup.DeleteRecursive(objFileDelete);
			}
			// Delete node
			objTreeNodeRemoveData.objParentChildren.remove(objTreeNodeRemoveData.objTreeNode);
		}
		
		// Set treeview as dirty so it can be prompted for save
		SetAllIsDirty(true);

	}

	private void FindIsDeletedTreeNodes(ArrayList<clsTreeNodeRemoveData> arrTreeNodesToBeRemoved,
			clsTreeNode objTreeNode, ArrayList<clsTreeNode> objParentChildrenArrayList) {
		if (objTreeNode.boolIsDeleted) {
			clsTreeNodeRemoveData objTreeNodeRemoveData = new clsTreeNodeRemoveData(objTreeNode,
					objParentChildrenArrayList);
			arrTreeNodesToBeRemoved.add(objTreeNodeRemoveData);
		}
		for (clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
			FindIsDeletedTreeNodes(arrTreeNodesToBeRemoved, objChildTreeNode, objTreeNode.objChildren);
		}
	}

	public void RemoveTreeNode(clsTreeNode objDeletedTreeNode, boolean boolDeleteThumbnail) {
		// Not used by notes, notes are synced and removal needs to be delayed
		// If there is a thumbnail, delete that first
		if (objDeletedTreeNode.resourcePath != "" && boolDeleteThumbnail) {
			String strDelFilename = objDeletedTreeNode.guidTreeNode.toString() + ".jpg";
			File objFileDelete = new File(ActivityExplorerStartup.fileTreeNodesDir, strDelFilename);
			ActivityNoteStartup.DeleteRecursive(objFileDelete);
		}

		// Delete children first recursively
		while (objDeletedTreeNode.objChildren.size() != 0) {
			RemoveTreeNode(objDeletedTreeNode.objChildren.get(0), boolDeleteThumbnail);
		}

		// Delete this element
		clsTreeNode objPeerTreeNode = getTreeNodeFromUuid(objDeletedTreeNode.guidTreeNode);
		clsTreeNode objParentTreeNode = getParentTreeNode(objPeerTreeNode);
		if (objParentTreeNode == null) {
			getRepository().objRootNodes.remove(objDeletedTreeNode);
		} else {
			objParentTreeNode.objChildren.remove(objDeletedTreeNode);
		}
	}

	public ArrayList<clsTreeNode> RecursiveGetCheckedTreeNode(clsTreeNode objCurrentTreeNode,
			ArrayList<clsTreeNode> objFoundTreeNodes) {

		if (objCurrentTreeNode.getChecked() == true) {
			objFoundTreeNodes.add(objCurrentTreeNode);
		}

		for (clsTreeNode objChildTreeNode : objCurrentTreeNode.objChildren) {
			objFoundTreeNodes = RecursiveGetCheckedTreeNode(objChildTreeNode, objFoundTreeNodes);
		}
		return objFoundTreeNodes;

	}

	public void PasteClipboardTreeNodes(clsTreeNode objSelectedTreeNode, boolean boolPasteAtSameLevel,
			boolean boolDeleteSourceTreeNodes) {
		ArrayList<clsTreeNode> objClonedClipboardTreeNodes;
		if (boolDeleteSourceTreeNodes) {
			// Clone the clipboard but retain UUIDs
			objClonedClipboardTreeNodes = this.getClone(objClipboardTreeNodes, false);
			// Delete the selected treenodes as it is a CUT/paste operation
			for (clsTreeNode objClipBoardTreeNode : objClipboardTreeNodes) {
				RemoveTreeNode(objClipBoardTreeNode, false); // Don't delete
																// thumbnails
																// because items
																// are only
																// moved
			}
		} else {
			// Clone the clipboard but get new UUids
			objClonedClipboardTreeNodes = this.getClone(objClipboardTreeNodes, true);
		}
		// Replace clipboard with cloned items
		this.objClipboardTreeNodes = objClonedClipboardTreeNodes;

		// Do the paste
		ArrayList<clsTreeNode> objNewTreeNodes;
		if (objSelectedTreeNode == null) {
			// To be added at the end of root
			getRepository().objRootNodes.addAll(objClipboardTreeNodes);
		} else {
			if (boolPasteAtSameLevel) {
				// Paste at same level after selection
				clsTreeNode objParentTreeNode = getParentTreeNode(objSelectedTreeNode);
				if (objParentTreeNode == null) {
					// Add at root level but after selection
					objNewTreeNodes = InsertedClipboardAfterSelectedTreeNode(getRepository().objRootNodes,
							objSelectedTreeNode);
					getRepository().objRootNodes = objNewTreeNodes;
				} else {
					// Add at child level after selection
					objNewTreeNodes = InsertedClipboardAfterSelectedTreeNode(objParentTreeNode.objChildren,
							objSelectedTreeNode);
					objParentTreeNode.objChildren = objNewTreeNodes;
					objParentTreeNode.enumItemType = enumItemType.FOLDER_EXPANDED;
				}
			} else {
				// Paste at same level below selection
				for (clsTreeNode objClipTreeNode : objClipboardTreeNodes) {
					objSelectedTreeNode.objChildren.add(objClipTreeNode);
				}
				objSelectedTreeNode.enumItemType = enumItemType.FOLDER_EXPANDED;
			}
		}

	}

	private ArrayList<clsTreeNode> InsertedClipboardAfterSelectedTreeNode(ArrayList<clsTreeNode> objSourceTreeNodes,
			clsTreeNode objSelectedTreeNode) {
		ArrayList<clsTreeNode> objNewTreeNodes = new ArrayList<clsTreeNode>();
		for (clsTreeNode objTreeNode : objSourceTreeNodes) {
			objNewTreeNodes.add(objTreeNode);
			if (objTreeNode.equals(objSelectedTreeNode)) {
				for (clsTreeNode objClipTreeNode : objClipboardTreeNodes) {
					objNewTreeNodes.add(objClipTreeNode);
				}
			}
		}
		return objNewTreeNodes;
	}

	public ArrayList<clsTreeNode> getClone(ArrayList<clsTreeNode> objSelectedTreeNodes, boolean boolResetUuid) {
		ArrayList<clsTreeNode> objNewTreeNodes = new ArrayList<clsTreeNode>();
		for (clsTreeNode objTreeNode : objSelectedTreeNodes) {
			objNewTreeNodes.add(getClone(objTreeNode, boolResetUuid));
		}
		return objNewTreeNodes;
	}

	public void ClearAll() {
		
		try {
			while (getRepository().objRootNodes.size() != 0) {
				RemoveTreeNode(getRepository().objRootNodes.get(0), true);
			}
			getRepository().objImageLoadDatas.clear();
			getRepository().boolIsShared = false;
			getRepository().boolIsSubscribed = false;
			
		} catch (Exception e) {
			clsUtils.CustomLog("Error clearing repository. " + e.getStackTrace().toString());
		}
	}

	public void RecursiveSetChildrenChecked(clsTreeNode objTreeNode, boolean boolChecked) {
		objTreeNode.setChecked(boolChecked);
		for (clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
			RecursiveSetChildrenChecked(objChildTreeNode, boolChecked);
		}

	}

	public void RecursiveSetParentChecked(clsTreeNode objChildTreeNode, boolean boolChecked) {
		clsTreeNode objParentTreeNode = getParentTreeNode(objChildTreeNode);
		if (objParentTreeNode != null) {
			objParentTreeNode.setChecked(boolChecked);
			RecursiveSetParentChecked(objParentTreeNode, boolChecked);
		}
	}

	public void RecursiveSetChildrenHidden(clsTreeNode objTreeNode, boolean boolIsHidden) {
		objTreeNode.setHidden(boolIsHidden);
		for (clsTreeNode objChildTreeNode : objTreeNode.objChildren) {
			RecursiveSetChildrenHidden(objChildTreeNode, boolIsHidden);
		}

	}

	public void RecursiveSetParentHidden(clsTreeNode objChildTreeNode, boolean boolIsHidden) {
		clsTreeNode objParentTreeNode = getParentTreeNode(objChildTreeNode);
		if (objParentTreeNode != null) {
			objParentTreeNode.setChecked(boolIsHidden);
			RecursiveSetParentChecked(objParentTreeNode, boolIsHidden);
		}
	}

	public void RecursiveSetAllChecked(boolean boolChecked) {
		for (clsTreeNode objChildTreeNode : getRepository().objRootNodes) {
			RecursiveSetChildrenChecked(objChildTreeNode, boolChecked);
		}
	}

	public void RecursiveRemoveAllChecked() {
		ArrayList<clsTreeNode> objFoundTreeNodes = new ArrayList<clsTreeNode>();

		for (clsTreeNode objChildTreeNode : getRepository().objRootNodes) {
			objFoundTreeNodes = RecursiveGetCheckedTreeNode(objChildTreeNode, objFoundTreeNodes);
		}

		for (int i = objFoundTreeNodes.size() - 1; i >= 0; i--) {
			RemoveTreeNode(objFoundTreeNodes.get(i), true);
		}
	}

	public boolean IsAnyChildrenChecked(clsTreeNode objParentTreeNode) {
		for (clsTreeNode objChildTreeNode : objParentTreeNode.objChildren) {
			if (objChildTreeNode.getChecked() == false)
				return false;
			if (IsAnyChildrenChecked(objChildTreeNode) == false)
				return false;
		}
		return true;
	}

	public void ClearSelection() {
		for (clsTreeNode objTreeNode : getRepository().objRootNodes) {
			ClearSelectionRecursively(objTreeNode);
		}
	}

	private void ClearSelectionRecursively(clsTreeNode objParentTreeNode) {
		objParentTreeNode.setSelected(false);
		for (clsTreeNode objChildTreeNode : objParentTreeNode.objChildren) {
			ClearSelectionRecursively(objChildTreeNode);
		}
	}

	public void SetAllIsDirty(boolean boolIsDirty) {
		for (clsTreeNode objChildTreeNode : getRepository().objRootNodes) {
			SetIsDirtyRecursively(objChildTreeNode, boolIsDirty);
		}
		getRepository().boolIsDirty = boolIsDirty;
	}

	private void SetIsDirtyRecursively(clsTreeNode objParentTreeNode, boolean boolIsDirty) {
		objParentTreeNode.setIsDirty(boolIsDirty);
		for (clsTreeNode objChildTreeNode : objParentTreeNode.objChildren) {
			SetIsDirtyRecursively(objChildTreeNode, boolIsDirty);
		}
	}

	public boolean GetAllIsDirty() {
		if (getRepository().boolIsDirty) return true; // Overrides
		boolean boolIsDirty = false;
		for (clsTreeNode objChildTreeNode : getRepository().objRootNodes) {
			boolIsDirty = GetIsDirtyRecursively(objChildTreeNode);
			if (boolIsDirty)
				return true;
		}
		return boolIsDirty;
	}

	private boolean GetIsDirtyRecursively(clsTreeNode objParentTreeNode) {
		boolean boolIsDirty = false;
		if (objParentTreeNode.getIsDirty() == true)
			return true;
		for (clsTreeNode objChildTreeNode : objParentTreeNode.objChildren) {
			boolIsDirty = GetIsDirtyRecursively(objChildTreeNode);
			if (boolIsDirty)
				return true;
		}
		return boolIsDirty;
	}

	public clsTreeNode GetSharingFolder() {
		for (clsTreeNode objChildTreeNode : getRepository().objRootNodes) {
			if (objChildTreeNode.resourceId == clsTreeview.SHARED_FOLDER_RESOURCE) { 
				return objChildTreeNode;
			}
		}
		return null;
	}
	
	public clsTreeNode GetPublishingFolder() {
		for (clsTreeNode objChildTreeNode : getRepository().objRootNodes) {
			if (objChildTreeNode.resourceId == clsTreeview.PUBLISHED_FOLDER_RESOURCE) { 
				return objChildTreeNode;
			}
		}
		return null;
	}

	public static int translateResourceId(int inId) {
		switch (inId) {
		case ANNOTATION_RESOURCE:
			return clsAnnotationData.EDIT_ANNOTATION_TEXT;
		case IMAGE_RESOURCE:
		case WEB_RESOURCE:
			return clsAnnotationData.EDIT_ANNOTATION_IMAGE;
		default:
			return clsAnnotationData.INVALID;
		}
	}

	public boolean IsTargetChildOfSource(clsTreeNode objSourceTreeNode, clsTreeNode objTargetTreeNode) {
		if (objSourceTreeNode.equals(objTargetTreeNode)) {
			return true;
		}
		for (clsTreeNode objChildTreeNode: objSourceTreeNode.objChildren ) {
			if (IsTargetChildOfSource(objChildTreeNode,objTargetTreeNode)) {
				return true;
			}
		}
		return false;
	}

	public void addSourceTreeNodeBelowTarget(clsTreeNode objSourceTreeNode, clsTreeNode objTargetTreeNode) {
		// Remove from where its at this stage
		clsTreeNode objSourceParentTreeNode = getParentTreeNode(objSourceTreeNode);
		if (objSourceParentTreeNode == null) {
			getRepository().objRootNodes.remove(objSourceTreeNode);
		} else {
			objSourceParentTreeNode.objChildren.remove(objSourceTreeNode);
		}
		// Add as first item to target
		objTargetTreeNode.objChildren.add(objSourceTreeNode);
		setTreeNodeItemOrder(objSourceTreeNode,0);
		objSourceTreeNode.setIsDirty(true);
		objTargetTreeNode.enumItemType = enumItemType.FOLDER_EXPANDED;
		UpdateItemTypes();
	}
	
	public void addSourceTreeNodeBeforeOrAfterTarget(clsTreeNode objSourceTreeNode, clsTreeNode objTargetTreeNode, boolean boolIsBefore) {
		// Remove from where its at this stage
		clsTreeNode objSourceParentTreeNode = getParentTreeNode(objSourceTreeNode);
		if (objSourceParentTreeNode == null) {
			getRepository().objRootNodes.remove(objSourceTreeNode);
		} else {
			objSourceParentTreeNode.objChildren.remove(objSourceTreeNode);
		}
		// Add as item before target
		clsTreeNode objTargetParentTreeNode = getParentTreeNode(objTargetTreeNode);
		int intTargetOrder = getTreeNodeItemOrder(objTargetTreeNode);
		int intSourceOrder  = (boolIsBefore)?intTargetOrder-1:intTargetOrder + 1;
		if (objTargetParentTreeNode ==  null) {
			getRepository().objRootNodes.add(objSourceTreeNode);
			setTreeNodeItemOrder(objSourceTreeNode,intSourceOrder);
		} else {
			objTargetParentTreeNode.objChildren.add(objSourceTreeNode);
			setTreeNodeItemOrder(objSourceTreeNode,intSourceOrder);
		}
		objSourceTreeNode.setIsDirty(true);
		UpdateItemTypes();
	}
	
	// Must be overridden
	public void UpdateItemTypes() {
		clsUtils.CustomLog("UpdateItemTypes unimplemented");
	}

	// Must be overridden
	public boolean IsSourceDropableOnTarget(clsTreeNode objSourceTreeNode, clsTreeNode objTargetTreeNode) {
		clsUtils.CustomLog("IsSourceDropableOnTarget unimplemented");
		return false;
	}

}
