package com.example.spacesavertreeview;


import com.example.spacesavertreeview.clsTreeview.enumItemLevelRelation;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class clsIndentableTextView extends TextView {
	
	TextView objMyTextView;

	private int _intTextSize;
	private int _intIndentLevelAmountMax;
	private int intTabWidthInPx;

	private clsListItem _objListItem;
	
	private Context _context;
	
	private int intThumbnailWidthInPx;
	private int intThumbnailHeightInPx;
	
	private int intCheckBoxWidthInPx;
	
  public clsIndentableTextView (Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    _context = context;
    init();
    _intIndentLevelAmountMax = getResources().getInteger(R.integer.indent_amount_max);
    this.objMyTextView  = this;
    
  }
  public clsIndentableTextView (Context context) {
    super(context);
    _context = context;
    init();
    _intIndentLevelAmountMax = getResources().getInteger(R.integer.indent_amount_max);
    this.objMyTextView  = this;
  }
  public clsIndentableTextView (Context context, AttributeSet attrs) {
    super(context, attrs);
    _context = context;
    init();
    _intIndentLevelAmountMax = getResources().getInteger(R.integer.indent_amount_max);
    this.objMyTextView  = this;
  }
  
  
  private Rect objRectMyView = new Rect();
  private Rect objRectMyViewHeightAdjusted;
  int colorFrom;
  int colorTo;
  Paint paintTextWhite;
  Paint paintTextBlack;
  Paint paintPlainBlack;
  Paint paintIndentBackground;
  Paint paintSelectOutline;
  private LinearGradient myLinearGradient;
  int intColorStart;
  int intColorEnd;
  int intSelectColor;
  
 
  private void init() {
	  	    
	  
	  /*
	  
	  float[] mat = new float[]
	  	        {
	  	            -1,  0,  0, 0,  255,
	  	             0, -1,  0, 0,  255,
	  	             0,  0, -1, 0,  255,
	  	             0,  0,  0, 1,  0
	  	        };
      cf = new ColorMatrixColorFilter(new ColorMatrix(mat));
      
	  paintTextWhite = new Paint();
      paintTextWhite.setDither(true);
      paintTextWhite.setAntiAlias(true);
	  paintTextWhite.setColor(Color.rgb(127, 127, 127));
	  paintTextWhite.setTextSize(_intTextSize);
	  paintTextWhite.setTextAlign(Align.LEFT);
	  paintTextWhite.setColorFilter(cf);

	  paintTextBlack = new Paint();
	  paintTextBlack.setDither(true);
	  paintTextBlack.setAntiAlias(true);
	  paintTextBlack.setColor(Color.rgb(127, 127, 127));
	  paintTextBlack.setTextSize(_intTextSize);
	  paintTextBlack.setTextAlign(Align.LEFT);
	  paintTextBlack.setColorFilter(cf);
	  
	  PorterDuffColorFilter porterDuffColorFilter 
	   = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.OVERLAY);
	  */
	   
      paintPlainBlack = new Paint();
      paintPlainBlack.setDither(true);
      paintPlainBlack.setAntiAlias(true);
      paintPlainBlack.setColor(Color.BLACK);
      
      paintSelectOutline =  new Paint();
      paintSelectOutline.setDither(true);
      paintSelectOutline.setAntiAlias(true);
      paintSelectOutline.setColor(getResources().getInteger(R.color.select_outline_color_notes));
      paintSelectOutline.setStyle(Paint.Style.STROKE);
      float fltLineWidthInDp = clsUtils.dpToPx(getContext(),getResources().getInteger(R.integer.select_line_width_in_dp));
      paintSelectOutline.setStrokeWidth(fltLineWidthInDp * 2 );

  	       
      paintIndentBackground = new Paint();
           
      setWillNotDraw(false);
  }
  
  
  
  
  public void setListItem(clsListItem objListItem){
	  _objListItem = objListItem;
  }
  
 
  public void setRawTextSizeDp(int intTextSize){
	  _intTextSize = intTextSize;
	  paintPlainBlack.setTextSize(clsUtils.dpToPx(_context, _intTextSize));
  }
  
  public void setSelectColour(int intSelectColour){
	  this.intSelectColor = intSelectColour;
	  paintSelectOutline.setColor(intSelectColour);
  }
  
  public int getSelectionColour() {
	  return this.intSelectColor;
  }
  
//	//delegate the event to the gesture detector
//	@Override
//	public boolean onTouchEvent(MotionEvent e) {
//	   return gestureDetector.onTouchEvent(e);
//	}
//
//
//private class GestureListener extends GestureDetector.SimpleOnGestureListener {
//
//   @Override
//   public boolean onDown(MotionEvent e) {
//       return true;
//   }
//   @Override
//   public  boolean  onSingleTapConfirmed(MotionEvent e) {
//       clsUtils.CustomLog("onSingleTapConfirmed");
//       return true;
//   }
//   @Override
//   public boolean onDoubleTap(MotionEvent e) {
//	   clsUtils.CustomLog("onDoubleTap");
//       return true;
//   }
//}
 
  @Override
	protected void onDraw(Canvas canvas) {

		  this.getDrawingRect(objRectMyView);
		  clsUtils.CustomLog("onDraw getClipBounds.width = " + (objRectMyView.right - objRectMyView.left));
	  
		  // Modify height due to thumbnail size
		  objRectMyViewHeightAdjusted =  new Rect( 
				  objRectMyView.left,
				  objRectMyView.top,
				  objRectMyView.right,
				  Math.max(objRectMyView.bottom,objRectMyView.top + GetThumbnailHeightInPx() ));
		  
		  objRectMyViewHeightAdjusted.right = getMeasuredWidth();

		  // Calculate where the tab split must occur
		  float fltRowWidth = objRectMyViewHeightAdjusted.right - objRectMyViewHeightAdjusted.left;
		  float fltTabWidth;
		  if (intTabWidthInPx == 0) {
			  fltTabWidth = fltRowWidth/_intIndentLevelAmountMax;
		  } else {
			  fltTabWidth = intTabWidthInPx;
		  }
		  float fltIndentWidth = _objListItem.getLevel() * fltTabWidth;
		  float fltIndentRemainWidth = fltRowWidth - fltIndentWidth;
		  
		  RectF objIndentRect = new RectF();
		  objIndentRect.left = objRectMyViewHeightAdjusted.left + fltIndentWidth;
		  objIndentRect.top = objRectMyViewHeightAdjusted.top;
		  objIndentRect.right = objIndentRect.left + fltIndentRemainWidth;
		  objIndentRect.bottom = objRectMyViewHeightAdjusted.bottom;
		  
		  
		  int intTextYpos = (int) (((objRectMyViewHeightAdjusted.bottom-objRectMyViewHeightAdjusted.top) / 2) - ((paintPlainBlack.descent() + paintPlainBlack.ascent()) / 2)) ; 

		  if(_objListItem.getSelected()){
			  intColorStart = getSelectionColour();
			  setBackgroundColor(getResources().getInteger(R.color.select_fill_color));
		  } else {
			  intColorStart = getResources().getInteger(R.color.unselect_fill_color);
			  setBackgroundColor(getResources().getInteger(R.color.white_background));
		  }
		  paintIndentBackground.setColor(intColorStart);
		  myLinearGradient = new LinearGradient(objIndentRect.left,objIndentRect.top,
				  objIndentRect.right,objIndentRect.bottom,intColorStart,intColorEnd,Shader.TileMode.CLAMP);
		  paintIndentBackground.setShader(myLinearGradient);
	  
		  DrawCustomRect(canvas,objIndentRect,paintIndentBackground);		  
		  canvas.drawText(FormatString(_objListItem.getName(),paintPlainBlack), 0, intTextYpos, paintPlainBlack);
		  	  	  
		  super.onDraw(canvas);
	}
  
  private void DrawCustomRect(Canvas objCanvas, RectF objRect, Paint objPaint){
	  final float fltRadius = 15f;
	  final float fltDiam = fltRadius * 2;
	  Path path = new Path();
	  if (_objListItem.GetAboveItemLevelRelation() == enumItemLevelRelation.SAME){
		  if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.SAME) {
			// Top has 90 degree corner, bottom has 90 degree corner
			  objCanvas.drawRect(objRect,objPaint);
		  } else if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.HIGHER) {
			// Top has 90 degree corner, bottom has soft corner
	  		  path.moveTo(objRect.right, objRect.top);
			  path.lineTo(objRect.right, objRect.bottom);
			  path.lineTo(objRect.left+fltRadius, objRect.bottom);
			  path.addArc(new RectF(objRect.left,objRect.bottom-fltDiam,objRect.left+fltDiam,objRect.bottom), 90, 90);
			  path.lineTo(objRect.left, objRect.top);
			  path.lineTo(objRect.right, objRect.top);
			  objCanvas.drawPath(path,objPaint);
		  } else if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.LOWER) {
			// Top has 90 degree corner, bottom has spiked corner
	  		  path.moveTo(objRect.right, objRect.top);
	  		  path.lineTo(objRect.right, objRect.bottom);
	  		  path.lineTo(objRect.left-fltRadius, objRect.bottom);
			  path.addArc(new RectF(objRect.left-fltDiam,objRect.bottom-fltDiam,objRect.left,objRect.bottom), 90, -90);
			  path.lineTo(objRect.left, objRect.top);
			  path.lineTo(objRect.right, objRect.top);
			  objCanvas.drawPath(path,objPaint);
		  }
	  } else if (_objListItem.GetAboveItemLevelRelation() == enumItemLevelRelation.HIGHER){
		  if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.SAME) {
			// Top has soft corner, bottom has 90 degree corner
			  path.moveTo(objRect.right, objRect.top);
	  		  path.lineTo(objRect.right, objRect.bottom);
	  		  path.lineTo(objRect.left, objRect.bottom);
		  	  path.lineTo(objRect.left, objRect.top+fltRadius);
		  	  path.addArc(new RectF(objRect.left,objRect.top,objRect.left+fltDiam,objRect.top+fltDiam), 180, 90);
		  	  path.lineTo(objRect.right, objRect.top);		  
		  	  objCanvas.drawPath(path,objPaint);
		  } else if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.HIGHER) {
			// Top has soft degree corner, bottom has soft corner
			  path.moveTo(objRect.right, objRect.top);
			  path.lineTo(objRect.right, objRect.bottom);
			  path.lineTo(objRect.left+fltRadius, objRect.bottom);
			  path.addArc(new RectF(objRect.left,objRect.bottom-fltDiam,objRect.left+fltDiam,objRect.bottom), 90, 90);
			  path.lineTo(objRect.left, objRect.top+fltRadius);
			  path.lineTo(objRect.right, objRect.top);
		  	  path.addArc(new RectF(objRect.left,objRect.top,objRect.left+fltDiam,objRect.top+fltDiam), 180, 90);
		  	  path.lineTo(objRect.right, objRect.top);		  
		  	  objCanvas.drawPath(path,objPaint);
		  } else if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.LOWER) {
			// Top has soft degree corner, bottom has spiked corner
			  path.moveTo(objRect.right, objRect.top);
	  		  path.lineTo(objRect.right, objRect.bottom);
	  		  path.lineTo(objRect.left-fltRadius, objRect.bottom);
			  path.addArc(new RectF(objRect.left-fltDiam,objRect.bottom-fltDiam,objRect.left,objRect.bottom), 90, -90);
			  path.lineTo(objRect.left, objRect.top+fltRadius);
			  path.lineTo(objRect.right, objRect.top);
		  	  path.addArc(new RectF(objRect.left,objRect.top,objRect.left+fltDiam,objRect.top+fltDiam), 180, 90);
		  	  path.lineTo(objRect.right, objRect.top);
		  	  objCanvas.drawPath(path,objPaint);
		  }
	  } else if (_objListItem.GetAboveItemLevelRelation() == enumItemLevelRelation.LOWER) {
		  if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.SAME) {
			// Top has spiked corner, bottom has 90 degree corner
			  path.moveTo(objRect.right, objRect.top);
	  		  path.lineTo(objRect.right, objRect.bottom);
	  		  path.lineTo(objRect.left, objRect.bottom);
	  		  path.lineTo(objRect.left, objRect.top+fltRadius);
	  		  path.addArc(new RectF(objRect.left-fltDiam,objRect.top,objRect.left,objRect.top+fltDiam), 0, -90);
	  		  path.lineTo(objRect.right, objRect.top);
	  		  objCanvas.drawPath(path,objPaint);    
		  } else if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.HIGHER) {
			// Top has spiked corner, bottom has soft corner
			  path.moveTo(objRect.right, objRect.top);
			  path.lineTo(objRect.right, objRect.bottom);
			  path.lineTo(objRect.left+fltRadius, objRect.bottom);
			  path.addArc(new RectF(objRect.left,objRect.bottom-fltDiam,objRect.left+fltDiam,objRect.bottom), 90, 90);		  
			  path.lineTo(objRect.left, objRect.top+fltRadius);
			  path.lineTo(objRect.right, objRect.top);
			  path.addArc(new RectF(objRect.left-fltDiam,objRect.top,objRect.left,objRect.top+fltDiam), 0, -90);
	  		  path.lineTo(objRect.right, objRect.top);
	  		  objCanvas.drawPath(path,objPaint);
		  } else if (_objListItem.GetBelowItemLevelRelation() == enumItemLevelRelation.LOWER) {
			// Top has spiked corner, bottom has spiked corner
			  path.moveTo(objRect.right, objRect.top);
	  		  path.lineTo(objRect.right, objRect.bottom);
	  		  path.lineTo(objRect.left-fltRadius, objRect.bottom);
			  path.addArc(new RectF(objRect.left-fltDiam,objRect.bottom-fltDiam,objRect.left,objRect.bottom), 90, -90);
			  path.lineTo(objRect.left, objRect.top+fltRadius);
			  path.lineTo(objRect.right, objRect.top);
			  path.addArc(new RectF(objRect.left-fltDiam,objRect.top,objRect.left,objRect.top+fltDiam), 0, -90);
	  		  path.lineTo(objRect.right, objRect.top);
	  		  objCanvas.drawPath(path,objPaint); 
		  }
	  } 
	  
  }
   
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      //getMeasuredHeight() and getMeasuredWidth() now contain the suggested size
      clsUtils.CustomLog("onMeasure getMeasuredWidth = " + getMeasuredWidth());
  }
  
  private void DrawSelectRect(Canvas objCanvas, Rect objRect, Paint objPaint){
	  objCanvas.drawLine(objRect.left,objRect.top, objRect.right,objRect.top, objPaint);
	  objCanvas.drawLine(objRect.right,objRect.top, objRect.right,objRect.bottom, objPaint);
	  objCanvas.drawLine(objRect.left,objRect.bottom, objRect.right,objRect.bottom, objPaint);
  }
  
 
  private String FormatString(String strIn, Paint objPaint) { 
	  // Truncate if string getting to long. Cannot get wrapping to work yet. Temporary workaround 
	  
	  float fltTextSize = objPaint.measureText(strIn) ;
	  float[] fltMeasuredWidth = {0f};
	  float fltMaxWidth;
	  
	  fltMaxWidth = objRectMyViewHeightAdjusted.right - objRectMyViewHeightAdjusted.left - GetThumbnailWidthInPx();
	  clsUtils.CustomLog("FormatString GetThumbnailWidthInPx() = " + GetThumbnailWidthInPx());
	  clsUtils.CustomLog("FormatString GetCheckBoxWidthInPx() = " + GetCheckBoxWidthInPx());
	  clsUtils.CustomLog("FormatString dpToPx(_context, 5) = " + clsUtils.dpToPx(_context, 5));
      int intBreakChar = objPaint.breakText(strIn, true, fltMaxWidth, fltMeasuredWidth);
      if (intBreakChar != 0) {
    	  if (fltMaxWidth  <= fltTextSize){
    		  return strIn.substring(0, intBreakChar-3) + "...";
    	  } 
      }
	  return strIn;
  }
  
	  public void SetThumbnailWidthInPx(int intWidthInPx){
		  this.intThumbnailWidthInPx = intWidthInPx;
	  }
	  
	private int GetThumbnailWidthInPx() {
		return this.intThumbnailWidthInPx; 
	}

	public void SetThumbnailHeightInPx(int intHeightInPx){
		  this.intThumbnailHeightInPx = intHeightInPx;
	}
	
	private int GetThumbnailHeightInPx() {
		return this.intThumbnailHeightInPx; 
	}
  
	 public void SetCheckBoxWidthInPx(int intWidthInPx){
		  this.intCheckBoxWidthInPx = intWidthInPx;
	  }
	  
	private int GetCheckBoxWidthInPx() {
		return this.intCheckBoxWidthInPx; 
	}
	
	public void SetTabWidthInPx(int intTabWidthInPx) {
		this.intTabWidthInPx = intTabWidthInPx; 
	}
	
}
