package com.treeapps.treenotes;

import java.util.ArrayList;

import com.treeapps.treenotes.clsListItem.enumNewItemType;
import com.treeapps.treenotes.clsTreeview.enumItemType;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class clsNewItemsIndicatorView extends View {
	
	Context context;
	Rect objRect;
	Rect objIndicatorRectBorder;
	Paint paintIndicator;
	int intIndicatorWidthInPx;
	
	public static enum enumIndicatorItemType {
		NEW, PARENT_OF_NEW, NEW_AND_PARENT_OF_NEW
	}
	
	private class clsIndicatorItem {
		public int intStartPosPx;
		public int intStopPosPx;
		public int intItemNum;
		public enumIndicatorItemType intIndicatorItemType;
	}
	private ArrayList<clsIndicatorItem> objIndicatorItems = new ArrayList<clsIndicatorItem>();
	private ArrayList<clsListItem> objListItems;

	public clsNewItemsIndicatorView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		Init();
	}

	public clsNewItemsIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
		Init();
	}

	public clsNewItemsIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		this.context = context;
		Init();
	}
	
	private void Init() {
		intIndicatorWidthInPx = clsUtils.dpToPx(context, context.getResources().getInteger(R.integer.newitems_indicator_view_width_dp));
		
		paintIndicator = new Paint();
		paintIndicator.setDither(true);
		paintIndicator.setAntiAlias(true);


	}
	public void UpdateListItems(ArrayList<clsListItem> objListItems) {
		this.objListItems = objListItems;
		this.invalidate();
	}
	
	private void UpdateIndicators(ArrayList<clsListItem> objListItems, int intNoteLengthPx) {
		
		objIndicatorItems.clear();
		if (objListItems == null) return;
		if (objListItems.size() == 0) return;
		// First pass, find how many displayed items are new (or lowest leaves pointing to undisplayed item)
		// Also, mark their position in the list, and mark their type
		int intItemLengthPx = intNoteLengthPx/objListItems.size();
		int intItemNum = 0;
		for (clsListItem objListItem: objListItems) {
			clsIndicatorItem objIndicatorItem;
			switch (objListItem.intNewItemType) {
			case OLD:
				break;
			case ROOT_PARENT_OF_NEW:
			case PARENT_OF_NEW:
				// Only display if its collapsed and there is a a new child underneath
				if (objListItem.getItemType() == enumItemType.FOLDER_COLLAPSED) {
					objIndicatorItem = new clsIndicatorItem();
					objIndicatorItem.intIndicatorItemType = enumIndicatorItemType.PARENT_OF_NEW;
					objIndicatorItem.intStartPosPx = intItemNum * intItemLengthPx;
					objIndicatorItem.intStopPosPx = objIndicatorItem.intStartPosPx + intItemLengthPx;
					objIndicatorItems.add(objIndicatorItem);
				} else {
					// Do not display
				}
				break;
			case NEW_AND_ROOT_PARENT_OF_NEW:
			case NEW_AND_PARENT_OF_NEW:
				objIndicatorItem = new clsIndicatorItem();
				objIndicatorItem.intIndicatorItemType = enumIndicatorItemType.NEW_AND_PARENT_OF_NEW;
				objIndicatorItem.intStartPosPx = intItemNum * intItemLengthPx;
				objIndicatorItem.intStopPosPx = objIndicatorItem.intStartPosPx + intItemLengthPx;
				objIndicatorItems.add(objIndicatorItem);
				break;				
			case NEW:
				objIndicatorItem = new clsIndicatorItem();
				objIndicatorItem.intIndicatorItemType = enumIndicatorItemType.NEW;
				objIndicatorItem.intStartPosPx = intItemNum * intItemLengthPx;
				objIndicatorItem.intStopPosPx = objIndicatorItem.intStartPosPx + intItemLengthPx;
				objIndicatorItems.add(objIndicatorItem);
				break;
			}	
			intItemNum += 1;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		objRect = new Rect();
		getDrawingRect(objRect);
		int intHalfIndicatorWidthInPx = intIndicatorWidthInPx/2;
		int intQuaterIndicatorWidthInPx = (int) Math.ceil(intIndicatorWidthInPx/4);
		int intLineLeftPos = objRect.right - intIndicatorWidthInPx;
		UpdateIndicators(objListItems, objRect.bottom);
		for (clsIndicatorItem objIndicatorItem: objIndicatorItems ) {
			switch (objIndicatorItem.intIndicatorItemType) {
			case NEW:
				objIndicatorRectBorder = new Rect(intLineLeftPos,objIndicatorItem.intStartPosPx,
						intLineLeftPos + intIndicatorWidthInPx, objIndicatorItem.intStopPosPx);
				paintIndicator.setColor(Color.RED);
				paintIndicator.setStyle(Paint.Style.FILL);
				canvas.drawRect(objIndicatorRectBorder, paintIndicator);
				break;
			case PARENT_OF_NEW:				
				objIndicatorRectBorder = new Rect(intLineLeftPos,objIndicatorItem.intStartPosPx,
						intLineLeftPos + intIndicatorWidthInPx, objIndicatorItem.intStopPosPx);
				paintIndicator.setColor(Color.rgb(0xFF, 0xCC, 0x00)); // Yellow
				paintIndicator.setStyle(Paint.Style.FILL);
				canvas.drawRect(objIndicatorRectBorder, paintIndicator);
				break;
			case NEW_AND_PARENT_OF_NEW:

				objIndicatorRectBorder = new Rect(intLineLeftPos,objIndicatorItem.intStartPosPx,
						intLineLeftPos + intIndicatorWidthInPx, objIndicatorItem.intStopPosPx);
				paintIndicator.setColor(Color.RED);
				paintIndicator.setStyle(Paint.Style.FILL);
				canvas.drawRect(objIndicatorRectBorder, paintIndicator);
				objIndicatorRectBorder = new Rect(intLineLeftPos + intQuaterIndicatorWidthInPx ,objIndicatorItem.intStartPosPx,
						intLineLeftPos + intQuaterIndicatorWidthInPx + intHalfIndicatorWidthInPx, objIndicatorItem.intStopPosPx);
				paintIndicator.setColor(Color.rgb(0xFF, 0xCC, 0x00));
				paintIndicator.setStyle(Paint.Style.FILL);
				canvas.drawRect(objIndicatorRectBorder, paintIndicator);
				break;
			}
		}	
	}
}
