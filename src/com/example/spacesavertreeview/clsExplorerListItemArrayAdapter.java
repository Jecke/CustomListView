package com.example.spacesavertreeview;

import java.util.List;
import java.util.UUID;

import com.example.spacesavertreeview.clsTreeview.clsTreeNode;
import com.example.spacesavertreeview.clsTreeview.enumItemType;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class clsExplorerListItemArrayAdapter extends clsListItemArrayAdapter {

	Context _context;

	public clsExplorerListItemArrayAdapter(Context context, int _resource, List<clsListItem> objects,
			clsTreeview objTreeview, int intTabWidthInPx ) {
		super(context, _resource, objects, objTreeview, intTabWidthInPx);
		_context = context;
	}

	public void UpdateEnvironment(Context context, clsTreeview objTreeview) {
		_context = context;
		super.UpdateEnvironment(objTreeview);
	}

	@Override
	public void SelectItemTypeFolder(clsListItem objListItem, ImageView myImageView) {
		switch (objListItem.getItemType()) {
		case FOLDER_EXPANDED:
			myImageView.setImageResource(R.drawable.folder_expanded3);
			break;
		case FOLDER_COLLAPSED:
			myImageView.setImageResource(R.drawable.folder_collapsed6);
			break;
		case FOLDER_EMPTY:
			myImageView.setImageResource(R.drawable.folder);
			break;
		case OTHER:
			myImageView.setImageResource(R.drawable.notes3);
			break;
		}
	}

	@Override
	public int GetSelectColour() {
		return context.getResources().getInteger(R.color.select_outline_color_explorer);
	}

	@Override
	public int GetBackgroundResource(boolean boolIsSelected) {
		if (boolIsSelected) {
			// Note: There is another one in MyTextView.java that does that view
			// only because it has higher z-order
			return R.drawable.listitem_selected_shape_explorer;
		} else {
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
	public void SetCheckBoxVisibilityBasedOnSettings(boolean boolIsHideSelectionStarted, boolean boolIsCheckList,
			CheckBox objCheckBox) {
		objCheckBox.setVisibility(View.GONE);
	}

	boolean firstTouch = false;
	long time;

	private class MyOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {

			ExplorerOnClickExec(v);

		}

		
	}
	
	private void ExplorerOnClickExec(View v) {
		clsIndentableTextView myTextView = (clsIndentableTextView) v.findViewById(R.id.row);
		clsListItem objListItem = (clsListItem) myTextView.getTag();
		UUID objUuid = objListItem.getTreeNodeGuid();
		clsTreeNode objTreeNode = objTreeview.getTreeNodeFromUuid(objUuid);
		// Ensure item is selected first before opening by clicking
		if (objTreeNode.getSelected() == false) {
			objTreeview.ClearSelection();
			objTreeNode.setSelected(true);
			((ActivityExplorerStartup) context).RefreshListView();
			return;
		}

		// Remove any multi selection and change selection to clicked item
		objTreeview.ClearSelection();
		objTreeNode.setSelected(true);
		((ActivityExplorerStartup) context).RefreshListView();

		// Apply edit based on item type
		if (objTreeNode.enumItemType == enumItemType.OTHER) {
			Intent intent = new Intent(getContext(), ActivityNoteStartup.class);
			intent.putExtra(ActivityExplorerStartup.TREENODE_UID, objTreeNode.guidTreeNode.toString());
			intent.putExtra(ActivityExplorerStartup.TREENODE_NAME, objTreeview.getTreeNodeFromUuid(objUuid)
					.getName());
			String strImageLoadDatas = clsUtils.SerializeToString(((ActivityExplorerStartup)_context).objExplorerTreeview.getRepository().objImageLoadDatas);
			intent.putExtra(ActivityExplorerStartup.IMAGE_LOAD_DATAS,strImageLoadDatas);
			((Activity) context).startActivityForResult(intent, ActivityExplorerStartup.EDIT_NOTE);
		} else {
			((ActivityExplorerStartup) context).AddEditFolder(objTreeNode.getName(), false, false, objUuid);
		}
	}

	private class MyTextOnLongClickListener implements View.OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			// Selection
			TextView myTextView = (TextView) v.findViewById(R.id.row);
			clsListItem objListItem = (clsListItem) myTextView.getTag();
			clsTreeNode objNewSelectedTreeNode = objTreeview.getTreeNodeFromUuid(objListItem.getTreeNodeGuid());
			objTreeview.setMultipleSelectedTreenodes(objNewSelectedTreeNode);

			// Refresh view
			((ActivityExplorerStartup) context).RefreshListView();

			return true;
		}
	}
	
	private long pressStartTime;
	private float pressedX;
	private float pressedY;
	private long MAX_CLICK_DURATION = 500;
	private float MAX_CLICK_DISTANCE = 10;
	
	private final class MyOnTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent motionEvent) {

			switch (motionEvent.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					pressStartTime = System.currentTimeMillis();
					pressedX = motionEvent.getX();
					pressedY = motionEvent.getY();
					break;
				}
				case MotionEvent.ACTION_UP: {
					long pressDuration = System.currentTimeMillis() - pressStartTime;
					if (pressDuration < MAX_CLICK_DURATION
							&& distance(pressedX, pressedY, motionEvent.getX(), motionEvent.getY()) < MAX_CLICK_DISTANCE) {
						// Click event has occurred
						ExplorerOnClickExec(view);
						return false;
					}
					break;
				}
			}

			if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
				if (distance(pressedX, pressedY, motionEvent.getX(), motionEvent.getY()) > MAX_CLICK_DISTANCE) {
					// Move event has occurred
					ClipData data = ClipData.newPlainText("", "");
					DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
					view.startDrag(data, shadowBuilder, view, 0);
					return true;
				}
			}
			return false;
		}
	}
	
	public float distance(float x1, float y1, float x2, float y2) {
	    float dx = x1 - x2;
	    float dy = y1 - y2;
	    float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
	    return pxToDp(distanceInPx);
	}

	public float pxToDp(float px) {
	    return px / context.getResources().getDisplayMetrics().density;
	}

	@Override
	public View.OnLongClickListener MakeOnLongClickListener() {
		return new MyTextOnLongClickListener();
	}

	@Override
	public View.OnClickListener MakeOnClickListener() {
		return new MyOnClickListener();
	}

	@Override
	public View.OnTouchListener MakeOnTouchListener() {
		return new MyOnTouchListener();
	}
}
