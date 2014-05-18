package com.example.spacesavertreeview.sharing;

import java.util.ArrayList;        
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import com.example.spacesavertreeview.clsIntentMessaging;
import com.example.spacesavertreeview.clsTreeview;
import com.example.spacesavertreeview.sharing.clsGroupMembers.clsGroup;

public class clsGroupMembersTreeview extends clsTreeview {
	

	Context context;
	
	public clsGroupMembersTreeview(Context context, clsGroupMembers objGroupMembers){
		super(objGroupMembers);
		this.context = context;
	}
	
	
	public class clsGroupMembersTreeNode extends clsTreeNode{

		private static final long serialVersionUID = 1L;

		
		clsGroupMembersTreeNode(String strName, enumItemType enumItemType, boolean	boolIsSelected){
			super(strName,enumItemType,boolIsSelected,"",0,"","","");
		}


		public boolean ChildExists(String strUuid) {
			// TODO Auto-generated method stub
			for (clsTreeNode objChildTreeNode : this.objChildren){
				if (objChildTreeNode.enumItemType == com.example.spacesavertreeview.clsTreeview.enumItemType.OTHER) {
					clsGroupMemberMemberTreeNode objGroupMemberMemberTreeNode = (clsGroupMemberMemberTreeNode) objChildTreeNode;
					if (objGroupMemberMemberTreeNode.strUserUuid == strUuid ) return true;
				}
			}
			return false;
		}

		public clsGroup AddSelfToRepository(clsGroup objGroup, clsGroupMembers objGroupMembers) {
			// Do nothing, to be overriden
			return null;
		}


		public void AddCheckedItemsToResultsRecursively(Set<String>  objResultsAsSet) {
			// TODO Auto-generated method stub
			// Do nothing, to be overriden
		}
		
		@Override
		public boolean IsTreeViewPurposeIsToFindCheckStateOnly() {
			return true;
		}


		public void CheckSelfBasedOnSharedMembers(clsIntentMessaging.clsChosenMembers objChosenMembers) {
			// TODO Auto-generated method stub
			// Do nothing, to be overriden
		}

		
	}
	
	@Override
	public clsTreeNode getClone(clsTreeNode objTreeNode, boolean boolResetUuid){
		clsTreeNode objNewTreeNode = new clsTreeNode();
		objNewTreeNode.setName(objTreeNode.getName());
		objNewTreeNode.guidTreeNode = (boolResetUuid)? UUID.randomUUID():objTreeNode.guidTreeNode;
		objNewTreeNode.enumItemType = objTreeNode.enumItemType;
		objNewTreeNode.boolIsSelected = false;
		objNewTreeNode.resourcePath = objTreeNode.resourcePath;
		objNewTreeNode.resourceId = objTreeNode.resourceId;
		
		for (clsTreeNode objNewChildNode:objTreeNode.objChildren){
			objNewTreeNode.objChildren.add(getClone(objNewChildNode,boolResetUuid));
		}
		return objNewTreeNode;
	}
	
	public class clsGroupMemberFolderTreeNode extends clsGroupMembersTreeNode{
		private static final long serialVersionUID = -2641773753694333301L;
		private boolean boolIsAllUserFolder;

		clsGroupMemberFolderTreeNode(String strName, enumItemType enumItemType, boolean	boolIsSelected, boolean boolIsAllUserFolder){
			super(strName,enumItemType,boolIsSelected);
			this.boolIsAllUserFolder = boolIsAllUserFolder;
		}
		@Override
		public boolean IsAllUserFolder() {
			return this.boolIsAllUserFolder;
		}
		@Override
		public clsGroup AddSelfToRepository(clsGroup objGroup, clsGroupMembers objGroupMembers) {
			// This is a folder, make a copy
			clsGroup objNewGroup = objGroupMembers.new clsGroup();
			objNewGroup.strName = this.getName();
			objGroup.objChildGroups.add(objNewGroup);
			return objNewGroup;
		}
		
		@Override
		public void AddCheckedItemsToResultsRecursively(Set<String> objResultsAsSet) {
			for(clsTreeNode objChildTreeNode:this.objChildren){
				clsGroupMembersTreeNode objTreeNode = (clsGroupMembersTreeNode) objChildTreeNode;
				objTreeNode.AddCheckedItemsToResultsRecursively(objResultsAsSet);
			}
		}
		
		@Override
		public void CheckSelfBasedOnSharedMembers(clsIntentMessaging.clsChosenMembers objChosenMembers) {
			// Check child item
			for (clsTreeNode objChildTreeNode: objChildren){
				clsGroupMembersTreeNode objTreeNode = (clsGroupMembersTreeNode) objChildTreeNode;
				objTreeNode.CheckSelfBasedOnSharedMembers(objChosenMembers);
			}
		}
	}
	
	public class clsGroupMemberMemberTreeNode extends clsGroupMembersTreeNode{
		private static final long serialVersionUID = 2987196740778934783L;
		private String strUserUuid = "";

		clsGroupMemberMemberTreeNode(String strName, enumItemType enumItemType, boolean	boolIsSelected, String strUserUuid){
			super(strName,enumItemType,boolIsSelected);
			this.strUserUuid = strUserUuid;
		}

		public String getStrUserUuid() {
			return strUserUuid;
		}

		public void setStrUserUuid(String strUserUuid) {
			this.strUserUuid = strUserUuid;
		}
		
		@Override
		public clsGroup AddSelfToRepository(clsGroup objGroup, clsGroupMembers objGroupMembers) {
			// This is a member, add to same group
			objGroup.strUserUuids.add(this.strUserUuid);
			return null;
		}
		@Override
		public void AddCheckedItemsToResultsRecursively(Set<String> objResultsAsSet) {
			if (getChecked()) {
				objResultsAsSet.add(getStrUserUuid());
			}
		}
		
		@Override
		public void CheckSelfBasedOnSharedMembers(clsIntentMessaging.clsChosenMembers objChosenMembers) {
			
			for (String strUserUuid: objChosenMembers.strUserUuids ) {
				if (strUserUuid.equals(this.strUserUuid)) {
					this.setChecked(true);
					return;
				}
			}
			this.setChecked(false);
		}
	
	}

	public clsIntentMessaging.clsChosenMembers GetAllSelectedMembers(clsGroupMembersTreeview objTreeView) {
		// TODO Auto-generated method stub
		HashSet<String> objResultsAsSet = new HashSet<String>();
		for (clsTreeNode objChildTreeNode:objTreeView.getRepository().objRootNodes){
			clsGroupMembersTreeNode objTreeNode = (clsGroupMembersTreeNode) objChildTreeNode;
			objTreeNode.AddCheckedItemsToResultsRecursively(objResultsAsSet);
		}
		clsIntentMessaging objIntentMessaging = new clsIntentMessaging();
		clsIntentMessaging.clsChosenMembers objResults = objIntentMessaging.new clsChosenMembers();
		objResults.strUserUuids = new ArrayList<String>(objResultsAsSet);
		return objResults;
	}

	public void CheckAllNodesBasedOnSharedMembers(clsIntentMessaging.clsChosenMembers objChosenMembers) {
		// TODO Auto-generated method stub
		if (objChosenMembers == null) return;
		for (clsTreeNode objChildTreeNode:getRepository().objRootNodes){
			clsGroupMembersTreeNode objTreeNode = (clsGroupMembersTreeNode) objChildTreeNode;
			objTreeNode.CheckSelfBasedOnSharedMembers(objChosenMembers);
		}
	}





	



	
	

	
	
}
