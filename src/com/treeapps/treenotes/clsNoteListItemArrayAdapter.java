package com.treeapps.treenotes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;





import com.treeapps.treenotes.ActivityNoteStartup.clsNoteItemStatus;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

public class clsNoteListItemArrayAdapter extends clsListItemArrayAdapter {

	public clsNoteListItemArrayAdapter(Context context, int _resource, List<clsListItem> objects,
			clsTreeview objTreeview, int intTabWidthInPx) {
		super(context, _resource, objects, objTreeview, intTabWidthInPx);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void SetCheckBoxVisibilityBasedOnSettings(boolean boolIsHideSelectionStarted, boolean boolIsCheckList,
			CheckBox objCheckBox) {
		// Override by other activities
		objCheckBox.setVisibility(View.GONE);
		if (boolIsHideSelectionStarted) {
			objCheckBox.setVisibility(View.VISIBLE);
			objCheckBox.setButtonDrawable(R.drawable.custom_check_box_red);
			return;
		}
		if (boolIsCheckList) {
			objCheckBox.setVisibility(View.VISIBLE);
			objCheckBox.setButtonDrawable(R.drawable.custom_check_box_black);
		}
	}
	
	@Override
	public void SelectItemTypeFolder(clsListItem objListItem, ImageView myImageView) {
		// This is NOTE specific, override by other activities using treeviews
		clsTreeNode objTreenode = ActivityNoteStartup.objNoteTreeview
				.getTreeNodeFromUuid(objListItem.getTreeNodeGuid());
		if (ActivityNoteStartup.objNoteTreeview.getRepository().boolHiddenSelectionIsActive == false) {
			if (!((objListItem.getFolderHasHiddenItems() == true) && objTreeview.getRepository().boolIsHiddenActive)) {
				DrawIcon(myImageView, ActivityNoteStartup.objNoteTreeview.GetIconResourceId(objTreenode,
						((ActivityNoteStartup) context).objGroupMembers, ActivityNoteStartup.objNoteTreeview),
						false,objListItem.intNewItemType);
			} else {
				DrawIcon(myImageView, ActivityNoteStartup.objNoteTreeview.GetIconResourceId(objTreenode, 
						((ActivityNoteStartup) context).objGroupMembers, ActivityNoteStartup.objNoteTreeview),
						true,objListItem.intNewItemType);
			}
		} else {
			DrawIcon(myImageView, ActivityNoteStartup.objNoteTreeview.GetIconResourceId(objTreenode, 
					((ActivityNoteStartup) context).objGroupMembers, ActivityNoteStartup.objNoteTreeview),
					false,objListItem.intNewItemType);
		}
	}
	
	@Override
	public int GetSelectColour() {
		// Override by other activities
		return (context.getResources().getColor(R.color.select_outline_color_notes));
	}
	
	@Override
	public int GetBackgroundResource(boolean boolIsSelected) {
		// Override by other activities
		if (boolIsSelected) {
			// Note: There is another one in MyTextView.java that does that view
			// only because it has higher z-order
			return R.drawable.listitem_selected_shape_notes;
		} else {
			return R.drawable.listitem_unselected_shape;
		}
	}
	
	@Override
	public void createThumbnailFromImage(String strTreenodeUuid, boolean boolIsAnnotated, int intResourceId)
	// Override by other activities
	{
		Resources r = context.getResources();
		
		String strImageFilename = ActivityExplorerStartup.fileTreeNodesDir + "/" + strTreenodeUuid + ".jpg";
		Bitmap bitmap = BitmapFactory.decodeFile(strImageFilename);

		myMediaPreviewLayerDrawable.setDrawableByLayerId(R.id.media_preview_layer_background, new BitmapDrawable(r,
				bitmap));
		if (intResourceId != clsTreeview.WEB_RESOURCE) {
			if (boolIsAnnotated) {
				myMediaPreviewLayerDrawable.setDrawableByLayerId(R.id.media_preview_layer_foreground,
						r.getDrawable(R.drawable.annotation));
			} else {
				myMediaPreviewLayerDrawable.setDrawableByLayerId(R.id.media_preview_layer_foreground, new ColorDrawable(
						Color.TRANSPARENT));
			}		
		} else {
			if (boolIsAnnotated) {
				myMediaPreviewLayerDrawable.setDrawableByLayerId(R.id.media_preview_layer_foreground,
						r.getDrawable(R.drawable.annotation_www));
			} else {
				myMediaPreviewLayerDrawable.setDrawableByLayerId(R.id.media_preview_layer_foreground, r.getDrawable(R.drawable.www));
			}
		}
	}

	@Override
	public void OnClickExec(View v) {
		clsIndentableTextView myTextView = (clsIndentableTextView) v.findViewById(R.id.row);
		clsListItem objListItem = (clsListItem) myTextView.getTag();
		UUID objUuid = objListItem.getTreeNodeGuid();
		clsTreeNode objTreeNode = objTreeview.getTreeNodeFromUuid(objUuid);

		// Shell out to edit activity
		Intent intent = new Intent(getContext(), ActivityNoteAddNew.class);
		intent.putExtra(ActivityNoteStartup.DESCRIPTION, objTreeNode.getName());
		intent.putExtra(ActivityNoteStartup.RESOURCE_ID, objTreeNode.resourceId);
		intent.putExtra(ActivityNoteStartup.RESOURCE_PATH, objTreeNode.resourcePath);
		intent.putExtra(ActivityNoteStartup.TREENODE_UID, objTreeNode.guidTreeNode.toString());
		intent.putExtra(ActivityNoteStartup.TREENODE_OWNERNAME, ((ActivityNoteStartup) context).objGroupMembers
				.GetUserNameFomUuid(objTreeNode.getStrOwnerUserUuid()));
		clsNoteItemStatus objNoteItemStatus = ((ActivityNoteStartup) context).new clsNoteItemStatus();
		((ActivityNoteStartup) context).DetermineNoteItemStatus(objTreeNode, objNoteItemStatus,
				((ActivityNoteStartup) context).objGroupMembers, ActivityNoteStartup.objNoteTreeview);
		intent.putExtra(ActivityNoteStartup.READONLY, !objNoteItemStatus.boolSelectedNoteItemBelongsToUser);

		// Send description of node's 
		String strParentDescription;
		clsTreeNode objParentTreeNode = objTreeview.getParentTreeNode(objTreeNode);
		
		strParentDescription = (objParentTreeNode == null)?(ActivityNoteStartup.objNoteTreeview.getRepository().getName())
														  :(objParentTreeNode.getName());
		
		intent.putExtra(ActivityNoteStartup.TREENODE_PARENTNAME, strParentDescription); 
		
		String strAnnotationDataGson = clsUtils.SerializeToString(objTreeNode.annotation);
		intent.putExtra(ActivityNoteStartup.ANNOTATION_DATA_GSON, strAnnotationDataGson);
		intent.putExtra(ActivityNoteStartup.USE_ANNOTATED_IMAGE, objTreeNode.getBoolUseAnnotatedImage());
		intent.putExtra(ActivityNoteStartup.ISDIRTY, false);

		((Activity) context).startActivityForResult(intent, ActivityNoteStartup.EDIT_DESCRIPTION);
		
	}
	
	@Override
	protected void RefreshListView() {
		ArrayList<clsListItem> objListItems = objTreeview.getListItems();
		clear();
		addAll(objListItems);
		notifyDataSetChanged();
		clsNewItemsIndicatorView objClsNewItemsIndicatorView = (clsNewItemsIndicatorView)((Activity) context).findViewById(R.id.newitems_indicator_view);
		objClsNewItemsIndicatorView.UpdateListItems(objListItems);
		((Activity) context).invalidateOptionsMenu();
	}

}
