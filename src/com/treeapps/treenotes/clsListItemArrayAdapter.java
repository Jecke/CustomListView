package com.treeapps.treenotes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;




import com.treeapps.treenotes.ActivityNoteStartup.clsNoteItemStatus;
import com.treeapps.treenotes.ActivityViewImage.clsListViewState;
import com.treeapps.treenotes.clsListItem.enumNewItemType;
import com.treeapps.treenotes.clsTreeview.clsRepository;
import com.treeapps.treenotes.clsTreeview.clsTreeNode;
import com.treeapps.treenotes.clsTreeview.enumItemType;
import com.treeapps.treenotes.imageannotation.clsAnnotationData;
import com.treeapps.treenotes.imageannotation.clsShapeFactory.Shape;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

public abstract class clsListItemArrayAdapter extends ArrayAdapter<clsListItem> {

	// Environment variables
	public clsTreeview objTreeview;

	// Temporary local variables
	int resource;
	Context context;
	List<clsListItem> objListItems;
	RelativeLayout todoView;
	private ImageView myMediaPreviewView;
	protected LayerDrawable myMediaPreviewLayerDrawable;
	clsListItemArrayAdapter objThisArrayAdapter;
	ViewGroup.MarginLayoutParams  objMarginLayoutParamsNarrow;
	int int2Dp;
	private int intTabWidthInPx;
	private static final int MAX_CLICK_DISTANCE = 2;
	private float fltMaxClickDistance = MAX_CLICK_DISTANCE;
	
	AlertDialog levelDialog;

	public clsListItemArrayAdapter(Context context, int _resource, List<clsListItem> objects, 
			clsTreeview objTreeview, int intTabWidthInPx) {
		super(context, _resource, objects);
 
		this.context = context;
		this.resource = _resource;
		this.objListItems = objects;
		this.objTreeview = objTreeview;
		this.objThisArrayAdapter = this;
		this.intTabWidthInPx = intTabWidthInPx;		
		int2Dp = clsUtils.dpToPx(getContext(), 2);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		clsListItem objListItem = getItem(position);
		
		if (convertView == null) {
			todoView = new RelativeLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater) getContext().getSystemService(inflater);
			li.inflate(resource, todoView, true);
		} else {
			todoView = (RelativeLayout) convertView;
		}
		// todoView.setBackgroundColor(Color.WHITE);
		todoView.setTag(objListItem);
		// Draw background of the parent list item
		if (objListItem.getSelected()) {
			// Note: There is another one in custom view that does that view
			// only because it has higher z-order and overwrites part of this
			// background
			todoView.setBackgroundResource(GetBackgroundResource(true));
		} else {
			todoView.setBackgroundResource(GetBackgroundResource(false));
		}
		
		clsIndentableTextView myTextView = (clsIndentableTextView) todoView.findViewById(R.id.row);
		myTextView.setListItem(objListItem);
		myTextView.setRawTextSizeDp(todoView.getResources().getInteger(R.integer.text_size));
		myTextView.SetTabWidthInPx(intTabWidthInPx);
		myTextView.setTag(objListItem);
		myTextView.setSelectColour(GetSelectColour());
		myTextView.setOnLongClickListener(new MyTextOnLongClickListener());
		// myTextView.setOnClickListener(MakeOnClickListener());
		myTextView.setOnTouchListener(new MyOnTouchListener());
		// Draw background of the parent list item
		if (objListItem.getSelected()) {
			objMarginLayoutParamsNarrow = (MarginLayoutParams) myTextView.getLayoutParams();		
			objMarginLayoutParamsNarrow.setMargins(0, int2Dp, int2Dp, int2Dp);
		} else {
			objMarginLayoutParamsNarrow = (MarginLayoutParams) myTextView.getLayoutParams();
			objMarginLayoutParamsNarrow.setMargins(0, 0, 0, 0);
		}
		myTextView.setOnDragListener(new MyOnDragListener());
		// myTextView.setOnDragListener(MakeOnDragListener()); 
		myTextView.requestLayout();

		ImageView myImageView = (ImageView) todoView.findViewById(R.id.icon);
		myImageView.setTag(objListItem);
		myImageView.setOnClickListener(new MyImageOnClickListener());
		SelectItemTypeFolder(objListItem, myImageView);

		myMediaPreviewView = (ImageView) todoView.findViewById(R.id.media_preview);
		myMediaPreviewView.setTag(objListItem);
		myMediaPreviewView.setOnClickListener(new MyPreviewOnClickListener());

		myMediaPreviewLayerDrawable = (LayerDrawable) context.getResources()
				.getDrawable(R.drawable.media_preview_layer);
		myMediaPreviewView.setImageDrawable(myMediaPreviewLayerDrawable);
		
		fltMaxClickDistance = clsUtils.pxToDp(getContext(), myMediaPreviewView.getLayoutParams().height/10); // This is to detect a drag

		// draw content of preview ImageView if resource is image or video
		switch (objListItem.getResourceId()) {
		case clsTreeview.TEXT_RESOURCE:
			myMediaPreviewView.setVisibility(View.GONE);
			break;

		case clsTreeview.IMAGE_RESOURCE:
			myMediaPreviewView.setVisibility(View.VISIBLE);
			createThumbnailFromImage(objListItem.getTreeNodeGuid().toString(), objListItem.boolIsAnnotated, objListItem.getResourceId());
			break;

		case clsTreeview.VIDEO_RESOURCE:
			myMediaPreviewView.setVisibility(View.VISIBLE);
			createThumbnailFromImage(objListItem.getTreeNodeGuid().toString(), objListItem.boolIsAnnotated, objListItem.getResourceId());
			break;

		case clsTreeview.WEB_RESOURCE:
			myMediaPreviewView.setVisibility(View.VISIBLE);
			createThumbnailFromImage(objListItem.getTreeNodeGuid().toString(), objListItem.boolIsAnnotated, objListItem.getResourceId());
			break;

		default:
			myMediaPreviewView.setVisibility(View.GONE);
			break;
		}

		ProvideThumbnailSizeToCustomView(myTextView);

		// Checklist or Hide activities
		CheckBox objCheckBox = (CheckBox) todoView.findViewById(R.id.checkBox_checklist);
		clsRepository objRepository = objTreeview.getRepository();
		SetCheckBoxVisibilityBasedOnSettings(objRepository.boolHiddenSelectionIsActive, objRepository.IsCheckList(),
				objCheckBox);
		if (objRepository.boolHiddenSelectionIsActive == false) {
			// Hide selection is inactive
			if (objRepository.IsCheckList() == true) {
				// Checklist activities
				// Set items depending on checklist type note

				UUID objUuid = objListItem.getTreeNodeGuid();
				final clsListItem objDialogListItem = objListItem;
				final clsTreeNode objDialogTreeNode = objTreeview.getTreeNodeFromUuid(objUuid);
				objCheckBox.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (((CheckBox) v).isChecked()) {
							if (objDialogListItem.getItemType() != enumItemType.FOLDER_EMPTY) {
								if (objTreeview.IsAnyChildrenChecked(objDialogTreeNode) == false) {
									// If checkbox is on a parent, ask user if
									// he wants all children checked
									AlertDialog.Builder builder = new AlertDialog.Builder(context);
									builder.setTitle("All items below will be checked also. Do you want to proceed?");
									builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											objTreeview.RecursiveSetChildrenChecked(objDialogTreeNode, true);
											RefreshListView();
										}
									});
									builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											objDialogTreeNode.setChecked(false);
											dialog.cancel();
											RefreshListView();
										}
									});
									builder.show();
								}
							}
							// If checkbox is on a child, just carry on checking
							// it
							objDialogTreeNode.setChecked(true);
							RefreshListView();
						} else {
							if (objDialogListItem.getItemType() != enumItemType.FOLDER_EMPTY) {

								if (objDialogTreeNode.objChildren.size() != 0) {
									// If checkbox is on a parent, ask user if he
									// wants all children unchecked
									AlertDialog.Builder builder = new AlertDialog.Builder(context);
									builder.setTitle("All items below will be unchecked also. Do you want to proceed?");
									builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											objTreeview.RecursiveSetChildrenChecked(objDialogTreeNode, false);
											RefreshListView();
										}
									});
									builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											objDialogTreeNode.setChecked(true);
											dialog.cancel();
											RefreshListView();
										}
									});
									builder.show();
								} else {
									objTreeview.RecursiveSetChildrenChecked(objDialogTreeNode, false);
									RefreshListView();
								}
							}

							// If checkbox is on a child, just carry on
							// unchecking it
							objDialogTreeNode.setChecked(false);
							// Uncheck all parents because if any child
							// inchecked, no parent can be set
							objTreeview.RecursiveSetParentChecked(objDialogTreeNode, false);

							RefreshListView();
						}
					}
				});
				if (objDialogTreeNode.getChecked()) {
					objCheckBox.setChecked(true);
				} else {
					objCheckBox.setChecked(false);
				}
				ProvideCheckBoxSizeToCustomView(myTextView, objCheckBox);

				// End of checklist activities
			}

		} else {
			// Hide selection is active
			UUID objUuid = objListItem.getTreeNodeGuid();
			final clsListItem objDialogHideListItem = objListItem;
			final clsTreeNode objDialogHideTreeNode = objTreeview.getTreeNodeFromUuid(objUuid);
			objCheckBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						objTreeview.RecursiveSetChildrenHidden(objDialogHideTreeNode, true);
						RefreshListView();
					} else {
						objTreeview.RecursiveSetChildrenHidden(objDialogHideTreeNode, false);
						RefreshListView();
					}
				}
			});
			if (objDialogHideTreeNode.getHidden()) {
				objCheckBox.setChecked(true);
			} else {
				objCheckBox.setChecked(false);
			}
			ProvideCheckBoxSizeToCustomView(myTextView, objCheckBox);
		}

		return todoView;
	}
	
	
	

	public abstract void SetCheckBoxVisibilityBasedOnSettings(boolean boolIsHideSelectionStarted, boolean boolIsCheckList,
			CheckBox objCheckBox);

	public void ProvideThumbnailSizeToCustomView(clsIndentableTextView myTextView) {
		// Sizing of the custom list item
		if (myMediaPreviewView.getVisibility() == View.VISIBLE) {
			ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) myMediaPreviewView.getLayoutParams();
			int intThumbnailWidth = myMediaPreviewView.getLayoutParams().width;
			myTextView.SetThumbnailWidthInPx(intThumbnailWidth);
			int intThumbnailHeight = myMediaPreviewView.getLayoutParams().height + vlp.leftMargin + vlp.rightMargin;
			myTextView.SetThumbnailHeightInPx(intThumbnailHeight);
		} else {
			myTextView.SetThumbnailWidthInPx(0);
			myTextView.SetThumbnailHeightInPx(0);
		}

	}

	public void ProvideCheckBoxSizeToCustomView(clsIndentableTextView myTextView, CheckBox objCheckBox) {
		// Sizing of the custom list item
		if (objCheckBox.getVisibility() == View.VISIBLE) {
			int intCheckBoxWidth = objCheckBox.getLayoutParams().width;
			myTextView.SetCheckBoxWidthInPx(intCheckBoxWidth);
		} else {
			myTextView.SetCheckBoxWidthInPx(0);
		}
	}

	public abstract void SelectItemTypeFolder(clsListItem objListItem, ImageView myImageView);
	
	public void DrawIcon (ImageView myImageView, int intBackgroundIconRid, boolean boolIsHidden, enumNewItemType intNewItemType  ) {
		int intLayerCount = 1;
		boolean boolIsNew = (intNewItemType != enumNewItemType.OLD) ? true: false;
		if (boolIsHidden) intLayerCount +=1;
		if (boolIsNew) intLayerCount +=1;
			
		if (intLayerCount == 1) {
			myImageView.setImageResource(intBackgroundIconRid);
			return;
		}
		
		Resources r = context.getResources();
		Drawable[] layers = new Drawable[intLayerCount];
		layers[0] = r.getDrawable(intBackgroundIconRid);
		if (intLayerCount ==  2) {
			if (boolIsHidden) layers[1] = r.getDrawable(R.drawable.icon_overlay_hidden);
			if (boolIsNew)  {
				switch (intNewItemType) {
				case ROOT_PARENT_OF_NEW:
				case PARENT_OF_NEW:
					layers[1] = r.getDrawable(R.drawable.icon_overlay_new_yellow);
					break;
				case NEW_AND_ROOT_PARENT_OF_NEW:
				case NEW_AND_PARENT_OF_NEW:
					layers[1] = r.getDrawable(R.drawable.icon_overlay_new_redyellow);
					break;				
				case NEW:
					layers[1] = r.getDrawable(R.drawable.icon_overlay_new);
					break;
				default:
					break;
				}				
			}
		} else {
			layers[1] = r.getDrawable(R.drawable.icon_overlay_hidden);
			layers[2] = r.getDrawable(R.drawable.icon_overlay_new);
		}
		LayerDrawable layerDrawable = new LayerDrawable(layers);
		myImageView.setImageDrawable(layerDrawable);
	}

	public abstract int GetSelectColour();

	public abstract int GetBackgroundResource(boolean boolIsSelected);
	
	
	public abstract void createThumbnailFromImage(String strTreenodeUuid, boolean boolIsAnnotated, int intResourceId);

	private class MyImageOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ImageView myImageView = (ImageView) v.findViewById(R.id.icon);
			clsListItem objListItem = (clsListItem) myImageView.getTag();
			clsTreeNode objTreeNode = objTreeview.getTreeNodeFromUuid(objListItem.getTreeNodeGuid());

			if (objTreeNode.enumItemType == enumItemType.FOLDER_COLLAPSED) {
				objTreeNode.enumItemType = enumItemType.FOLDER_EXPANDED;
				objTreeview.SetAllIsDirty(true);
			} else if (objTreeNode.enumItemType == enumItemType.FOLDER_EXPANDED) {
				objTreeNode.enumItemType = enumItemType.FOLDER_COLLAPSED;
				objTreeview.SetAllIsDirty(true);
			} else {
				return;
			}

			// Update changes
			RefreshListView();
		}
	}

	// class used on single click on image/video thumbnail
	private class MyPreviewOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {

			// the folder has access to all object data via TAG
			ImageView folder = (ImageView) v.findViewById(R.id.media_preview);
			clsListItem objListItem = (clsListItem) folder.getTag();
			
			// Determine the access rights of the node
			clsTreeNode objTreenode = objTreeview.getTreeNodeFromUuid(objListItem.getTreeNodeGuid());
			clsNoteItemStatus objStatus = ((ActivityNoteStartup) context).new clsNoteItemStatus();
			((ActivityNoteStartup) context).DetermineNoteItemStatus(objTreenode, objStatus, ((ActivityNoteStartup) context).objGroupMembers, objTreeview);
			
			// Determine if image needs to display annotated or full image
			boolean boolDisplayAnnotated = DetermineIfAnnotatedImageToBeDisplayed(objTreenode, objStatus);
			
			// Start the display
			String contentString = objListItem.getResourcePath();

			switch (objListItem.getResourceId()) {
			case clsTreeview.IMAGE_RESOURCE:
			case clsTreeview.WEB_RESOURCE:				
				if (!boolDisplayAnnotated) { 
					// Display using original source using stock viewer if only image available
					boolean boolIsRemoteImage;
					File fileLocalImageUri;
					fileLocalImageUri = new File (clsUtils.GetFullImageFileName(context, objListItem.getTreeNodeGuid().toString()));
					if (!fileLocalImageUri.exists()) {
						if (!contentString.startsWith("/", 0) && !contentString.startsWith("file://", 0)) {
							// Uri appears to be for a remote image
							boolIsRemoteImage = true;						
						} else {
							clsUtils.MessageBox(context, "Image does not exists locally and remotely", true);
							return;
						}
					} else {
						boolIsRemoteImage = false;
					}

					// for remote images
					// (local files start either with file:// or /)
					if (boolIsRemoteImage) {
						// for remote image
						Intent img_intent = new Intent();
						img_intent.setAction(Intent.ACTION_VIEW);
						img_intent.setData(Uri.parse(contentString));
						try {
							context.startActivity(img_intent);
						} catch (Exception e) {
							clsUtils.MessageBox(context, "Unable to display remote image", true);
							return;
						}
					} else // for local unannotated images
					{
						// Local file, use custom viewer
						Intent intentViewImage = new Intent(context, ActivityViewImage.class);
						intentViewImage.putExtra(ActivityViewImage.URL, "");
						intentViewImage.putExtra(ActivityNoteStartup.TREENODE_UID, objListItem.getTreeNodeGuid().toString());
						intentViewImage.putExtra(ActivityViewImage.DESCRIPTION, objTreenode.getName());
						intentViewImage.putExtra(ActivityViewImage.LISTVIEWSTATES_GSON, "");
						intentViewImage.putExtra(ActivityNoteStartup.ANNOTATION_DATA_GSON, "");								
						context.startActivity(intentViewImage);
					}
				} else {
					// Annotated, display using custom viewer
					// ActivityViewImage also displays additional text information
					Intent intentViewImage = new Intent(context, ActivityViewImage.class);

					intentViewImage.putExtra(ActivityViewImage.URL, objTreenode.strWebPageURL);
					intentViewImage.putExtra(ActivityNoteStartup.TREENODE_UID, objListItem.getTreeNodeGuid().toString());

					clsAnnotationData objAnnotationData = objTreenode.annotation;
					ArrayList<ActivityViewImage.clsListViewState> objListViewStates = new ArrayList<ActivityViewImage.clsListViewState>();
					if (objAnnotationData != null) {
						for (clsAnnotationData.clsAnnotationItem objAnnotationItem : objAnnotationData.items) {
							if (objAnnotationItem.getType() == Shape.NUMBERED_ARROW) {
								ActivityViewImage.clsListViewState objListViewState = new clsListViewState();
								objListViewState.strArrowDescription = objAnnotationItem.getAnnotationText();
								objListViewStates.add(objListViewState);
							}
						}
					}
					String strListViewStates = clsUtils.SerializeToString(objListViewStates);
					intentViewImage.putExtra(ActivityViewImage.DESCRIPTION, objTreenode.getName());
					intentViewImage.putExtra(ActivityViewImage.LISTVIEWSTATES_GSON, strListViewStates);
					
					String strAnnotationData = clsUtils.SerializeToString(objAnnotationData);
					intentViewImage.putExtra(ActivityNoteStartup.ANNOTATION_DATA_GSON, strAnnotationData);
					
					context.startActivity(intentViewImage);
				}
				break;

			case clsTreeview.VIDEO_RESOURCE:
				Intent vid_intent = new Intent();
				vid_intent.setAction(Intent.ACTION_VIEW);

				// TODO JE Handle remote (URL, Uri) videos

				if (!contentString.isEmpty()) {
					String prefix;
					
					if(contentString.startsWith("/"))
					{
						prefix = "file:/";
					}
					else
					{
						prefix = "file://";
					}

					vid_intent.setDataAndType(Uri.parse(prefix + contentString), "video/*");
					
					context.startActivity(vid_intent);
				}
				break;

			default:
				assert (false);
				break;
			}
		}

		private boolean DetermineIfAnnotatedImageToBeDisplayed(clsTreeNode objTreenode, clsNoteItemStatus objStatus) {
			// Annotated == true, full = false
			if (objTreenode.annotation == null) {
				// No annotation
				return false;
			} else {
				// Annotated, determine if item has rights for optional display
				if (objStatus.boolIsUserRegistered) {
					// User is registered, so check rights
					if (!objStatus.boolSelectedNoteItemBelongsToUser) {
						// No option, just display annotation
						return  true;
					} else {
						// Option exists, use option
						if (objTreenode.getBoolUseAnnotatedImage()) {
							return  true;
						} else {
							return  false;
						}
					}
				} else {
					// User not registered, so if item has rights by default for optional display
					if (objTreenode.getBoolUseAnnotatedImage()) {
						return  true;
					} else {
						return  false;
					}
				}
			}
		}

		// helper: retrieve the path of a resource from the internal database
		// by using the URI
		private String getLocalPathFromUri(int resourceId, Uri uri) {
			String path = "";
			String[] filePathColumn;
			Cursor cursor;
			int columnIndex;

			switch (resourceId) {
			case clsTreeview.IMAGE_RESOURCE: {
				filePathColumn = new String[1];
				filePathColumn[0] = MediaStore.Images.Media.DATA;
			}
				break;

			case clsTreeview.VIDEO_RESOURCE: {
				filePathColumn = new String[1];
				filePathColumn[0] = MediaStore.Video.Media.DATA;
			}
				break;

			default:
				assert (false);
				return path;
			}

			// retrieve the file path from the database
			cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					columnIndex = cursor.getColumnIndex(filePathColumn[0]);

					path = cursor.getString(columnIndex);

					cursor.close();
				} else {
					// show error dialog because file could not be found
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage(R.string.dlg_error_file_not_found);

					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

						}
					});

					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}

			return path;
		}
	}

	protected abstract void RefreshListView();

	private class MyTextOnLongClickListener implements View.OnLongClickListener {
		public boolean onLongClick(View v) {
			// Selection
			TextView myTextView = (TextView) v.findViewById(R.id.row);
			clsListItem objListItem = (clsListItem) myTextView.getTag();
			clsTreeNode objNewSelectedTreeNode = objTreeview.getTreeNodeFromUuid(objListItem.getTreeNodeGuid());
			objTreeview.setMultipleSelectedTreenodes(objNewSelectedTreeNode);

			// Refresh view
			RefreshListView();
			return true;
		}
	}

	
	private long pressStartTime;
	private float pressedX;
	private float pressedY;
	private long MAX_CLICK_DURATION = 500;
	
	

	public final class MyOnTouchListener implements OnTouchListener {
		public boolean onTouch(View view, MotionEvent motionEvent) {

			switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				pressStartTime = System.currentTimeMillis();
				pressedX = motionEvent.getX();
				pressedY = motionEvent.getY();
				break;
			case MotionEvent.ACTION_UP:
				long pressDuration = System.currentTimeMillis() - pressStartTime;
				if (pressDuration < MAX_CLICK_DURATION
						&& distance(pressedX, pressedY, motionEvent.getX(), motionEvent.getY()) < fltMaxClickDistance) {
					// Click event has occurred
					// Make sure event is fired, if already selected
					clsIndentableTextView myTextView = (clsIndentableTextView) view.findViewById(R.id.row);
					clsListItem objListItem = (clsListItem) myTextView.getTag();
					UUID objUuid = objListItem.getTreeNodeGuid();
					clsTreeNode objTreeNode = objTreeview.getTreeNodeFromUuid(objUuid);

					// Ensure item is selected first before opening by clicking
					if (objTreeNode.getSelected() == false) {
						objTreeview.ClearSelection();
						objTreeNode.setSelected(true);
						RefreshListView();
						return false;
					}
					
					// Ensure if there is a multiselect, only a single select happens, no fire of event
					if (objTreeview.getSelectedTreenodes().size() > 1) {
						objTreeview.ClearSelection();
						objTreeNode.setSelected(true);
						RefreshListView();
						return false;
					}
					
					objTreeview.ClearSelection();
					objTreeNode.setSelected(true);
					
					// Now fire event
					OnClickExec  (view);
					return false;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				
				if (pressedX < (2 * view.getWidth())/3) break;
				
				float fltDistance = distance(pressedX, pressedY, motionEvent.getX(), motionEvent.getY());
				clsUtils.CustomLog("fltDistance =: " + fltDistance);
				if (fltDistance > fltMaxClickDistance) {
					// Move event has occurred
					// Get the nodeUuid from where the move started
					clsListItem objListItem = (clsListItem) view.getTag();
					String strTreeNodeUuid = objListItem.getTreeNodeGuid().toString();

					// Start the move
					ClipData data = ClipData.newPlainText("strTreeNodeUuid", strTreeNodeUuid);
					DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
					view.startDrag(data, shadowBuilder, view, 0);
					return true;
				}
			}
			return false;
		}
	}
	
	public class MyOnDragListener implements OnDragListener {
		@Override
		public boolean onDrag(View v, DragEvent event) {
			int action = event.getAction();
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			case DragEvent.ACTION_DROP:
				clsListItem objListItem = (clsListItem) v.getTag();
				final String strTargetTreeNodeUuid = objListItem.getTreeNodeGuid().toString();
				final ClipData objClipData = event.getClipData();
				if (objClipData.getItemAt(0) != null) {
					// Create dialog to determine where user wants new placement
					final CharSequence[] items = { " Below ", " Before ", " After ", " Cancel " };
					// Creating and Building the Dialog
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Where do you want to drop the item?");
					builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int item) {
							// TODO Auto-generated method stub
							String strSourceTreeNodeUuid = (String) objClipData.getItemAt(0).coerceToText(getContext());
							clsTreeNode objSourceTreeNode = objTreeview.getTreeNodeFromUuid(UUID
									.fromString(strSourceTreeNodeUuid));
							clsTreeNode objTargetTreeNode = objTreeview.getTreeNodeFromUuid(UUID
									.fromString(strTargetTreeNodeUuid));
							boolean boolIsTargetChildOfSource = objTreeview.IsTargetChildOfSource(objSourceTreeNode,
									objTargetTreeNode);

							if (item != 3 && boolIsTargetChildOfSource) {
								// Can only move if items are peers
								clsUtils.MessageBox(context, "Cannot move item into own child items", true);
							} else {
								switch (item) {
								case 0:
									// Below
									boolean boolIsSourceDropable = objTreeview.IsSourceDropableOnTarget(objSourceTreeNode,
											objTargetTreeNode);
									if (boolIsSourceDropable) {
										objTreeview.addSourceTreeNodeBelowTarget(objSourceTreeNode, objTargetTreeNode);
									} else {
										clsUtils.MessageBox(context, "A folder cannot reside below a note", true);
									}
									break;
								case 1:
									// Before
									objTreeview.addSourceTreeNodeBeforeOrAfterTarget(objSourceTreeNode,
											objTargetTreeNode, true);
									break;
								case 2:
									// After
									objTreeview.addSourceTreeNodeBeforeOrAfterTarget(objSourceTreeNode,
											objTargetTreeNode, false);
									break;
								case 3:
									// Cancel
									break;
								}
							}
							levelDialog.dismiss();
							RefreshListView();
						}
					});
					levelDialog = builder.create();
					levelDialog.show();
				}
				break;
			case DragEvent.ACTION_DRAG_ENDED:
			default:
				break;
			}
			return true;
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
	
	// Override by other activities
	public abstract void OnClickExec(View v);
	

	public void UpdateEnvironment(clsTreeview objTreeview) {
		// TODO Auto-generated method stub
		this.objTreeview = objTreeview;
		 SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		 String strTabWidthInPx = sharedPref.getString("treenotes_default_user_indent_tab_width", "");
		 intTabWidthInPx = clsUtils.dpToPx(context, Integer.parseInt(strTabWidthInPx));
		 sharedPref= null;
	}
	

}
