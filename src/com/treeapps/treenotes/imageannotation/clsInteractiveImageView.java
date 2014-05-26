package com.treeapps.treenotes.imageannotation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

// based on Nicolas Tyler's answer on http://stackoverflow.com/questions/6650398/android-imageview-zoom-in-and-zoom-out
public class clsInteractiveImageView extends ImageView implements OnTouchListener
{
	Context context;
	
	Matrix matrix;
	Matrix inverseMatrix;

	static final int NONE 		     = 0;
    static final int DRAG 		     = 1;
    static final int ZOOM 		     = 2;
    static final int SHAPE_SELECT 	 = 3;
    static final int SHAPE_PRESELECT = 4;
    
    // radius around a point which is considered to be a click event
    static final int CLICK = 10;
    
    PointF last  = new PointF();
    PointF start = new PointF();
    
    float currentMatrixValues[];
    
    float minScale  = 1f;
    float maxScale  = 4f;
    float saveScale = 1f;
    
    float width, height;
    float origWidth, origHeight;
    float bmWidth, bmHeight;
    float right, bottom;
    float redundantXSpace, redundantYSpace;
    
    // set if redraw is necessary
    // avoids redraws if for instance user tries to move the image while it is already
    // fully visible
    boolean drawPending;
    
    // this matrix holds the values used for scaling and translation of the picture to 'fitCenter' the imageview
    Matrix initialMatrix;
    
    private int mode = NONE;
	
	ScaleGestureDetector mScaleDetector;
	
	// constructor of super class
	public clsInteractiveImageView(Context context, AttributeSet attr)
	{
		super(context, attr);
		super.setClickable(true);
		
		this.context = context;
		
		initialise();
	}
	
	// constructor of super class
	public clsInteractiveImageView(Context context) {
	    super(context);
	    super.setClickable(true);
			
	    this.context = context;
		
		initialise();
	}

	// constructor of super class
	public clsInteractiveImageView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    super.setClickable(true);
			
	    this.context = context;
		
		initialise();
	}

	public void initialise()
	{
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		matrix        = new Matrix();
		inverseMatrix = new Matrix();
		
		currentMatrixValues = new float[9];
		
		setOnTouchListener(this);
	}
	
	// Needs to be called after the image has been loaded.
	// Performs initializations which require the ImageView to be valid
	public void startMatrixOperation()
	{		
		// retrieve the current matrix and create working copy
		initialMatrix = getImageMatrix();
		matrix.set(initialMatrix);
		//matrix.invert(inverseMatrix);
		
		// change the scale type of the image so that we can manipulate it
		setScaleType(ScaleType.MATRIX);
		
		// set the working copy as the matrix of the image
		setImageMatrix(matrix);
		matrix.invert(inverseMatrix);
		//invalidate();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {	
		mScaleDetector.onTouchEvent(event);
		
		matrix.getValues(currentMatrixValues);
		
		float x = currentMatrixValues[Matrix.MTRANS_X];
		float y = currentMatrixValues[Matrix.MTRANS_Y];

		// coordinates of event
		PointF curr = new PointF(event.getX(), event.getY());
		
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				last.set(event.getX(), event.getY());
				start.set(last);
				mode = DRAG;
				break;
				
			case MotionEvent.ACTION_POINTER_DOWN:
				last.set(event.getX(), event.getY());
				start.set(last);
				mode = ZOOM;
				break;
				
			case MotionEvent.ACTION_UP:
				mode = NONE;
				/*int xDiff = (int)Math.abs(curr.x - start.x);
				int yDiff = (int)Math.abs(curr.y - start.y);
				if(xDiff < CLICK && yDiff < CLICK)
				{
					performClick();
				}*/
				break;
				
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
				
			case MotionEvent.ACTION_MOVE:
				if(mode == ZOOM || 
				   (mode == DRAG && saveScale > minScale))
				{
					drawPending = false;
					
					float deltaX = curr.x - last.x;
					float deltaY = curr.y - last.y;
					
					float scaleWidth = Math.round(origWidth * saveScale);
					float scaleHeight = Math.round(origHeight * saveScale);
					
					if(scaleWidth < width)
					{
						deltaX = 0;
						if(y + deltaY > 0)
						{
							deltaY = -y;
						}
						else if(y + deltaY < -bottom)
						{
							deltaY = -(y + bottom);
						}
					}
					else if(scaleHeight < height)
					{
						deltaY = 0;
						if(x + deltaX > 0)
						{
							deltaX = -x;
						}
						else if(x + deltaX < -right)
						{
							deltaX = -(x + right);
						}
					}
					else
					{
						if(x + deltaX > 0)
						{
							deltaX = -x;
						}
						else if (x + deltaX < -right)
						{
							deltaX = -(x + right);
						}
						
						if(y + deltaY > 0)
						{
							deltaY = -y;
						}
						else if(y + deltaY < -bottom)
						{
							deltaY = -(y + bottom);
						}
					}
					matrix.postTranslate(deltaX, deltaY);
					last.set(curr.x, curr.y);
					
					drawPending = true;
				}
				break;
		}
		
		//Toast.makeText(context, "touched", Toast.LENGTH_SHORT).show();
		// avoid unnecessary draw operations
		if(drawPending)
		{
			setImageMatrix(matrix);
			matrix.invert(inverseMatrix);
			invalidate();
		}
		return true;
	}
		
	@Override
	public void setImageBitmap(Bitmap bm)
	{
		super.setImageBitmap(bm);
		
		bmWidth  = bm.getWidth();
		bmHeight = bm.getHeight();
		
		
		Log.d("bm", String.valueOf(bmWidth)+"/"+String.valueOf(bmHeight));
		
	}
	
	public void setMaxZoom(float x)
	{
		maxScale = x;
	}
	
	//
	//
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
	{
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector)
		{
			mode = ZOOM;
			
			return true;
		}
		
		@Override
		public boolean onScale(ScaleGestureDetector detector)
		{
			float mScaleFactor = detector.getScaleFactor();
			float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale)
            {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            }
            else if (saveScale < minScale)
            {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
            right = width * saveScale - width - (2 * redundantXSpace * saveScale);
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
            if (origWidth * saveScale <= width || origHeight * saveScale <= height)
            {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
                if (mScaleFactor < 1)
                {
                    matrix.getValues(currentMatrixValues);
                    float x = currentMatrixValues[Matrix.MTRANS_X];
                    float y = currentMatrixValues[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1)
                    {
                        if (Math.round(origWidth * saveScale) < width)
                        {
                            if (y < -bottom)
                                matrix.postTranslate(0, -(y + bottom));
                            else if (y > 0)
                                matrix.postTranslate(0, -y);
                        }
                        else
                        {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0);
                            else if (x > 0)
                                matrix.postTranslate(-x, 0);
                        }
                    }
                }
            }
            else
            {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                matrix.getValues(currentMatrixValues);
                float x = currentMatrixValues[Matrix.MTRANS_X];
                float y = currentMatrixValues[Matrix.MTRANS_Y];
                if (mScaleFactor < 1) {
                    if (x < -right)
                        matrix.postTranslate(-(x + right), 0);
                    else if (x > 0)
                        matrix.postTranslate(-x, 0);
                    if (y < -bottom)
                        matrix.postTranslate(0, -(y + bottom));
                    else if (y > 0)
                        matrix.postTranslate(0, -y);
                }
            }

			return true;
		}	
	}
	
	@Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		width = MeasureSpec.getSize(widthMeasureSpec);
    	height = MeasureSpec.getSize(heightMeasureSpec);

        if(bmHeight > 0 && bmWidth > 0)
		{
        	//Fit to screen.
        	float scale;
        	float scaleX = width / bmWidth;
        	float scaleY = height / bmHeight;
        	scale = Math.min(scaleX, scaleY);
        	matrix.setScale(scale, scale);
        	setImageMatrix(matrix);
        	saveScale = 1f;

        	// Center the image
        	redundantYSpace = height - (scale * bmHeight) ;
        	redundantXSpace = width - (scale * bmWidth);
        	redundantYSpace /= 2;
        	redundantXSpace /= 2;

        	matrix.postTranslate(redundantXSpace, redundantYSpace);

        	origWidth = width - 2 * redundantXSpace;
        	origHeight = height - 2 * redundantYSpace;
        	right = width * saveScale - width - (2 * redundantXSpace * saveScale);
        	bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);

        	setImageMatrix(matrix);
        	matrix.invert(inverseMatrix);

        	matrix.getValues(currentMatrixValues);
		}
    }
}
