package com.treeapps.treenotes.imageannotation;

import java.util.Arrays;

import com.treeapps.treenotes.R;
import com.treeapps.treenotes.clsUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.graphics.PointF;

/*
 * Base class for supported shapes
 */
// base class for shapes
public class clsShapeBase
{
	protected static final float HOTSPOT_RADIUS = 10f;
	protected static final float HOTSPOT_PICKRADIUS = 2 * HOTSPOT_RADIUS;

	private static final float TEXT_SIZE_DEFAULT = 30f;
	private float textSize = 30;

	protected Context objContext;

	protected clsShapeFactory.Shape type;
	protected float[] reference;

	protected boolean selected;

	protected Paint paint;
	protected Paint hotspotPaint;
	protected Paint textPaint;
	protected Paint rubberBand;
	
	protected Path  path;
	
	protected PointF clipRegionUL;
	protected PointF clipRegionLR;

	// attributes
	protected clsAnnotationData.AttributeContainer objAttr;
	
	protected clsShapeBase(Context objContext, 
			clsAnnotationData.AttributeContainer attributes,
			float[] reference, clsShapeFactory.Shape type,
			float maxWidth, float maxHeight, float minScreenDim)
	{
		this.reference = Arrays.copyOf(reference, reference.length);
		
		this.type    = type;
		this.objContext = objContext;
		
		clsAnnotationData data = new clsAnnotationData("");
		objAttr = data.new AttributeContainer(attributes);

		// compute dimension of text
		textSize  = Math.min(maxWidth,  maxHeight) * TEXT_SIZE_DEFAULT / minScreenDim;

		// Define bounding box of shape movement and resize operations
		// Note that the coordinate system has its origin in the upper left
		// corner of the screen.
		clipRegionUL = new PointF(0, 0);
		clipRegionLR = new PointF(maxWidth, maxHeight);
		
		paint = new Paint();
		// outline
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE); // should be FILL_AND_STROKE for text
		paint.setColor(objAttr.lineColor);
		paint.setStrokeWidth(objAttr.lineWidth);
		paint.setPathEffect(clsUtils.getPathEffect(objAttr.lineStyle));

		// hotspots are circles which are not filled
		hotspotPaint = new Paint(paint);
		hotspotPaint.setPathEffect(clsUtils.getPathEffect(clsUtils.Linestyle.LS_SOLID));
		hotspotPaint.setStyle(Style.STROKE);
		hotspotPaint.setStrokeWidth(5);
		
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.WHITE); // default
		textPaint.setStrokeWidth(3);
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Align.CENTER);
		
		// The paint used to draw the selected object is based on the
		// normal paint.
		rubberBand = new Paint(paint);
		rubberBand.setColor(objContext.getResources().getInteger(R.color.color_rubberband));
		rubberBand.setPathEffect(clsUtils.getPathEffect(clsUtils.Linestyle.LS_SOLID));

		path  = new Path();
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	protected clsShapeFactory.Shape getSelectedShapeType()
	{
		return type;
	}

	protected void changeAttributes(clsAnnotationData.AttributeContainer attributes)
	{
		// Copy Attributes
		objAttr.setAttributes(attributes);

		paint.setColor(objAttr.lineColor);
		paint.setStrokeWidth(objAttr.lineWidth);
		paint.setPathEffect(clsUtils.getPathEffect(objAttr.lineStyle));
		
		rubberBand.setStrokeWidth(objAttr.lineWidth);
		
		textPaint.setColor(objAttr.textColor);
	}

	// draw
	protected void draw(Canvas canvas) {}
	
	// Checks if the given point is within the clip region
	protected boolean coordInClipRegion(float inX, float inY)
	{
		boolean isInside = false;
		
		if((inX >= clipRegionUL.x && inX <= clipRegionLR.x) &&
		   (inY >= clipRegionUL.y && inY <= clipRegionLR.y))
		{
			isInside = true;
		}
		
		return isInside;
	}

	// resize or move shape
	public void applyOffset(float deltaX, float deltaY){}

	// Extract the base attributes
	public void extractAnnotation(clsAnnotationData.clsAnnotationItem elem)
	{
		// set attributes
		elem.setAttributes(objAttr);
	}
	
	public clsAnnotationData.AttributeContainer getAttributes()
	{
		return objAttr;
	}
}