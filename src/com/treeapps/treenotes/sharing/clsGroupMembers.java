package com.treeapps.treenotes.sharing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;



import com.google.gson.Gson;
import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.clsTreeview.clsRepository;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsTreeview.enumItemType;
import com.treeapps.treenotes.sharing.clsGroupMembersTreeview.clsGroupMemberFolderTreeNode;
import com.treeapps.treenotes.sharing.clsGroupMembersTreeview.clsGroupMemberMemberTreeNode;
import com.treeapps.treenotes.sharing.clsGroupMembersTreeview.clsGroupMembersTreeNode;

public class clsGroupMembers {
	
	private Context _context;
	
	public clsMembersRepository objMembersRepository = new clsMembersRepository();
	
	public clsGroupMembers(Context context){
		this._context = context;
	}
	
	public void UpdateEnvironment(Context context) {
		this._context = context;
	}
	
	public class clsMembersRepository{
		public ArrayList<clsUser> objUsers = new  ArrayList<clsUser>();
		public clsGroup objGroup = new clsGroup();
		private String strRegisteredUserUuid = "";
		private String strRegisteredUserName = "";
	    
		public String getStrRegisteredUserUuid() {
			return strRegisteredUserUuid;
		}
		public void setStrRegisteredUserUuid(String strUserUuid) {
			this.strRegisteredUserUuid = strUserUuid;
		}
		public String getStrRegisteredUserName() {
			return strRegisteredUserName;
		}
		public void setStrRegisteredUserName(String strUserName) {
			this.strRegisteredUserName = strUserName;
		}
		
		private ArrayList<clsSyncUser> CopyUsers() {
			// TODO Auto-generated method stub
			ArrayList<clsSyncUser> objCopiedUser = new ArrayList<clsSyncUser>();
			for (clsUser objUser: objUsers) {
				objCopiedUser.add(objUser.Copy()); 
			}		
			return objCopiedUser;
		}
		
		public clsSyncMembersRepository Copy() {
			// TODO Auto-generated method stub
			clsSyncMembersRepository objSyncMembersRepository = new clsSyncMembersRepository();
			objSyncMembersRepository.objUsers = this.CopyUsers();
			objSyncMembersRepository.objUserGroup = this.CopyUserGroup();
			objSyncMembersRepository.strRegisteredUserUuid = this.strRegisteredUserUuid;
			objSyncMembersRepository.strRegisteredUserName = this.strRegisteredUserName;
			return objSyncMembersRepository;
		}
		private clsSyncGroup CopyUserGroup() {
			// TODO Auto-generated method stub
			clsSyncGroup objSyncGroup = new clsSyncGroup();
			objSyncGroup.strName = this.objGroup.strName;
			objSyncGroup.strUserUuids = this.objGroup.strUserUuids;
			objSyncGroup.objChildGroups = this.objGroup.CopyChildGroups();		
			return objSyncGroup;
		}
	}
	
	public class clsSyncMembersRepository{
		public ArrayList<clsSyncUser> objUsers;
		public clsSyncGroup objUserGroup;
		private String strRegisteredUserUuid = "";
		private String strRegisteredUserName = "";
		
		public clsMembersRepository Copy() {
			clsMembersRepository objMembersRepository = new clsMembersRepository();
			objMembersRepository.objUsers = CopyUsers(this.objUsers);
			objMembersRepository.objGroup = this.objUserGroup.Copy();
			objMembersRepository.strRegisteredUserUuid =this.strRegisteredUserUuid;
			objMembersRepository.strRegisteredUserName = this.strRegisteredUserName ;
			return objMembersRepository;
		}
		
		private ArrayList<clsUser> CopyUsers(ArrayList<clsSyncUser> objUsers) {
			// TODO Auto-generated method stub
			ArrayList<clsUser> objCopiedUser = new ArrayList<clsUser>();
			for (clsSyncUser objUser: objUsers) {
				objCopiedUser.add(objUser.Copy()); 
			}		
			return objCopiedUser;
		}
	}
	    
	public class clsUser implements Serializable {
		private static final long serialVersionUID = 1L;
		public String strUserUuid = "";
        public String strUserName = "";
        
		public clsUser Clone() {
			// TODO Auto-generated method stub
			clsUser clsUserClone = new clsUser();
			clsUserClone.strUserUuid = this.strUserUuid;
			clsUserClone.strUserName = this.strUserName;
			return clsUserClone;
		}

		public clsSyncUser Copy() {
			// TODO Auto-generated method stub
			clsSyncUser objUserClone = new clsSyncUser();
			objUserClone.strUserUuid = this.strUserUuid;
			objUserClone.strUserName = this.strUserName;
			return objUserClone;
		}
	}
	
	public class clsSyncUser implements Serializable  {
		private static final long serialVersionUID = 2630564058787417448L;
		public String strUserUuid = "";
        public String strUserName = "";

		public clsUser Copy() {
			// TODO Auto-generated method stub
			clsUser objUser = new clsUser();
			objUser.strUserUuid = this.strUserUuid;
			objUser.strUserName = this.strUserName;
			return objUser;
		}}
	
	public class clsGroup {
		public String strName = "";
		public ArrayList<String> strUserUuids = new ArrayList<String>();
		public ArrayList<clsGroup> objChildGroups = new ArrayList<clsGroup>();
		
		public clsSyncGroup Copy() {
			clsSyncGroup objSyncUserGroup = new clsSyncGroup();
			objSyncUserGroup.strName =  this.strName;
			objSyncUserGroup.strUserUuids = this.strUserUuids;
			objSyncUserGroup.objChildGroups = CopyGroups(this.objChildGroups); 
			return objSyncUserGroup;
		}

		public ArrayList<clsSyncGroup> CopyChildGroups() {
			// TODO Auto-generated method stub
			return CopyGroups(this.objChildGroups);
		}

		private ArrayList<clsSyncGroup> CopyGroups(ArrayList<clsGroup> objGroups) {
			// TODO Auto-generated method stub
			ArrayList<clsSyncGroup> objNewGroups = new ArrayList<clsSyncGroup>();
			for (clsGroup objGroup: objGroups) {
				objNewGroups.add(objGroup.Copy());
			}
			return objNewGroups;
		}
	}
	
	public class clsSyncGroup implements Serializable  {
		private static final long serialVersionUID = 7742899942605417054L;
		public String strName = "";
		public ArrayList<String> strUserUuids = new ArrayList<String>();
		public ArrayList<clsSyncGroup> objChildGroups = new ArrayList<clsSyncGroup>();
		public clsGroup Copy() {
			// TODO Auto-generated method stub
			clsGroup objUserGroup = new clsGroup();
			objUserGroup.strName =  this.strName;
			objUserGroup.strUserUuids = this.strUserUuids;
			objUserGroup.objChildGroups = CopyGroup(this.objChildGroups); 
			return objUserGroup;
		}
		private ArrayList<clsGroup> CopyGroup(ArrayList<clsSyncGroup> objGroups) {
			// TODO Auto-generated method stub
			ArrayList<clsGroup> objNewGroups = new ArrayList<clsGroup>();
			if (objGroups == null) return objNewGroups;
			for (clsSyncGroup objGroup: objGroups) {
				objNewGroups.add(objGroup.Copy());
			}
			return objNewGroups;
		}
	}
	

	public void SaveFile() {
		File objFile = clsUtils.BuildUsersRepositoryFilename(new File(clsUtils.GetTreeNotesDirectoryName(_context)));
		SerializeToFile(objFile);
	}
	
	private void SerializeToFile(File objFile){
        Gson gson = new Gson();
        String strSerialized = gson.toJson(objMembersRepository);
        try {
        	FileWriter out = new FileWriter(objFile);
            out.write(strSerialized);
            out.close();
        } catch (IOException e) {
        	e.printStackTrace();
        	Log.e("Exception", "File write failed: " + e.toString());
        } 
	}
	
	
	
	public void LoadFile (){
		File objFile = clsUtils.BuildUsersRepositoryFilename(new File(clsUtils.GetTreeNotesDirectoryName(_context)));
		DeserializeFromFile(objFile);
	}
	
	private void DeserializeFromFile(File objFile){
		if (objFile.exists()== false) {
			clsUtils.CustomLog("GroupMember file does not exist");
			ClearRepository();
			return;
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
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		if (strSerialize != "") {
			Gson gson = new Gson();	
			objMembersRepository = gson.fromJson(strSerialize,clsMembersRepository.class);
		} else {
			clsUtils.CustomLog("Backup file is empty");
		}
	}

	private void ClearRepository() {
		// TODO Auto-generated method stub
		objMembersRepository.objGroup.objChildGroups.clear();
		objMembersRepository.objGroup.strUserUuids.clear();
		objMembersRepository.objUsers.clear();
	}

	public clsUser GetFromUuid(String strUserUuid) {
		// TODO Auto-generated method stub
		for(clsUser objUser:objMembersRepository.objUsers){
			if(objUser.strUserUuid.equals(strUserUuid)){
				return objUser;
			}
		}
		return null;
	}
	
	public void BuildTreeviewFromGroupMemberData(clsGroupMembersTreeview objTreeview) {
		// TODO Auto-generated method stub
		objTreeview.ClearAll();
		clsRepository objTreeviewRepository = objTreeview.getRepository();
		// Add the All Members group at the top
		clsGroupMemberFolderTreeNode objParentFolderTreeNode = objTreeview.new clsGroupMemberFolderTreeNode("All members", enumItemType.FOLDER_EXPANDED, false, true);
		objTreeviewRepository.objRootNodes.add(objParentFolderTreeNode);
		for (clsUser objUser: objMembersRepository.objUsers){
			clsGroupMemberMemberTreeNode objUserTreeNode = objTreeview.new clsGroupMemberMemberTreeNode(objUser.strUserName, enumItemType.OTHER, false,objUser.strUserUuid);
			objParentFolderTreeNode.objChildren.add(objUserTreeNode);
		}
		
		// Add the Custom Groups below
		objParentFolderTreeNode = objTreeview.new clsGroupMemberFolderTreeNode("All groups", enumItemType.FOLDER_EXPANDED, false, false);
		objTreeviewRepository.objRootNodes.add(objParentFolderTreeNode);
		if (objMembersRepository.objGroup != null) {
			AddGroupUsers(objTreeview,objParentFolderTreeNode,objMembersRepository.objGroup);
			for (clsGroup objGroup: objMembersRepository.objGroup.objChildGroups){
				AddChildGroupsWithUsersRecursively(objTreeview,objParentFolderTreeNode,objGroup);
			}
		}
	}

	private void AddChildGroupsWithUsersRecursively(clsGroupMembersTreeview objTreeview,
			clsGroupMemberFolderTreeNode objParentFolderTreeNode, clsGroup objGroup) {
		// TODO Auto-generated method stub
		clsGroupMemberFolderTreeNode objChildFolderTreeNode = objTreeview.new clsGroupMemberFolderTreeNode(objGroup.strName, enumItemType.FOLDER_EXPANDED, false, false);
		objParentFolderTreeNode.objChildren.add(objChildFolderTreeNode);
		AddGroupUsers(objTreeview,objChildFolderTreeNode,objGroup);
		for (clsGroup objChildGroup: objGroup.objChildGroups){
			AddChildGroupsWithUsersRecursively(objTreeview,objChildFolderTreeNode,objChildGroup);
		}
	}

	private void AddGroupUsers(clsGroupMembersTreeview objTreeview,	clsGroupMemberFolderTreeNode objChildFolderTreeNode, clsGroup objGroup) {
		// TODO Auto-generated method stub
		for (String strUserUuid: objGroup.strUserUuids){
			clsUser objUser = GetFromUuid(strUserUuid);
			if (objUser != null) {
				clsGroupMemberMemberTreeNode objUserTreeNode = objTreeview.new clsGroupMemberMemberTreeNode(objUser.strUserName, enumItemType.OTHER, false, objUser.strUserUuid);
				objChildFolderTreeNode.objChildren.add(objUserTreeNode);
			}
		}	
	}

	public void Delete(clsTreeNode objDeletedTreeNode) {
		// TODO Auto-generated method stub

	}

	public void Clear() {
		// TODO Auto-generated method stub
		this.ClearRepository();
		this.SaveFile();
	}

	public void BuildGroupMemberDataFromTreeView(clsGroupMembers objGroupMembers, clsGroupMembersTreeview objGroupMembersTreeview) {
		// TODO Auto-generated method stub
		clsMembersRepository objNewMembersRepository = new clsMembersRepository();
		objNewMembersRepository.strRegisteredUserUuid = objGroupMembers.objMembersRepository.strRegisteredUserUuid;
		objNewMembersRepository.strRegisteredUserName = objGroupMembers.objMembersRepository.strRegisteredUserName;
		// AllUsers first
		for (clsTreeNode objTreeNode: objGroupMembersTreeview.getRepository().objRootNodes.get(0).objChildren ){
			clsGroupMemberMemberTreeNode objGroupMemberMemberTreeNode = (clsGroupMemberMemberTreeNode)objTreeNode;
			clsUser objUser = CloneUserFromUuid(objGroupMemberMemberTreeNode.getStrUserUuid());
			objNewMembersRepository.objUsers.add(objUser);
		}
		
		
		// Groups second
		clsRepository objTreeViewRepository = objGroupMembersTreeview.getRepository();
		ArrayList<clsTreeNode> objSourceTreeNodes = objTreeViewRepository.objRootNodes.get(1).objChildren;
		for (clsTreeNode objSourceChildTreeNode: objSourceTreeNodes ){
			clsGroupMembersTreeNode objGroupMembersSourceChildTreeNode = (clsGroupMembersTreeNode) objSourceChildTreeNode;
			CopyGroupsRecursively(objGroupMembersSourceChildTreeNode,objNewMembersRepository.objGroup, this);		
		}
		
		this.objMembersRepository = objNewMembersRepository;
	}
	
	
	

	private void CopyGroupsRecursively(clsGroupMembersTreeNode objSourceTreeNode, clsGroup objGroup, clsGroupMembers objGroupMembers) {
		// TODO Auto-generated method stub
		clsGroupMembersTreeNode objGroupMembersTreeNode = (clsGroupMembersTreeNode) objSourceTreeNode;		
		clsGroup objNewGroup = objGroupMembersTreeNode.AddSelfToRepository(objGroup,objGroupMembers);
		for (clsTreeNode objSourceChildTreeNode: objGroupMembersTreeNode.objChildren ){
			clsGroupMembersTreeNode objGroupMembersSourceChildTreeNode = (clsGroupMembersTreeNode) objSourceChildTreeNode;
			CopyGroupsRecursively(objGroupMembersSourceChildTreeNode,objNewGroup, this);		
		}
	}


	private clsUser CloneUserFromUuid(String strUserUuid) {
		// TODO Auto-generated method stub
		clsUser objUserOrig = this.GetFromUuid(strUserUuid);
		return objUserOrig.Clone();
	}

	public String GetUserNameFomUuid(String strUuid) {
		// TODO Auto-generated method stub
		clsUser objUser = GetFromUuid(strUuid);
		if (objUser == null) {
			// None in GroupMembers 
			if (objMembersRepository.strRegisteredUserUuid != null) {
				if (objMembersRepository.strRegisteredUserUuid.equals(strUuid)) {
					// Is the registered person however
					return objMembersRepository.strRegisteredUserName;
				}
			}		
			return "Unknown";
		}
		return objUser.strUserName;
	}
	
	public clsUser GetRegisteredUser () {
		clsUser objOwner = new clsUser();
		if (this.objMembersRepository.strRegisteredUserUuid.isEmpty()) {
			objOwner.strUserName = _context.getResources().getString(R.string.unregistered_username);
			objOwner.strUserUuid = "";
		} else {
			objOwner.strUserName = this.objMembersRepository.strRegisteredUserName;
			objOwner.strUserUuid = this.objMembersRepository.strRegisteredUserUuid;
		}
		return objOwner;
	}

	public void Unregister() {
		// TODO Auto-generated method stub
		this.objMembersRepository.strRegisteredUserName = "";
		this.objMembersRepository.strRegisteredUserUuid = "";
		SaveFile();
	}

	public void ClearAll() {
		// TODO Auto-generated method stub
		this.objMembersRepository.objUsers.clear();
		this.objMembersRepository.objGroup.objChildGroups.clear();
		this.objMembersRepository.strRegisteredUserName = "";
		this.objMembersRepository.strRegisteredUserUuid = "";
		SaveFile();
	}	
}
