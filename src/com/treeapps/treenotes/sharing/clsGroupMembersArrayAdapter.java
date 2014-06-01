package com.treeapps.treenotes.sharing;

import java.util.List;
import java.util.UUID;



import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsIndentableTextView;
import com.treeapps.treenotes.clsListItem;
import com.treeapps.treenotes.clsListItemArrayAdapter;
import com.treeapps.treenotes.clsTreeview;
import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsTreeview.enumItemType;

import android.content.Context;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class clsGroupMembersArrayAdapter extends clsListItemArrayAdapter  {

	Context _context;
	
	public clsGroupMembersArrayAdapter(Context context, int _resource,
			 List<clsListItem> objects, clsTreeview objTreeview, int intTabWidthInPx) {
		super(context, _resource, objects, objTreeview, intTabWidthInPx);
		_context = context;
	}
	
	
	@Override
	public void SelectItemTypeFolder(clsListItem objListItem, ImageView myImageView) {
		switch (objListItem.getItemType()){
		 case FOLDER_EXPANDED:
			 myImageView.setImageResource(R.drawable.users_expanded);
			 break;
		 case FOLDER_COLLAPSED:
			 myImageView.setImageResource(R.drawable.users_collapsed);
			 break;
		 case FOLDER_EMPTY:
			 myImageView.setImageResource(R.drawable.users_empty);
			 break;
		 case OTHER:
			 myImageView.setImageResource(R.drawable.user_medium);
			 break;
		 }
	}
	@Override
	public int GetSelectColour() {
		return _context.getResources().getInteger(R.color.select_outline_color_explorer);
	}
		
	@Override
	public int GetBackgroundResource(boolean boolIsSelected){
		if(boolIsSelected){
			// Note: There is another one in MyTextView.java that does that view only because it has higher z-order
			  return R.drawable.listitem_selected_shape_explorer;
		  } else{
			  return R.drawable.listitem_unselected_shape;
		  }
	}
	
	@Override
	public void ProvideThumbnailSizeToCustomView(clsIndentableTextView myTextView) {
		// No thumbnails are used in this treeview
		 myTextView.SetThumbnailWidthInPx(0);
		 myTextView.SetThumbnailHeightInPx(0);
	}
	
	
	@Override
	public void ProvideCheckBoxSizeToCustomView(clsIndentableTextView myTextView, CheckBox objCheckBox) {
		// No checklist option exists
		myTextView.SetCheckBoxWidthInPx(0);
	}
	
	@Override
	public void SetCheckBoxVisibilityBasedOnSettings(boolean boolIsHideSelectionStarted, boolean boolIsCheckList, CheckBox objCheckBox) {
		objCheckBox.setVisibility(View.GONE);
		if(boolIsCheckList){
			 objCheckBox.setVisibility(View.VISIBLE);
			 objCheckBox.setButtonDrawable(R.drawable.custom_check_box_black);
		}
	}
	

	@Override
	public void createThumbnailFromImage(String strTreenodeUuid, boolean boolIsAnnotated, int intResourceId) {
		// Do nothing	
	}


	@Override
	public void OnClickExec(View v) {
		clsIndentableTextView myTextView = (clsIndentableTextView)v.findViewById(R.id.row);
		clsListItem objListItem = (clsListItem)myTextView.getTag();
		UUID objUuid = objListItem.getTreeNodeGuid();
		clsTreeNode objTreeNode = objTreeview.getTreeNodeFromUuid(objUuid);
		
		// Remove any multi selection and change selection to clicked item
		objTreeview.ClearSelection();
		 objTreeNode.setSelected(true);
		 ((ActivityGroupMembers)_context).RefreshListView();
		
		// Apply edit based on item type
		if (objTreeNode.enumItemType == enumItemType.OTHER) {

		 } else {
			 ((ActivityGroupMembers)_context).AddEditFolder(objTreeNode.getName(),false,false, objUuid);
		 }
	}
	
	
}
