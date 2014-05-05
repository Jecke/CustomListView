package com.example.spacesavertreeview.sharing;

import java.util.List;
import java.util.UUID;

import com.example.spacesavertreeview.R;
import com.example.spacesavertreeview.clsIndentableTextView;
import com.example.spacesavertreeview.clsListItem;
import com.example.spacesavertreeview.clsListItemArrayAdapter;
import com.example.spacesavertreeview.clsTreeview;
import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.clsTreeview.enumItemType;
import com.example.spacesavertreeview.clsUtils;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class clsGroupMembersArrayAdapter extends clsListItemArrayAdapter  {

	Context _context;
	
	public clsGroupMembersArrayAdapter(Context context, int _resource,
			 List<clsListItem> objects, clsTreeview objTreeview) {
		super(context, _resource, objects, objTreeview);
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
	
	
	boolean firstTouch = false;
	long time;
	private class MyOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			
			
			if(firstTouch && (clsUtils.getCurrentTimeInMilliSeconds() - time) <= 300) {
                // Do stuff here for double tap
                firstTouch = false;
                EditSelectedItem(v);
            } else {
            	// Do stuff here for single tap
                firstTouch = true;
                time = clsUtils.getCurrentTimeInMilliSeconds();

                // Set single selection
                clsIndentableTextView myTextView = (clsIndentableTextView)v.findViewById(R.id.row);
        		clsListItem objListItem = (clsListItem)myTextView.getTag();
        		UUID objUuid = objListItem.getTreeNodeGuid();
        		clsTreeNode objTreeNode = objTreeview.getTreeNodeFromUuid(objUuid);
        		
        		// Remove any multi selection and change selection to clicked item
        		objTreeview.ClearSelection();
        		objTreeNode.setSelected(true);
        		((ActivityGroupMembers)_context).RefreshListView();  			
            }

		}
	}
	
	private class MyTextOnLongClickListener implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			// Selection
			TextView myTextView = (TextView)v.findViewById(R.id.row);
	        clsListItem objListItem = (clsListItem)myTextView.getTag();
	    	clsTreeNode objNewSelectedTreeNode = objTreeview.getTreeNodeFromUuid(objListItem.getTreeNodeGuid());
    		objTreeview.setMultipleSelectedTreenodes(objNewSelectedTreeNode);
    	    
    	    // Refresh view
    		((ActivityGroupMembers)_context).RefreshListView();

			return true;
		}
	}
	
	@Override
	public View.OnLongClickListener MakeOnLongClickListener() { return new MyTextOnLongClickListener();}
	
	@Override
	public View.OnClickListener MakeOnClickListener() { return new MyOnClickListener();}


	private void EditSelectedItem(View v) {
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
