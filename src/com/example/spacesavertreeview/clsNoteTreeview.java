package com.example.spacesavertreeview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.GestureDetector.OnDoubleTapListener;

import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.clsTreeview.enumItemType;
import com.example.spacesavertreeview.sharing.clsGroupMembers;
import com.google.gson.Gson;

public class clsNoteTreeview extends clsTreeview {
	
	
	public clsNoteTreeview(clsGroupMembers objGroupMembers) {
		super(objGroupMembers);
		// TODO Auto-generated constructor stub
	}

	public void DeSerializeFromSharedPreferences(Activity _context, String strKey){

		getRepository().objRootNodes.clear();
		SharedPreferences sharedPref = _context.getSharedPreferences(strKey,Context.MODE_PRIVATE);
		String strSerialize = sharedPref.getString("strSerialized","");
		if (strSerialize.length()!= 0) {
			Gson gson = new Gson();		
			//java.lang.reflect.Type collectionType = new TypeToken<ArrayList<clsTreeNode>>(){}.getType();
			//objRootNodes = gson.fromJson(strSerialize, collectionType);
			setRepository(gson.fromJson(strSerialize,clsRepository.class));
		}
		sharedPref = null;
	}
	
	public void SerializeToSharedPreferences(Activity _context, String strKey){
        Gson gson = new Gson();
        String strSerialized = gson.toJson(getRepository());
		SharedPreferences sharedPref = _context.getSharedPreferences(strKey,Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPref.edit();
    	editor.putString("strSerialized",strSerialized);
    	editor.commit();
    	sharedPref = null;
	}
		

	public boolean boolIsUserCommenting(String strRegisteredUserUuid) {
		// Determine if user shall add comments rather than notes
		if (this.getRepository().getNoteOwnerUserUuid().isEmpty()) return false;
		if (this.getRepository().getNoteOwnerUserUuid().equals(strRegisteredUserUuid)) {
			return false;
		}
		return true;
	}

	@Override
	public void UpdateItemTypes() {
		// Correct possible mistakes due to pasting
		for (clsTreeNode objTreeNode: this.getRepository().objRootNodes) {
			UpdateItemTypeRecursively(objTreeNode);
		}
		
	}
	
	private void UpdateItemTypeRecursively(clsTreeNode objTreeNode) {
		// Correct icons due to pasting
		switch (objTreeNode.enumItemType) {
			case FOLDER_EXPANDED:
			case FOLDER_COLLAPSED:
				if (objTreeNode.IsEmptyOrAllChildrenDeleted()) {
					objTreeNode.enumItemType = enumItemType.FOLDER_EMPTY;
				}	
				break;
			case FOLDER_EMPTY:
			case OTHER:
				if (!objTreeNode.IsEmptyOrAllChildrenDeleted()) {
					objTreeNode.enumItemType = enumItemType.FOLDER_COLLAPSED;
				}
				break;
		}
		for (clsTreeNode objChildTreeNode: objTreeNode.objChildren) {
			UpdateItemTypeRecursively(objChildTreeNode);
		}
	}

	public int GetIconResourceId(clsTreeNode objTreeNode, clsGroupMembers objGroupMembers, clsTreeview objTreeview) {
		enumItemType intItemType = objTreeNode.enumItemType;
		if (!objTreeNode.boolIsNodeDisplayedAsComment(objGroupMembers, objTreeview)) {
			switch (intItemType) {
			case FOLDER_EXPANDED:
				return R.drawable.notes_expanded_green;
			case FOLDER_COLLAPSED:
				return R.drawable.notes_collapsed6_green;
			case FOLDER_EMPTY:
				return R.drawable.notes_green;
			case OTHER:
				return R.drawable.notes_green;
			}
		} else {
			if (objTreeNode.boolIsOwnNote(objGroupMembers, objTreeview)) {
				if (objTreeNode.boolIsOwnComment(objGroupMembers, objTreeview)) {
					// Own note, own comment, display green
					switch (intItemType) {
					case FOLDER_EXPANDED:
						return R.drawable.comment_green_expanded;
					case FOLDER_COLLAPSED:
						return R.drawable.comment_green_collapsed;
					case FOLDER_EMPTY:
						return R.drawable.comment_green;
					case OTHER:
						return R.drawable.comment_green;
					}
				} else {
					// Own note, foreign comment, display grey
					switch (intItemType) {
					case FOLDER_EXPANDED:
						return R.drawable.comment_grey_expanded;
					case FOLDER_COLLAPSED:
						return R.drawable.comment_grey_collapsed;
					case FOLDER_EMPTY:
						return R.drawable.comment_grey;
					case OTHER:
						return R.drawable.comment_grey;
					}
				}
			} else {
				if (objTreeNode.boolIsOwnComment(objGroupMembers, objTreeview)) {
					// Foreign note, own comment 
					// Display green
					switch (intItemType) {
					case FOLDER_EXPANDED:
						return R.drawable.comment_green_expanded;
					case FOLDER_COLLAPSED:
						return R.drawable.comment_green_collapsed;
					case FOLDER_EMPTY:
						return R.drawable.comment_green;
					case OTHER:
						return R.drawable.comment_green;
					}
				} else {
					// Foreign note, foreign comment
					// Display grey

					switch (intItemType) {
					case FOLDER_EXPANDED:
						return R.drawable.comment_grey_expanded;
					case FOLDER_COLLAPSED:
						return R.drawable.comment_grey_collapsed;
					case FOLDER_EMPTY:
						return R.drawable.comment_grey;
					case OTHER:
						return R.drawable.comment_grey;
					}
				}
			}
		}
		return R.drawable.ic_launcher;
	}
	
	@Override
	public boolean IsSourceDropableOnTarget(clsTreeNode objSourceTreeNode, clsTreeNode objTargetTreeNode) {
		return true;
	}
}
