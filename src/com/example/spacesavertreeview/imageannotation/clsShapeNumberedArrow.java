package com.example.spacesavertreeview.imageannotation;

import com.example.spacesavertreeview.clsUtils;
import com.example.spacesavertreeview.imageannotation.clsShapeFactory.Shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Color;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

// arrow with a number attached
public class clsShapeNumberedArrow extends clsShapeArrow
{
	private static final float NUMBER_CIRCLE_RADIUS_DEFAULT = 25f;
	private float NUMBER_CIRCLE_RADIUS;
	
	private String numberedNote = "";
	private int runningNumber;
	
	// midpoint of the circle around the Id of the arrow
	private PointF circledNumber;
	
	public clsShapeNumberedArrow(Context context, clsAnnotationData.AttributeContainer attributes,
									String text, float[] reference, float maxWidth, float maxHeight,
									float minScreenDim,
									int id)
	{
		super(context, attributes, reference, maxWidth, maxHeight, minScreenDim);
		type = clsShapeFactory.Shape.NUMBERED_ARROW;

		// compute dimension of circle enclosing the id
		NUMBER_CIRCLE_RADIUS  = Math.min(maxWidth,  maxHeight) * NUMBER_CIRCLE_RADIUS_DEFAULT / minScreenDim;
		
		setDescription(text);
		runningNumber = id;
		
		// Compute the center of the circle enclosing the running number of the arrow.
		// We assume here that less than a hundred numbered arrows are used as annotation per image.
		float tw = paint.measureText("88");
		float th = Math.abs(paint.ascent());
		
		float radius = Math.max(tw,  th);
		
		circledNumber = new PointF(tail.x, tail.y + radius);
		
		textPaint.setColor(attr.textColor);
	}
	
	/*
	 * Draw shape considering that it might be selected
	 * 
	 * 	in:	canvas
	 */
	public void draw(Canvas canvas)
	{
		super.draw(canvas);
	
		drawNumber(canvas);
	}
	
	private void drawNumber(Canvas canvas)
	{
		path.moveTo(tail.x, tail.y);
		path.lineTo(circledNumber.x, circledNumber.y);
	
		if(selected)
		{
			canvas.drawCircle(tail.x, tail.y, NUMBER_CIRCLE_RADIUS, rubberBand);
		}
		else
		{
			// save current style
			Style styleBackup = paint.getStyle();
			PathEffect pathBackup = paint.getPathEffect();
			
			paint.setStyle(Style.FILL_AND_STROKE);
			paint.setPathEffect(clsUtils.getPathEffect(clsUtils.Linestyle.LS_SOLID));

			canvas.drawCircle(tail.x, tail.y, NUMBER_CIRCLE_RADIUS, paint);
			
			// restore style
			paint.setStyle(styleBackup);
			paint.setPathEffect(pathBackup);
		}

		// Center text on center of circle
		Rect bounds = new Rect();
		String text = String.valueOf(runningNumber);
		textPaint.getTextBounds(text, 0, text.length(), bounds);

		canvas.drawText(String.valueOf(runningNumber), 
				tail.x, 
				tail.y + bounds.height()/2f, 
			    textPaint);
	}
	
	// Change the number Id associated with that numbered arrow
	public void setNumber(int id)
	{
		runningNumber = id;
	}
	
	public String getDescription()
	{
		return String.valueOf(runningNumber) + ": " + numberedNote;
	}
	
	public void setDescription(String description)
	{
		// cut the prefix (the running number of the arrow can change) and store the
		// actual note
		int idx = description.indexOf(": ");
		if(idx != -1)
		{
			// display the remaining text in the editable text field
			numberedNote = description.substring(idx + 2);
		}
	}

	public void extractAnnotation(clsAnnotationData.clsAnnotationItem elem)
	{
		// let the base class write the common arrow data
		super.extractAnnotation(elem);

		// override type
		elem.setType(Shape.NUMBERED_ARROW);
		
		// add annotation text
		// start and end point of arrow
		elem.setAnnotationText(getDescription());
	}
}
