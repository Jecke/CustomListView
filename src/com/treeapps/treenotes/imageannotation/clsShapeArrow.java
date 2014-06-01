package com.treeapps.treenotes.imageannotation;

import com.treeapps.treenotes.clsUtils;
import com.treeapps.treenotes.imageannotation.clsShapeFactory.Shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.util.Log;

// Basic arrow
public class clsShapeArrow extends clsShapeBase
{
	private static final float HEAD_WIDTH_DEFAULT  = 15f;
	private static final float HEAD_LENGTH_DEFAULT = 30f;

	private float HEAD_WIDTH;
	private float HEAD_LENGTH;

	// hotspots
	protected PointF tip;
	protected PointF tail;
	
	private enum Hotspot {
		TIP,
		TAIL,

		NONE
	}
	private Hotspot pickedHotspot;

	// test 
	Matrix m = new Matrix(); 

	
	public clsShapeArrow(Context context, clsAnnotationData.AttributeContainer attributes, 
			float[] reference, float maxWidth, float maxHeight, float minScreenDim)
	{
		super(context, attributes, reference, clsShapeFactory.Shape.ARROW,
				maxWidth, maxHeight, minScreenDim);
		
		Log.d("ARROW", "("+String.valueOf(reference[0])+";"+String.valueOf(reference[1])+") - " +
				"("+String.valueOf(reference[2])+";"+String.valueOf(reference[3])+") max(" + 
				String.valueOf(maxWidth)+"/"+String.valueOf(maxHeight)+")" 
				
				
				);
		// set hotspots
		// Note: The object assumes that the default shape is starting from the middle
		// of the bitmap running down. So tip is always inside the bitmap but the 
		// y-coordinate of the tail might be outside
		tip  = new PointF(reference[0], reference[1]);
		tail = new PointF(reference[2], Math.min(reference[3], clipRegionLR.y));
		
		// compute dimension of arrow head
		HEAD_WIDTH  = Math.min(maxWidth,  maxHeight) * HEAD_WIDTH_DEFAULT / minScreenDim;
		HEAD_LENGTH = Math.min(maxWidth,  maxHeight) * HEAD_LENGTH_DEFAULT / minScreenDim;
		
		// arrow head is filled
		paint.setStyle(Style.FILL_AND_STROKE);
	}

	/*
	 * Draw shape considering that it might be selected
	 * 
	 * 	in:	canvas
	 */
	public void draw(Canvas canvas)
	{
		// For faster drawing we keep the internal data structures of the path. That is possible because
		// the number of points in the path will never change.
		path.rewind();
		
		// draw arrow head
		drawArrowHead(canvas);

		// draw arrow body
		
		paint.setStyle(Style.STROKE);
		path.moveTo(tip.x, tip.y);
		path.lineTo(tail.x, tail.y);

		
		// Draw an arrow with the tip of the arrow head as the reference point.
		// If the shape is currently selected draw it in a different color and add a visual
		// feedback for the hotspots.
		if(selected)
		{
			canvas.drawPath(path, rubberBand);

			// draw hotspots
			drawHotSpots(canvas);
		}
		else
		{
			canvas.drawPath(path, paint);
		}
	}
	
	protected void drawHotSpots(Canvas canvas)
	{
		// tip of arrow
		canvas.drawCircle(tip.x, tip.y, HOTSPOT_RADIUS, hotspotPaint);
		// end of arrow
		canvas.drawCircle(tail.x, tail.y, HOTSPOT_RADIUS, hotspotPaint);
	}
	
	private void drawArrowHead(Canvas canvas)
	{
		// perpendicular lines 
		float dx = tail.x - tip.x;
		float dy = tail.y - tip.y;
		
		// compute normal vector
		double normLength = Math.sqrt(dx*dx+dy*dy);
		double nx = dx/normLength;
		double ny = dy/normLength;
		
		double scale = HEAD_WIDTH/Math.sqrt(dx*dx+dy*dy);
		
		// create perpendicular lines forming the base of the arrow head
		double dx2 = -dy * scale;
		double dy2 = dx * scale;

		double dx3 = dy * scale;
		double dy3 = -dx * scale;

		// calculate the intersection point with the arrow body
		double x3 = tip.x + (HEAD_LENGTH * nx);
		double y3 = tip.y + (HEAD_LENGTH * ny);
		
		// draw arrow head
		path.moveTo((float)(x3+dx2), (float)(y3+dy2));
		path.lineTo((float)(x3+dx3), (float)(y3+dy3));
		path.lineTo(tip.x, tip.y);
		path.close();
	
		if(selected)
		{
			canvas.drawPath(path, rubberBand);
		}
		else
		{
			Style styleBackup = paint.getStyle();
			PathEffect pathBackup = paint.getPathEffect();
			
			paint.setStyle(Style.FILL_AND_STROKE);
			paint.setPathEffect(clsUtils.getPathEffect(clsUtils.Linestyle.LS_SOLID));
			
			canvas.drawPath(path, paint);

			paint.setStyle(styleBackup);
			paint.setPathEffect(pathBackup);
		}
	}

	// returns the minimum distance of the event coordinates to the shape
	public double distanceToPick(float[] coord)
	{
		PointF event = new PointF(coord[0], coord[1]);

		double minDist;
		
		PointF[] vertices = {tip, tail};

		// check distance to arrow body (note: arrow head is not considered)
		minDist = clsUtils.absDistanceToEdges(vertices, event);

		// Now check distance to hotspots.
		// Hotspots are harder to pick, so we override minDist in the case
		// where the event coordinates are in the pre-defined surrounding of a hotspot
		// no matter how close we were to the arrow body.
		double[] hotspotDist = clsUtils.distanceBetweenPoints(vertices, event);
		
		if(hotspotDist[0] <= HOTSPOT_PICKRADIUS)
		{
			pickedHotspot = Hotspot.TIP;
			minDist = 0; 
		}
		else if (hotspotDist[1] <= HOTSPOT_PICKRADIUS)
		{
			pickedHotspot = Hotspot.TAIL;
			minDist = 0; 
		}
		else
			pickedHotspot = Hotspot.NONE;
			
		return minDist;
	}
	
	// resize or move shape
	public void applyOffset(float deltaX, float deltaY)
	{
		switch(pickedHotspot)
		{
			// Resize and rotate (with pivot = Tail) at the same time and 
			// consider the minimum length of the arrow
			case TIP:
				if(coordInClipRegion(tip.x + deltaX, tip.y + deltaY))
				{
					tip.offset(deltaX, deltaY);
				}
				break;
			
			// Resize and rotate (with pivot = Tip) at the same time and 
			// consider the minimum length of the arrow
			case TAIL:
				if(coordInClipRegion(tail.x + deltaX, tail.y + deltaY))
				{
					tail.offset(deltaX, deltaY);
				}
				break;
			
			// Move the arrow while considering the clipping boundaries (bitmap) 
			case NONE:
				if(coordInClipRegion(tip.x + deltaX, tip.y + deltaY) && 
			       coordInClipRegion(tail.x + deltaX, tail.y + deltaY))
				{
					// just add the offset to all points
					tip.offset(deltaX, deltaY);
					tail.offset(deltaX, deltaY);
				}
				break;
		}
	}
	
	public void extractAnnotation(clsAnnotationData.clsAnnotationItem elem)
	{
		elem.setType(Shape.ARROW);
		
		// let the base class store the attributes
		super.extractAnnotation(elem);
		
		// add geometrical data
		// start and end point of arrow
		elem.addReferencePoint(tip.x, tip.y);
		elem.addReferencePoint(tail.x, tail.y);
	}
}
