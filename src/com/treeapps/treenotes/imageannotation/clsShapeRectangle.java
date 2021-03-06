package com.treeapps.treenotes.imageannotation;

import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.imageannotation.clsShapeFactory.Shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

// Rectangle with rounded corners
public class clsShapeRectangle extends clsShapeBase
{
	// Minimum dimension of rectangle. Restricts interactive resizing.
	private static final int MIN_WIDTH  = 50;
	private static final int MIN_HEIGHT = 50;
	
	// default radius of the rounded corners of the rectangle
	private static final int CORNER_RADIUS = 10;

	private enum Hotspot {
		UPPER_LEFT,
		UPPER_RIGHT,
		LOWER_LEFT,
		LOWER_RIGHT,

		NONE
	}

	private RectF objRect;
	
	// hotspots
	private PointF ul;
	private PointF ur;
	private PointF ll;
	private PointF lr;
	
	private Hotspot pickedHotspot;
	
	/*
	 * Constructor
	 * 
	 *  in: color
	 *  	lineWidth (px)
	 *  	linestyle (enum)
	 *  	reference (coordinates of upper left and lower right corner)
	 */
	public clsShapeRectangle(Context context, clsAnnotationData.AttributeContainer attributes, 
							float[] reference, float maxWidth, float maxHeight, float minScreenDim)
	{
		super(context, attributes, reference, clsShapeFactory.Shape.RECTANGLE,
				maxWidth, maxHeight, minScreenDim);

		// create a new rectangle with the given upper left and lower right corner
		objRect = new RectF(reference[0], reference[1],
						 reference[2], reference[3]);

		// Allocate hotspots
		ul = new PointF();
		ur = new PointF();
		ll = new PointF();
		lr = new PointF();
	}

	/*
	 * Draw shape considering that it might be selected
	 * 
	 * 	in:	canvas
	 */
	public void draw(Canvas canvas)
	{
		// Draw a rectangle with rounded corners with the center as the reference point.
		// If the shape is currently selected draw it in a different color and add a visual
		// feedback for the hotspots.
		if(selected)
		{
			canvas.drawRoundRect(objRect, CORNER_RADIUS, CORNER_RADIUS, rubberBand);

			// draw hotspots (corners of bounding box)
			// upper left
			canvas.drawCircle(objRect.left, objRect.top, HOTSPOT_RADIUS, hotspotPaint);
			// upper right
			canvas.drawCircle(objRect.right, objRect.top, HOTSPOT_RADIUS, hotspotPaint);
			// lower left
			canvas.drawCircle(objRect.left, objRect.bottom, 10, hotspotPaint);
			// lower right
			canvas.drawCircle(objRect.right, objRect.bottom, 10, hotspotPaint);
		}
		else
		{
			canvas.drawRoundRect(objRect, CORNER_RADIUS, CORNER_RADIUS, paint);
		}
	}

	public double distanceToPick(float[] coord)
	{
		PointF event = new PointF(coord[0], coord[1]);

		double minDist;

		ul.x = objRect.left;  ul.y = objRect.top;
		ur.x = objRect.right; ur.y = objRect.top;
		ll.x = objRect.left;  ll.y = objRect.bottom;
		lr.x = objRect.right; lr.y = objRect.bottom;

		// closed polygon
		PointF[] vertices = {ul, ur, lr, ll, ul};

		// calculate minimum distance of point to rectangle
		minDist = clsUtils.absDistanceToEdges(vertices, event);
		
		// now check the distance to a hotspot to determine
		// between move and resize
		double[] hotspotDist = clsUtils.distanceBetweenPoints(vertices, event);
		
		// Hotspots are harder to pick, so we override minDist in the case
		// where the event coordinates are in the pre-defined surrounding of a hotspot
		// no matter how close we were to a line.
		if(hotspotDist[0] <= HOTSPOT_PICKRADIUS)
		{
			pickedHotspot = Hotspot.UPPER_LEFT;
			minDist = 0; 
		}
		else if (hotspotDist[1] <= HOTSPOT_PICKRADIUS)
		{
			pickedHotspot = Hotspot.UPPER_RIGHT;
			minDist = 0; 
		}
		else if (hotspotDist[2] <= HOTSPOT_PICKRADIUS)
		{
			pickedHotspot = Hotspot.LOWER_RIGHT;
			minDist = 0; 
		}
		else if (hotspotDist[3] <= HOTSPOT_PICKRADIUS)
		{
			pickedHotspot = Hotspot.LOWER_LEFT;
			minDist = 0; 
		}
		else
			pickedHotspot = Hotspot.NONE;
			
		return minDist;
	}
	
	// resize or move shape
	public void applyOffset(float deltaX, float deltaY)
	{
		float x, y;
		
		switch(pickedHotspot)
		{
		case UPPER_LEFT:
			x = objRect.left + deltaX;
			y = objRect.top  + deltaY;
			
			// clipping
			if(objRect.right - x  >= MIN_WIDTH &&
			   objRect.bottom - y >= MIN_HEIGHT)
			{
				objRect.left = x;
				objRect.top  = y;
			}
				
			break;

		case UPPER_RIGHT:
			x = objRect.right + deltaX;
			y = objRect.top   + deltaY;

			if(x - objRect.left   >= MIN_WIDTH &&
			   objRect.bottom - y >= MIN_HEIGHT)
			{
				objRect.right = x;
				objRect.top   = y;
			}
			
			break;

		case LOWER_LEFT:
			x = objRect.left   + deltaX;
			y = objRect.bottom + deltaY;

			if(objRect.right  - x >= MIN_WIDTH &&
			   y - objRect.top    >= MIN_HEIGHT)
			{
				objRect.left   = x;
				objRect.bottom = y;
			}
			break;

		case LOWER_RIGHT:
			x = objRect.right  + deltaX;
			y = objRect.bottom + deltaY;
			Log.d("==", "lower right");
			if(x - objRect.left >= MIN_WIDTH &&
			   y - objRect.top  >= MIN_HEIGHT)
			{Log.d("==", "change objRect");
				objRect.right  = x;
				objRect.bottom = y;
			}
			break;

		// a side has been picked -> move the entire rectangle
		case NONE:
			if ((coordInClipRegion(objRect.left  + deltaX, objRect.top    + deltaY)) &&
				(coordInClipRegion(objRect.right + deltaX, objRect.bottom + deltaY)))
			{
				objRect.offset(deltaX, deltaY);
			}
			break;
		}
	}
	
	public void extractAnnotation(clsAnnotationData.clsAnnotationItem elem)
	{
		elem.setType(Shape.RECTANGLE);
		
		// let the base class store the attributes
		super.extractAnnotation(elem);
		
		// add geometrical data
		// upper left corner
		elem.addReferencePoint(objRect.left, objRect.top);
		// lower right corner
		elem.addReferencePoint(objRect.right, objRect.bottom);
	}
}
