package com.treeapps.treenotes;

import java.util.UUID;

import com.treeapps.treenotes.clsTreeview.enumItemLevelRelation;
import com.treeapps.treenotes.clsTreeview.enumItemType;

public class clsListItem {
	private UUID _guidTreeNode;
	private String _strName = "";
	private int _intLevel;
	private clsTreeview.enumItemType _enumItemType;
	private enumItemLevelRelation _AboveItemLevelRelation, _BelowItemLevelRelation;
	public boolean	_boolIsSelected;
	public boolean _boolFolderHasHiddenItems;
	public enumNewItemType intNewItemType;
	
	// JE
	private String _resourcePath;
	private int    _resourceId = -1;
	private String _strWebPageURL;
	
	public static enum enumNewItemType {
		OLD, NEW, NEW_AND_PARENT_OF_NEW, NEW_AND_ROOT_PARENT_OF_NEW, PARENT_OF_NEW, ROOT_PARENT_OF_NEW
	}
	

	// Annotation
	public boolean boolIsAnnotated = false;
	
	public clsListItem(String strName, int intLevel, 
						UUID guidTreeNode,  enumItemType enumItemType, 
						boolean	boolIsSelected, String resourcePath, int resourceId, boolean boolIsAnnotated, String strWebPageURL){
		_strName = strName;
		_intLevel = intLevel;
		_guidTreeNode = guidTreeNode;
		_enumItemType = enumItemType;
		_boolIsSelected = boolIsSelected;
		_resourcePath = resourcePath;
		_resourceId = resourceId;
		_boolFolderHasHiddenItems = false;
		this.boolIsAnnotated = boolIsAnnotated;
		_strWebPageURL = strWebPageURL;
	}
	
	public String getName(){
		return _strName;
	}
	
	// get path and filename of image or video
	// returns empty string for text resource
	public String getResourcePath(){
		return _resourcePath;
	}
	
	public int getLevel(){
		return _intLevel;
	}
	
	// return the type of the row content (text, image or video) 
	public int getResourceId(){
		return _resourceId;
	}
	
	public String getWebPageURL() {
		return _strWebPageURL;
	}

	public void setWebPageURL(String strWebPageURL) {
		this._strWebPageURL = strWebPageURL;
	}
	
	public void setLevel(int intLevel){
		_intLevel = intLevel;
	}
	
	public boolean getSelected(){
		return _boolIsSelected;
	}
	
	public void setSelected(boolean boolIsSelected ){
		_boolIsSelected = boolIsSelected;
	}
	
	public boolean getFolderHasHiddenItems(){
		return _boolFolderHasHiddenItems;
	}
	
	public void setFolderHasHiddenItems(boolean boolHasHiddenItems ){
		_boolFolderHasHiddenItems = boolHasHiddenItems;
	}
	
	
	
	
	@Override
	public String toString() {
		return _strName;
	}


	public UUID getTreeNodeGuid() {
		// TODO Auto-generated method stub
		return _guidTreeNode;
	}

	public clsTreeview.enumItemType getItemType() {
		
		// TODO Auto-generated method stub
		return _enumItemType;
	}
	
	public enumItemLevelRelation GetAboveItemLevelRelation(){
		return _AboveItemLevelRelation;
	}
	
	public void SetAboveItemLevelRelation(enumItemLevelRelation enumItemLevelRelation){
		_AboveItemLevelRelation = enumItemLevelRelation;
	}
	
	public enumItemLevelRelation GetBelowItemLevelRelation(){
		return _BelowItemLevelRelation;
	}
	
	public void SetBelowItemLevelRelation(enumItemLevelRelation enumItemLevelRelation){
		_BelowItemLevelRelation = enumItemLevelRelation;
	}

	public boolean getIsImageDisplayed() {
		// TODO Auto-generated method stub
		return (_resourceId==clsTreeview.TEXT_RESOURCE)? false:true;
	}
}
